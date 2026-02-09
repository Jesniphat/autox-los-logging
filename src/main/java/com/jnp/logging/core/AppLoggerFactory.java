package com.jnp.logging.core;

import com.jnp.logging.config.LoggingProperties;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for creating AppLogger instances.
 * Caches logger instances for reuse.
 */
public class AppLoggerFactory {

    private final String applicationName;
    private final LoggingProperties properties;
    private final ConcurrentMap<String, AppLogger> loggerCache = new ConcurrentHashMap<>();

    public AppLoggerFactory(
            @Value("${spring.application.name:application}") String applicationName,
            LoggingProperties properties) {
        this.applicationName = applicationName;
        this.properties = properties;
    }

    /**
     * Get a logger for the specified class.
     */
    public AppLogger getLogger(Class<?> clazz) {
        return loggerCache.computeIfAbsent(clazz.getName(), 
                name -> new AppLogger(clazz, getEffectiveApplicationName(), properties));
    }

    /**
     * Get a logger with the specified name.
     */
    public AppLogger getLogger(String name) {
        return loggerCache.computeIfAbsent(name, 
                n -> new AppLogger(n, getEffectiveApplicationName(), properties));
    }

    private String getEffectiveApplicationName() {
        if (properties.getApplicationName() != null && !properties.getApplicationName().isBlank()) {
            return properties.getApplicationName();
        }
        return applicationName;
    }

    /**
     * Get the configured application name.
     */
    public String getApplicationName() {
        return getEffectiveApplicationName();
    }
}
