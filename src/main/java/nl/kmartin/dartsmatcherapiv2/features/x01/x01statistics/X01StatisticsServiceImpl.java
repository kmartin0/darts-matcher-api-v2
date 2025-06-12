package nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics.IX01AverageStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics.IX01CheckoutStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics.IX01ScoreStatisticsService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class X01StatisticsServiceImpl implements IX01StatisticsService {
    private final IX01ScoreStatisticsService scoreStatisticsService;
    private final IX01CheckoutStatisticsService checkoutStatisticsService;
    private final IX01AverageStatisticsService averageStatisticsService;
    private final IX01LegService legService;

    public X01StatisticsServiceImpl(IX01ScoreStatisticsService scoreStatisticsService,
                                    IX01CheckoutStatisticsService checkoutStatisticsService,
                                    IX01AverageStatisticsService averageStatisticsService,
                                    IX01LegService legService) {
        this.scoreStatisticsService = scoreStatisticsService;
        this.checkoutStatisticsService = checkoutStatisticsService;
        this.averageStatisticsService = averageStatisticsService;
        this.legService = legService;
    }

    /**
     * Recalculates and sets the statistics for all match players
     *
     * @param sets         {@link List<X01Set>} the list of sets containing the data for the statistics
     * @param matchPlayers {@link List<X01MatchPlayer>} the players for which the statistics need to be updated
     */
    @Override
    public void updatePlayerStatistics(List<X01Set> sets, List<X01MatchPlayer> matchPlayers) {
        if (sets == null || matchPlayers == null) return;

        // Reset the statistics for all players
        resetPlayerStatistics(matchPlayers);

        // Process and update the player statistics using the data of all the sets
        processSets(sets, matchPlayers);
    }

    /**
     * Process and update the player statistics from the data of all the sets
     *
     * @param sets   {@link List<X01Set>} the list of sets containing the player turns
     * @param {@link List<X01MatchPlayer>} the players for which the statistics need to be updated
     */
    private void processSets(List<X01Set> sets, List<X01MatchPlayer> matchPlayers) {
        if (sets == null || matchPlayers == null) return;

        // Process and update the player statistics using the data of all the legs
        sets.forEach(x01Set -> processLegs(x01Set.getLegs(), matchPlayers));
    }

    /**
     * Process and update the player statistics using the data of all the legs
     *
     * @param sets   {@link List<X01Leg>} the list of legs containing the player turns
     * @param {@link List<X01MatchPlayer>} the players for which the statistics need to be updated
     */
    private void processLegs(List<X01Leg> legs, List<X01MatchPlayer> matchPlayers) {
        if (legs == null || matchPlayers == null) return;

        // Process and update the player statistics using the data of all the leg rounds
        legs.forEach(x01Leg -> processLegRounds(x01Leg.getRounds(), x01Leg, matchPlayers));
    }

    /**
     * Process and update the player statistics using the data of all the leg rounds
     *
     * @param rounds       {@link List<X01LegRound>} the list of leg rounds containing the player turns
     * @param x01Leg       {@link X01Leg} the leg from which the rounds originate
     * @param matchPlayers {@link List<X01MatchPlayer>} the players for which the statistics need to be updated
     */
    private void processLegRounds(List<X01LegRound> rounds, X01Leg x01Leg, List<X01MatchPlayer> matchPlayers) {
        if (rounds == null || x01Leg == null || matchPlayers == null) return;

        // Process and update player statistics based on the scores from all rounds
        rounds.forEach(x01LegRound -> processRoundScores(x01LegRound.getScores(), x01Leg, x01LegRound, matchPlayers));
    }

    /**
     * Process and update player statistics based on the scores from a round
     *
     * @param roundScores  Map<ObjectId, X01LegRoundScore> the player scores made in a round
     * @param x01Leg       {@link X01Leg} the leg from which the scores originate
     * @param x01LegRound  {@link X01LegRound} the round from which the scores originate
     * @param matchPlayers {@link List<X01MatchPlayer>} the players for which the statistics need to be updated
     */
    private void processRoundScores(Map<ObjectId, X01LegRoundScore> roundScores, X01Leg x01Leg, X01LegRound x01LegRound, List<X01MatchPlayer> matchPlayers) {
        if (roundScores == null || x01Leg == null || x01LegRound == null || matchPlayers == null) return;

        // Update the player statistics for all players that scored in this round
        roundScores.forEach((playerId, roundScore) -> {
            // Find the player that scored this turn
            Optional<X01MatchPlayer> playerOpt = findPlayerById(matchPlayers, playerId);

            // Check if the player from this turn exists.
            if (playerOpt.isPresent()) {
                // If the player does not have statistics, initialize them
                if (playerOpt.get().getStatistics() == null) {
                    playerOpt.get().setStatistics(new X01Statistics());
                }

                // Update the player statistics for the player that scored this turn
                processPlayerScore(playerOpt.get(), x01Leg, x01LegRound, roundScore);
            }
        });
    }

    /**
     * Update the player statistics for the player that scored
     *
     * @param player      {@link X01MatchPlayer} the player that scored
     * @param x01Leg      {@link X01Leg} the leg from which the score originates
     * @param x01LegRound {@link X01LegRound} the round from which the score originates
     * @param playerScore {@link X01LegRoundScore} the object containing the score and associated statistics for the player's turn
     */
    private void processPlayerScore(X01MatchPlayer player, X01Leg x01Leg, X01LegRound x01LegRound, X01LegRoundScore playerScore) {
        if (player == null || x01Leg == null || x01LegRound == null || playerScore == null) return;

        // Get the player's current statistics
        X01Statistics playerStats = player.getStatistics();

        // Update score stats
        scoreStatisticsService.updateScoreStatistics(playerStats.getScoreStatistics(), playerScore);

        // Update checkout stats
        X01CheckoutStatistics playerCheckoutStats = playerStats.getCheckoutStats();
        boolean isScoreCheckout = legService.isScoreCheckout(x01Leg, x01LegRound, player.getPlayerId());
        checkoutStatisticsService.updateCheckoutStatistics(playerCheckoutStats, playerScore, isScoreCheckout);

        // Update average stats
        X01AverageStatistics playerAverageStats = playerStats.getAverageStats();
        averageStatisticsService.updateAverageStats(playerAverageStats, playerScore, x01LegRound.getRound());
    }

    /**
     * Finds a player by their unique player ID from a list of match players.
     *
     * @param matchPlayers {@link List<X01MatchPlayer>} the list of players participating in the match.
     * @param playerId     {@link ObjectId} the unique ID of the player to find.
     * @return {@link Optional<X01MatchPlayer>} an Optional containing the player if found, or an empty Optional if no player matches the given ID.
     */
    private Optional<X01MatchPlayer> findPlayerById(List<X01MatchPlayer> matchPlayers, ObjectId playerId) {
        // Find the first player with the playerId
        return matchPlayers.stream()
                .filter(player -> player.getPlayerId().equals(playerId))
                .findFirst();
    }

    /**
     * Resets the statistics for all players in the match.
     * If a player does not have statistics, a new {@link X01Statistics} object is created for them.
     *
     * @param matchPlayers {@link List<X01MatchPlayer>} the list of players whose statistics need to be reset.
     */
    private void resetPlayerStatistics(List<X01MatchPlayer> matchPlayers) {
        matchPlayers.forEach(matchPlayer -> {
            // If a player doesn't already have statistics, initialize them with a new X01Statistics object
            if (matchPlayer.getStatistics() == null) {
                matchPlayer.setStatistics(new X01Statistics());
            }

            // Reset the player's statistics to the default state
            matchPlayer.getStatistics().reset();
        });
    }
}