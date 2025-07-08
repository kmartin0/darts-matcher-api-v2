package nl.kmartin.dartsmatcherapiv2.config;

import jakarta.servlet.http.HttpSession;
import nl.kmartin.dartsmatcherapiv2.common.Constants;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Intercepts the native WebSocket handshake process before it completes.
 *
 * Copies the correlationId from the initial HTTP
 * session to the attributes of the WebSocket session, enabling
 * consistent logging context across the connection's lifecycle.
 */
@Component
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession(false);
            if (session != null) {
                Object correlationId = session.getAttribute(Constants.CORRELATION_ID_KEY);
                if (correlationId != null) {
                    attributes.put(Constants.CORRELATION_ID_KEY, correlationId.toString());
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {
        // No action needed
    }
}
