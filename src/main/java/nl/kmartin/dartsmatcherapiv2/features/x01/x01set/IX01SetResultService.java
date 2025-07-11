package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01BestOf;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01SetEntry;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.TreeMap;

public interface IX01SetResultService {
    void updateSetResult(X01SetEntry setEntry, X01BestOf bestOf, List<X01MatchPlayer> players, int x01);

    void updateLegResults(X01Set set, List<X01MatchPlayer> players, int x01);

    List<ObjectId> getSetWinners(X01SetEntry setEntry, X01BestOf bestOf, List<X01MatchPlayer> players);

    TreeMap<Integer, List<ObjectId>> getSetStandings(X01Set set, List<X01MatchPlayer> players);

    void removeLegsAfterSetWinner(X01Set set, List<ObjectId> setWinners);
}
