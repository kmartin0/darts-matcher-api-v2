package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.response.TargetError;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkout.IX01CheckoutService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.utils.MessageKeys;
import nl.kmartin.dartsmatcherapiv2.utils.MessageResolver;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class X01LegServiceImpl implements IX01LegService {

    private final IX01CheckoutService checkoutService;
    private final MessageResolver messageResolver;
    private final IX01LegRoundService legRoundService;

    public X01LegServiceImpl(IX01CheckoutService checkoutService, MessageResolver messageResolver, IX01LegRoundService legRoundService) {
        this.checkoutService = checkoutService;
        this.messageResolver = messageResolver;
        this.legRoundService = legRoundService;
    }

    /**
     * Finds the lowest-numbered leg which does not have a winner from a list of legs.
     *
     * @param legs {@link List<X01Leg>} the list of legs to check
     * @return {@link Optional<X01Leg>} empty when all legs have winners. otherwise the lowest leg without winner.
     */
    @Override
    public Optional<X01Leg> getCurrentLeg(List<X01Leg> legs) {
        if (legs == null) return Optional.empty();

        return legs.stream()
                .filter(leg -> leg.getWinner() == null) // get legs without result
                .min(Comparator.comparingInt(X01Leg::getLeg)); // Get the lowest numbered leg
    }

    /**
     * Creates the next leg for a list of legs but doesn't exceed the maximum number of legs.
     *
     * @param legs             {@link List<X01Leg>} the list of legs a leg has to be added to.
     * @param players          {@link List<X01MatchPlayer>} the match players.
     * @param bestOfLegs       int the maximum number of legs.
     * @param throwsFirstInSet {@link ObjectId} the player that throws first in the set.
     * @return {@link Optional<X01Leg>} the created leg, empty when the maximum number of legs was reached.
     */
    @Override
    public Optional<X01Leg> createNextLeg(List<X01Leg> legs, List<X01MatchPlayer> players, int bestOfLegs, ObjectId throwsFirstInSet) {
        if (legs == null) return Optional.empty();

        // Get existing leg numbers
        Set<Integer> existingLetNumbers = getLegNumbers(legs);

        // Find the next available leg number (ensure it doesn't exceed the best of legs)
        int nextLetNumber = NumberUtils.findNextNumber(existingLetNumbers, bestOfLegs);
        if (nextLetNumber == -1) return Optional.empty();

        // Create and add the next leg to the legs.
        X01Leg newLeg = createNewLeg(nextLetNumber, throwsFirstInSet, players);
        legs.add(newLeg);
        return Optional.of(newLeg);
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
        return new X01Leg(legNumber, null, throwsFirstInLeg, new ArrayList<>());
    }

    /**
     * Collects the unique leg numbers from a list of legs
     *
     * @param legs {@link List<X01Leg>} the list of legs
     * @return {@link Set<Integer>} the leg numbers
     */
    @Override
    public Set<Integer> getLegNumbers(List<X01Leg> legs) {
        if (legs == null) return null;

        // Map the leg numbers and collect to an integer set
        return legs.stream()
                .map(X01Leg::getLeg)
                .collect(Collectors.toSet());
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
        if (players == null) return null;

        // Get the index of the player that starts the set
        int numOfPlayers = players.size();
        int startingIndexOfSet = IntStream.range(0, numOfPlayers)
                .filter(i -> players.get(i).getPlayerId().equals(throwsFirstInSet))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Set starter not found in player list."));

        // Calculate the first thrower for this leg
        int throwsFirstIndex = (startingIndexOfSet + (legNumber - 1)) % numOfPlayers;
        return players.get(throwsFirstIndex).getPlayerId();
    }

    /**
     * Adds a score from a player to the list of scores in a round. Verifies if the added
     * score results in an illegal leg state, then the score will be set to zero and darts used to 3.
     *
     * @param x01              int the x01 the leg is played in
     * @param x01Leg           {@link X01Leg} the leg of which the round belongs to
     * @param x01LegRound      {@link X01LegRound} the round of which the score belongs to
     * @param x01LegRoundScore {@link X01LegRoundScore} the score that needs to be added to the round
     * @param matchPlayers     {@link List<X01MatchPlayer>} the list of match players.
     * @param throwerId        {@link ObjectId} the player that has thrown the score.
     * @throws IOException If there's an issue reading the checkouts file.
     */
    @Override
    public void addScore(int x01, X01Leg x01Leg, X01LegRound x01LegRound, X01LegRoundScore x01LegRoundScore, List<X01MatchPlayer> matchPlayers, ObjectId throwerId) throws IOException {
        if (x01Leg == null || x01LegRound == null || x01LegRound.getScores() == null || x01LegRoundScore == null)
            return;

        // Add the score to the round.
        x01LegRound.getScores().put(throwerId, x01LegRoundScore);

        // Verify if the rounds are legal after adding the new score.
        boolean isPlayerRoundsLegal = validatePlayerRounds(x01, x01Leg.getRounds(), throwerId);

        // When the round is not legal. Set the score to zero and darts used to 3
        if (!isPlayerRoundsLegal) {
            x01LegRoundScore.setScore(0);
            x01LegRoundScore.setDartsUsed(3);
        }

        // Update the leg result
        updateLegResult(x01Leg, matchPlayers, x01);
    }

    /**
     * Determines if a leg is concluded by checking the winner property
     *
     * @param x01Leg {@link X01Leg} the leg that needs to be checked
     * @return boolean if the leg is concluded
     */
    @Override
    public boolean isLegConcluded(X01Leg x01Leg) {
        return x01Leg != null && x01Leg.getWinner() != null;
    }

    /**
     * Find a leg number in a list of legs.
     *
     * @param legs      {@link List<X01Leg>} the list of legs
     * @param legNumber int the leg number that needs to be found
     * @return {@link Optional<X01Leg>} the matching leg, empty if no leg is found
     */
    @Override
    public Optional<X01Leg> getLeg(List<X01Leg> legs, int legNumber) {
        if (legs == null || legNumber < 0) return Optional.empty();

        return legs.stream().filter(x01Leg -> x01Leg.getLeg() == legNumber).findFirst();
    }

    /**
     * Determines if a leg can be edited. When a leg is concluded, only the score of the winner can be modified.
     *
     * @param x01Leg   {@link X01Leg} the leg to be modified
     * @param playerId {@link ObjectId} the player of which the score is going to be modified
     * @return boolean if the score of the player in the leg can be modified
     */
    @Override
    public boolean isLegEditable(X01Leg x01Leg, ObjectId playerId) {
        // If the leg is already won by another player the turn cannot be modified.
        if (isLegConcluded(x01Leg) && !Objects.equals(x01Leg.getWinner(), playerId)) {
            throw new InvalidArgumentsException(new TargetError("score", messageResolver.getMessage(MessageKeys.MESSAGE_LEG_ALREADY_WON)));
        }

        return true;
    }

    /**
     * Updates the leg result for all legs in a list of legs.
     *
     * @param legs    {@link List<X01Leg>} the list of legs that needs to be updated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @param x01     int the x01 setting for the legs
     */
    @Override
    public void updateLegResults(List<X01Leg> legs, List<X01MatchPlayer> players, int x01) {
        if (legs == null || players == null) return;

        // For each leg update the leg result.
        legs.forEach(x01Leg -> updateLegResult(x01Leg, players, x01));
    }

    /**
     * Updates the leg result for a leg.
     *
     * @param leg     {@link X01Leg} the leg to be updated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @param x01     int the x01 setting for the leg
     */
    @Override
    public void updateLegResult(X01Leg leg, List<X01MatchPlayer> players, int x01) {
        if (leg == null || players == null) return;

        // Find the winner of the leg by getting the first player who reaches zero remaining points.
        Optional<X01MatchPlayer> winner = players.stream()
                .filter(matchPlayer -> legRoundService.calculateRemainingScore(x01, leg.getRounds(), matchPlayer.getPlayerId()) == 0).findFirst();

        // If a winner is present, set the player ID, otherwise set the winner to null
        winner.ifPresentOrElse(
                matchPlayer -> leg.setWinner(matchPlayer.getPlayerId()),
                () -> leg.setWinner(null)
        );
    }

    /**
     * Validates the scores (and checkout if applicable) of a player in a list of rounds are valid according to the game rules.
     *
     * @param x01       int the x01 rule of the match
     * @param rounds    {@link List<X01LegRound>} the list of rounds that need to be checked
     * @param throwerId {@link ObjectId} the player who needs to be validated
     * @return boolean whether the list of scores in the rounds are valid for a player according to the game rules.
     * @throws IOException If there's an issue reading the checkouts file.
     */
    private boolean validatePlayerRounds(int x01, List<X01LegRound> rounds, ObjectId throwerId) throws IOException {
        // Get the remaining score for the player
        int remaining = legRoundService.calculateRemainingScore(x01, rounds, throwerId);

        // The remaining score cannot 1 or below 0
        if (remaining < 0 || remaining == 1) return false;

        // When no remaining points are left, determine the validity of the last score (checkout)
        if (remaining == 0) {
            Optional<X01LegRoundScore> playerLatestTurn = getPlayerLatestTurn(rounds, throwerId);
            if (playerLatestTurn.isPresent())
                return checkoutService.isValidCheckout(playerLatestTurn.get().getScore(), playerLatestTurn.get().getDartsUsed());
        }

        // The rounds are in line with the game rules.
        return true;
    }

    /**
     * Find the latest player turn.
     *
     * @param rounds    {@link List<X01LegRound>} the list of rounds containing the player scores
     * @param throwerId {@link ObjectId} the player id for which the latest turn needs to be found
     * @return {@link Optional<X01LegRoundScore>} the round score for a player in their latest round, if no score found empty
     */
    private Optional<X01LegRoundScore> getPlayerLatestTurn(List<X01LegRound> rounds, ObjectId throwerId) {
        // Find the rounds containing a score for the player
        List<X01LegRound> playerRounds = rounds.stream().filter(round -> round.getScores().containsKey(throwerId)).toList();

        // Get the highest round containing the player score
        return playerRounds.stream()
                .max(Comparator.comparingInt(X01LegRound::getRound))
                .flatMap(round -> Optional.ofNullable(round.getScores().get(throwerId)));
    }
}
