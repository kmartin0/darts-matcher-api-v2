package nl.kmartin.dartsmatcherapiv2.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A Spring {@link TaskDecorator} that propagates the MDC (Mapped Diagnostic Context)
 * from a parent thread to a child thread in a thread pool.
 *
 * This is used for maintaining a consistent correlationId in logs
 * during asynchronous WebSocket message processing.
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
