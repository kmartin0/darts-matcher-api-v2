package nl.kmartin.dartsmatcherapiv2.features.x01.x01resultstatistics;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01ResultStatistics;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Service
public class X01ResultStatisticsServiceImpl implements IX01ResultStatisticsService {

    /**
     * Updates the sets won statistics for each player based on the result of a set.
     * Increments the sets won count for players who either won or drew the set.
     *
     * @param set        The set containing the results of players.
     * @param playersMap A map of player IDs to {@link X01MatchPlayer} instances.
     *                   Used to update each player's result statistics.
     */
    public void updateSetsWonStatistics(X01Set set, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (set == null || set.getResult() == null || CollectionUtils.isEmpty(playersMap)) return;

        set.getResult().forEach((playerId, resultType) -> {
            if ((resultType == ResultType.WIN || resultType == ResultType.DRAW) && playersMap.containsKey(playerId)) {
                X01ResultStatistics playerResultStatistics = playersMap.get(playerId).getStatistics().getResultStatistics();
                playerResultStatistics.setSetsWon(playerResultStatistics.getSetsWon() + 1);
            }
        });
    }

    /**
     * Updates the legs won statistics for each player based on the result of a leg.
     * Increments the legs won count for the player who won the leg.
     *
     * @param leg        The leg containing the leg winner.
     * @param playersMap A map of player IDs to {@link X01MatchPlayer} instances.
     *                   Used to update the player's result statistics.
     */
    public void updateLegsWonStatistics(X01Leg leg, Map<ObjectId, X01MatchPlayer> playersMap) {
        if (leg == null || leg.getWinner() == null || CollectionUtils.isEmpty(playersMap)) return;

        if (playersMap.containsKey(leg.getWinner())) {
            X01ResultStatistics playerResultStatistics = playersMap.get(leg.getWinner()).getStatistics().getResultStatistics();
            playerResultStatistics.setLegsWon(playerResultStatistics.getLegsWon() + 1);
        }
    }
}
