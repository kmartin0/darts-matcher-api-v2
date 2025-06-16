package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface IX01SetProgressService {
    Optional<X01Leg> getLeg(X01Set set, int legNumber, boolean throwIfNotFound);

    Optional<X01Leg> getCurrentLeg(X01Set set);

    Optional<X01Leg> createNextLeg(X01Set set, List<X01MatchPlayer> players, int bestOfLegs, ObjectId throwsFirstInSet);

    java.util.Set<Integer> getLegNumbers(X01Set set);

    boolean isSetConcluded(X01Set x01Set, List<X01MatchPlayer> players);
}
