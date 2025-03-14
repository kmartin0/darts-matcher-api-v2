package nl.kmartin.dartsmatcherapiv2.features.x01.x01leground;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01LegRoundServiceImpl implements IX01LegRoundService {

    /**
     * Finds the lowest-numbered round which does not have a score for all players.
     *
     * @param rounds       {@link List<X01LegRound>} representing the rounds in the current leg.
     * @param matchPlayers {@link List<X01MatchPlayer>} representing the players of the match.
     * @return {@link Optional<X01LegRound>} the lowest round in play, otherwise empty.
     */
    @Override
    public Optional<X01LegRound> getCurrentLegRound(List<X01LegRound> rounds, List<X01MatchPlayer> matchPlayers) {
        if (rounds == null || matchPlayers == null) return Optional.empty();

        // Filter rounds with missing player scores and get the lowest round number.
        return rounds.stream()
                .filter(round -> matchPlayers.stream().anyMatch(matchPlayer -> round.getScores().get(matchPlayer.getPlayerId()) == null))
                .min(Comparator.comparingInt(X01LegRound::getRound));
    }

    /**
     * Creates the next missing round and adds it to the rounds list.
     *
     * @param rounds {@link List<X01LegRound>} the list of rounds a new round has to be added to.
     * @return {@link Optional<X01LegRound>} the added round.
     */
    @Override
    public Optional<X01LegRound> createNextLegRound(List<X01LegRound> rounds) {
        if (rounds == null) return Optional.empty();

        // Get existing round numbers.
        Set<Integer> existingRoundNumbers = getLegRoundNumbers(rounds);

        // Find the next available leg round number.
        int nextRoundNumber = NumberUtils.findNextNumber(existingRoundNumbers);
        if (nextRoundNumber == -1) return Optional.empty();

        // Create and add a new leg round to the leg.
        X01LegRound newLegRound = new X01LegRound(nextRoundNumber, new HashMap<>());
        rounds.add(newLegRound);
        return Optional.of(newLegRound);
    }

    /**
     * Finds all unique round numbers for a list of rounds.
     *
     * @param rounds {@link List<X01LegRound>} the list of rounds
     * @return {@link Set<Integer>} an unordered set containing the round numbers.
     */
    @Override
    public Set<Integer> getLegRoundNumbers(List<X01LegRound> rounds) {
        if (rounds == null) return null;

        // Map all round numbers to a set of integers.
        return rounds.stream()
                .map(X01LegRound::getRound)
                .collect(Collectors.toSet());
    }

    /**
     * Find the player whose turn it is to throw in a round.
     *
     * @param x01LegRound      {@link X01LegRound} the round to check.
     * @param throwsFirstInLeg {@link ObjectId} the id of the player that started the leg.
     * @param players          {@link List<X01MatchPlayer>} list of all match players.
     * @return {@link ObjectId} the player whose turn it is.
     */
    @Override
    public ObjectId getCurrentThrowerInRound(X01LegRound x01LegRound, ObjectId throwsFirstInLeg, List<X01MatchPlayer> players) {
        if (x01LegRound == null || x01LegRound.getScores() == null || players == null) return null;

        // Get the players who haven't scored yet in this round
        List<X01MatchPlayer> playersToThrow = getPlayersToThrowInRound(x01LegRound, players);

        // When all players have thrown, there is no current throws for this round.
        if (playersToThrow.isEmpty()) return null;

        // Find the index of the player who stars the round.
        List<X01MatchPlayer> orderedPlayers = getLegRoundThrowingOrder(throwsFirstInLeg, players);
        if (orderedPlayers == null) return null;

        // Find the first player in the order who hasn't scored yet.
        return orderedPlayers.stream()
                .map(X01MatchPlayer::getPlayerId)
                .filter(playerId -> !x01LegRound.getScores().containsKey(playerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find a round by its round number in a list of rounds.
     *
     * @param rounds {@link List<X01LegRound>} the list of rounds to find the round in
     * @param round  int the round number to find
     * @return {@link Optional<X01LegRound>} an optional round containing the round if it exists otherwise empty.
     */
    @Override
    public Optional<X01LegRound> getLegRound(List<X01LegRound> rounds, int round) {
        if (rounds == null || round < 0) return Optional.empty();

        // Return the first round with the round number otherwise empty.
        return rounds.stream().filter(x01LegRound -> x01LegRound.getRound() == round).findFirst();
    }

    /**
     * Calculates the remaining score for a player in a list of rounds.
     *
     * @param x01      int of the starting score
     * @param rounds   {@link List<X01LegRound>} the list of rounds containing the player scores
     * @param playerId {@link ObjectId} the player for which the remaining score needs to be calculated
     * @return int of the remaining score for a player
     */
    @Override
    public int calculateRemainingScore(int x01, List<X01LegRound> rounds, ObjectId playerId) {
        if (rounds == null) return -1; // TODO: Error handling here.

        // For every round map the player score and sum these up.
        int totalScored = rounds.stream()
                .mapToInt(value -> {
                    X01LegRoundScore playerScore = value.getScores().get(playerId);
                    return (playerScore != null) ? playerScore.getScore() : 0;
                }).sum();

        // Subtract the total scored points from the starting score (x01).
        return x01 - totalScored;
    }


    /**
     * Find all players that have yet to score in a round.
     *
     * @param x01LegRound {@link X01LegRound} the round to check
     * @param players     {@link List<X01MatchPlayer>} list of all match players.
     * @return {@link List<X01MatchPlayer>} list of players that have not scored in the round.
     */
    private List<X01MatchPlayer> getPlayersToThrowInRound(X01LegRound x01LegRound, List<X01MatchPlayer> players) {
        if (x01LegRound == null || x01LegRound.getScores() == null || players == null) return null;

        // Find players that haven't scored and map to a list.
        return players.stream()
                .filter(player -> !x01LegRound.getScores().containsKey(player.getPlayerId()))
                .toList();
    }

    /**
     * Orders a player list to the playing order for a round.
     *
     * @param throwsFirst {@link ObjectId} the player starting the round
     * @param players     {@link List<X01MatchPlayer>} list of all match players.
     * @return {@link List<X01MatchPlayer>} players ordered in the playing order of the round.
     */
    private List<X01MatchPlayer> getLegRoundThrowingOrder(ObjectId throwsFirst, List<X01MatchPlayer> players) {
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
}
