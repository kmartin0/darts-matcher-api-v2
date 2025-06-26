package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface IX01LegProgressService {
    Optional<X01LegRoundEntry> getLegRound(X01Leg leg, int roundNumber, boolean throwIfNotFound);

    Optional<X01LegRoundEntry> getCurrentLegRound(X01Leg leg, List<X01MatchPlayer> players);

    Optional<X01LegRoundEntry> createNextLegRound(X01Leg leg);

    Set<Integer> getLegRoundNumbers(X01Leg leg);

    Optional<X01LegRoundEntry> getLastRound(X01Leg leg);

    Optional<X01LegRoundScore> getLastScoreForPlayer(X01Leg leg, ObjectId throwerId);

    boolean isLegConcluded(X01Leg leg);

    boolean removeLastScoreFromLeg(X01Leg leg);
}
