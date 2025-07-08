package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final IX01MatchPublishService matchPublishService;

    public X01MatchServiceImpl(IX01MatchRepository matchRepository, IX01MatchSetupService matchSetupService,
                               IX01MatchResultService matchResultService, IX01MatchProgressService matchProgressService,
                               IX01StatisticsService statisticsService, IX01SetProgressService setProgressService,
                               IX01LegService legService, IX01LegRoundService legRoundService, IX01DartBotService dartBotService,
                               IX01MatchPublishService matchPublishService) {
        this.matchRepository = matchRepository;
        this.matchSetupService = matchSetupService;
        this.matchResultService = matchResultService;
        this.matchProgressService = matchProgressService;
        this.statisticsService = statisticsService;
        this.setProgressService = setProgressService;
        this.legService = legService;
        this.legRoundService = legRoundService;
        this.dartBotService = dartBotService;
        this.matchPublishService = matchPublishService;
    }

    /**
     * Creates a new Match with default properties and saves it to the database.
     *
     * @param match X01Match to be created
     * @return X01Match the saved match
     */
    @Override
    @Transactional
    public X01Match createMatch(@NotNull @Valid X01Match match) {
        // Initialize properties for the new match.
        matchSetupService.setupMatch(match);

        // Save the match to the repository and return it.
        saveMatchAndProcessBotTurns(match, X01MatchEventType.PROCESS_MATCH);
        return match;
    }

    /**
     * Get an X01Match from the repository using the id.
     *
     * @param matchId ObjectId the id of the X01Match to be retrieved
     * @return X01Match corresponding to the matchId
     * @throws ResourceNotFoundException when there is no match that has the matchId
     */
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional
    public X01Match addTurn(@NotNull ObjectId matchId, @NotNull @Valid X01Turn turn) {
        // Find the match
        X01Match match = this.getMatch(matchId);

        // Add the turn to the current player of the match
        addTurnToCurrentPlayer(match, turn);

        // Add the turn to the match and save the updated match to the repository.
        saveMatchAndProcessBotTurns(match, X01MatchEventType.ADD_HUMAN_TURN);
        return match;
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
    @Transactional
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
        saveMatchAndProcessBotTurns(match, X01MatchEventType.EDIT_TURN);
        return match;
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
    @Transactional
    public X01Match deleteLastTurn(@NotNull ObjectId matchId) {
        // Find the match
        X01Match match = this.getMatch(matchId);

        // Delete the last round score
        matchProgressService.removeLastScoreFromMatch(match);

        // Save the updated match to the repository.
        saveMatchAndProcessBotTurns(match, X01MatchEventType.DELETE_LAST_TURN);
        return match;
    }

    /**
     * Deletes the X01 match with the given ID from the repository.
     *
     * @param matchId the {@link ObjectId} of the match to be deleted
     */
    @Override
    @Transactional
    public void deleteMatch(ObjectId matchId) {
        this.matchRepository.deleteById(matchId);
        this.matchPublishService.publish(new X01MatchEvent.X01DeleteMatchEvent(matchId));
    }

    /**
     * Resets an X01 match to its initial state using the match setup service.
     *
     * @param matchId the {@link ObjectId} of the match to be reset
     * @return {@link X01Match} object of the reset match
     */
    @Override
    @Transactional
    public X01Match resetMatch(ObjectId matchId) {
        // Find the match
        X01Match match = this.getMatch(matchId);

        // Reapply match setup to return to a clean starting state
        matchSetupService.setupMatch(match);

        // Save the reset match to the repository.
        saveMatchAndProcessBotTurns(match, X01MatchEventType.RESET_MATCH);
        return match;
    }

    @Override
    @Transactional
    public X01Match reprocessMatch(ObjectId matchId) {
        // Find the match
        X01Match match = this.getMatch(matchId);

        // Update calculated match fields (winner, statistics etc.), process bot turns and save it to the repository.
        this.saveMatchAndProcessBotTurns(match, X01MatchEventType.PROCESS_MATCH);

        return match;
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
     */
    private void saveMatchAndProcessBotTurns(X01Match match, X01MatchEventType eventType) {
        // Update, Save and Broadcast the match.
        saveMatch(match, eventType);

        // If it's a dart bots' turn. Create and Add the bot turn and then Update, Save and Broadcast the match.
        if (isCurrentThrowerDartBot(match)) {
            X01Turn dartBotTurn = dartBotService.createDartBotTurn(match);
            addTurnToCurrentPlayer(match, dartBotTurn);
            saveMatch(match, X01MatchEventType.ADD_BOT_TURN);
            if (isCurrentThrowerDartBot(match))
                throw new IllegalStateException("Invalid match state: two bot turns in a row are not allowed (matchId=" + match.getId() + ")");
        }
    }

    /**
     * Saves the given X01Match object by updating it, persisting it,
     * and publishing the corresponding match event based on the event type.
     *
     * @param match     the X01Match object to be saved and published
     * @param eventType the type of event indicating the nature of the save operation
     */
    private void saveMatch(X01Match match, X01MatchEventType eventType) {
        // Update the match
        updateMatch(match);

        // Save the Match
        matchRepository.save(match);

        // Publish the match event.
        X01MatchEvent publishEvent = createSaveEventFromType(match, eventType);
        this.matchPublishService.publish(publishEvent);
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

        // Update the publishing version
        match.setBroadcastVersion(match.getBroadcastVersion() + 1);
    }

    /**
     * Creates an X01MatchEvent object based on the provided 'save' event type.
     *
     * @param match     the X01Match associated with the event
     * @param eventType the type of event to create
     * @return an instance of X01MatchEvent corresponding to the eventType
     * @throws IllegalArgumentException if the eventType is not valid for this operation
     */
    private X01MatchEvent createSaveEventFromType(X01Match match, X01MatchEventType eventType) {
        return switch (eventType) {
            case PROCESS_MATCH -> new X01MatchEvent.X01ProcessMatchEvent(match);
            case ADD_HUMAN_TURN -> new X01MatchEvent.X01AddHumanTurnEvent(match);
            case ADD_BOT_TURN -> new X01MatchEvent.X01AddBotTurnEvent(match);
            case EDIT_TURN -> new X01MatchEvent.X01EditTurnEvent(match);
            case DELETE_LAST_TURN -> new X01MatchEvent.X01DeleteLastTurnEvent(match);
            case RESET_MATCH -> new X01MatchEvent.X01ResetMatchEvent(match);
            default -> throw new IllegalArgumentException("Invalid event type for this operation: " + eventType);
        };
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
