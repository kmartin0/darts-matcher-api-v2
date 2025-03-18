package nl.kmartin.dartsmatcherapiv2.utils;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StandingsUtils {

    /**
     * Determines the winner(s) based on the current standings and the number of remaining legs.
     * A player is considered a winner if they have won the most legs and cannot be caught
     * by any other player given the remaining matches. When there are multiple winners it is considered a draw.
     *
     * @param standings       Map<ObjectId, Long> A map of player IDs to the number of legs they have won.
     * @param bestOfRemaining The number of remaining legs in the match.
     * @return A list of player IDs who are determined to be the winners.
     */
    public static List<ObjectId> determineWinners(Map<ObjectId, Long> standings, int bestOfRemaining) {
        if (standings == null) return null;

        // Initialize the winner list.
        List<ObjectId> winners = new ArrayList<>();

        // Step 1: Find the most legs won.
        long mostLegsWon = standings.values().stream().max(Long::compare).orElse(0L);

        // Step 2: Find if a player is unbeatable (nobody can catch up anymore with the remaining legs).
        standings.forEach((playerId, legsWon) -> {
            if (hasPlayerWonOrDrawInStandings(playerId, legsWon, mostLegsWon, standings, bestOfRemaining)) {
                winners.add(playerId);
            }
        });

        // Return the winners list.
        return winners;
    }

    /**
     * Determines if a player has won or drawn based on the standings and the remaining legs.
     * A player is considered a winner if:
     * - They have the most wins, and no remaining legs allow another player to surpass them.
     * - If the game has ended (no remaining legs), a player with the most wins has either won or drawn.
     *
     * @param playerId        {@link ObjectId}   The ID of the player being evaluated.
     * @param playerWins      long The number of legs won by the player.
     * @param mostWins        long The highest number of legs won by any player.
     * @param standings       Map<ObjectId, Long> The current standings of all players.
     * @param bestOfRemaining int The number of remaining legs in the match.
     * @return boolean if the player has won or drawn.
     */
    private static boolean hasPlayerWonOrDrawInStandings(ObjectId playerId, long playerWins, long mostWins, Map<ObjectId, Long> standings, int bestOfRemaining) {
        if (standings == null) return false;

        // If the player has the most wins, determine if they have won outright.
        if (playerWins == mostWins) {
            if (bestOfRemaining == 0)
                return true; // When there no more remaining legs the player equaling the most wins has either won or drew.
            if (standings.size() == 1)
                return false; // When only 1 player is playing ensure to go the length of the game.

            // Check if any other player can still catch up.
            return standings.entrySet().stream()
                    .noneMatch(entry -> !entry.getKey().equals(playerId) &&
                            entry.getValue() + bestOfRemaining >= playerWins);
        }

        // The player hasn't won or drawn.
        return false;
    }

}
