package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchType;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import nl.kmartin.dartsmatcherapiv2.utils.StandingsUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01MatchServiceImpl implements IX01MatchService {
    private final IX01MatchRepository x01matchRepository;
    private final IX01SetService setService;
    private final IX01LegService legService;
    private final IX01LegRoundService legRoundService;
    private final IX01StatisticsService statisticsService;

    public X01MatchServiceImpl(IX01MatchRepository x01matchRepository, IX01SetService setService,
                               IX01LegService legService, IX01LegRoundService legRoundService, IX01StatisticsService statisticsService) {
        this.x01matchRepository = x01matchRepository;
        this.setService = setService;
        this.legService = legService;
        this.legRoundService = legRoundService;
        this.statisticsService = statisticsService;
    }

    /**
     * Creates a new Match with default properties and saves it to the database.
     *
     * @param x01Match X01Match to be created
     * @return X01Match the saved match
     */
    @Override
    public X01Match createMatch(@Valid @NotNull X01Match x01Match) {
        // Initialize properties for the new match.
        initNewMatchProperties(x01Match);

        // Save the match to the repository and return it.
        return this.x01matchRepository.save(x01Match);
    }

    /**
     * Helper method to initialize the default match properties of a new X01Match.
     *
     * @param x01Match X01Match to be initialized
     */
    private void initNewMatchProperties(X01Match x01Match) {
        // Generate a unique id for each player and set their statistics.
        x01Match.getPlayers().forEach(player -> {
            player.setPlayerId(new ObjectId());
            player.setStatistics(new X01Statistics());
        });

        // Set the match type to X01 and the status to IN_PLAY.
        x01Match.setMatchType(MatchType.X01);
        x01Match.setMatchStatus(MatchStatus.IN_PLAY);

        // Set the start date of the match to the current time in UTC and make sure the end date is not set.
        x01Match.setStartDate(Instant.now());
        x01Match.setEndDate(null);

        // Initialize the match progress.
        x01Match.setSets(new ArrayList<>());
        ObjectId startsMatch = x01Match.getPlayers().get(0).getPlayerId();
        X01MatchProgress initialMatchProgress = new X01MatchProgress(1, 1, 1, startsMatch);
        x01Match.setMatchProgress(initialMatchProgress);
    }

    /**
     * Get an X01Match from the repository using the id.
     *
     * @param matchId ObjectId the id of the X01Match to be retrieved
     * @return X01Match corresponding to the matchId
     * @throws ResourceNotFoundException when there is no match that has the matchId
     */
    @Override
    public X01Match getMatch(@NotNull ObjectId matchId) throws ResourceNotFoundException {
        return x01matchRepository.findById(matchId).orElseThrow(() -> new ResourceNotFoundException(X01Match.class, matchId));
    }

    /**
     * Verifies and adds the current player's turn to the current round of the match.
     * After adding the turn, the match progress and state are recalculated.
     *
     * @param x01Turn {@link X01Turn} The turn of a player
     * @return {@link X01Match} The updated match
     * @throws IOException If there's an issue reading the checkouts file.
     */
    @Override
    public X01Match addTurn(X01Turn x01Turn) throws IOException {
        X01Match x01Match = this.getMatch(x01Turn.getMatchId());

        // Get the current set/leg/round.
        Optional<X01Set> currentSet = getCurrentSet(x01Match);
        Optional<X01Leg> currentLeg = getCurrentLeg(x01Match, currentSet.orElse(null));
        Optional<X01LegRound> currentLegRound = getCurrentLegRound(x01Match, currentLeg.orElse(null));

        // Adds the score to the leg round in play for player whose turn it is. If the player checks out also sets the leg winner.
        if (currentLeg.isPresent() && currentLegRound.isPresent()) {
            ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(currentLegRound.get(), currentLeg.get().getThrowsFirst(), x01Match.getPlayers());

            X01LegRoundScore roundScore = new X01LegRoundScore(x01Turn.getDoublesMissed(), x01Turn.getDartsUsed(), x01Turn.getScore());

            legService.addScore(
                    x01Match.getMatchSettings().getX01(),
                    currentLeg.get(),
                    currentLegRound.get(),
                    roundScore,
                    x01Match.getPlayers(),
                    currentThrower
            );
        }

        // Sync match progress.
        updateMatchState(x01Match);

        // Save and return the saved match
        return x01matchRepository.save(x01Match);
    }

    /**
     * Updates the set results, leg results and match progress fields for a given match
     *
     * @param x01Match {@link X01Match} the match for which the state needs to be updated
     */
    private void updateMatchState(X01Match x01Match) {
        // Update match results
        updateMatchResult(x01Match);

        // Update match statistics
        updateMatchStatistics(x01Match);

        // Update Match Progress
        updateMatchProgress(x01Match);
    }

    /**
     * First updates the set results. Then the match players results for a match.
     *
     * @param x01Match {@link X01Match} the match to be updated
     */
    private void updateMatchResult(X01Match x01Match) {
        if (x01Match == null) return;

        // First update all set results.
        int bestOfLegs = x01Match.getMatchSettings().getBestOf().getLegs();
        int x01 = x01Match.getMatchSettings().getX01();
        setService.updateSetResults(x01Match.getSets(), x01Match.getPlayers(), bestOfLegs, x01);

        // Get the standings for the match.
        Map<ObjectId, Long> matchStandings = getMatchStandings(x01Match, x01Match.getPlayers());

        // Get the player(s) that have won the match
        int remainingSets = calcRemainingSets(x01Match.getMatchSettings().getBestOf().getSets(), x01Match);
        List<ObjectId> matchWinners = StandingsUtils.determineWinners(matchStandings, remainingSets);

        // If multiple players have won the set, that means they have drawn.
        ResultType winOrDrawType = matchWinners.size() > 1 ? ResultType.DRAW : ResultType.WIN;

        // Set the individual results for each player
        x01Match.getPlayers().forEach(player -> player.setResultType(
                matchWinners.isEmpty() ? null : (matchWinners.contains(player.getPlayerId()) ? winOrDrawType : ResultType.LOSS)
        ));

        // When there are match winners the match is concluded
        x01Match.setMatchStatus(matchWinners.isEmpty() ? MatchStatus.IN_PLAY : MatchStatus.CONCLUDED);
    }

    private void updateMatchStatistics(X01Match x01Match) {
        statisticsService.updatePlayerStatistics(x01Match.getSets(), x01Match.getPlayers());
    }

    /**
     * Update the calculated fields inside the {@link X01MatchProgress} field of a match.
     *
     * @param x01Match {@link X01Match} the match to be updated
     */
    private void updateMatchProgress(X01Match x01Match) {
        if (x01Match == null) return;

        // Get the match progress object from the match
        X01MatchProgress matchProgress = x01Match.getMatchProgress();

        // Get and set the current set.
        Optional<X01Set> currentSet = getCurrentSet(x01Match);
        matchProgress.setCurrentSet(currentSet.isPresent() ? currentSet.get().getSet() : null);

        // Get and set the current leg.
        Optional<X01Leg> currentLeg = getCurrentLeg(x01Match, currentSet.orElse(null));
        matchProgress.setCurrentLeg(currentLeg.isPresent() ? currentLeg.get().getLeg() : null);

        // Get and set the current leg round.
        Optional<X01LegRound> currentLegRound = getCurrentLegRound(x01Match, currentLeg.orElse(null));
        matchProgress.setCurrentRound(currentLegRound.isPresent() ? currentLegRound.get().getRound() : null);

        // Determine and update the current thrower for the match
        if (currentLeg.isPresent() && currentLegRound.isPresent()) {
            ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(currentLegRound.get(), currentLeg.get().getThrowsFirst(), x01Match.getPlayers());
            matchProgress.setCurrentThrower(currentThrower);
        } else {
            matchProgress.setCurrentThrower(null);
        }
    }

    /**
     * Finds the current set in play of a match. If the set is not created yet. A new set will be made and added
     * to the match.
     *
     * @param x01Match {@link X01Match} the match for which the current set needs to be determined
     * @return {@link Optional<X01Set>} the current set in play.
     */
    private Optional<X01Set> getCurrentSet(X01Match x01Match) {
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
    private Optional<X01Leg> getCurrentLeg(X01Match x01Match, X01Set currentSet) {
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
    private Optional<X01LegRound> getCurrentLegRound(X01Match x01Match, X01Leg currentLeg) {
        if (x01Match == null || currentLeg == null) return Optional.empty();

        // First find the current leg round in the active list of rounds from the current leg.
        Optional<X01LegRound> curLegRound = legRoundService.getCurrentLegRound(currentLeg.getRounds(), x01Match.getPlayers());

        // If there is no current leg round and the leg isn't concluded, create the next leg round.
        return curLegRound.isEmpty() && !legService.isLegConcluded(currentLeg)
                ? legRoundService.createNextLegRound(currentLeg.getRounds())
                : curLegRound;
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

    /**
     * Determine the number of sets that are yet to be played in a match
     *
     * @param bestOfSets int the maximum number of sets going to be played
     * @param x01Match   {@link X01Match} the match for which the remaining sets need to be determined
     * @return
     */
    private int calcRemainingSets(int bestOfSets, X01Match x01Match) {
        if (x01Match == null || x01Match.getSets() == null) return -1; // TODO: Error handling here

        // Count the number of concluded sets
        long completedSets = x01Match.getSets().stream()
                .filter(x01Set -> setService.isSetConcluded(x01Set, x01Match.getPlayers()))
                .count();

        // Determine the number of remaining sets and return them. Ensuring they are not negative.
        int remainingSets = bestOfSets - (int) completedSets;
        return Math.max(remainingSets, 0);
    }

    /**
     * Determines the number of sets each player has won for a given match
     *
     * @param match   {@link X01Match} the match for which standings need to be calculated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @return Map<ObjectId, Long> containing the number of sets each player has won
     */
    private Map<ObjectId, Long> getMatchStandings(X01Match match, List<X01MatchPlayer> players) {
        if (match == null || match.getSets() == null || players == null) return null;

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
}