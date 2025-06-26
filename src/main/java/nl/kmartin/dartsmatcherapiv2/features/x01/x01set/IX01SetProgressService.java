package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegEntry;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IX01SetProgressService {
    Optional<X01LegEntry> getLeg(X01Set set, int legNumber, boolean throwIfNotFound);

    Optional<X01LegEntry> getCurrentLeg(X01Set set);

    Optional<X01LegEntry> createNextLeg(X01Set set, List<X01MatchPlayer> players, int bestOfLegs, ObjectId throwsFirstInSet);

    Set<Integer> getLegNumbers(X01Set set);

    boolean isSetConcluded(X01Set set, List<X01MatchPlayer> players);
}
