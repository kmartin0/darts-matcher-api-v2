package nl.kmartin.dartsmatcherapiv2.features.x01.x01leground;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class X01LegRoundServiceImpl implements IX01LegRoundService {

    /**
     * Find the player whose turn it is to throw in a round.
     *
     * @param legRound      {@link X01LegRound} the round to check.
     * @param throwsFirstInLeg {@link ObjectId} the id of the player that started the leg.
     * @param players          {@link List<X01MatchPlayer>} list of all match players.
     * @return {@link ObjectId} the player whose turn it is.
     */
    @Override
    public ObjectId getCurrentThrowerInRound(X01LegRound legRound, ObjectId throwsFirstInLeg, List<X01MatchPlayer> players) {
        if (legRound == null || players == null) return null;

        // Get the players who haven't scored yet in this round
        List<X01MatchPlayer> playersToThrow = getPlayersToThrowInRound(legRound, players);

        // When all players have thrown, there is no current throws for this round.
        if (playersToThrow.isEmpty()) return null;

        // Find the index of the player who stars the round.
        List<X01MatchPlayer> orderedPlayers = getLegRoundThrowingOrder(throwsFirstInLeg, players);
        if (orderedPlayers == null) return null;

        // Find the first player in the order who hasn't scored yet.
        return orderedPlayers.stream()
                .map(X01MatchPlayer::getPlayerId)
                .filter(playerId -> !legRound.getScores().containsKey(playerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find all players that have yet to score in a round.
     *
     * @param legRound {@link X01LegRound} the round to check
     * @param players     {@link List<X01MatchPlayer>} list of all match players.
     * @return {@link List<X01MatchPlayer>} list of players that have not scored in the round.
     */
    @Override
    public List<X01MatchPlayer> getPlayersToThrowInRound(X01LegRound legRound, List<X01MatchPlayer> players) {
        if (legRound == null || X01ValidationUtils.isPlayersEmpty(players)) return Collections.emptyList();

        // Find players that haven't scored and map to a list.
        return players.stream()
                .filter(player -> !legRound.getScores().containsKey(player.getPlayerId()))
                .toList();
    }

    /**
     * Orders a player list to the playing order for a round.
     *
     * @param throwsFirst {@link ObjectId} the player starting the round
     * @param players     {@link List<X01MatchPlayer>} list of all match players.
     * @return {@link List<X01MatchPlayer>} players ordered in the playing order of the round.
     */
    @Override
    public List<X01MatchPlayer> getLegRoundThrowingOrder(ObjectId throwsFirst, List<X01MatchPlayer> players) {
        if (players == null) return null;

        // Find the index of the player that starts the round.
        int throwsFirstIndex = players.indexOf(players.stream()
                .filter(player -> player.getPlayerId().equals(throwsFirst))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found")));

        // Order the players in the throwing order of the round.
        List<X01MatchPlayer> orderedPlayers = new ArrayList<>();
        orderedPlayers.addAll(players.subList(throwsFirstIndex, players.size()));
        orderedPlayers.addAll(players.subList(0, throwsFirstIndex));

        // Return the ordered list.
        return orderedPlayers;
    }

    /**
     * Removes the last round score from the given leg round.
     *
     * @param legRound {@link X01LegRound} from which to remove the last score
     * @return boolean if a score was removed
     */
    @Override
    public boolean removeLastScoreFromRound(X01LegRound legRound) {
        if (X01ValidationUtils.isScoresEmpty(legRound)) return false;

        // Create a list of the score keys and reverse it to have the last score first.
        Map<ObjectId, X01LegRoundScore> scores = legRound.getScores();
        List<ObjectId> reverseKeys = new ArrayList<>(scores.keySet());
        Collections.reverse(reverseKeys);

        // If there is at least one score, remove the most recently added one
        if (!reverseKeys.isEmpty()) {
            legRound.getScores().remove(reverseKeys.get(0));
            return true;
        }

        // No scores were present to remove
        return false;
    }

}
