package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegResultService {
    void updateLegResult(X01Leg leg, List<X01MatchPlayer> players, int x01);

    void removeScoresAfterWinner(X01Leg leg, ObjectId legWinner);

    int calculateRemainingScore(X01Leg leg, int x01, ObjectId playerId);

    int calculateDartsUsed(X01Leg leg, ObjectId playerId);
}
