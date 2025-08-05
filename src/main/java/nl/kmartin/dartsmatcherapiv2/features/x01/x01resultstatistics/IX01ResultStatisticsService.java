package nl.kmartin.dartsmatcherapiv2.features.x01.x01resultstatistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.bson.types.ObjectId;

import java.util.Map;

public interface IX01ResultStatisticsService {
    void updateSetsWonStatistics(X01Set set, Map<ObjectId, X01MatchPlayer> playersMap);

    void updateLegsWonStatistics(X01Leg leg, Map<ObjectId, X01MatchPlayer> playersMap);
}
