package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class X01LegResultServiceImpl implements IX01LegResultService {

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
        if (leg == null || leg.getRounds().isEmpty() || legWinner == null) return;

        // Create a reversed copy of the rounds to iterate from last to first
        List<X01LegRound> reverseRounds = new ArrayList<>(leg.getRounds());
        Collections.reverse(reverseRounds);

        // Trims scores beyond the final score of the leg winner.
        // This situation can occur if a score is edited and, as a result, another player becomes the winner.
        outer:
        for (X01LegRound round : reverseRounds) {
            List<ObjectId> reverseKeys = new ArrayList<>(round.getScores().keySet());
            Collections.reverse(reverseKeys);

            for (ObjectId playerId : reverseKeys) {
                if (!playerId.equals(legWinner)) {
                    round.getScores().remove(playerId);
                    if (round.getScores().isEmpty()) leg.getRounds().remove(round);
                } else break outer;
            }
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
        if (leg == null || leg.getRounds() == null) return x01;

        // For every round map the player score and sum these up.
        int totalScored = leg.getRounds().stream()
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
    public int calculateDartsUsed(List<X01LegRound> rounds, ObjectId playerId) {
        if (rounds == null) return 0;

        // For every round map the player darts used and sum these up.
        return rounds.stream()
                .mapToInt(value -> {
                    X01LegRoundScore playerScore = value.getScores().get(playerId);
                    return (playerScore != null) ? playerScore.getDartsUsed() : 0;
                }).sum();
    }
}
