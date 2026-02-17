package th.co.autox.logging.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Context holder for log-related data per request/thread.
 */
public class LogContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    private LogContext() {
        // Utility class
    }

    /**
     * Put a value in the context.
     */
    public static void put(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    /**
     * Get a value from the context.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) CONTEXT.get().get(key);
    }

    /**
     * Get a value from the context with a default value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        T value = (T) CONTEXT.get().get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Remove a value from the context.
     */
    public static void remove(String key) {
        CONTEXT.get().remove(key);
    }

    /**
     * Get all context values as a map.
     */
    public static Map<String, Object> getAll() {
        return new HashMap<>(CONTEXT.get());
    }

    /**
     * Clear all context values.
     */
    public static void clear() {
        CONTEXT.get().clear();
        CONTEXT.remove();
    }

    /**
     * Check if context contains a key.
     */
    public static boolean contains(String key) {
        return CONTEXT.get().containsKey(key);
    }
}

