package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IX01SetProgressService {
    Optional<X01LegEntry> getLeg(X01Set set, int legNumber, boolean throwIfNotFound);

    Optional<X01LegEntry> getCurrentLeg(X01Set set);

    Optional<X01LegEntry> createNextLeg(X01SetEntry setEntry, List<X01MatchPlayer> players, X01BestOf bestOf, ObjectId throwsFirstInSet);

    Set<Integer> getLegNumbers(X01Set set);

    boolean isSetConcluded(X01Set set, List<X01MatchPlayer> players);

    boolean removeLastScoreFromSet(X01Set set);
}
