package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ProcessingLimitReachedException;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01dartbot.IX01DartBotService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api.IX01MatchRepository;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.event.X01MatchEvent;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.event.X01MatchEventType;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.IX01MatchSetupService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class X01MatchServiceImpl implements IX01MatchService {

    private final IX01MatchRepository matchRepository;
    private final IX01MatchSetupService matchSetupService;
    private final IX01MatchResultService matchResultService;
    private final IX01MatchProgressService matchProgressService;
    private final IX01StatisticsService statisticsService;
    private final IX01SetProgressService setProgressService;
    private final IX01LegService legService;
    private final IX01LegRoundService legRoundService;
    private final IX01DartBotService dartBotService;
    private final ApplicationEventPublisher eventPublisher;

    public X01MatchServiceImpl(IX01MatchRepository matchRepository, IX01MatchSetupService matchSetupService,
                               IX01MatchResultService matchResultService, IX01MatchProgressService matchProgressService,
                               IX01StatisticsService statisticsService, IX01SetProgressService setProgressService,
                               IX01LegService legService, IX01LegRoundService legRoundService, IX01DartBotService dartBotService, ApplicationEventPublisher eventPublisher) {
        this.matchRepository = matchRepository;
        this.matchSetupService = matchSetupService;
        this.matchResultService = matchResultService;
        this.matchProgressService = matchProgressService;
        this.statisticsService = statisticsService;
        this.setProgressService = setProgressService;
        this.legService = legService;
        this.legRoundService = legRoundService;
        this.dartBotService = dartBotService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new Match with default properties and saves it to the database.
     *
     * @param match X01Match to be created
     * @return X01Match the saved match
     */
    @Override
    public X01Match createMatch(@NotNull @Valid X01Match match) {
        // Initialize properties for the new match.
        matchSetupService.setupMatch(match);

        // Save the match to the repository and return it.
        return saveMatchAndProcessBotTurns(match, X01MatchEventType.PROCESS_MATCH);
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
        return matchRepository.findById(matchId).orElseThrow(() -> new ResourceNotFoundException(X01Match.class, matchId));
    }

    /**
     * Determines whether a match exists in the repository by using the id, throwing a
     * ResourceNotFoundException if not found.
     *
     * @param matchId ObjectId the id of the X01Match to be checked
     */
    @Override
    public void checkMatchExists(ObjectId matchId) {
        if (!matchRepository.existsById(matchId)) throw new ResourceNotFoundException(X01Match.class, matchId);
    }

    /**
     * Verifies and adds the current player's turn to the current round of the match.
     * After adding the turn, the match progress and state are recalculated.
     *
     * @param matchId {@link ObjectId} The ID of the match the turn will be added to.
     * @param turn    {@link X01Turn} The turn of a player
     * @return {@link X01Match} The updated match
     */
    @Override
    public X01Match addTurn(@NotNull ObjectId matchId, @NotNull @Valid X01Turn turn) {
        // Find the match
        X01Match match = this.getMatch(matchId);

        // Add the turn to the current player of the match
        addTurnToCurrentPlayer(match, turn);

        // Add the turn to the match and save the updated match to the repository.
        return saveMatchAndProcessBotTurns(match, X01MatchEventType.ADD_HUMAN_TURN);
    }

    /**
     * Edits a score from a round for a player. After the score is edited will update the match state and save to
     * the repository
     *
     * @param matchId  {@link ObjectId} The ID of the match the turn will be edited in.
     * @param editTurn {@link X01EditTurn} the edited score of a player
     * @return {@link X01Match} The updated match
     */
    @Override
    public X01Match editTurn(@NotNull ObjectId matchId, @NotNull @Valid X01EditTurn editTurn) {
        // Find the match
        X01Match match = this.getMatch(matchId);

        // Get the leg that contains the round.
        Optional<X01LegEntry> legOpt = matchProgressService.getSet(match, editTurn.getSet(), true)
                .flatMap(setEntry -> setProgressService.getLeg(setEntry.set(), editTurn.getLeg(), true));

        // Replace the current score with the updated turn
        legOpt.ifPresent(legEntry -> {
            int x01 = match.getMatchSettings().getX01();
            boolean trackDoubles = match.getMatchSettings().isTrackDoubles();
            List<X01MatchPlayer> players = match.getPlayers();

            legService.addScore(x01, legEntry.leg(), editTurn.getRound(), editTurn, players, editTurn.getPlayerId(), trackDoubles);
        });

        // Save the updated match to the repository.
        return saveMatchAndProcessBotTurns(match, X01MatchEventType.EDIT_TURN);
    }

    /**
     * Deletes the last turn (X01LegRoundScore) from a match.
     *
     * Finds the match by ID, removes the last submitted score , and then saves the updated match.
     *
     * @param matchId {@link ObjectId} The ID of the match the last turn will be deleted from.
     * @return {@link X01Match} The updated match after deleting the last turn.
     */
    @Override
    public X01Match deleteLastTurn(@NotNull ObjectId matchId) {
        // Find the match
        X01Match x01Match = this.getMatch(matchId);

        // Delete the last round score
        matchProgressService.removeLastScoreFromMatch(x01Match);

        // Save the updated match to the repository.
        return saveMatchAndProcessBotTurns(x01Match, X01MatchEventType.DELETE_LAST_TURN);
    }

    /**
     * Deletes the X01 match with the given ID from the repository.
     *
     * @param matchId the {@link ObjectId} of the match to be deleted
     */
    @Override
    public void deleteMatch(ObjectId matchId) {
        this.matchRepository.deleteById(matchId);
        this.eventPublisher.publishEvent(new X01MatchEvent.X01DeleteMatchEvent(matchId));
    }

    /**
     * Resets an X01 match to its initial state using the match setup service.
     *
     * @param matchId the {@link ObjectId} of the match to be reset
     * @return {@link X01Match} object of the reset match
     */
    @Override
    public X01Match resetMatch(ObjectId matchId) {
        // Find the match
        X01Match match = this.getMatch(matchId);

        // Reapply match setup to return to a clean starting state
        matchSetupService.setupMatch(match);

        // Save the reset match to the repository.
        return saveMatchAndProcessBotTurns(match, X01MatchEventType.RESET_MATCH);
    }

    /**
     * Verifies and adds the current player's turn to the current round of the match.
     *
     * @param match {@link X01Match} The match the turn will be added to.
     * @param turn  {@link X01Turn} The turn of a player
     */
    private void addTurnToCurrentPlayer(@NotNull X01Match match, @NotNull @Valid X01Turn turn) {
        // Get the current set
        X01SetEntry currentSetEntry = matchProgressService.getCurrentSetOrCreate(match)
                .orElseThrow(() -> new ResourceNotFoundException(X01Set.class, null));

        // Get the current leg
        X01LegEntry currentLegEntry = matchProgressService.getCurrentLegOrCreate(match, currentSetEntry.set())
                .orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, null));

        // Get the current leg round
        X01LegRoundEntry currentRoundEntry = matchProgressService.getCurrentLegRoundOrCreate(match, currentLegEntry.leg())
                .orElseThrow(() -> new ResourceNotFoundException(X01LegRound.class, null));

        // Add the turn to the current thrower of the current round.
        int x01 = match.getMatchSettings().getX01();
        boolean trackDoubles = match.getMatchSettings().isTrackDoubles();
        List<X01MatchPlayer> players = match.getPlayers();
        ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(currentRoundEntry.round(), currentLegEntry.leg().getThrowsFirst(), match.getPlayers());

        legService.addScore(x01, currentLegEntry.leg(), currentRoundEntry.roundNumber(), turn, players, currentThrower, trackDoubles);
    }

    /**
     * Saves the current match and processes Dart Bot turns until the current thrower is no longer a bot.
     *
     * @param match     {@link X01Match} the match to be saved and processed
     * @param eventType {@link X01MatchEventType} the type of the operation that triggered the save
     * @return {@link X01Match} The updated match after processing bot turns
     */
    private X01Match saveMatchAndProcessBotTurns(X01Match match, X01MatchEventType eventType) {
        // Save the match.
        match = saveMatch(match, eventType);

        // Create and add bot turns until the curren thrower is not a bot.
        match = processBotTurns(match);

        // Return the updated match.
        return match;
    }

    /**
     * processes Dart Bot turns until the current thrower is no longer a bot.
     * The match is saved after each turn to allow websocket subscribers to observe and react to
     * changes in the match state, such as new turns being added.
     *
     * To prevent infinite loops or excessive processing, a maximum limit on processed bot turns is enforced.
     * If the limit is exceeded, a {@link ProcessingLimitReachedException} is thrown.
     *
     * @param match {@link X01Match} the match to be saved and processed
     * @return {@link X01Match} The updated match after processing bot turns
     */
    private X01Match processBotTurns(X01Match match) {
        final int processingLimit = 500;
        int turnsProcessed = 0;

        // Create and add bot turns until the curren thrower is not a bot.
        while (isCurrentThrowerDartBot(match)) {
            // Generate the bot turn, add it to the current player and save the match.
            X01Turn botTurn = dartBotService.createDartBotTurn(match);
            addTurnToCurrentPlayer(match, botTurn);
            match = saveMatch(match, X01MatchEventType.ADD_BOT_TURN);

            // For safety throw a processing limit reached exception if the limit of iterations is reached to avoid
            // a potential infinite loop or excessive processing.
            turnsProcessed++;
            if (turnsProcessed >= processingLimit)
                throw new ProcessingLimitReachedException(X01Match.class, match.getId().toString());
        }

        return match;
    }

    /**
     * Updates and saves a match, then publishes an event to notify subscribers
     * about the updated match state.
     *
     * @param match     {@link X01Match} to update and save
     * @param eventType {@link X01MatchEventType} the type of the operation that triggered the save
     * @return {@link X01Match} the updated and saved match
     */
    private X01Match saveMatch(X01Match match, X01MatchEventType eventType) {
        // Update the match
        updateMatch(match);

        // Save the match
        match = matchRepository.save(match);

        // Publish the match
        X01MatchEvent event = switch (eventType) {
            case PROCESS_MATCH -> new X01MatchEvent.X01ProcessMatchEvent(match);
            case ADD_HUMAN_TURN -> new X01MatchEvent.X01AddHumanTurnEvent(match);
            case ADD_BOT_TURN -> new X01MatchEvent.X01AddBotTurnEvent(match);
            case EDIT_TURN -> new X01MatchEvent.X01EditTurnEvent(match);
            case DELETE_LAST_TURN -> new X01MatchEvent.X01DeleteLastTurnEvent(match);
            case RESET_MATCH -> new X01MatchEvent.X01ResetMatchEvent(match);
            default -> throw new IllegalArgumentException("Invalid event type for this operation: " + eventType);
        };
        eventPublisher.publishEvent(event);

        return match;
    }

    /**
     * Updates calculated fields and cleans up a match. Includes updating match/set/leg results, player statistics,
     * and match progress.
     *
     * @param match the {@link X01Match} to update
     */
    private void updateMatch(X01Match match) {
        // Update match results
        matchResultService.updateMatchResult(match);

        // Update match statistics
        statisticsService.updatePlayerStatistics(match);

        // Update Match Progress
        matchProgressService.updateMatchProgress(match);
    }

    /**
     * Checks whether the current thrower in the match is a Dart Bot.
     *
     * @param match the {@link X01Match} to check
     * @return true if the current thrower is a Dart Bot; false otherwise
     */
    private boolean isCurrentThrowerDartBot(X01Match match) {
        if (match == null || match.getMatchProgress().getCurrentThrower() == null) return false;

        return getPlayerById(match, match.getMatchProgress().getCurrentThrower())
                .map(player -> player.getPlayerType().equals(PlayerType.DART_BOT))
                .orElse(false);
    }

    /**
     * Retrieves a player from the match by their player ID.
     *
     * @param match    {@link X01Match} containing players
     * @param playerId {@link ObjectId} id of the player to retrieve
     * @return {@link Optional<X01MatchPlayer>} containing the matching player if found; otherwise empty
     */
    private Optional<X01MatchPlayer> getPlayerById(X01Match match, ObjectId playerId) {
        if (playerId == null) return Optional.empty();

        return match.getPlayers().stream()
                .filter(matchPlayer -> matchPlayer.getPlayerId().equals(playerId))
                .findFirst();
    }
}
