package nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01ScoreStatistics;
import org.springframework.stereotype.Service;

@Service
public class X01ScoreStatisticsServiceImpl implements IX01ScoreStatisticsService {
    /**
     * Updates the score statistics with the score from the current round.
     *
     * @param playerScoreStats  {@link X01ScoreStatistics} the object to store the updated score statistics
     * @param playerScore {@link X01LegRoundScore} the score information from the current round
     */
    public void updateScoreStatistics(X01ScoreStatistics playerScoreStats, X01LegRoundScore playerScore) {
        if (playerScoreStats == null || playerScore == null) return;

        // Retrieve the score from the player's turn in the current leg round
        int score = playerScore.getScore();

        // Update the score statistics based on the score thresholds
        if (score == 180) playerScoreStats.incrementTonEighty();
        else if (score >= 140) playerScoreStats.incrementTonFortyPlus();
        else if (score >= 100) playerScoreStats.incrementTonPlus();
        else if (score >= 80) playerScoreStats.incrementEightyPlus();
        else if (score >= 60) playerScoreStats.incrementSixtyPlus();
        else if (score >= 40) playerScoreStats.incrementFortyPlus();
    }
}
