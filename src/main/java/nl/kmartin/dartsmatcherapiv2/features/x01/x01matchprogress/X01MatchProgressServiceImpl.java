package nl.kmartin.dartsmatcherapiv2.features.x01.x01matchprogress;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetService;
import nl.kmartin.dartsmatcherapiv2.utils.StandingsUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01MatchProgressServiceImpl implements IX01MatchProgressService {

    private final IX01SetService setService;
    private final IX01LegService legService;
    private final IX01LegRoundService legRoundService;

    public X01MatchProgressServiceImpl(IX01SetService setService, IX01LegService legService, IX01LegRoundService legRoundService) {
        this.setService = setService;
        this.legService = legService;
        this.legRoundService = legRoundService;
    }

    /**
     * First updates the set results. Then the match players results for a match.
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
        setService.removeSetsAfterWinner(x01Match.getSets(), matchWinners);

        // When there are match winners the match is concluded
        x01Match.setMatchStatus(matchWinners.isEmpty() ? MatchStatus.IN_PLAY : MatchStatus.CONCLUDED);
    }

    /**
     * Update the calculated fields inside the {@link X01MatchProgress} field of a match.
     *
     * @param x01Match {@link X01Match} the match to be updated
     */
    @Override
    public void updateMatchProgress(X01Match x01Match) {
        if (x01Match == null) return;

        // Get the current set/leg/round
        X01Set currentSet = getCurrentSetOrCreate(x01Match).orElse(null);
        X01Leg currentLeg = getCurrentLegOrCreate(x01Match, currentSet).orElse(null);
        X01LegRound currentLegRound = getCurrentLegRoundOrCreate(x01Match, currentLeg).orElse(null);

        // Get the current thrower for the current round
        ObjectId throwsFirstInCurrentLeg = currentLeg != null ? currentLeg.getThrowsFirst() : null;
        ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(currentLegRound, throwsFirstInCurrentLeg, x01Match.getPlayers());

        // Update the match progress with the new state of the match
        x01Match.setMatchProgress(new X01MatchProgress(
                currentSet != null ? currentSet.getSet() : null,
                currentLeg != null ? currentLeg.getLeg() : null,
                currentLegRound != null ? currentLegRound.getRound() : null,
                currentThrower
        ));
    }

    /**
     * Finds the current set in play of a match. If the set is not created yet. A new set will be made and added
     * to the match.
     *
     * @param x01Match {@link X01Match} the match for which the current set needs to be determined
     * @return {@link Optional<X01Set>} the current set in play.
     */
    @Override
    public Optional<X01Set> getCurrentSetOrCreate(X01Match x01Match) {
        if (x01Match == null) return Optional.empty();

        // First find the current set in the active list of sets.
        int bestOfSets = x01Match.getMatchSettings().getBestOf().getSets();
        Optional<X01Set> curSet = setService.getCurrentSet(x01Match.getSets());

        // If there is no current set and the match isn't concluded, create the next set.
        return curSet.isEmpty() && !isMatchConcluded(x01Match)
                ? setService.createNextSet(x01Match.getSets(), x01Match.getPlayers(), bestOfSets)
                : curSet;
    }

    /**
     * Finds the current leg in play inside a set. If the leg is not created yet. A new leg will be made and
     * added to the current set.
     *
     * @param x01Match   {@link X01Match}  the match for which the current leg needs to be determined
     * @param currentSet {@link X01Set} the current set in play.
     * @return {@link Optional<X01Leg>} the current leg in play.
     */
    @Override
    public Optional<X01Leg> getCurrentLegOrCreate(X01Match x01Match, X01Set currentSet) {
        if (x01Match == null || currentSet == null) return Optional.empty();

        // First find the current leg in the active list of legs from the current set.
        int bestOfLegs = x01Match.getMatchSettings().getBestOf().getLegs();
        Optional<X01Leg> curLeg = legService.getCurrentLeg(currentSet.getLegs());

        // If there is no current leg and the set isn't concluded, create the next leg.
        return curLeg.isEmpty() && !setService.isSetConcluded(currentSet, x01Match.getPlayers())
                ? legService.createNextLeg(currentSet.getLegs(), x01Match.getPlayers(), bestOfLegs, currentSet.getThrowsFirst())
                : curLeg;
    }

    /**
     * Finds the current leg round in play inside a leg. If the leg round is not created yet.
     * A new leg round will be made and added to the current leg.
     *
     * @param x01Match   {@link X01Match}  the match for which the current leg round needs to be determined
     * @param currentLeg {@link X01Leg} the current leg in play.
     * @return {@link Optional<X01LegRound>} the current leg round in play.
     */
    @Override
    public Optional<X01LegRound> getCurrentLegRoundOrCreate(X01Match x01Match, X01Leg currentLeg) {
        if (x01Match == null || currentLeg == null) return Optional.empty();

        // First find the current leg round in the active list of rounds from the current leg.
        Optional<X01LegRound> curLegRound = legRoundService.getCurrentLegRound(currentLeg.getRounds(), x01Match.getPlayers());

        // If there is no current leg round and the leg isn't concluded, create the next leg round.
        return curLegRound.isEmpty() && !legService.isLegConcluded(currentLeg)
                ? legRoundService.createNextLegRound(currentLeg.getRounds())
                : curLegRound;
    }

    /**
     * Determine the number of sets that are yet to be played in a match
     *
     * @param bestOfSets int the maximum number of sets going to be played
     * @param x01Match   {@link X01Match} the match for which the remaining sets need to be determined
     * @return int the maximum number of sets that can still be played
     */
    private int calcRemainingSets(int bestOfSets, X01Match x01Match) {
        // If the match or its sets are null, return the current best-of sets value.
        if (x01Match == null || x01Match.getSets() == null) return bestOfSets;

        // Count the number of concluded sets
        long completedSets = x01Match.getSets().stream()
                .filter(x01Set -> setService.isSetConcluded(x01Set, x01Match.getPlayers()))
                .count();

        // Determine the number of remaining sets and return them. Ensuring they are not negative.
        int remainingSets = bestOfSets - (int) completedSets;
        return Math.max(remainingSets, 0);
    }

    /**
     * Updates the result for each set from a match
     *
     * @param x01Match {@link X01Match} the match that contains the sets
     */
    private void updateSetResults(X01Match x01Match) {
        if (x01Match == null) return;

        // Delegate to set service to update each set result for the sets in this match
        setService.updateSetResults(
                x01Match.getSets(),
                x01Match.getPlayers(),
                x01Match.getMatchSettings().getBestOf().getLegs(),
                x01Match.getMatchSettings().getX01()
        );
    }

    /**
     * A list of object ids is created containing all players that have won the match. Multiple match winners
     * means a draw has occurred.
     *
     * @param x01Match {@link X01Match} The match for which the winners are being determined.
     * @return {@link List<ObjectId>} containing the IDs of players who won the match. multiple winners indicates a draw.
     */
    private List<ObjectId> getMatchWinners(X01Match x01Match) {
        if (x01Match == null) return new ArrayList<>();

        // Get the standings for the match.
        Map<ObjectId, Long> matchStandings = getMatchStandings(x01Match, x01Match.getPlayers());

        // Get the player(s) that have won the match
        int remainingSets = calcRemainingSets(x01Match.getMatchSettings().getBestOf().getSets(), x01Match);
        return StandingsUtils.determineWinners(matchStandings, remainingSets);
    }

    /**
     * Determines the number of sets each player has won for a given match
     *
     * @param match   {@link X01Match} the match for which standings need to be calculated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return Map<ObjectId, Long> containing the number of sets each player has won
     */
    private Map<ObjectId, Long> getMatchStandings(X01Match match, List<X01MatchPlayer> players) {
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
     * Determines if a match is concluded by checking if all players have a match result.
     *
     * @param x01Match {@link X01Match} the match
     * @return boolean if the match is concluded
     */
    private boolean isMatchConcluded(X01Match x01Match) {
        if (x01Match == null) return false;

        // When all players have a result the match is concluded
        return x01Match.getPlayers().stream().allMatch(player -> player.getResultType() != null);
    }
}