package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IX01LegService {

    void addScore(int x01, X01Leg x01Leg, X01LegRound x01LegRound, X01LegRoundScore x01LegRoundScore, List<X01MatchPlayer> matchPlayers, ObjectId throwerId) throws IOException;

    Optional<X01Leg> getCurrentLeg(List<X01Leg> legs);

    Optional<X01Leg> createNextLeg(List<X01Leg> legs, List<X01MatchPlayer> players, int bestOfLegs, ObjectId throwsFirstInSet);

    X01Leg createNewLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players);

    Set<Integer> getLegNumbers(List<X01Leg> legs);

    ObjectId calcThrowsFirstInLeg(int legNumber, ObjectId throwsFirstInSet, List<X01MatchPlayer> players);

    boolean isLegConcluded(X01Leg x01Leg);

    Optional<X01Leg> getLeg(List<X01Leg> legs, int legNumber);

    boolean isLegEditable(X01Leg x01Leg, ObjectId playerId);

    void updateLegResults(List<X01Leg> legs, List<X01MatchPlayer> players, int x01);

    void updateLegResult(X01Leg leg, List<X01MatchPlayer> players, int x01);

    boolean isScoreCheckout(X01Leg x01Leg, X01LegRound x01LegRound, ObjectId playerId);
}
