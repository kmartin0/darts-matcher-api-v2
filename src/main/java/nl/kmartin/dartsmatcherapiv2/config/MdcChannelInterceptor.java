package nl.kmartin.dartsmatcherapiv2.config;

import nl.kmartin.dartsmatcherapiv2.common.Constants;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A Spring Messaging {@link ChannelInterceptor}
 *
 * Before a message is processed, it retrieves the correlationId from the
 * WebSocket session attributes and places it into the MDC. It clears the MDC
 * after processing is complete.
 */
@Component
public class MdcChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null && sessionAttributes.containsKey(Constants.CORRELATION_ID_KEY)) {
            MDC.put(Constants.CORRELATION_ID_KEY, (String) sessionAttributes.get(Constants.CORRELATION_ID_KEY));
        }
        return message;
    }

    @Override
    public void afterSendCompletion(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent, Exception ex) {
        MDC.clear();
    }
}
