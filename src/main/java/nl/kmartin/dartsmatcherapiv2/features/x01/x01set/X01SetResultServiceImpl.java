package nl.kmartin.dartsmatcherapiv2.features.x01.x01set;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegResultService;
import nl.kmartin.dartsmatcherapiv2.utils.StandingsUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class X01SetResultServiceImpl implements IX01SetResultService {

    private final IX01LegProgressService legProgressService;
    private final IX01LegResultService legResultService;

    public X01SetResultServiceImpl(IX01LegProgressService legProgressService, IX01LegResultService legResultService) {
        this.legProgressService = legProgressService;
        this.legResultService = legResultService;
    }

    /**
     * Updates the player results for a set.
     *
     * @param x01Set     {@link X01Set} the set to be updated
     * @param bestOfLegs int the best of setting for the legs
     * @param players    {@link List<X01MatchPlayer>} the list of match players
     */
    @Override
    public void updateSetResult(X01Set x01Set, int bestOfLegs, List<X01MatchPlayer> players, int x01) {
        // If the set is null exit early, if the players are null clear the set result and exit early.
        if (x01Set == null) return;
        if (X01ValidationUtils.isPlayersEmpty(players)) {
            x01Set.setResult(null);
            return;
        }

        updateLegResults(x01Set, players, x01);

        // Get the player(s) that have won the set
        List<ObjectId> setWinners = getSetWinners(x01Set, bestOfLegs, players);

        if (!setWinners.isEmpty()) { // The set has a result
            // If multiple players have won the set, that means they have drawn.
            ResultType winOrDrawType = setWinners.size() > 1 ? ResultType.DRAW : ResultType.WIN;

            // Map and set the player results in the set. Players in the setWinners list get a win/draw. The rest gets a loss
            x01Set.setResult(players.stream()
                    .collect(Collectors.toMap(
                            X01MatchPlayer::getPlayerId,
                            player -> setWinners.contains(player.getPlayerId()) ? winOrDrawType : ResultType.LOSS
                    ))
            );

            // Clears legs that might linger after the set winners.
            removeLegsAfterSetWinner(x01Set, setWinners);
        } else { // The set has no result
            x01Set.setResult(null);
        }
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
        if (X01ValidationUtils.isLegsEmpty(set)) return;

        // For each leg update the leg result.
        set.getLegs().forEach(x01Leg -> legResultService.updateLegResult(x01Leg, players, x01));
    }

    /**
     * A list of object ids is created containing all players that have won the set. Multiple set winners
     * means a draw has occurred.
     *
     * @param x01Set     {@link X01Set} The set for which the winners are being determined.
     * @param bestOfLegs int the best of setting for the legs
     * @param players    {@link List<X01MatchPlayer>} the list of match players
     * @return {@link List<ObjectId>} containing the IDs of players who won the set. multiple winners indicates a draw.
     */
    @Override
    public List<ObjectId> getSetWinners(X01Set x01Set, int bestOfLegs, List<X01MatchPlayer> players) {
        if (X01ValidationUtils.isLegsEmpty(x01Set) || X01ValidationUtils.isPlayersEmpty(players))
            return Collections.emptyList();

        // Get the standings for the set.
        Map<ObjectId, Long> setStandings = getSetStandings(x01Set, players);

        // Get the player(s) that have won the set
        int remainingLegs = calcRemainingLegs(bestOfLegs, x01Set);
        return StandingsUtils.determineWinners(setStandings, remainingLegs);
    }

    /**
     * Determines the number of legs each player has won for a given set
     *
     * @param set     {@link X01Set} the set for which standings need to be calculated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return Map<ObjectId, Long> containing the number of legs each player has won
     */
    @Override
    public Map<ObjectId, Long> getSetStandings(X01Set set, List<X01MatchPlayer> players) {
        if (set == null || X01ValidationUtils.isPlayersEmpty(players)) return Collections.emptyMap();

        // Initialize standings map with all players and 0 wins
        Map<ObjectId, Long> standings = players.stream()
                .collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0L));

        // Update the map with the number of wins from the legs for each player
        set.getLegs().stream()
                .filter(x01Leg -> x01Leg.getWinner() != null)  // Filter out legs with no winner
                .forEach(x01Leg -> standings.merge(x01Leg.getWinner(), 1L, Long::sum)); // Increment win count for the winner

        // Return the standings map
        return standings;
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
        if (X01ValidationUtils.isLegsEmpty(set) || CollectionUtils.isEmpty(setWinners)) return;

        List<X01Leg> reverseLegs = new ArrayList<>(set.getLegs());
        Collections.reverse(reverseLegs);

        // Iterate legs backwards, removing any legs that come after a set winner. Stop when a winner is matched.
        for (X01Leg leg : reverseLegs) {
            if (setWinners.contains(leg.getWinner())) break;
            set.getLegs().remove(leg);
        }
    }

    /**
     * Determine the number of legs that are yet to be played in a set
     *
     * @param bestOfLegs int the maximum number of legs going to be played
     * @param x01Set     {@link X01Set} the set for which the remaining legs need to be determined
     * @return int the number of legs can still be played
     */
    private int calcRemainingLegs(int bestOfLegs, X01Set x01Set) {
        // If the set or legs don't exist, the remaining legs are the maximum number of legs to be played
        if (X01ValidationUtils.isLegsEmpty(x01Set)) return bestOfLegs;

        // Count the number of completed legs (legs with a winner)
        long completedLegs = x01Set.getLegs().stream()
                .filter(legProgressService::isLegConcluded)
                .count();

        // Determine the number of remaining legs and return them. Ensuring they are not negative.
        int remainingLegs = bestOfLegs - (int) completedLegs;
        return Math.max(remainingLegs, 0);
    }

}
