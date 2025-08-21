package nl.kmartin.dartsmatcherapiv2.features.x01.x01rules;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01ClearByTwoRule;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;

@Service
public class X01RulesServiceImpl implements IX01RulesService {

    /**
     * Get the maximum number to play:
     * - When clear by two is enabled; sum of bestOf and clear by two limit.
     * - When clear by two is disabled; sum of bestOf.
     *
     * @param bestOf         the best of setting.
     * @param clearByTwoRule the clear by two rule.
     * @return the maximum number to play.
     */
    @Override
    public int getMaxToPlay(int bestOf, X01ClearByTwoRule clearByTwoRule) {
        return clearByTwoRule.isEnabled() ? bestOf + clearByTwoRule.getLimit() : bestOf;
    }

    /**
     * Checks if the match has only one player.
     *
     * @param standings     The current standings map.
     * @param leaderScore   The highest score.
     * @param runnerUpScore The second-highest score, or null if none (all players same score).
     * @return true if there is exactly one player, false otherwise.
     */
    @Override
    public boolean isSinglePlayerMatch(TreeMap<Integer, List<ObjectId>> standings, int leaderScore, Integer runnerUpScore) {
        // A match has only one player if there is exactly 1 leader and there are no runner-ups.
        return runnerUpScore == null && standings.get(leaderScore).size() == 1;
    }

    /**
     * Determines if the winner is confirmed. First checks if the winner cannot be caught by the standard best of rules.
     * Then checks if the clear by two rule is satisfied.
     *
     * @param diff            The difference in score won between the leader and runner-up.
     * @param bestOfRemaining The number remaining to be played.
     * @param played          The number already played.
     * @param bestOf          the best of setting.
     * @param clearByTwoRule  The clear-by-two rule settings.
     * @return true if the winner can be confirmed, false otherwise.
     */
    @Override
    public boolean isWinnerConfirmed(int diff, int bestOfRemaining, int played, int bestOf, X01ClearByTwoRule clearByTwoRule) {
        return winnerCannotBeCaught(diff, bestOfRemaining) &&
                isClearByTwoSatisfied(diff, played, bestOf, clearByTwoRule);
    }

    /**
     * Determines if the leader cannot be caught by the runner-up given the remaining number to be played.
     *
     * @param diff            The difference score between the leader and runner-up.
     * @param bestOfRemaining The number remaining to be played.
     * @return true if the leader's lead is greater than the remaining number to be played, or if there is none remaining.
     */
    @Override
    public boolean winnerCannotBeCaught(int diff, int bestOfRemaining) {
        return bestOfRemaining == 0 || diff > bestOfRemaining;
    }

    /**
     * Checks if the clear-by-two rule is satisfied. it is satisfied when:
     * - The rule is disabled
     * - Difference between winner and runner-up is two
     * - The match has reached the best of limit + clear by two limit.
     *
     * @param diff           The difference between the leader and runner-up.
     * @param played         The number for already played.
     * @param bestOf         the best of setting.
     * @param clearByTwoRule The clear-by-two rule settings.
     * @return true if the clear-by-two condition is met or the match should end due to limit, false otherwise.
     */
    @Override
    public boolean isClearByTwoSatisfied(int diff, int played, int bestOf, X01ClearByTwoRule clearByTwoRule) {
        // Clear by two is disabled, so it is always satisfied.
        if (!clearByTwoRule.isEnabled()) return true;

        // Difference is 2 or more so clear by two is satisfied.
        if (diff >= 2) return true;

        // Calculate the maximum number that can be played.
        int maxToPlay = getMaxToPlay(bestOf, clearByTwoRule);

        // Match ends if played equals or exceeds max.
        return played >= maxToPlay;
    }

}
