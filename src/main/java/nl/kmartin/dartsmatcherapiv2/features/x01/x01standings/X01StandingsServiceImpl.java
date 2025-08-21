package nl.kmartin.dartsmatcherapiv2.features.x01.x01standings;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01rules.IX01RulesService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01StandingsServiceImpl implements IX01StandingsService {

    private final IX01MatchProgressService matchProgressService;
    private final IX01RulesService rulesService;

    public X01StandingsServiceImpl(IX01MatchProgressService matchProgressService, IX01RulesService rulesService) {
        this.matchProgressService = matchProgressService;
        this.rulesService = rulesService;
    }

    /**
     * Updates the match standings for a match.
     *
     * @param match {@link X01Match} The match for which the standings need to be updated.
     */
    @Override
    public void updateMatchStandings(X01Match match) {
        if (match == null) return;

        // Get the current set or the last set if the match is concluded.
        Optional<X01SetEntry> currentSet = matchProgressService.getCurrentSet(match)
                .or(() -> Optional.ofNullable(match.getSets().lastEntry())
                        .map(X01SetEntry::new));

        // Create the initial standings map with a value for each player set to 0 wins.
        LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings = createInitialStandings(match.getPlayers());

        // Iterate through the sets and update the legsWonInSet and setsWon counts for the set/leg winners.
        match.getSets().entrySet().stream().map(X01SetEntry::new).forEach(setEntry -> {
            // For the current set update the legsWonInSet counts.
            currentSet.ifPresent(currentSetEntry -> {
                if (currentSetEntry.setNumber() == setEntry.setNumber())
                    updateStandingsWithLegWinners(currentSetEntry, matchStandings);
            });

            // Update the setsWon counts
            updateStandingsWithSetWinners(setEntry, matchStandings);
        });

        // Replace the match standings with the newly created standings.
        match.setStandings(matchStandings);
    }

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
        if (rulesService.isSinglePlayerMatch(standings, leaderScore, runnerUpScore)) {
            if (bestOfRemaining == 0) return new ArrayList<>(standings.get(leaderScore));
            else return Collections.emptyList();
        }

        // Step 4: If leaderScore cannot be caught up by the remaining best of then everyone in the leaderScore group is a winner.
        if (rulesService.isWinnerConfirmed(diff, bestOfRemaining, played, bestOf, clearByTwoRule)) {
            return new ArrayList<>(standings.get(leaderScore));
        }

        // Step 5: No winners so return the empty winners list.
        return Collections.emptyList();
    }

    /**
     * Creates a standings map with an entry for each player with 0 sets/leg wins.
     *
     * @param players The players that should be in the standings
     * @return A linked hashmap keyed by player id and with a value of an empty {@link X01StandingsEntry}
     */
    private LinkedHashMap<ObjectId, X01StandingsEntry> createInitialStandings(List<X01MatchPlayer> players) {
        return players.stream()
                .collect(Collectors.toMap(
                        X01MatchPlayer::getPlayerId,
                        player -> new X01StandingsEntry(0, 0),
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new
                ));
    }

    /**
     * Updates the standings for all players that have won legs in a set.
     *
     * @param currentSetEntry The set containing the legs to update the standings with.
     * @param matchStandings  The standings that need to be updated.
     */
    private void updateStandingsWithLegWinners(X01SetEntry currentSetEntry, LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings) {
        currentSetEntry.set().getLegs().entrySet().stream().map(X01LegEntry::new).forEach(legEntry -> {
            ObjectId legWinner = legEntry.leg().getWinner();
            if (legWinner != null && matchStandings.containsKey(legWinner)) {
                X01StandingsEntry playerStandings = matchStandings.get(legWinner);
                playerStandings.setLegsWonInCurrentSet(playerStandings.getLegsWonInCurrentSet() + 1);
            }
        });
    }

    /**
     * Updates the standings for all players that have won or drawn a set.
     *
     * @param setEntry       The set containing the result to update the standings with.
     * @param matchStandings The standings that need to be updated.
     */
    private void updateStandingsWithSetWinners(X01SetEntry setEntry, LinkedHashMap<ObjectId, X01StandingsEntry> matchStandings) {
        if (!CollectionUtils.isEmpty(setEntry.set().getResult())) {
            setEntry.set().getResult().entrySet().stream().filter(resultEntry ->
                    (resultEntry.getValue().equals(ResultType.WIN) || resultEntry.getValue().equals(ResultType.DRAW) &&
                            matchStandings.containsKey(resultEntry.getKey()))).forEach(resultEntry -> {
                X01StandingsEntry playerStandings = matchStandings.get(resultEntry.getKey());
                playerStandings.setSetsWon(playerStandings.getSetsWon() + 1);
            });
        }
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

}
