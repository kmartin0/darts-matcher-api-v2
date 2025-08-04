package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.TreeMap;

public interface IX01MatchResultService {
    void updateMatchResult(X01Match match);

    void updateSetResults(X01Match match);

    List<ObjectId> getMatchWinners(X01Match match);

    TreeMap<Integer, List<ObjectId>> getMatchStandings(X01Match match);

    void removeSetsAfterWinner(X01Match match, List<ObjectId> matchWinners);
}
