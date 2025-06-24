package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;

import java.util.Map;
import java.util.Optional;

public interface IX01MatchProgressService {
    Optional<X01Set> getSet(X01Match match, int setNumber, boolean throwIfNotFound);

    Optional<X01Set> getCurrentSet(X01Match match);

    Optional<X01Set> createNextSet(X01Match match);

    java.util.Set<Integer> getSetNumbers(X01Match match);

    Optional<X01Set> getCurrentSetOrCreate(X01Match match);

    Optional<X01Leg> getCurrentLegOrCreate(X01Match match, X01Set currentSet);

    Optional<Map.Entry<Integer, X01LegRound>> getCurrentLegRoundOrCreate(X01Match match, X01Leg currentLeg);

    boolean isMatchConcluded(X01Match match);

    void deleteLastScore(X01Match match);

    void updateMatchProgress(X01Match match);
}
