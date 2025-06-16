package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.api;

import nl.kmartin.dartsmatcherapiv2.features.x01.model.X01Match;
import nl.kmartin.dartsmatcherapiv2.utils.WebsocketDestinations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class X01MatchWebsocketServiceImpl implements IX01MatchWebsocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public X01MatchWebsocketServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends an update of the specified X01 match to all subscribers of the match
     *
     * This method will broadcast the given X01 match object to the appropriate destination
     * where all subscribed clients will receive the updated match information.
     *
     * @param match {@link X01Match} the match the subscribers will get
     */
    @Override
    public void sendX01MatchUpdate(X01Match match) {
        if (match == null) return;

        // Create the broadcast destination url for the match
        String destination = WebsocketDestinations.getX01MatchBroadcastDestination(match.getId());

        // Send the match to the subscribers of the match.
        messagingTemplate.convertAndSend(destination, match);
    }
}