package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegResultService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01standings.IX01StandingsService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01SetResultServiceImpl implements IX01SetResultService {

    private final IX01LegProgressService legProgressService;
    private final IX01LegResultService legResultService;
    private final IX01StandingsService standingsService;

    public X01SetResultServiceImpl(IX01LegProgressService legProgressService, IX01LegResultService legResultService, IX01StandingsService standingsService) {
        this.legProgressService = legProgressService;
        this.legResultService = legResultService;
        this.standingsService = standingsService;
    }

    /**
     * Updates the player results for a set.
     *
     * @param setEntry {@link X01SetEntry} the set to be updated
     * @param bestOf   {@link X01BestOf} the best of setting for the match
     * @param players  {@link List<X01MatchPlayer>} the list of match players
     */
    @Override
    public void updateSetResult(X01SetEntry setEntry, X01BestOf bestOf, List<X01MatchPlayer> players, int x01) {
        // If the set is null exit early, if the players are null clear the set result and exit early.
        if (setEntry == null || setEntry.set() == null) return;
        X01Set set = setEntry.set();

        if (X01MatchUtils.isPlayersEmpty(players)) {
            set.setResult(null);
            return;
        }

        // Update all leg results within this set.
        updateLegResults(set, players, x01);

        // Update the set results map.
        List<ObjectId> setWinners = getSetWinners(setEntry, bestOf, players);
        updatePlayerResults(set, players, setWinners);
    }

    /**
     * Updates the leg result for all legs in a set
     *
     * @param set     {@link X01Set} the set for which the legs need to be updated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @param x01     int the x01 setting for the legs
     */
    @Override
    public void updateLegResults(X01Set set, List<X01MatchPlayer> players, int x01) {
        if (X01MatchUtils.isLegsEmpty(set)) return;

        // For each leg update the leg result.
        set.getLegs().values().forEach(x01Leg -> legResultService.updateLegResult(x01Leg, players, x01));
    }

    /**
     * A list of object ids is created containing all players that have won the set. Multiple set winners
     * means a draw has occurred.
     *
     * @param setEntry {@link X01SetEntry} The set for which the winners are being determined.
     * @param bestOf   {@link X01BestOf} the best of setting for the match
     * @param players  {@link List<X01MatchPlayer>} the list of match players
     * @return {@link List<ObjectId>} containing the IDs of players who won the set. multiple winners indicates a draw.
     */
    @Override
    public List<ObjectId> getSetWinners(X01SetEntry setEntry, X01BestOf bestOf, List<X01MatchPlayer> players) {
        if (setEntry == null || X01MatchUtils.isLegsEmpty(setEntry.set()) || X01MatchUtils.isPlayersEmpty(players))
            return Collections.emptyList();

        // Get the standings for the set.
        TreeMap<Integer, List<ObjectId>> setStandings = getSetStandings(setEntry.set(), players);

        // Get the parameters for determine winners method.
        int legsPlayedInSet = calcLegsPlayed(setEntry.set());
        int bestOfLegs = bestOf.getLegs();
        X01ClearByTwoRule clearByTwoLegsRule = bestOf.getClearByTwoLegsRuleForSet(setEntry.setNumber());

        // Get the player(s) that have won the set
        return standingsService.determineWinners(setStandings, legsPlayedInSet, bestOfLegs, clearByTwoLegsRule);
    }

    /**
     * Determines the number of legs each player has won for a given set
     *
     * @param set     {@link X01Set} the set for which standings need to be calculated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return TreeMap<Integer, List<ObjectId>> containing the number of legs each player has won
     */
    @Override
    public TreeMap<Integer, List<ObjectId>> getSetStandings(X01Set set, List<X01MatchPlayer> players) {
        if (set == null || X01MatchUtils.isPlayersEmpty(players)) return new TreeMap<>();

        // Initialize standings map with all players and 0 wins
        Map<ObjectId, Long> winsPerPlayer = players.stream()
                .collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0L));

        // Update the map with the number of wins from the legs for each player
        set.getLegs().values().stream()
                .filter(x01Leg -> x01Leg.getWinner() != null)  // Filter out legs with no winner
                .forEach(x01Leg -> winsPerPlayer.merge(x01Leg.getWinner(), 1L, Long::sum)); // Increment win count for the winner

        // Step 3: Group players by number of wins in a tree map.
        return standingsService.groupByWinCounts(winsPerPlayer);
    }

    /**
     * Removes all legs from the given list that occur after the last leg won by a player
     * present in the setWinners list.
     *
     * This is useful for cleaning up any trailing legs after a set winner has
     * already been decided, which may happen after score edits or corrections.
     *
     * @param set        {@link List<X01Leg>} the set to be potentially modified
     * @param setWinners {@link List<ObjectId>} the list of player IDs who have won (or drawn) the set
     */
    @Override
    public void removeLegsAfterSetWinner(X01Set set, List<ObjectId> setWinners) {
        if (X01MatchUtils.isLegsEmpty(set) || CollectionUtils.isEmpty(setWinners)) return;

        // Iterate legs backwards, removing any legs that come after a set winner. Stop when a winner is matched.
        Iterator<Integer> iterator = set.getLegs().descendingKeySet().iterator();
        while (iterator.hasNext()) {
            Integer legKey = iterator.next();
            X01Leg leg = set.getLegs().get(legKey);
            if (setWinners.contains(leg.getWinner())) break;
            iterator.remove();
        }
    }

    /**
     * Updates the player results for a given set based on the list of set winners.
     * If there are one or more winners:
     * - A single winner is marked with {@link ResultType#WIN}.
     * - Multiple winners are marked with {@link ResultType#DRAW}.
     * - All other players are marked with {@link ResultType#LOSS}.
     *
     * If no winners are present, the set result is set to null
     * Additionally, lingering legs after the winners' final leg get removed.
     *
     * @param set        {@link X01Set} the set to update the player results for.
     * @param players    {@link List<X01MatchPlayer>} the players playing the match.
     * @param setWinners {@link List<ObjectId>} the winners of the set (be empty if no result)
     */
    private void updatePlayerResults(X01Set set, List<X01MatchPlayer> players, List<ObjectId> setWinners) {
        if (!setWinners.isEmpty()) { // The set has a result
            // If multiple players have won the set, that means they have drawn.
            ResultType winOrDrawType = setWinners.size() > 1 ? ResultType.DRAW : ResultType.WIN;

            // Map and set the player results in the set. Players in the setWinners list get a win/draw. The rest gets a loss
            set.setResult(players.stream()
                    .collect(Collectors.toMap(
                            X01MatchPlayer::getPlayerId,
                            player -> setWinners.contains(player.getPlayerId()) ? winOrDrawType : ResultType.LOSS
                    ))
            );

            // Clears legs that might linger after the set winners.
            removeLegsAfterSetWinner(set, setWinners);
        } else { // The set has no result
            set.setResult(null);
        }
    }

    /**
     * Calculates the number of legs that have been played (concluded) within the given set.
     *
     * @param set {@link X01Set} the set to calculate legs played for
     * @return int the count of concluded legs in the set
     */
    private int calcLegsPlayed(X01Set set) {
        if (set == null) return 0;

        // Count the number of concluded legs
        long completedLegs = set.getLegs().values().stream()
                .filter(legProgressService::isLegConcluded)
                .count();

        // Return the number of concluded legs
        return (int) completedLegs;
    }

}
