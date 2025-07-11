package nl.kmartin.dartsmatcherapiv2.features.x01.x01standings;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01ClearByTwoRule;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01StandingsServiceImpl implements IX01StandingsService {

    /**
     * Determines the winners from the current standings based on the number of legs/sets played,
     * the target number to win (bestOf), and the clear-by-two rule.
     *
     * @param standings      TreeMap with key the number of legs/sets won, and value a list of ObjectIds of players with that score.
     * @param played         The number of legs or sets played so far.
     * @param bestOf         the best of setting.
     * @param clearByTwoRule The clear-by-two rule settings.
     * @return A list of ObjectIds representing the winner(s), or an empty list if no winner yet.
     */
    @Override
    public List<ObjectId> determineWinners(TreeMap<Integer, List<ObjectId>> standings, int played, int bestOf, X01ClearByTwoRule clearByTwoRule) {
        // Step 1: Empty standings means no winners.
        if (CollectionUtils.isEmpty(standings)) return Collections.emptyList();

        // Step 2: Find the most legs won and second most legs won and calculate the difference.
        int leaderScore = standings.lastKey();
        Integer runnerUpScore = standings.lowerKey(leaderScore);
        int diff = leaderScore - (runnerUpScore != null ? runnerUpScore : leaderScore);
        int bestOfRemaining = bestOf - played;

        // Step 3: For single player match continue until no remaining
        if (isSinglePlayerMatch(standings, leaderScore, runnerUpScore)) {
            if (bestOfRemaining == 0) return new ArrayList<>(standings.get(leaderScore));
            else return Collections.emptyList();
        }

        // Step 4: If leaderScore cannot be caught up by the remaining best of then everyone in the leaderScore group is a winner.
        if (isWinnerConfirmed(diff, bestOfRemaining, played, bestOf, clearByTwoRule)) {
            return new ArrayList<>(standings.get(leaderScore));
        }

        // Step 5: No winners so return the empty winners list.
        return Collections.emptyList();
    }

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
     * Groups players by their win counts into a TreeMap:
     * - Key is the number of wins
     * - Value is the list of player Ids who have that number of wins.
     *
     * @param winsPerPlayer a map of player IDs to their number of wins
     * @return a TreeMap where each key is a win count and the value is a list of player IDs with that win count
     */
    @Override
    public TreeMap<Integer, List<ObjectId>> groupByWinCounts(Map<ObjectId, Long> winsPerPlayer) {
        return winsPerPlayer.entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getValue().intValue(), // classifier: group by win count
                        TreeMap::new, // supplier: group into a sorted TreeMap
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList()) // downstream: map entries to player IDs list
                ));
    }

    /**
     * Checks if the match has only one player.
     *
     * @param standings     The current standings map.
     * @param leaderScore   The highest score.
     * @param runnerUpScore The second-highest score, or null if none (all players same score).
     * @return true if there is exactly one player, false otherwise.
     */
    private boolean isSinglePlayerMatch(TreeMap<Integer, List<ObjectId>> standings, int leaderScore, Integer runnerUpScore) {
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
    private boolean isWinnerConfirmed(int diff, int bestOfRemaining, int played, int bestOf, X01ClearByTwoRule clearByTwoRule) {
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
    private boolean winnerCannotBeCaught(int diff, int bestOfRemaining) {
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
    private boolean isClearByTwoSatisfied(int diff, int played, int bestOf, X01ClearByTwoRule clearByTwoRule) {
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
