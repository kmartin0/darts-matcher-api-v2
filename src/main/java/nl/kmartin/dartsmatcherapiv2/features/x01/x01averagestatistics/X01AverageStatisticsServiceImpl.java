package nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import org.springframework.stereotype.Service;

@Service
public class X01AverageStatisticsServiceImpl implements IX01AverageStatisticsService {

    /**
     * Updates the player's average statistics based on the current round's score and dart usage.
     *
     * @param playerAverageStats {@link X01AverageStatistics} the average statistics of the player to be updated.
     * @param playerScore  {@link X01LegRoundScore} the score and dart usage details for the current round.
     * @param roundNumber       {@link int} the current round number (used to differentiate first nine rounds).
     */
    @Override
    public void updateAverageStats(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore, int roundNumber) {
        if (playerAverageStats == null || playerScore == null) return;

        // Update the average statistics for with this score
        updatePointsThrown(playerAverageStats, playerScore);
        updateDartsThrown(playerAverageStats, playerScore);
        updateAverage(playerAverageStats);

        // If it's one of the first three rounds, update the first nine statistics
        if (roundNumber <= 3) {
            updatePointsThrownFirstNine(playerAverageStats, playerScore);
            updateDartsThrownFirstNine(playerAverageStats, playerScore);
            updateAverageFirstNine(playerAverageStats);
        }
    }

    /**
     * Updates the total points thrown by the player based on the current round's score.
     *
     * @param playerAverageStats {@link X01AverageStatistics} the player's average statistics.
     * @param playerScore       {@link X01LegRoundScore} the current round's score information.
     */
    private void updatePointsThrown(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore) {
        if (playerAverageStats == null || playerScore == null) return;

        // Increment the total points thrown by the player's score in the current round
        playerAverageStats.setPointsThrown(playerAverageStats.getPointsThrown() + playerScore.getScore());
    }

    /**
     * Updates the total darts thrown by the player based on the current round's dart usage.
     *
     * @param playerAverageStats {@link X01AverageStatistics} the player's average statistics.
     * @param playerScore       {@link X01LegRoundScore} the current round's dart usage information.
     */
    private void updateDartsThrown(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore) {
        if (playerAverageStats == null || playerScore == null) return;

        // Increment the total darts thrown by the player's darts used in the current round
        playerAverageStats.setDartsThrown(playerAverageStats.getDartsThrown() + playerScore.getDartsUsed());
    }

    /**
     * Calculates and updates the player's overall average based on the total points and darts thrown.
     *
     * @param playerAverageStats {@link X01AverageStatistics} the player's average statistics.
     */
    private void updateAverage(X01AverageStatistics playerAverageStats) {
        if (playerAverageStats == null) return;

        // Calculate the one-dart average and update the player's overall average with their three-dart average
        double oneDartAvg = (double) playerAverageStats.getPointsThrown() / playerAverageStats.getDartsThrown();
        playerAverageStats.setAverage((int) Math.round(oneDartAvg * 3));
    }

    /**
     * Updates the total points thrown by the player for the first nine darts.
     *
     * @param playerAverageStats {@link X01AverageStatistics} the player's average statistics.
     * @param playerScore       {@link X01LegRoundScore} the current round's score information.
     */
    private void updatePointsThrownFirstNine(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore) {
        if (playerAverageStats == null || playerScore == null) return;

        // Increment the total points thrown for the first nine darts
        playerAverageStats.setPointsThrownFirstNine(playerAverageStats.getPointsThrownFirstNine() + playerScore.getScore());
    }

    /**
     * Updates the total darts thrown by the player for the first nine darts.
     *
     * @param playerAverageStats {@link X01AverageStatistics} the player's average statistics.
     * @param playerScore       {@link X01LegRoundScore} the current round's dart usage information.
     */
    private void updateDartsThrownFirstNine(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore) {
        if (playerAverageStats == null || playerScore == null) return;

        // Increment the total darts thrown for the first nine darts
        playerAverageStats.setDartsThrownFirstNine(playerAverageStats.getDartsThrownFirstNine() + playerScore.getDartsUsed());
    }

    /**
     * Calculates and updates the player's average for the first nine darts.
     *
     * @param playerAverageStats {@link X01AverageStatistics} the player's average statistics.
     */
    private void updateAverageFirstNine(X01AverageStatistics playerAverageStats) {
        if (playerAverageStats == null) return;

        // Calculate the three-dart average for the first nine darts and update the player's average for the first nine darts
        double firstNineOneDartAvg = (double) playerAverageStats.getPointsThrownFirstNine() / playerAverageStats.getDartsThrownFirstNine();
        playerAverageStats.setAverageFirstNine((int) Math.round(firstNineOneDartAvg * 3));
    }

}
