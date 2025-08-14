package nl.kmartin.dartsmatcherapiv2.features.x01.x01leground;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01LegRoundService {
    ObjectId getCurrentThrowerInRound(X01LegRound legRound, ObjectId throwsFirstInLeg, List<X01MatchPlayer> players);

    List<X01MatchPlayer> getPlayersToThrowInRound(X01LegRound legRound, List<X01MatchPlayer> players);

    boolean removeLastScoreFromRound(X01LegRound legRound);

    void removeScoresAfterWinner(X01LegRound round, ObjectId legWinner);
}
