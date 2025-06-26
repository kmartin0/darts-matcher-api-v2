package nl.kmartin.dartsmatcherapiv2.features.x01.x01leground;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Primary
public class X01LegRoundServiceImpl implements IX01LegRoundService {

    /**
     * Find the player whose turn it is to throw in a round.
     *
     * @param legRound         {@link X01LegRound} the round to check.
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
     * @param players  {@link List<X01MatchPlayer>} list of all match players.
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
        // Nothing to remove
        if (X01ValidationUtils.isScoresEmpty(legRound)) return false;

        // Iterate through the scores map keys. When the last key is reached, remove it and return true.
        Iterator<ObjectId> it = legRound.getScores().keySet().iterator();
        while (it.hasNext()) {
            ObjectId playerId = it.next();
            if (!it.hasNext()) {
                legRound.getScores().remove(playerId);
                return true;
            }
        }

        // Not score removed.
        return false;
    }

    /**
     * Removes all the scores in a round that occur after the winner's score.
     *
     * @param round     {@link X01LegRound} the round to trim.
     * @param legWinner {@link ObjectId} the player that won the leg.
     */
    @Override
    public void removeScoresAfterWinner(X01LegRound round, ObjectId legWinner) {
        if (round == null || legWinner == null) return;

        // Iterate through the scores in insertion order. Remove all scores occur after the winner's score.
        Iterator<ObjectId> scoresIterator = round.getScores().keySet().iterator();
        boolean winnerHasThrown = false;
        while (scoresIterator.hasNext()) {
            ObjectId playerId = scoresIterator.next();

            if (winnerHasThrown) scoresIterator.remove();
            else if (playerId.equals(legWinner)) winnerHasThrown = true;
        }
    }

}
