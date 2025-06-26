package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class X01LegResultServiceImpl implements IX01LegResultService {

    private final IX01LegRoundService legRoundService;

    public X01LegResultServiceImpl(IX01LegRoundService legRoundService) {
        this.legRoundService = legRoundService;
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
        // If the leg is null exit early, if the players are null clear the winner and exit early.
        if (leg == null) return;
        if (X01ValidationUtils.isPlayersEmpty(players)) {
            leg.setWinner(null);
            return;
        }

        // Find the winner of the leg by getting the first player who reaches zero remaining points.
        Optional<X01MatchPlayer> winner = players.stream()
                .filter(matchPlayer -> calculateRemainingScore(leg, x01, matchPlayer.getPlayerId()) == 0).findFirst();

        // If a winner is present, set the player ID, otherwise set the winner to null
        winner.ifPresentOrElse(
                matchPlayer -> {
                    leg.setWinner(matchPlayer.getPlayerId());
                    removeScoresAfterWinner(leg, leg.getWinner());
                },
                () -> leg.setWinner(null)
        );
    }

    /**
     * Removes all scores that appear after the final score of the leg winner. If rounds become empty after
     * removing scores they will also be removed. The situation of scores/rounds beyond the winning point could
     * occur due to editing a score.
     *
     * @param leg       {@link X01Leg} the leg
     * @param legWinner {@link ObjectId} the player id of the leg winner
     */
    @Override
    public void removeScoresAfterWinner(X01Leg leg, ObjectId legWinner) {
        // If there is no leg winner. No trimming needs to happen.
        if (X01ValidationUtils.isRoundsEmpty(leg) || legWinner == null) return;

        // Iterate through the rounds in reverse order (latest round first)
        Iterator<X01LegRound> reverseRoundsIterator = leg.getRounds().descendingMap().values().iterator();
        while (reverseRoundsIterator.hasNext()) {
            X01LegRound round = reverseRoundsIterator.next();

            // If the leg winner has not thrown in this round, remove the round and move on to the next round.
            if (!round.getScores().containsKey(legWinner)) {
                reverseRoundsIterator.remove();
                continue;
            }

            // Remove all scores from this round that occur after the winner's score.
            legRoundService.removeScoresAfterWinner(round, legWinner);

            // This round contains the winner's score so we can stop trimming.
            break;
        }
    }


    /**
     * Calculates the remaining score for a player in a leg.
     *
     * @param leg      {@link X01Leg} the leg containing the player scores
     * @param x01      int of the starting score
     * @param playerId {@link ObjectId} the player for which the remaining score needs to be calculated
     * @return int of the remaining score for a player
     */
    @Override
    public int calculateRemainingScore(X01Leg leg, int x01, ObjectId playerId) {
        // When no rounds exist, the player has no throws so his remaining score is the starting score
        if (X01ValidationUtils.isRoundsEmpty(leg) || playerId == null) return x01;

        // For every round map the player score and sum these up.
        int totalScored = leg.getRounds().values().stream()
                .mapToInt(value -> {
                    X01LegRoundScore playerScore = value.getScores().get(playerId);
                    return (playerScore != null) ? playerScore.getScore() : 0;
                }).sum();

        // Subtract the total scored points from the starting score (x01).
        return x01 - totalScored;
    }

    /**
     * Calculates the total number of darts used by a specific player across a leg.
     *
     * @param leg      {@link X01Leg} the leg to check
     * @param playerId the ID of the player whose darts usage is being calculated
     * @return int The sum of darts used by the player across the leg.
     */
    @Override
    public int calculateDartsUsed(X01Leg leg, ObjectId playerId) {
        // When no rounds exist, the player has no darts used.
        if (X01ValidationUtils.isRoundsEmpty(leg) || playerId == null) return 0;

        // If the player has won the leg, get the checkout round.
        Optional<Integer> checkoutRoundNumber = leg.getWinner() == null
                ? Optional.empty()
                : leg.getRounds().keySet().stream().max(Integer::compareTo);

        // For every round map the player darts used and sum these up.
        return leg.getRounds().entrySet().stream()
                .mapToInt(entry -> {
                    X01LegRoundScore playerScore = entry.getValue().getScores().get(playerId);
                    if (playerScore == null) return 0;
                    if (checkoutRoundNumber.isPresent() && checkoutRoundNumber.get().equals(entry.getKey()))
                        return leg.getCheckoutDartsUsed();

                    return 3;
                }).sum();
    }
}
