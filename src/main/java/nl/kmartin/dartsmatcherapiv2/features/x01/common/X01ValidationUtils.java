package nl.kmartin.dartsmatcherapiv2.features.x01.common;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Leg;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01LegRound;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Set;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class X01ValidationUtils {
    private X01ValidationUtils() {}

    public static boolean isSetsEmpty(X01Match match) {
        return match == null || CollectionUtils.isEmpty(match.getSets());
    }

    public static boolean isLegsEmpty(X01Set set) {
        return set == null || CollectionUtils.isEmpty(set.getLegs());
    }

    public static boolean isRoundsEmpty(X01Leg leg) {
        return leg == null || CollectionUtils.isEmpty(leg.getRounds());
    }

    public static boolean isScoresEmpty(X01LegRound legRound) {
        return legRound == null || CollectionUtils.isEmpty(legRound.getScores());
    }

    public static boolean isPlayersEmpty(List<? extends MatchPlayer> players) {
        return players == null || CollectionUtils.isEmpty(players);
    }
}
