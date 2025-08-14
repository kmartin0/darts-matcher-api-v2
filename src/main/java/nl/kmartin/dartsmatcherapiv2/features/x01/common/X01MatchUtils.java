package nl.kmartin.dartsmatcherapiv2.features.x01.common;

import nl.kmartin.dartsmatcherapiv2.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.*;
import org.bson.types.ObjectId;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class X01MatchUtils {
    private X01MatchUtils() {}

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

    public static List<X01MatchPlayer> getThrowingOrder(ObjectId throwsFirst, List<X01MatchPlayer> players) {
        if (throwsFirst == null || players == null) return players;

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
