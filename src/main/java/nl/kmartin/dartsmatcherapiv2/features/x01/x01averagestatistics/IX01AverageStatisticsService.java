package nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01AverageStatistics;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;

public interface IX01AverageStatisticsService {
    void updateAverageStats(X01AverageStatistics playerAverageStats, X01LegRoundScore playerScore, int roundNumber, Integer checkoutDartsUsed);
}
