package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.exceptionhandler.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import nl.kmartin.dartsmatcherapiv2.utils.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class X01LegProgressServiceImpl implements IX01LegProgressService {

    private final IX01LegRoundService legRoundService;

    public X01LegProgressServiceImpl(IX01LegRoundService legRoundService) {
        this.legRoundService = legRoundService;
    }

    /**
     * Find a round by its round number from a leg.
     *
     * @param leg         {@link X01Leg} the leg to find the round in
     * @param roundNumber int the round number to find
     * @return {@link Optional<X01LegRoundEntry>} an optional round containing the round if it exists otherwise empty.
     */
    @Override
    public Optional<X01LegRoundEntry> getLegRound(X01Leg leg, int roundNumber, boolean throwIfNotFound) {
        ResourceNotFoundException notFoundException = new ResourceNotFoundException(X01LegRound.class, roundNumber);

        // If the round can't exist. Early exit.
        if (X01MatchUtils.isRoundsEmpty(leg) || roundNumber < 1) {
            if (throwIfNotFound) throw notFoundException;
            else return Optional.empty();
        }

        // Find the first round in the rounds list matching the round number.
        Optional<X01LegRoundEntry> roundEntry = Optional.ofNullable(leg.getRounds().get(roundNumber))
                .map(round -> new X01LegRoundEntry(roundNumber, round));

        if (throwIfNotFound && roundEntry.isEmpty()) throw notFoundException;

        // Return the first round with the round number otherwise empty.
        return roundEntry;
    }

    /**
     * Finds the lowest-numbered round which does not have a score for all players.
     *
     * @param leg     {@link X01Leg} representing the leg for which the current round needs to be found.
     * @param players {@link List<X01MatchPlayer>} representing the players of the match.
     * @return Optional X01LegRound entry for the lowest round in play, otherwise empty.
     */
    @Override
    public Optional<X01LegRoundEntry> getCurrentLegRound(X01Leg leg, List<X01MatchPlayer> players) {
        if (X01MatchUtils.isRoundsEmpty(leg) || X01MatchUtils.isPlayersEmpty(players))
            return Optional.empty();

        // Filter rounds with missing player scores and get the lowest round number.
        return leg.getRounds().entrySet().stream()
                .filter(entry -> players.stream().anyMatch(matchPlayer -> entry.getValue().getScores().get(matchPlayer.getPlayerId()) == null))
                .findFirst()
                .map(X01LegRoundEntry::new);
    }

    /**
     * Creates the next missing round from a leg and adds it to the rounds list.
     *
     * @param leg {@link X01Leg} the leg for which the next round has to be added to.
     * @return Optional X01LegRound entry for the added round.
     */
    @Override
    public Optional<X01LegRoundEntry> createNextLegRound(X01Leg leg) {
        if (leg == null) return Optional.empty();

        // Get existing round numbers.
        Set<Integer> existingRoundNumbers = getLegRoundNumbers(leg);

        // Find the next available leg round number.
        int nextRoundNumber = NumberUtils.findNextNumber(existingRoundNumbers);
        if (nextRoundNumber == -1) return Optional.empty();

        // Create and add a new leg round to the leg.
        X01LegRoundEntry newLegRoundEntry = new X01LegRoundEntry(nextRoundNumber, new X01LegRound(new LinkedHashMap<>()));

        leg.getRounds().put(newLegRoundEntry.roundNumber(), newLegRoundEntry.round());
        return Optional.of(newLegRoundEntry);
    }

    /**
     * Finds all unique round numbers for a list of rounds.
     *
     * @param leg {@link X01Leg} the leg for which the round numbers need to be determined.
     * @return {@link Set<Integer>} an unordered set containing the round numbers.
     */
    @Override
    public Set<Integer> getLegRoundNumbers(X01Leg leg) {
        if (X01MatchUtils.isRoundsEmpty(leg)) return Collections.emptySet();

        // Map all round numbers to a set of integers.
        return leg.getRounds().keySet();
    }

    /**
     * Finds the last round from a leg.
     *
     * @param leg {@link X01Leg} the list of rounds
     * @return {@link Optional<X01LegRound>} the highest numbered round from a leg, empty if the leg has no rounds
     */
    @Override
    public Optional<X01LegRoundEntry> getLastRound(X01Leg leg) {
        if (X01MatchUtils.isRoundsEmpty(leg)) return Optional.empty();

        return Optional.ofNullable(leg.getRounds().lastEntry()).map(X01LegRoundEntry::new);
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
        if (X01MatchUtils.isRoundsEmpty(leg) || throwerId == null) return Optional.empty();

        // Iterate rounds in descending order (highest round first)
        for (Map.Entry<Integer, X01LegRound> entry : leg.getRounds().descendingMap().entrySet()) {
            X01LegRoundScore score = entry.getValue().getScores().get(throwerId);
            if (score != null) return Optional.of(score);
        }

        return Optional.empty();
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

    /**
     * Removes the last score from a leg.
     * While traversing in reverse order also cleans up empty rounds, up to the removed score.
     *
     * @param leg {@link X01Leg} the leg from which to remove the last score
     * @return true if a score was successfully removed; false otherwise
     */
    @Override
    public boolean removeLastScoreFromLeg(X01Leg leg) {
        Iterator<Integer> reverseRoundsIterator = leg.getRounds().descendingKeySet().iterator();
        while (reverseRoundsIterator.hasNext()) {
            X01LegRound round = leg.getRounds().get(reverseRoundsIterator.next());
            boolean scoreRemoved = legRoundService.removeLastScoreFromRound(round);

            if (round.getScores().isEmpty()) reverseRoundsIterator.remove();
            if (scoreRemoved) return true;
        }

        return false;
    }
}
