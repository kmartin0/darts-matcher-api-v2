package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

@Service
@Primary
public class X01LegServiceImpl implements IX01LegService {

    private final MessageResolver messageResolver;
    private final IX01LegProgressService legProgressService;
    private final IX01LegResultService legResultService;
    private final IX01CheckoutService checkoutService;

    public X01LegServiceImpl(MessageResolver messageResolver, IX01LegProgressService legProgressService,
                             IX01LegResultService legResultService, IX01CheckoutService checkoutService) {
        this.messageResolver = messageResolver;
        this.legProgressService = legProgressService;
        this.legResultService = legResultService;
        this.checkoutService = checkoutService;
    }

    /**
     * Creates a new leg with the correct starting player.
     *
     * @param legNumber        int the leg number
     * @param throwsFirstInSet {@link ObjectId} the player that started the set
     * @param players          {@link List<X01MatchPlayer>} the list of match players
     * @return {@link X01Leg} the created leg
     */
    @Override
    public X01Leg createNewLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players) {
        ObjectId throwsFirstInLeg = calcThrowsFirstInLeg(legNumber, throwsFirstInSet, players);
        return new X01Leg(legNumber, null, throwsFirstInLeg, new TreeMap<>());
    }

    /**
     * Adds a score from a player to the list of scores in a round. Verifies if the added
     * score results in an illegal leg state, then the score will be set to zero and darts used to 3.
     *
     * @param x01         int the x01 the leg is played in
     * @param leg         {@link X01Leg} the leg of which the round belongs to
     * @param roundNumber int the round of which the score belongs to
     * @param turn        {@link X01Turn} the turn that needs to be added to the round
     * @param players     {@link List<X01MatchPlayer>} the list of match players.
     * @param throwerId   {@link ObjectId} the player that has thrown the score.
     */
    @Override
    public void addScore(int x01, X01Leg leg, int roundNumber, X01Turn turn, List<X01MatchPlayer> players, ObjectId throwerId, boolean trackDoubles) {
        if (leg == null || turn == null || players == null) return;

        // Determine if the leg is editable, will throw InvalidArgumentsException if the leg is not editable.
        checkLegEditable(leg, throwerId);

        Optional<X01LegRound> x01LegRound = legProgressService.getLegRound(leg, roundNumber, true);
        if (x01LegRound.isEmpty()) return;

        // Add the score to the round.
        X01LegRoundScore roundScore = new X01LegRoundScore(turn, trackDoubles);
        x01LegRound.get().getScores().put(throwerId, roundScore);
        if (turn.getCheckoutDartsUsed() != null) leg.setCheckoutDartsUsed(turn.getCheckoutDartsUsed());

        // Verify if the rounds are legal after adding the new score.
        boolean isPlayerRoundsLegal = validateLegForPlayer(leg, x01, throwerId);

        // When the round is not legal. Set the score to zero and checkout darts used to null
        if (!isPlayerRoundsLegal) {
            roundScore.setScore(0);
            leg.setCheckoutDartsUsed(null);
        }

        // Update the leg result
        legResultService.updateLegResult(leg, players, x01);
    }

    /**
     * Determines if a leg can be edited. When a leg is concluded, only the score of the winner can be modified.
     *
     * @param leg      {@link X01Leg} the leg to be modified
     * @param playerId {@link ObjectId} the player of which the score is going to be modified
     */
    @Override
    public void checkLegEditable(X01Leg leg, ObjectId playerId) {
        if (leg == null) return;

        // If the leg is already won by another player the turn cannot be modified.
        if (legProgressService.isLegConcluded(leg) && !Objects.equals(leg.getWinner(), playerId)) {
            throw new InvalidArgumentsException(new TargetError("score", messageResolver.getMessage(MessageKeys.MESSAGE_LEG_ALREADY_WON)));
        }
    }

    /**
     * Determines if a score made by a player in a round of a leg is a checkout.
     *
     * @param leg      {@link X01Leg} the leg that the round is in
     * @param roundNumber the round number to check
     * @param playerId {@link ObjectId} the player that scored
     * @return boolean whether the score made a player is a checkout
     */
    @Override
    public boolean isPlayerCheckoutRound(X01Leg leg, int roundNumber, ObjectId playerId) {
        if (leg == null) return false;

        return playerId.equals(leg.getWinner()) && leg.getRounds().higherKey(roundNumber) == null;
    }

    /**
     * Validates that the scores (and checkout if applicable) of a player in a leg
     * are valid according to the game rules.
     *
     * @param leg       {@link X01Leg} the leg that need to be checked
     * @param x01       int the x01 rule of the match
     * @param throwerId {@link ObjectId} the player who needs to be validated
     * @return boolean whether the list of scores in the leg are valid for a player according to the game rules.
     */
    @Override
    public boolean validateLegForPlayer(X01Leg leg, int x01, ObjectId throwerId) {
        // Get the remaining score for the player
        int remaining = legResultService.calculateRemainingScore(leg, x01, throwerId);

        // The remaining score cannot be 1 or below 0
        if (checkoutService.isRemainingBust(remaining)) return false;

        // When no remaining points are left, determine the validity of the last score (checkout)
        if (checkoutService.isRemainingZero(remaining)) {
            Optional<X01LegRoundScore> playerLatestTurn = legProgressService.getLastScoreForPlayer(leg, throwerId);
            if (playerLatestTurn.isPresent())
                return checkoutService.isScoreCheckout(playerLatestTurn.get().getScore(), leg.getCheckoutDartsUsed());
        }

        // The rounds are in line with the game rules.
        return true;
    }

    /**
     * Determines who throws first in a leg
     *
     * @param legNumber        int the number of the leg
     * @param throwsFirstInSet {@link ObjectId} the player id that throws first in the set
     * @param players          {@link List<X01MatchPlayer>} the list of match players
     * @return {@link ObjectId} the player who throws first in the leg
     */
    @Override
    public ObjectId calcThrowsFirstInLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players) {
        if (X01ValidationUtils.isPlayersEmpty(players)) {
            throw new IllegalArgumentException("Cannot calculate first thrower from a null or empty player list.");
        }

        // Get the index of the player that starts the set
        int numOfPlayers = players.size();
        int startingIndexForSet = IntStream.range(0, numOfPlayers)
                .filter(i -> players.get(i).getPlayerId().equals(throwsFirstInSet))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Set starter not found in player list."));

        // Calculate the first thrower for this leg
        int throwsFirstIndex = (startingIndexForSet + (legNumber - 1)) % numOfPlayers;
        return players.get(throwsFirstIndex).getPlayerId();
    }

}
