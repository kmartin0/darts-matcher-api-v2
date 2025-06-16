package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetResultService;
import nl.kmartin.dartsmatcherapiv2.utils.StandingsUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01MatchResultServiceImpl implements IX01MatchResultService {

    private final IX01SetResultService setResultService;
    private final IX01SetProgressService setProgressService;

    public X01MatchResultServiceImpl(IX01SetResultService setResultService, IX01SetProgressService setProgressService) {
        this.setResultService = setResultService;
        this.setProgressService = setProgressService;
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
        if (match == null || match.getSets() == null || match.getPlayers() == null) return;

        List<X01MatchPlayer> players = match.getPlayers();
        int x01 = match.getMatchSettings().getX01();
        int bestOfLegs = match.getMatchSettings().getBestOf().getLegs();

        // For each set update the leg results and after update the set result
        match.getSets().forEach(x01Set -> setResultService.updateSetResult(x01Set, bestOfLegs, players, x01));
    }

    /**
     * A list of object ids is created containing all players that have won the match. Multiple match winners
     * means a draw has occurred.
     *
     * @param x01Match {@link X01Match} The match for which the winners are being determined.
     * @return {@link List<ObjectId>} containing the IDs of players who won the match. multiple winners indicates a draw.
     */
    @Override
    public List<ObjectId> getMatchWinners(X01Match x01Match) {
        if (x01Match == null) return new ArrayList<>();

        // Get the standings for the match.
        Map<ObjectId, Long> matchStandings = getMatchStandings(x01Match, x01Match.getPlayers());

        // Get the player(s) that have won the match
        int remainingSets = calcRemainingSets(x01Match);
        return StandingsUtils.determineWinners(matchStandings, remainingSets);
    }

    /**
     * Determines the number of sets each player has won for a given match
     *
     * @param match   {@link X01Match} the match for which standings need to be calculated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return Map<ObjectId, Long> containing the number of sets each player has won
     */
    @Override
    public Map<ObjectId, Long> getMatchStandings(X01Match match, List<X01MatchPlayer> players) {
        if (match == null || match.getSets() == null || players == null) return new HashMap<>();

        // Initialize standings map with all players and 0 wins
        Map<ObjectId, Long> standings = players.stream()
                .collect(Collectors.toMap(MatchPlayer::getPlayerId, player -> 0L));

        // Update the map with the number of wins from the sets for each player
        match.getSets().stream()
                .filter(x01Set -> x01Set.getResult() != null && !x01Set.getResult().isEmpty())  // Filter out sets with no result
                .flatMap(x01Set -> x01Set.getResult().entrySet().stream())  // Continue with the results map
                .filter(entry -> entry.getValue() == ResultType.WIN || entry.getValue() == ResultType.DRAW)  // Filter for WIN or DRAW
                .forEach(entry -> standings.merge(entry.getKey(), 1L, Long::sum));  // Increment the score for the player

        // Return the standings map
        return standings;
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
        if (match == null || match.getSets().isEmpty() || matchWinners == null || matchWinners.isEmpty()) return;

        List<X01Set> setsReverse = new ArrayList<>(match.getSets());
        Collections.reverse(setsReverse);

        for (X01Set set : setsReverse) {
            Map<ObjectId, ResultType> setResultMap = set.getResult();
            boolean setContainsWinner = setResultMap != null && matchWinners.stream().anyMatch(winner -> {
                ResultType result = setResultMap.get(winner);
                return result == ResultType.WIN || result == ResultType.DRAW;
            });
            if (setContainsWinner) break;
            match.getSets().remove(set);
        }
    }

    /**
     * Determine the number of sets that are yet to be played in a match
     *
     * @param match {@link X01Match} the match for which the remaining sets need to be determined
     * @return int the maximum number of sets that can still be played
     */
    private int calcRemainingSets(X01Match match) {
        // If the match or its sets are null, return the current best-of sets value.
        int bestOfSets = match.getMatchSettings().getBestOf().getSets();
        if (match == null || match.getSets() == null) return bestOfSets;

        // Count the number of concluded sets
        long completedSets = match.getSets().stream()
                .filter(x01Set -> setProgressService.isSetConcluded(x01Set, match.getPlayers()))
                .count();

        // Determine the number of remaining sets and return them. Ensuring they are not negative.
        int remainingSets = bestOfSets - (int) completedSets;
        return Math.max(remainingSets, 0);
    }
}
