package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01ValidationUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class X01LegProgressServiceImpl implements IX01LegProgressService {

    /**
     * Find a round by its round number from a leg.
     *
     * @param leg         {@link X01Leg} the leg to find the round in
     * @param roundNumber int the round number to find
     * @return {@link Optional<X01LegRound>} an optional round containing the round if it exists otherwise empty.
     */
    @Override
    public Optional<X01LegRound> getLegRound(X01Leg leg, int roundNumber, boolean throwIfNotFound) {
        ResourceNotFoundException notFoundException = new ResourceNotFoundException(X01LegRound.class, roundNumber);

        // If the round can't exist. Early exit.
        if (X01ValidationUtils.isRoundsEmpty(leg) || roundNumber < 1) {
            if (throwIfNotFound) throw notFoundException;
            else return Optional.empty();
        }

        // Find the first round in the rounds list matching the round number.
        Optional<X01LegRound> round = leg.getRounds().stream().filter(x01LegRound -> x01LegRound.getRound() == roundNumber).findFirst();
        if (throwIfNotFound && round.isEmpty()) throw notFoundException;

        // Return the first round with the round number otherwise empty.
        return round;
    }

    /**
     * Finds the lowest-numbered round which does not have a score for all players.
     *
     * @param leg          {@link X01Leg} representing the leg for which the current round needs to be found.
     * @param players {@link List<X01MatchPlayer>} representing the players of the match.
     * @return {@link Optional<X01LegRound>} the lowest round in play, otherwise empty.
     */
    @Override
    public Optional<X01LegRound> getCurrentLegRound(X01Leg leg, List<X01MatchPlayer> players) {
        if (X01ValidationUtils.isRoundsEmpty(leg) || X01ValidationUtils.isPlayersEmpty(players)) return Optional.empty();

        // Filter rounds with missing player scores and get the lowest round number.
        return leg.getRounds().stream()
                .filter(round -> players.stream().anyMatch(matchPlayer -> round.getScores().get(matchPlayer.getPlayerId()) == null))
                .min(Comparator.comparingInt(X01LegRound::getRound));
    }

    /**
     * Creates the next missing round from a leg and adds it to the rounds list.
     *
     * @param leg {@link X01Leg} the leg for which the next round has to be added to.
     * @return {@link Optional<X01LegRound>} the added round.
     */
    @Override
    public Optional<X01LegRound> createNextLegRound(X01Leg leg) {
        if (leg == null) return Optional.empty();

        // Get existing round numbers.
        Set<Integer> existingRoundNumbers = getLegRoundNumbers(leg);

        // Find the next available leg round number.
        int nextRoundNumber = NumberUtils.findNextNumber(existingRoundNumbers);
        if (nextRoundNumber == -1) return Optional.empty();

        // Create and add a new leg round to the leg.
        X01LegRound newLegRound = new X01LegRound(nextRoundNumber, new HashMap<>());
        leg.getRounds().add(newLegRound);
        return Optional.of(newLegRound);
    }

    /**
     * Finds all unique round numbers for a list of rounds.
     *
     * @param leg {@link X01Leg} the leg for which the round numbers need to be determined.
     * @return {@link Set<Integer>} an unordered set containing the round numbers.
     */
    @Override
    public Set<Integer> getLegRoundNumbers(X01Leg leg) {
        if (X01ValidationUtils.isRoundsEmpty(leg)) return Collections.emptySet();

        // Map all round numbers to a set of integers.
        return leg.getRounds().stream()
                .map(X01LegRound::getRound)
                .collect(Collectors.toSet());
    }

    /**
     * Finds the last round from a leg.
     *
     * @param leg {@link X01Leg} the list of rounds
     * @return {@link Optional<X01LegRound>} the highest numbered round from a leg, empty if the leg has no rounds
     */
    @Override
    public Optional<X01LegRound> getLastRound(X01Leg leg) {
        if (X01ValidationUtils.isRoundsEmpty(leg)) return Optional.empty();

        return leg.getRounds().stream().max(Comparator.comparingInt(X01LegRound::getRound));
    }

    /**
     * Find the last score for a player from a leg.
     *
     * @param leg       {@link X01Leg} the leg in which the last score for a player needs to be found.
     * @param throwerId {@link ObjectId} the player id for which the latest turn needs to be found
     * @return {@link Optional<X01LegRoundScore>} the round score for a player in their latest round, if no score found empty
     */
    @Override
    public Optional<X01LegRoundScore> getLastScoreForPlayer(X01Leg leg, ObjectId throwerId) {
        if(X01ValidationUtils.isRoundsEmpty(leg) || throwerId == null) return Optional.empty();

        // Find the rounds containing a score for the player
        List<X01LegRound> playerRounds = leg.getRounds().stream().filter(round -> round.getScores().containsKey(throwerId)).toList();

        // Get the highest round containing the player score
        return playerRounds.stream()
                .max(Comparator.comparingInt(X01LegRound::getRound))
                .flatMap(round -> Optional.ofNullable(round.getScores().get(throwerId)));
    }

    /**
     * Determines if a leg is concluded by checking the winner property
     *
     * @param leg {@link X01Leg} the leg that needs to be checked
     * @return boolean if the leg is concluded
     */
    @Override
    public boolean isLegConcluded(X01Leg leg) {
        return leg != null && leg.getWinner() != null;
    }

}
