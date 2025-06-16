package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01SetResultService {
    void updateSetResult(X01Set set, int bestOfLegs, List<X01MatchPlayer> players, int x01);

    void updateLegResults(X01Set set, List<X01MatchPlayer> players, int x01);

    List<ObjectId> getSetWinners(X01Set set, int bestOfLegs, List<X01MatchPlayer> players);

    java.util.Map<ObjectId, Long> getSetStandings(X01Set set, List<X01MatchPlayer> players);

    void removeLegsAfterSetWinner(X01Set set, List<ObjectId> setWinners);
}
