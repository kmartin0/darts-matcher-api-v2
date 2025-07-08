package nl.kmartin.dartsmatcherapiv2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.kmartin.dartsmatcherapiv2.common.Constants;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Creates a unique correlation ID for each incoming HTTP request and adds it to the MDC.
 */
@Component
public class MdcFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String correlationId = UUID.randomUUID().toString();
            MDC.put(Constants.CORRELATION_ID_KEY, correlationId);
            request.getSession().setAttribute(Constants.CORRELATION_ID_KEY, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            // Clear the MDC after the request is complete
            MDC.clear();
        }
    }
}
