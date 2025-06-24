package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.Set;


public interface IX01LegProgressService {
    Optional<X01LegRound> getLegRound(X01Leg leg, int roundNumber, boolean throwIfNotFound);

    Optional<Map.Entry<Integer, X01LegRound>> getCurrentLegRound(X01Leg leg, List<X01MatchPlayer> players);

    Optional<Map.Entry<Integer, X01LegRound>> createNextLegRound(X01Leg leg);

    Set<Integer> getLegRoundNumbers(X01Leg leg);

    Optional<X01LegRound> getLastRound(X01Leg leg);

    Optional<X01LegRoundScore> getLastScoreForPlayer(X01Leg leg, ObjectId throwerId);

    boolean isLegConcluded(X01Leg leg);
}
