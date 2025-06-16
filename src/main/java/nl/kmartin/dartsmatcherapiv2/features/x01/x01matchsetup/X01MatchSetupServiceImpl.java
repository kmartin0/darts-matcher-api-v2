package nl.kmartin.dartsmatcherapiv2.features.x01.x01matchsetup;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchProgress;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Statistics;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class X01MatchSetupServiceImpl implements IX01MatchSetupService {

    /**
     * Sets up an X01 match by setting up players, match type and status,
     * start and end dates, and initial match progress.
     *
     * @param match the {@link X01Match} instance to set up
     */
    @Override
    public void setupNewMatch(@Valid X01Match match) {
        if (match == null) return;

        setupMatchPlayers(match);
        setMatchTypeAndStatus(match);
        setMatchDates(match);
        setupMatchProgress(match);
    }

    /**
     * Generate a unique id for all match players and initialize their statistics.
     *
     * @param match {@link X01Match} the match whose players need to be set up
     */
    private void setupMatchPlayers(X01Match match) {
        // Generate a unique id for each player and set their statistics.
        match.getPlayers().forEach(player -> {
            player.setPlayerId(new ObjectId());
            player.setStatistics(new X01Statistics());
        });
    }

    /**
     * Sets the match type to X01 and the initial match status to IN_PLAY
     *
     * @param match {@link X01Match} the match whose type and status need to be set up
     */
    private void setMatchTypeAndStatus(X01Match match) {
        // Set the match type to X01 and the status to IN_PLAY
        match.setMatchType(MatchType.X01);
        match.setMatchStatus(MatchStatus.IN_PLAY);
    }

    /**
     * Initializes the start date to the current time in UTC and clears the end date.
     *
     * @param match {@link X01Match} the match whose start and end date need to be initialized
     */
    private void setMatchDates(X01Match match) {
        // Set the start date of the match to the current time in UTC and make sure the end date is not set.
        match.setStartDate(Instant.now());
        match.setEndDate(null);
    }

    /**
     * Initializes the match progress by setting up an empty list of sets and
     * creating an initial match progress with the first player in the list of players starting the match.
     *
     * @param match {@link X01Match} the match whose progress needs to be set up
     */
    private void setupMatchProgress(X01Match match) {
        // Initialize the sets list.
        match.setSets(new ArrayList<>());

        // Initialize the match progress indicating the starting round and starting player.
        ObjectId startsMatch = match.getPlayers().get(0).getPlayerId();
        X01MatchProgress initialMatchProgress = new X01MatchProgress(1, 1, 1, startsMatch);
        match.setMatchProgress(initialMatchProgress);
    }
}
