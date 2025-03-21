package nl.kmartin.dartsmatcherapiv2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for websocket message broker
 * =
 * Client to server (message) prefix:                       /app
 * Server to client (single response subscription) prefix:  /app
 * Server to client (Broadcast subscription) prefix:        /topic
 * Server to user prefix:                                   /user
 * Websocket connection url:                                ws://localhost:8080/darts-matcher-websocket/websocket
 * =
 * Example Destinations:
 * STOMP subscribe destination (Single Response):       /app/matches/67dd4a0746cdab5415620e01
 * STOMP subscribe destination (Broadcast Response):    /topic/matches/67dd4a0746cdab5415620e01
 * STOMP subscribe error queue destination:             /user/queue/errors
 * STOMP send destination:                              /app/matches/x01/turn:add
 * STOMP message content (add turn):                    {"matchId": "67dd4a0746cdab5415620e01", "score": 60, "dartsUsed": 3, "doublesMissed": 0}
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    public static String APP_PREFIX = "/app";
    public static String BROADCAST_PREFIX = "/topic";
    public static String BROADCAST_ERROR_PREFIX = "/queue";
    public static String APP_WEBSOCKET_ENDPOINT = "/darts-matcher-websocket";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes(APP_PREFIX)
                .enableSimpleBroker(BROADCAST_PREFIX, BROADCAST_ERROR_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(APP_WEBSOCKET_ENDPOINT)
                .setAllowedOriginPatterns("*");
    }
}