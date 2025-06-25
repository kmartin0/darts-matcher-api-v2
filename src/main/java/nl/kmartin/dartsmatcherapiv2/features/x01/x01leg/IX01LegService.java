package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegService {
    X01LegEntry createNewLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players);

    void addScore(int x01, X01Leg leg, int roundNumber, X01Turn turn, List<X01MatchPlayer> players, ObjectId throwerId, boolean trackDoubles);

    void checkLegEditable(X01Leg leg, ObjectId playerId);

    boolean isPlayerCheckoutRound(X01Leg leg, int roundNumber, ObjectId playerId);

    boolean validateLegForPlayer(X01Leg leg, int x01, ObjectId throwerId);

    ObjectId calcThrowsFirstInLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players);
}
