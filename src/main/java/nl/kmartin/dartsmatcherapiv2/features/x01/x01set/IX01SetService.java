package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IX01SetService {
    Optional<X01Set> getCurrentSet(List<X01Set> sets);

    Optional<X01Set> createNextSet(List<X01Set> sets, List<X01MatchPlayer> matchPlayers, int bestOfSets);

    X01Set createNewSet(int setNumber, List<X01MatchPlayer> players);

    Set<Integer> getSetNumbers(List<X01Set> sets);

    ObjectId calcThrowsFirstInSet(int setNumber, List<X01MatchPlayer> players);

    Map<ObjectId, Long> getSetStandings(X01Set set, List<X01MatchPlayer> players);

    int calcRemainingLegs(int bestOfLegs, X01Set x01Set);

    void updateSetResult(X01Set x01Set, int bestOfLegs, List<X01MatchPlayer> players);

    boolean isSetConcluded(X01Set x01Set, List<X01MatchPlayer> players);

    Optional<X01Set> getSet(List<X01Set> sets, int setNumber, boolean throwIfNotFound);

    void updateSetResults(List<X01Set> sets, List<X01MatchPlayer> players, int bestOfLegs, int x01);

    void deleteLastScore(List<X01Set> sets);
}
