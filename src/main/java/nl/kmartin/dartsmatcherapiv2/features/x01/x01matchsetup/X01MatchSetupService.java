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
public class X01MatchSetupService implements IX01MatchSetupService {
    public void setupNewMatch(@Valid X01Match x01Match) {
        if (x01Match == null) return;

        setupMatchPlayers(x01Match);
        setMatchTypeAndStatus(x01Match);
        setMatchDates(x01Match);
        setupMatchProgress(x01Match);
    }

    /**
     * Generate a unique id for all match players and initialize their statistics.
     *
     * @param x01Match {@link X01Match} the match whose players need to be set up
     */
    private void setupMatchPlayers(X01Match x01Match) {
        if (x01Match == null) return;

        // Generate a unique id for each player and set their statistics.
        x01Match.getPlayers().forEach(player -> {
            player.setPlayerId(new ObjectId());
            player.setStatistics(new X01Statistics());
        });
    }

    /**
     * Sets the match type to X01 and the initial match status to IN_PLAY
     *
     * @param x01Match {@link X01Match} the match whose type and status need to be set up
     */
    private void setMatchTypeAndStatus(X01Match x01Match) {
        if (x01Match == null) return;

        // Set the match type to X01 and the status to IN_PLAY
        x01Match.setMatchType(MatchType.X01);
        x01Match.setMatchStatus(MatchStatus.IN_PLAY);
    }

    /**
     * Initializes the start date to the current time in UTC and clears the end date.
     *
     * @param x01Match {@link X01Match} the match whose start and end date need to be initialized
     */
    private void setMatchDates(X01Match x01Match) {
        if (x01Match == null) return;

        // Set the start date of the match to the current time in UTC and make sure the end date is not set.
        x01Match.setStartDate(Instant.now());
        x01Match.setEndDate(null);
    }

    /**
     * Initializes the match progress by setting up an empty list of sets and
     * creating an initial match progress with the first player in the list of players starting the match.
     *
     * @param x01Match {@link X01Match} the match whose progress needs to be set up
     */
    private void setupMatchProgress(X01Match x01Match) {
        if (x01Match == null) return;

        // Initialize the sets list.
        x01Match.setSets(new ArrayList<>());

        // Initialize the match progress indicating the starting round and starting player.
        ObjectId startsMatch = x01Match.getPlayers().get(0).getPlayerId();
        X01MatchProgress initialMatchProgress = new X01MatchProgress(1, 1, 1, startsMatch);
        x01Match.setMatchProgress(initialMatchProgress);
    }
}
