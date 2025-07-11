package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetResultService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01standings.IX01StandingsService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01MatchResultServiceImpl implements IX01MatchResultService {

    private final IX01SetResultService setResultService;
    private final IX01SetProgressService setProgressService;
    private final IX01StandingsService standingsService;

    public X01MatchResultServiceImpl(IX01SetResultService setResultService, IX01SetProgressService setProgressService, IX01StandingsService standingsService) {
        this.setResultService = setResultService;
        this.setProgressService = setProgressService;
        this.standingsService = standingsService;
    }

    /**
     * First updates the set results. Then for each match player their results for a match.
     *
     * @param x01Match {@link X01Match} the match to be updated
     */
    @Override
    public void updateMatchResult(X01Match x01Match) {
        if (x01Match == null) return;

        // First update all set results.
        updateSetResults(x01Match);

        // Get the player(s) that have won the match
        List<ObjectId> matchWinners = getMatchWinners(x01Match);

        // If multiple players have won the set, that means they have drawn.
        ResultType winOrDrawType = matchWinners.size() > 1 ? ResultType.DRAW : ResultType.WIN;

        // Set the individual results for each player
        x01Match.getPlayers().forEach(player -> player.setResultType(
                matchWinners.isEmpty() ? null : (matchWinners.contains(player.getPlayerId()) ? winOrDrawType : ResultType.LOSS)
        ));

        // Cleanup trailing sets that may linger beyond the final set.
        removeSetsAfterWinner(x01Match, matchWinners);

        // When there are match winners the match is concluded
        x01Match.setMatchStatus(matchWinners.isEmpty() ? MatchStatus.IN_PLAY : MatchStatus.CONCLUDED);
    }

    /**
     * Updates the result for each set from a match
     *
     * @param match {@link X01Match} the match that contains the sets
     */
    @Override
    public void updateSetResults(X01Match match) {
        if (X01MatchUtils.isSetsEmpty(match)) return;

        List<X01MatchPlayer> players = match.getPlayers();
        int x01 = match.getMatchSettings().getX01();
        X01BestOf bestOf = match.getMatchSettings().getBestOf();

        // For each set update the leg results and after update the set result
        match.getSets().entrySet().forEach(setEntry -> setResultService.updateSetResult(new X01SetEntry(setEntry), bestOf, players, x01));
    }

    /**
     * A list of object ids is created containing all players that have won the match. Multiple match winners
     * means a draw has occurred.
     *
     * @param match {@link X01Match} The match for which the winners are being determined.
     * @return {@link List<ObjectId>} containing the IDs of players who won the match. multiple winners indicates a draw.
     */
    @Override
    public List<ObjectId> getMatchWinners(X01Match match) {
        if (match == null) return Collections.emptyList();

        // Get the standings for the match.
        TreeMap<Integer, List<ObjectId>> matchStandings = getMatchStandings(match);

        // Get the parameters for determine winners method.
        int setsPlayed = calcSetsPlayed(match);
        int bestOfSets = match.getMatchSettings().getBestOf().getSets();
        X01ClearByTwoRule clearByTwoSetsRule = match.getMatchSettings().getBestOf().getClearByTwoSetsRule();

        // Create the winners list.
        return standingsService.determineWinners(matchStandings, setsPlayed, bestOfSets, clearByTwoSetsRule);
    }

    /**
     * Determines the number of sets each player has won for a given match, stored in a tree map. where:
     * - key is number of set wins
     * - value is list of player ids who have the number of wins
     *
     * @param match {@link X01Match} the match for which standings need to be calculated
     * @return TreeMap<Integer, List<ObjectId>> containing the number of sets each player has won
     */
    @Override
    public TreeMap<Integer, List<ObjectId>> getMatchStandings(X01Match match) {
        if (match == null) return new TreeMap<>();

        // Step 1: Initialize a map containing the number of wins for each player. Initialized with an entry of 0 wins per player.
        Map<ObjectId, Long> winsPerPlayer = match.getPlayers().stream()
                .collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0L));

        // Step 2: Iterate through sets, for each won or draw set. Update win count map for the players that have won or drawn.
        match.getSets().values().stream()
                .filter(set -> set.getResult() != null && !set.getResult().isEmpty())
                .flatMap(set -> set.getResult().entrySet().stream())
                .filter(resultEntry -> resultEntry.getValue() == ResultType.WIN || resultEntry.getValue() == ResultType.DRAW)
                .forEach(resultEntry ->
                        winsPerPlayer.merge(resultEntry.getKey(), 1L, Long::sum)
                );

        // Step 3: Group players by number of wins in a tree map.
        return standingsService.groupByWinCounts(winsPerPlayer);
    }

    /**
     * Removes all sets from a match that occur after the last set won by a player
     * present in the matchWinners list.
     *
     * This is useful for cleaning up any trailing sets after a match winner has
     * already been decided, which may happen after score edits or corrections.
     *
     * @param match        {@link X01Match} the match for which the sets need to be potentially modified
     * @param matchWinners {@link List<ObjectId>} the list of player IDs who have won (or drawn) the match
     */
    @Override
    public void removeSetsAfterWinner(X01Match match, List<ObjectId> matchWinners) {
        if (X01MatchUtils.isSetsEmpty(match) || CollectionUtils.isEmpty(matchWinners)) return;

        // Iterate over the sets in reverse order.
        Iterator<X01Set> reverseSetsIterator = match.getSets().descendingMap().values().iterator();
        while (reverseSetsIterator.hasNext()) {
            X01Set set = reverseSetsIterator.next();

            // Determine if this set contains a 'set winner'.
            Map<ObjectId, ResultType> setResultMap = set.getResult();
            boolean setContainsWinner = setResultMap != null && matchWinners.stream().anyMatch(winner -> {
                ResultType result = setResultMap.get(winner);
                return result == ResultType.WIN || result == ResultType.DRAW;
            });

            if (setContainsWinner) break; // This is the deciding set, stop trimming.
            else reverseSetsIterator.remove(); // This set is trailing the deciding set, so remove it.
        }
    }

    /**
     * Determines the number of sets that have been concluded in a match (doesn't include sets still in progress).
     *
     * @param match {@link X01Match} the match to calculate the sets played in.
     * @return int the number of sets that have been concluded in a match (doesn't include sets still in progress).
     */
    private int calcSetsPlayed(X01Match match) {
        if (match == null) return 0;

        // Count the number of concluded sets
        long completedSets = match.getSets().values().stream()
                .filter(set -> setProgressService.isSetConcluded(set, match.getPlayers()))
                .count();

        // Return the number of completed sets
        return (int) completedSets;
    }
}
