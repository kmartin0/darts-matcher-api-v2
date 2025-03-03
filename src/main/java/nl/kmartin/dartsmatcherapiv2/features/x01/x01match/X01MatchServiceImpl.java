package nl.kmartin.dartsmatcherapiv2.features.x01.x01match;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Statistics;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class X01MatchServiceImpl implements IX01MatchService {
    private final X01MatchRepository x01matchRepository;

    public X01MatchServiceImpl(final X01MatchRepository x01matchRepository) {
        this.x01matchRepository = x01matchRepository;
    }

    /**
     * Creates a new Match with default properties and saves it to the database.
     *
     * @param x01Match X01Match to be created
     * @return X01Match the saved match
     */
    @Override
    public X01Match createMatch(@Valid X01Match x01Match) {
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

        // Initialize the sets and score line timeline.
        x01Match.setSets(new ArrayList<>());
        x01Match.setScoreLineTimeline(new ArrayList<>());

        // Set the match type to X01 and the status to IN_PLAY.
        x01Match.setMatchType(MatchType.X01);
        x01Match.setMatchStatus(MatchStatus.IN_PLAY);

        // Set the start date of the match to the current time in UTC and make sure the end date is not set.
        x01Match.setStartDate(Instant.now());
        x01Match.setEndDate(null);
    }
}
