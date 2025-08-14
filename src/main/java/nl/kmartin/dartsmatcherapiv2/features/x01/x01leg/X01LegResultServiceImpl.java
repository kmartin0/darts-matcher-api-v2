package nl.kmartin.dartsmatcherapiv2.features.x01.x01leg;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.X01MatchUtils;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRoundScore;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leground.IX01LegRoundService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class X01LegResultServiceImpl implements IX01LegResultService {

    private final IX01LegRoundService legRoundService;

    public X01LegResultServiceImpl(IX01LegRoundService legRoundService) {
        this.legRoundService = legRoundService;
    }

    /**
     * Updates the leg result for a leg.
     *
     * @param leg     {@link X01Leg} the leg to be updated
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @param x01     int the x01 setting for the leg
     */
    @Override
    public void updateLegResult(X01Leg leg, List<X01MatchPlayer> players, int x01) {
        // If the leg is null exit early, if the players are null clear the winner and exit early.
        if (leg == null) return;
        if (X01MatchUtils.isPlayersEmpty(players)) {
            leg.setWinner(null);
            return;
        }

        // Update the remaining fields.
        updateRemaining(leg, x01);

        // Find the winner of the leg by getting the first player who reaches zero remaining points.
        Optional<X01MatchPlayer> winner = findLegWinner(leg, players, x01);

        // If a winner is present, set the player ID, otherwise set the winner to null
        winner.ifPresentOrElse(
                matchPlayer -> {
                    leg.setWinner(matchPlayer.getPlayerId());
                    removeScoresAfterWinner(leg, leg.getWinner());
                },
                () -> leg.setWinner(null)
        );
    }

    /**
     * Removes all scores that appear after the final score of the leg winner. If rounds become empty after
     * removing scores they will also be removed. The situation of scores/rounds beyond the winning point could
     * occur due to editing a score.
     *
     * @param leg       {@link X01Leg} the leg
     * @param legWinner {@link ObjectId} the player id of the leg winner
     */
    @Override
    public void removeScoresAfterWinner(X01Leg leg, ObjectId legWinner) {
        // If there is no leg winner. No trimming needs to happen.
        if (X01MatchUtils.isRoundsEmpty(leg) || legWinner == null) return;

        // Iterate through the rounds in reverse order (latest round first)
        Iterator<X01LegRound> reverseRoundsIterator = leg.getRounds().descendingMap().values().iterator();
        while (reverseRoundsIterator.hasNext()) {
            X01LegRound round = reverseRoundsIterator.next();

            // If the leg winner has not thrown in this round, remove the round and move on to the next round.
            if (!round.getScores().containsKey(legWinner)) {
                reverseRoundsIterator.remove();
                continue;
            }

            // Remove all scores from this round that occur after the winner's score.
            legRoundService.removeScoresAfterWinner(round, legWinner);

            // This round contains the winner's score so we can stop trimming.
            break;
        }
    }

    /**
     * Finds the winner of a leg based on the remaining score reaching zero.
     *
     * @param leg     {@link X01Leg} the leg to evaluate
     * @param players {@link List<X01MatchPlayer>} the list of match players
     * @param x01     int the starting score for the leg
     * @return {@link Optional<X01MatchPlayer>} the winner if one exists, otherwise empty
     */
    @Override
    public Optional<X01MatchPlayer> findLegWinner(X01Leg leg, List<X01MatchPlayer> players, int x01) {
        List<X01MatchPlayer> orderedPlayers = X01MatchUtils.getThrowingOrder(leg.getThrowsFirst(), players);

        for (X01MatchPlayer player : orderedPlayers) {
            int remaining = getRemainingForPlayer(leg, player.getPlayerId(), x01);
            if (remaining == 0) {
                return Optional.of(player);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the remaining score for a specific player in a leg by checking the latest round in which they threw.
     *
     * @param leg      {@link X01Leg} the leg to evaluate
     * @param playerId {@link ObjectId} the player ID
     * @param x01      int the starting score for the leg
     * @return int the remaining score for the player
     */
    @Override
    public int getRemainingForPlayer(X01Leg leg, ObjectId playerId, int x01) {
        for (X01LegRound round : leg.getRounds().descendingMap().values()) {
            X01LegRoundScore playerScore = round.getScores().get(playerId);
            if (playerScore != null) {
                return playerScore.getRemaining();
            }
        }

        return x01;
    }

    /**
     * Updates the remaining scores for a specific player across all rounds in a leg.
     *
     * @param leg      {@link X01Leg} the leg to update
     * @param playerId {@link ObjectId} the ID of the player whose remaining scores should be updated
     * @param x01      int the starting score for the leg
     */
    @Override
    public void updateRemaining(X01Leg leg, ObjectId playerId, int x01) {
        if (leg == null) return;

        AtomicInteger previousRemaining = new AtomicInteger(x01);
        leg.getRounds().values().forEach(round -> {
            if (round.getScores().containsKey(playerId)) {
                X01LegRoundScore roundScore = round.getScores().get(playerId);

                roundScore.setRemaining(previousRemaining.get() - roundScore.getScore());
                previousRemaining.set(roundScore.getRemaining());
            }
        });
    }

    /**
     * Updates the remaining scores for all players across all rounds in a leg.
     *
     * @param leg {@link X01Leg} the leg to update
     * @param x01 int the starting score for the leg
     */
    @Override
    public void updateRemaining(X01Leg leg, int x01) {
        if (leg == null) return;

        Map<ObjectId, Integer> remainingMap = new HashMap<>();
        leg.getRounds().values().forEach(round -> {
            round.getScores().forEach((playerId, roundScore) -> {
                int previousRemaining = remainingMap.getOrDefault(playerId, x01);
                roundScore.setRemaining(previousRemaining - roundScore.getScore());
                remainingMap.put(playerId, roundScore.getRemaining());
            });
        });
    }

    /**
     * Calculates the total number of darts used by a specific player across a leg.
     *
     * @param leg      {@link X01Leg} the leg to check
     * @param playerId the ID of the player whose darts usage is being calculated
     * @return int The sum of darts used by the player across the leg.
     */
    @Override
    public int calculateDartsUsed(X01Leg leg, ObjectId playerId) {
        // When no rounds exist, the player has no darts used.
        if (X01MatchUtils.isRoundsEmpty(leg) || playerId == null) return 0;

        // If the player has won the leg, get the checkout round.
        Optional<Integer> checkoutRoundNumber = leg.getWinner() == null
                ? Optional.empty()
                : leg.getRounds().keySet().stream().max(Integer::compareTo);

        // For every round map the player darts used and sum these up.
        return leg.getRounds().entrySet().stream()
                .mapToInt(entry -> {
                    X01LegRoundScore playerScore = entry.getValue().getScores().get(playerId);
                    if (playerScore == null) return 0;
                    if (checkoutRoundNumber.isPresent() && checkoutRoundNumber.get().equals(entry.getKey()))
                        return leg.getCheckoutDartsUsed();

                    return 3;
                }).sum();
    }

}
