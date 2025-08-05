package nl.kmartin.dartsmatcherapiv2.features.x01.x01statistics;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01averagestatistics.IX01AverageStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01checkoutstatistics.IX01CheckoutStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01leg.IX01LegService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01resultstatistics.IX01ResultStatisticsService;
import nl.kmartin.dartsmatcherapiv2.features.x01.x01scorestatistics.IX01ScoreStatisticsService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class X01StatisticsServiceImpl implements IX01StatisticsService {
    private final IX01ResultStatisticsService resultStatisticsService;
    private final IX01ScoreStatisticsService scoreStatisticsService;
    private final IX01CheckoutStatisticsService checkoutStatisticsService;
    private final IX01AverageStatisticsService averageStatisticsService;
    private final IX01LegService legService;

    public X01StatisticsServiceImpl(IX01ResultStatisticsService resultStatisticsService, IX01ScoreStatisticsService scoreStatisticsService,
                                    IX01CheckoutStatisticsService checkoutStatisticsService,
                                    IX01AverageStatisticsService averageStatisticsService,
                                    IX01LegService legService) {
        this.resultStatisticsService = resultStatisticsService;
        this.scoreStatisticsService = scoreStatisticsService;
        this.checkoutStatisticsService = checkoutStatisticsService;
        this.averageStatisticsService = averageStatisticsService;
        this.legService = legService;
    }

    /**
     * Recalculates and sets the statistics for all match players from a match.
     *
     * @param match {@link X01Match} the match for which the player statistics need to be updated.
     */
    @Override
    public void updatePlayerStatistics(X01Match match) {
        if (match == null) return;

        // Reset the statistics for all players
        resetPlayerStatistics(match.getPlayers());

        // Convert the players list to a players map for quicker access.
        Map<ObjectId, X01MatchPlayer> playersMap = match.getPlayers()
                .stream()
                .collect(Collectors.toMap(X01MatchPlayer::getPlayerId, Function.identity()));

        // Process and update the player statistics using the data of all the sets
        processSets(match.getSets(), match.getMatchSettings().isTrackDoubles(), playersMap);
    }

    /**
     * Process and update the player statistics from the data of all the sets
     *
     * @param sets         NavigableMap<Integer, X01Set> the map of sets containing the player turns
     * @param trackDoubles boolean whether to track doubles missed
     * @param playersMap   Map<ObjectId, X01MatchPlayer> the players for which the statistics need to be updated
     */
    private void processSets(NavigableMap<Integer, X01Set> sets, boolean trackDoubles,
                             Map<ObjectId, X01MatchPlayer> playersMap) {
        if (sets == null || playersMap == null) return;

        // Process and update the player statistics using the data of all the legs
        sets.values().forEach(set -> {
            this.resultStatisticsService.updateSetsWonStatistics(set, playersMap);
            processLegs(set.getLegs(), trackDoubles, playersMap);
        });
    }

    /**
     * Process and update the player statistics using the data of all the legs
     *
     * @param legs         {@link List<X01Leg>} the list of legs containing the player turns
     * @param trackDoubles boolean whether to track doubles missed
     * @param playersMap   Map<ObjectId, X01MatchPlayer> the players for which the statistics need to be updated
     */
    private void processLegs(NavigableMap<Integer, X01Leg> legs, boolean trackDoubles, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (legs == null || playersMap == null) return;

        // Process and update the player statistics using the data of all the leg rounds
        legs.values().forEach(leg -> {
            this.resultStatisticsService.updateLegsWonStatistics(leg, playersMap);
            processLegRounds(leg.getRounds(), leg, trackDoubles, playersMap);
        });
    }

    /**
     * Process and update the player statistics using the data of all the leg rounds
     *
     * @param rounds       {@link List<X01LegRound>} the list of leg rounds containing the player turns
     * @param leg          {@link X01Leg} the leg from which the rounds originate
     * @param trackDoubles boolean whether to track doubles missed
     * @param playersMap   Map<ObjectId, X01MatchPlayer> the players for which the statistics need to be updated
     */
    private void processLegRounds(NavigableMap<Integer, X01LegRound> rounds, X01Leg leg,
                                  boolean trackDoubles, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (rounds == null || leg == null || playersMap == null) return;

        // Process and update player statistics based on the scores from all rounds
        rounds.entrySet().stream()
                .map(X01LegRoundEntry::new)
                .forEach(roundEntry -> processRoundScores(roundEntry.round().getScores(), leg, roundEntry, trackDoubles, playersMap));
    }

    /**
     * Process and update player statistics based on the scores from a round
     *
     * @param roundScores   Map<ObjectId, X01LegRoundScore> the player scores made in a round
     * @param leg           {@link X01Leg} the leg from which the scores originate
     * @param legRoundEntry Map entry for the round from which the score originates
     * @param trackDoubles  boolean whether to track doubles missed
     * @param playersMap    Map<ObjectId, X01MatchPlayer> the players for which the statistics need to be updated
     */
    private void processRoundScores(Map<ObjectId, X01LegRoundScore> roundScores, X01Leg leg,
                                    X01LegRoundEntry legRoundEntry, boolean trackDoubles, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (roundScores == null || leg == null || legRoundEntry == null || playersMap == null) return;

        // Update the player statistics for all players that scored in this round
        roundScores.forEach((playerId, roundScore) -> {
            // Find the player that scored this turn
            Optional<X01MatchPlayer> playerOpt = Optional.ofNullable(playersMap.get(playerId));

            // Check if the player from this turn exists.
            if (playerOpt.isPresent()) {
                // If the player does not have statistics, initialize them
                if (playerOpt.get().getStatistics() == null) {
                    playerOpt.get().setStatistics(new X01Statistics());
                }

                // Update the player statistics for the player that scored this turn
                processPlayerScore(playerOpt.get(), leg, legRoundEntry, roundScore, trackDoubles);
            }
        });
    }

    /**
     * Update the player statistics for the player that scored
     *
     * @param player        {@link X01MatchPlayer} the player that scored
     * @param leg           {@link X01Leg} the leg from which the score originates
     * @param legRoundEntry Map entry for the round from which the score originates
     * @param playerScore   {@link X01LegRoundScore} the object containing the score and associated statistics for the player's turn
     */
    private void processPlayerScore(X01MatchPlayer player, X01Leg leg, X01LegRoundEntry legRoundEntry,
                                    X01LegRoundScore playerScore, boolean trackDoubles) {
        if (player == null || leg == null || legRoundEntry == null || playerScore == null) return;

        // Get the player's current statistics
        X01Statistics playerStats = player.getStatistics();
        boolean isScoreCheckout = legService.isPlayerCheckoutRound(leg, legRoundEntry.roundNumber(), player.getPlayerId());

        // Update score stats
        scoreStatisticsService.updateScoreStatistics(playerStats.getScoreStatistics(), playerScore);

        // Update checkout stats
        X01CheckoutStatistics playerCheckoutStats = playerStats.getCheckoutStats();
        checkoutStatisticsService.updateCheckoutStatistics(playerCheckoutStats, playerScore, isScoreCheckout, trackDoubles);

        // Update average stats
        X01AverageStatistics playerAverageStats = playerStats.getAverageStats();
        Integer checkoutDartsUsed = isScoreCheckout ? leg.getCheckoutDartsUsed() : null;
        averageStatisticsService.updateAverageStats(playerAverageStats, playerScore, legRoundEntry.roundNumber(), checkoutDartsUsed);
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