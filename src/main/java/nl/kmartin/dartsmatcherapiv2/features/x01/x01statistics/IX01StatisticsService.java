package nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;

import java.util.List;

public interface IX01StatisticsService {
    void updatePlayerStatistics(List<X01Set> sets, List<X01MatchPlayer> matchPlayers);
}
