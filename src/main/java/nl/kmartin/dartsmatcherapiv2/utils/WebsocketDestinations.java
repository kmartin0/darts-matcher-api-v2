package nl.kmartin.dartsmatcherapiv2.utils;

import nl.kmartin.dartsmatcherapiv2.config.WebsocketConfig;
import org.bson.types.ObjectId;

public class WebsocketDestinations {
    private WebsocketDestinations() {
    }

    // X01Match Sockets
    public static final String X01_GET_MATCH = "/matches/{matchId}";
    public static final String X01_ADD_TURN = "/matches/x01/turn:add";
    public static final String X01_TURN_DART_BOT = "/matches/x01/turn:dart-bot";
    public static final String X01_EDIT_TURN = "/matches/x01/turn:edit";
    public static final String X01_DELETE_TURN = "/matches/x01/turn:delete";
    public static final String X01_DELETE_LEG = "/matches/x01/leg:delete";
    public static final String X01_DELETE_SET = "/matches/x01/set:delete";

    // Error queue
    public static final String ERROR_QUEUE = "/queue/errors";

    public static String getX01MatchBroadcastDestination(ObjectId matchId) {
        return WebsocketConfig.BROADCAST_PREFIX + X01_GET_MATCH.replace("{matchId}", String.valueOf(matchId));
    }
}