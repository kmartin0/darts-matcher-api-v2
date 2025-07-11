package nl.kmartin.dartsmatcherapiv2.features.x01.x01standings;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01ClearByTwoRule;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IX01StandingsService {
    List<ObjectId> determineWinners(TreeMap<Integer, List<ObjectId>> standings, int played, int bestOf, X01ClearByTwoRule clearByTwoRule);

    int getMaxToPlay(int bestOf, X01ClearByTwoRule clearByTwoRule);

    TreeMap<Integer, List<ObjectId>> groupByWinCounts(Map<ObjectId, Long> winsPerPlayer);
}
