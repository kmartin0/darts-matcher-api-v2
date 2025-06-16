package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;

public interface IX01MatchResultService {
    void updateMatchResult(X01Match x01Match);

    void updateSetResults(X01Match match);

    List<ObjectId> getMatchWinners(X01Match x01Match);

    java.util.Map<ObjectId, Long> getMatchStandings(X01Match match);

    void removeSetsAfterWinner(X01Match match, List<ObjectId> matchWinners);
}
