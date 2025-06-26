package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public interface IX01MatchResultService {
    void updateMatchResult(X01Match x01Match);

    void updateSetResults(X01Match match);

    List<ObjectId> getMatchWinners(X01Match match);

    Map<ObjectId, Long> getMatchStandings(X01Match match);

    void removeSetsAfterWinner(X01Match match, List<ObjectId> matchWinners);
}
