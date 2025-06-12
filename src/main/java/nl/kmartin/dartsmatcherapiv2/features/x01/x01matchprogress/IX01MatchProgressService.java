package nl.kmartin.dartsmatcherapiv2.features.x01.x01matchprogress;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;

import java.util.Optional;

public interface IX01MatchProgressService {
    void updateMatchResult(X01Match x01Match);

    void updateMatchProgress(X01Match x01Match);

    Optional<X01Set> getCurrentSetOrCreate(X01Match x01Match);

    Optional<X01Leg> getCurrentLegOrCreate(X01Match x01Match, X01Set currentSet);

    Optional<X01LegRound> getCurrentLegRoundOrCreate(X01Match x01Match, X01Leg currentLeg);
}
