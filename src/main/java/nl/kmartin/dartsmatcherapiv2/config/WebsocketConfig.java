package nl.kmartin.dartsmatcherapiv2.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
 * Websocket connection url:                                ws://localhost:8080/darts-matcher-websocket
 * =
 * Example Destinations:
 * STOMP subscribe destination (Single Response):       /app/matches/x01/67dd4a0746cdab5415620e01
 * STOMP subscribe destination (Broadcast Response):    /topic/matches/x01/67dd4a0746cdab5415620e01
 * STOMP subscribe error queue destination:             /user/queue/errors
 * STOMP publish destination:                           /app/matches/x01/67dd4a0746cdab5415620e01/turn/add
 * STOMP message content (add turn):                    {"matchId": "67dd4a0746cdab5415620e01", "score": 60, "dartsUsed": 3, "doublesMissed": 0}
 * =
 * Websocket Debug Tool: https://jiangxy.github.io/websocket-debug-tool/
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    public static String APP_PREFIX = "/app";
    public static String BROADCAST_PREFIX = "/topic";
    public static String BROADCAST_ERROR_PREFIX = "/queue";
    public static String APP_WEBSOCKET_ENDPOINT = "/darts-matcher-websocket";

    private final MdcChannelInterceptor mdcChannelInterceptor;
    private final WebsocketHandshakeInterceptor handshakeInterceptor;
    private final ThreadPoolTaskExecutorBuilder taskExecutorBuilder;
    private final MdcTaskDecorator mdcTaskDecorator;

    public WebsocketConfig(MdcChannelInterceptor mdcChannelInterceptor, WebsocketHandshakeInterceptor handshakeInterceptor, ThreadPoolTaskExecutorBuilder taskExecutorBuilder, MdcTaskDecorator mdcTaskDecorator) {
        this.mdcChannelInterceptor = mdcChannelInterceptor;
        this.handshakeInterceptor = handshakeInterceptor;
        this.taskExecutorBuilder = taskExecutorBuilder;
        this.mdcTaskDecorator = mdcTaskDecorator;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes(APP_PREFIX)
                .enableSimpleBroker(BROADCAST_PREFIX, BROADCAST_ERROR_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(APP_WEBSOCKET_ENDPOINT)
                .setAllowedOriginPatterns("*")
                .addInterceptors(this.handshakeInterceptor);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        ThreadPoolTaskExecutor mdcTaskExecutor = this.taskExecutorBuilder
                .taskDecorator(mdcTaskDecorator)
                .threadNamePrefix("clientInboundChannel-")
                .build();

        registration.interceptors(this.mdcChannelInterceptor);
        registration.taskExecutor(mdcTaskExecutor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        ThreadPoolTaskExecutor mdcTaskExecutor = this.taskExecutorBuilder
                .taskDecorator(new MdcTaskDecorator())
                .threadNamePrefix("clientOutboundChannel-")
                .build();

        registration.taskExecutor(mdcTaskExecutor);
    }
}