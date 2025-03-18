package nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01ScoreStatistics;

public interface IX01ScoreStatisticsService {
    void updateScoreStatistics(X01ScoreStatistics playerScoreStats, X01LegRoundScore playerScore);
}
