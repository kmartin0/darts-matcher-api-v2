package nl.kmartin.dartsmatcherapiv2.features.x01.x01leground;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IX01LegRoundService {
    Optional<X01LegRound> getCurrentLegRound(List<X01LegRound> rounds, List<X01MatchPlayer> matchPlayers);

    Optional<X01LegRound> createNextLegRound(List<X01LegRound> rounds);

    Set<Integer> getLegRoundNumbers(List<X01LegRound> rounds);

    ObjectId getCurrentThrowerInRound(X01LegRound x01LegRound, ObjectId throwsFirstInLeg, List<X01MatchPlayer> players);

    Optional<X01LegRound> getLegRound(List<X01LegRound> rounds, int roundNumber, boolean throwIfNotFound);

    Optional<X01LegRound> getLastRound(List<X01LegRound> rounds);

    int calculateRemainingScore(int x01, List<X01LegRound> rounds, ObjectId playerId);

    int calculateDartsUsed(List<X01LegRound> rounds, ObjectId playerId);

    boolean validateRoundsForPlayer(int x01, List<X01LegRound> rounds, ObjectId throwerId) throws IOException;

    Optional<X01LegRoundScore> getLastScoreForPlayer(List<X01LegRound> rounds, ObjectId throwerId);

    boolean removeLastScoreFromRound(X01LegRound legRound);
}
