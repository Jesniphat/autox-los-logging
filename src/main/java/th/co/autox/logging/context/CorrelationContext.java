package th.co.autox.logging.context;

import java.util.UUID;

/**
 * Utility class for managing correlation ID across threads.
 * Uses ThreadLocal to maintain correlation ID per request.
 */
public class CorrelationContext {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlation_id";

    private CorrelationContext() {
        // Utility class
    }

    /**
     * Get the current correlation ID.
     * If not set, generates a new one.
     */
    public static String getCorrelationId() {
        String correlationId = CORRELATION_ID.get();
        if (correlationId == null) {
            correlationId = generateCorrelationId();
            setCorrelationId(correlationId);
        }
        return correlationId;
    }

    /**
     * Set the correlation ID for the current thread.
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            CORRELATION_ID.set(correlationId);
        } else {
            CORRELATION_ID.set(generateCorrelationId());
        }
    }

    /**
     * Clear the correlation ID from the current thread.
     * Should be called at the end of request processing.
     */
    public static void clear() {
        CORRELATION_ID.remove();
    }

    /**
     * Generate a new correlation ID.
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Check if correlation ID is set for current thread.
     */
    public static boolean hasCorrelationId() {
        return CORRELATION_ID.get() != null;
    }
}

