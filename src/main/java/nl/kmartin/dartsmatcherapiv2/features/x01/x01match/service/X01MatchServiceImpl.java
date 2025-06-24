package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api.IX01MatchRepository;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup.IX01MatchSetupService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01set.IX01SetProgressService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics.IX01StatisticsService;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class X01MatchServiceImpl implements IX01MatchService {

    private final IX01MatchRepository matchRepository;
    private final IX01MatchSetupService matchSetupService;
    private final IX01MatchResultService matchResultService;
    private final IX01MatchProgressService matchProgressService;
    private final IX01StatisticsService statisticsService;
    private final IX01SetProgressService setProgressService;
    private final IX01LegService legService;
    private final IX01LegRoundService legRoundService;

    public X01MatchServiceImpl(IX01MatchRepository matchRepository, IX01MatchSetupService matchSetupService,
                               IX01MatchResultService matchResultService, IX01MatchProgressService matchProgressService,
                               IX01StatisticsService statisticsService, IX01SetProgressService setProgressService,
                               IX01LegService legService, IX01LegRoundService legRoundService) {
        this.matchRepository = matchRepository;
        this.matchSetupService = matchSetupService;
        this.matchResultService = matchResultService;
        this.matchProgressService = matchProgressService;
        this.statisticsService = statisticsService;
        this.setProgressService = setProgressService;
        this.legService = legService;
        this.legRoundService = legRoundService;
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
        return saveMatch(match);
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

        // Get the current set/leg/round.
        Optional<X01Set> currentSet = matchProgressService.getCurrentSetOrCreate(match);
        Optional<X01Leg> currentLeg = matchProgressService.getCurrentLegOrCreate(match, currentSet.orElse(null));
        Optional<X01LegRound> currentLegRound = matchProgressService.getCurrentLegRoundOrCreate(match, currentLeg.orElse(null));

        // Add the turn to the current thrower of the current round.
        if (currentLeg.isPresent() && currentLegRound.isPresent()) {
            int x01 = match.getMatchSettings().getX01();
            boolean trackDoubles = match.getMatchSettings().isTrackDoubles();
            List<X01MatchPlayer> players = match.getPlayers();
            ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(currentLegRound.get(), currentLeg.get().getThrowsFirst(), match.getPlayers());

            legService.addScore(x01, currentLeg.get(), currentLegRound.get().getRound(), turn, players, currentThrower, trackDoubles);
        }

        // Save the updated match to the repository.
        return saveMatch(match);
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
        Optional<X01Leg> legOpt = matchProgressService.getSet(match, editTurn.getSet(), true)
                .flatMap(set -> setProgressService.getLeg(set, editTurn.getLeg(), true));

        // Replace the current score with the updated turn
        legOpt.ifPresent(x01Leg -> {
            int x01 = match.getMatchSettings().getX01();
            boolean trackDoubles = match.getMatchSettings().isTrackDoubles();
            List<X01MatchPlayer> players = match.getPlayers();

            legService.addScore(x01, legOpt.get(), editTurn.getRound(), editTurn, players, editTurn.getPlayerId(), trackDoubles);
        });


        // Save the updated match to the repository.
        return saveMatch(match);
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
        matchProgressService.deleteLastScore(x01Match);

        // Save the updated match to the repository.
        return saveMatch(x01Match);
    }

    /**
     * Deletes the X01 match with the given ID from the repository.
     *
     * @param matchId the {@link ObjectId} of the match to be deleted
     */
    @Override
    public void deleteMatch(ObjectId matchId) {
        this.matchRepository.deleteById(matchId);
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
        X01Match x01Match = this.getMatch(matchId);

        // Reapply match setup to return to a clean starting state
        matchSetupService.setupMatch(x01Match);

        // Save the reset match to the repository.
        return saveMatch(x01Match);
    }

    /**
     * Helper method which updates the match state before saving it to the repository
     *
     * @param match {@link X01Match} the match to be saved
     * @return {@link X01Match} the saved match with updated match progress
     */
    private X01Match saveMatch(@Valid @NotNull X01Match match) {
        // Update match results
        matchResultService.updateMatchResult(match);

        // Update match statistics
        statisticsService.updatePlayerStatistics(match);

        // Update Match Progress
        matchProgressService.updateMatchProgress(match);

        // Save and return the saved match
        return matchRepository.save(match);
    }

}
