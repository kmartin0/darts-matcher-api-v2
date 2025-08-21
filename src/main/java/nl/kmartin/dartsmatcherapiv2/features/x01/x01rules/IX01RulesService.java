package nl.kmartin.dartsmatcherapiv2.features.x01.x01rules;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01ClearByTwoRule;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.TreeMap;

public interface IX01RulesService {

    int getMaxToPlay(int bestOf, X01ClearByTwoRule clearByTwoRule);

    boolean isSinglePlayerMatch(TreeMap<Integer, List<ObjectId>> standings, int leaderScore, Integer runnerUpScore);

    boolean isWinnerConfirmed(int diff, int bestOfRemaining, int played, int bestOf, X01ClearByTwoRule clearByTwoRule);

    boolean winnerCannotBeCaught(int diff, int bestOfRemaining);

    boolean isClearByTwoSatisfied(int diff, int played, int bestOf, X01ClearByTwoRule clearByTwoRule);
}
