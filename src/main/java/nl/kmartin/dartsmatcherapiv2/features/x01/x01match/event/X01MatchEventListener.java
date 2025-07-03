package nl.kmartin.dartsmatcherapiv2.features.x01.x01match.event;

import nl.kmartin.dartsmatcherapiv2.common.WebsocketDestinations;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class X01MatchEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public X01MatchEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleX01MatchEvent(X01MatchEvent event) {
        String destination = WebsocketDestinations.getX01MatchBroadcastDestination((event.getMatchId()));
        messagingTemplate.convertAndSend(destination, event);
    }
}