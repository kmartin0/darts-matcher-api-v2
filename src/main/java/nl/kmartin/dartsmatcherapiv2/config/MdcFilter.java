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

        // Retrieve and put the client ip address in the mdc.
        String clientIp = getClientIpAddress(request);
        MDC.put(Constants.CLIENT_IP_KEY, clientIp);

        // Create a correlation ID and add it to the MDC.
        String correlationId = UUID.randomUUID().toString();
        MDC.put(Constants.CORRELATION_ID_KEY, correlationId);
        request.getSession().setAttribute(Constants.CORRELATION_ID_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear the MDC after the request is complete
            MDC.clear();
        }
    }

    /**
     * Utility method to get the client IP address using the X-Forwarded-For header.
     * @param request The HttpServletRequest.
     * @return The client IP address.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Get the forwarded for header.
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            // X-Forwarded-For format comma seperated entries with client_ip as the first entry.
            return xForwardedForHeader.split(",")[0].trim();
        }
        // If no proxy header, use the direct connection address
        return request.getRemoteAddr();
    }
}
