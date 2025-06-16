package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegService {
    X01Leg createNewLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players);

    void addScore(int x01, X01Leg leg, int roundNumber, X01LegRoundScore roundScore, List<X01MatchPlayer> players, ObjectId throwerId);

    void checkLegEditable(X01Leg leg, ObjectId playerId);

    boolean isScoreCheckout(X01Leg leg, X01LegRound legRound, ObjectId playerId);

    boolean validateLegForPlayer(X01Leg leg, int x01, ObjectId throwerId);

    ObjectId calcThrowsFirstInLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players);
}
