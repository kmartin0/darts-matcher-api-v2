package nl.kmartin.dartsmatcherapiv2.common;

import nl.kmartin.dartsmatcherapiv2.config.WebsocketConfig;
import org.bson.types.ObjectId;

public class WebsocketDestinations {
    private WebsocketDestinations() {
    }

    // X01Match Sockets
    public static final String X01_GET_MATCH = "/x01/matches/{matchId}";
    public static final String X01_DELETE_MATCH = "/x01/matches/{matchId}/delete";
    public static final String X01_RESET_MATCH = "/x01/matches/{matchId}/reset";
    public static final String X01_REPROCESS_MATCH = "/x01/matches/{matchId}/reprocess";
    public static final String X01_ADD_TURN = "/x01/matches/{matchId}/turn/add";
    public static final String X01_EDIT_TURN = "/x01/matches/{matchId}/turn/edit";
    public static final String X01_DELETE_LAST_TURN = "/x01/matches/{matchId}/turn/delete-last";

    // Error queue
    public static final String ERROR_QUEUE = "/queue/errors";

    public static String getX01MatchBroadcastDestination(ObjectId matchId) {
        return WebsocketConfig.BROADCAST_PREFIX + X01_GET_MATCH.replace("{matchId}", String.valueOf(matchId));
    }
}