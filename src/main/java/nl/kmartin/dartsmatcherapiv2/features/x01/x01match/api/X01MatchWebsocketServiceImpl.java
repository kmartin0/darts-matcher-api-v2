package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import nl.kmartin.dartsmatcherapiv2.features.x01.common.WebsocketDestinations;
import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import org.bson.types.ObjectId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class X01MatchWebsocketServiceImpl implements IX01MatchWebsocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public X01MatchWebsocketServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Creates a WebSocket event for updating an X01 match.
     *
     * @param match the {@link X01Match} object containing the updated match data
     * @return a {@link X01MatchWebsocketEvent} containing the updated match
     */
    @Override
    public X01MatchWebsocketEvent<X01Match> createUpdateMatchEvent(X01Match match) {
        return new X01MatchWebsocketEvent<>(X01WebSocketEventType.UPDATE_MATCH, match);
    }

    /**
     * Creates a WebSocket event for deleting an X01 match.
     *
     * @param deletedMatchId the {@link ObjectId} of the match that was deleted
     * @return a {@link X01MatchWebsocketEvent} containing the ID of the deleted match
     */
    @Override
    public X01MatchWebsocketEvent<ObjectId> createDeleteMatchEvent(ObjectId deletedMatchId) {
        return new X01MatchWebsocketEvent<>(X01WebSocketEventType.DELETE_MATCH, deletedMatchId);
    }

    /**
     * Broadcasts an update event for an X01 match to all websocket subscribers.
     *
     * @param match {@link X01Match} the match the subscribers will get
     */
    @Override
    public void broadcastX01MatchUpdate(X01Match match) {
        if (match == null) return;

        // Create the event
        X01MatchWebsocketEvent<X01Match> event = createUpdateMatchEvent(match);

        // Create the broadcast destination url for the match
        String destination = WebsocketDestinations.getX01MatchBroadcastDestination(match.getId());

        // Broadcast the match to the subscribers of the match.
        messagingTemplate.convertAndSend(destination, event);
    }

    /**
     * Broadcasts a delete event for an X01 match to all WebSocket subscribers.
     *
     * @param deletedMatchId the {@link ObjectId} of the deleted match.
     */
    @Override
    public void broadcastX01MatchDelete(ObjectId deletedMatchId) {
        if (deletedMatchId == null) return;

        // Create the event
        X01MatchWebsocketEvent<ObjectId> event = createDeleteMatchEvent(deletedMatchId);

        // Create the broadcast destination url
        String destination = WebsocketDestinations.getX01MatchBroadcastDestination(deletedMatchId);

        // Broadcast the match to the subscribers of the match.
        messagingTemplate.convertAndSend(destination, event);
    }
}