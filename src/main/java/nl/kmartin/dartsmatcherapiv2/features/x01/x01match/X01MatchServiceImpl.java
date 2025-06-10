package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchprogress.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.IX01MatchSetupService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class X01MatchServiceImpl implements IX01MatchService {
    private final IX01MatchRepository x01matchRepository;
    private final IX01MatchSetupService matchSetupService;
    private final IX01LegService legService;
    private final IX01SetService setService;
    private final IX01LegRoundService legRoundService;
    private final IX01StatisticsService statisticsService;
    private final IX01MatchProgressService matchProgressService;

    public X01MatchServiceImpl(IX01MatchRepository x01matchRepository, IX01MatchSetupService matchSetupService,
                               IX01SetService setService, IX01LegService legService, IX01LegRoundService legRoundService,
                               IX01StatisticsService statisticsService, IX01MatchProgressService matchProgressService) {
        this.x01matchRepository = x01matchRepository;
        this.matchSetupService = matchSetupService;
        this.setService = setService;
        this.legService = legService;
        this.legRoundService = legRoundService;
        this.statisticsService = statisticsService;
        this.matchProgressService = matchProgressService;
    }

    /**
     * Creates a new Match with default properties and saves it to the database.
     *
     * @param x01Match X01Match to be created
     * @return X01Match the saved match
     */
    @Override
    public X01Match createMatch(@NotNull @Valid X01Match x01Match) {
        // Initialize properties for the new match.
        matchSetupService.setupNewMatch(x01Match);

        // Save the match to the repository and return it.
        return saveMatch(x01Match);
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
    public X01Match addTurn(@NotNull @Valid X01Turn x01Turn) throws IOException {
        // Find the match
        X01Match x01Match = this.getMatch(x01Turn.getMatchId());

        // Get the current set/leg/round.
        Optional<X01Set> currentSet = matchProgressService.getCurrentSet(x01Match);
        Optional<X01Leg> currentLeg = matchProgressService.getCurrentLeg(x01Match, currentSet.orElse(null));
        Optional<X01LegRound> currentLegRound = matchProgressService.getCurrentLegRound(x01Match, currentLeg.orElse(null));

        // Add the turn to the current thrower of the current round.
        if (currentLeg.isPresent() && currentLegRound.isPresent()) {
            ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(currentLegRound.get(), currentLeg.get().getThrowsFirst(), x01Match.getPlayers());
            addTurnToRound(x01Match, currentLeg.get(), currentLegRound.get().getRound(), x01Turn, currentThrower);
        }

        // Save the updated match to the repository.
        return saveMatch(x01Match);
    }

    /**
     * Edits a score from a round for a player. After the score is edited will update the match state and save to
     * the repository
     *
     * @param x01EditTurn {@link X01EditTurn} the edited score of a player
     * @return {@link X01Match} The updated match
     * @throws IOException If there's an issue reading the checkouts file.
     */
    @Override
    public X01Match editTurn(@NotNull @Valid X01EditTurn x01EditTurn) throws IOException {
        // Find the match
        X01Match x01Match = this.getMatch(x01EditTurn.getMatchId());

        // Get the leg that contains the round.
        Optional<X01Leg> legOpt = setService.getSet(x01Match.getSets(), x01EditTurn.getSet(), true)
                .flatMap(set -> legService.getLeg(set.getLegs(), x01EditTurn.getLeg(), true));

        // Replace the current score with the updated turn
        if (legOpt.isPresent()) {
            addTurnToRound(x01Match, legOpt.get(), x01EditTurn.getRound(), x01EditTurn, x01EditTurn.getPlayerId());
        }

        // Save the updated match to the repository.
        return saveMatch(x01Match);
    }

    /**
     * Deletes the last turn (X01LegRoundScore) from a match.
     *
     * Finds the match by ID, removes the last submitted score , and then saves the updated match.
     *
     * @param x01DeleteLastTurn {@link X01DeleteLastTurn} Object containing the match ID for which the last turn should be deleted.
     * @return {@link X01Match} The updated match after deleting the last turn.
     */
    @Override
    public X01Match deleteLastTurn(@NotNull @Valid X01DeleteLastTurn x01DeleteLastTurn) {
        // Find the match
        X01Match x01Match = this.getMatch(x01DeleteLastTurn.getMatchId());

        // Delete the last round score
        setService.deleteLastScore(x01Match.getSets());

        // Save the updated match to the repository.
        return saveMatch(x01Match);
    }

    /**
     * Adds a turn to the specified leg round of a given match.
     * Updates and validates the current leg round with the score from the provided turn.
     *
     * @param x01Match    The match to which the turn is being added.
     * @param x01Leg      The leg of the match that the turn is part of.
     * @param roundNumber int the specific round of the leg in which the turn occurs.
     * @param x01Turn     The turn object containing details about the player's turn (score, darts used, doubles missed).
     * @param throwerId   The id of the player who has thrown this turn
     * @throws IOException If there's an issue reading the checkouts file.
     */
    private void addTurnToRound(X01Match x01Match, X01Leg x01Leg, int roundNumber, X01Turn x01Turn, ObjectId throwerId) throws IOException {
        if (x01Match == null || x01Leg == null || x01Turn == null) return;

        // Make the score object containing the turn details (score, darts used, doubles missed)
        X01LegRoundScore roundScore = new X01LegRoundScore(x01Turn.getDoublesMissed(), x01Turn.getDartsUsed(), x01Turn.getScore());

        // Validate and add the score to the round.
        legService.addScore(x01Match.getMatchSettings().getX01(), x01Leg, roundNumber, roundScore, x01Match.getPlayers(), throwerId);
    }

    /**
     * Updates the set results, leg results and match progress fields for a given match
     *
     * @param x01Match {@link X01Match} the match for which the state needs to be updated
     */
    private void updateMatchState(X01Match x01Match) {
        // Update match results
        matchProgressService.updateMatchResult(x01Match);

        // Update match statistics
        statisticsService.updatePlayerStatistics(x01Match.getSets(), x01Match.getPlayers());

        // Update Match Progress
        matchProgressService.updateMatchProgress(x01Match);
    }

    /**
     * Helper method which updates the match state before saving it to the repository
     *
     * @param x01Match {@link X01Match} the match to be saved
     * @return {@link X01Match} the saved match with updated match progress
     */
    private X01Match saveMatch(@Valid @NotNull X01Match x01Match) {
        // Sync match progress.
        updateMatchState(x01Match);

        // Save and return the saved match
        return x01matchRepository.save(x01Match);
    }
}