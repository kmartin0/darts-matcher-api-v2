package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface IX01LegResultService {
    void updateLegResult(X01Leg leg, List<X01MatchPlayer> players, int x01);

    void removeScoresAfterWinner(X01Leg leg, ObjectId legWinner);

    Optional<X01MatchPlayer> findLegWinner(X01Leg leg, List<X01MatchPlayer> players, int x01);

    int getRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01);

    void updateRemaining(X01Leg leg, ObjectId playerId, int x01);

    void updateRemaining(X01Leg leg, int x01);

    int calculateDartsUsed(X01Leg leg, ObjectId playerId);
}
