package th.co.autox.logging.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import th.co.autox.logging.config.LoggingProperties;
import th.co.autox.logging.context.CorrelationContext;
import th.co.autox.logging.model.LogEntry;
import th.co.autox.logging.model.LogType;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * Core logger class that provides structured JSON logging capabilities.
 * Supports both request and application log types.
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Simple usage - no injection needed
 * private final AppLogger log = new AppLogger(MyService.class);
 *
 * // With Spring injection
 * private final AppLogger log;
 * public MyService(AppLoggerFactory factory) {
 *     this.log = factory.getLogger(MyService.class);
 * }
 * </pre>
 */
@Getter
public class AppLogger {

    private final Logger logger;
    private final String applicationName;
    private final LoggingProperties properties;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    /**
     * Level values matching Logback conventions.
     */
    private static final Map<String, Integer> LEVEL_VALUES = Map.of(
            "TRACE", 5000,
            "DEBUG", 10000,
            "INFO", 20000,
            "WARN", 30000,
            "ERROR", 40000
    );

    // ==================== Static Configuration ====================

    private static volatile String defaultApplicationName = "application";
    private static volatile LoggingProperties defaultProperties = new LoggingProperties();

    /**
     * Configure default settings for loggers created with simple constructor.
     * This is typically called by auto-configuration.
     */
    public static void configureDefaults(String applicationName, LoggingProperties properties) {
        if (applicationName != null && !applicationName.isBlank()) {
            defaultApplicationName = applicationName;
        }
        if (properties != null) {
            defaultProperties = properties;
        }
    }

    /**
     * Get the default application name.
     */
    public static String getDefaultApplicationName() {
        return defaultApplicationName;
    }

    // ==================== Constructors ====================

    /**
     * Create a logger for the specified class using default configuration.
     * This is the simplest way to create a logger:
     * <pre>
     * private final AppLogger log = new AppLogger(MyService.class);
     * </pre>
     */
    public AppLogger(Class<?> clazz) {
        this(clazz, defaultApplicationName, defaultProperties);
    }

    /**
     * Create a logger with the specified name using default configuration.
     */
    public AppLogger(String name) {
        this(name, defaultApplicationName, defaultProperties);
    }

    /**
     * Create a logger for the specified class with custom configuration.
     */
    public AppLogger(Class<?> clazz, String applicationName, LoggingProperties properties) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.applicationName = applicationName;
        this.properties = properties != null ? properties : new LoggingProperties();
        this.objectMapper = createObjectMapper();
    }

    /**
     * Create a logger with the specified name and custom configuration.
     */
    public AppLogger(String name, String applicationName, LoggingProperties properties) {
        this.logger = LoggerFactory.getLogger(name);
        this.applicationName = applicationName;
        this.properties = properties != null ? properties : new LoggingProperties();
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // ==================== Application Logging ====================

    /**
     * Log a debug message.
     */
    public void debug(String message) {
        if (logger.isDebugEnabled() && isApplicationLoggingEnabled()) {
            logApplication("DEBUG", message, null, null);
        }
    }

    /**
     * Log a debug message with additional fields.
     */
    public void debug(String message, Map<String, Object> extra) {
        if (logger.isDebugEnabled() && isApplicationLoggingEnabled()) {
            logApplication("DEBUG", message, null, extra);
        }
    }

    /**
     * Log an info message.
     */
    public void info(String message) {
        if (logger.isInfoEnabled() && isApplicationLoggingEnabled()) {
            logApplication("INFO", message, null, null);
        }
    }

    /**
     * Log an info message with additional fields.
     */
    public void info(String message, Map<String, Object> extra) {
        if (logger.isInfoEnabled() && isApplicationLoggingEnabled()) {
            logApplication("INFO", message, null, extra);
        }
    }

    /**
     * Log a warning message.
     */
    public void warn(String message) {
        if (logger.isWarnEnabled() && isApplicationLoggingEnabled()) {
            logApplication("WARN", message, null, null);
        }
    }

    /**
     * Log a warning message with a throwable.
     */
    public void warn(String message, Throwable throwable) {
        if (logger.isWarnEnabled() && isApplicationLoggingEnabled()) {
            logApplication("WARN", message, throwable, null);
        }
    }

    /**
     * Log a warning message with additional fields.
     */
    public void warn(String message, Map<String, Object> extra) {
        if (logger.isWarnEnabled() && isApplicationLoggingEnabled()) {
            logApplication("WARN", message, null, extra);
        }
    }

    /**
     * Log an error message.
     */
    public void error(String message) {
        if (logger.isErrorEnabled() && isApplicationLoggingEnabled()) {
            logApplication("ERROR", message, null, null);
        }
    }

    /**
     * Log an error message with a throwable.
     */
    public void error(String message, Throwable throwable) {
        if (logger.isErrorEnabled() && isApplicationLoggingEnabled()) {
            logApplication("ERROR", message, throwable, null);
        }
    }

    /**
     * Log an error message with additional fields.
     */
    public void error(String message, Throwable throwable, Map<String, Object> extra) {
        if (logger.isErrorEnabled() && isApplicationLoggingEnabled()) {
            logApplication("ERROR", message, throwable, extra);
        }
    }

    // ==================== Request Logging ====================

    /**
     * Log a request with full details.
     */
    public void logRequest(String method, String uri, int statusCode, long durationMs,
                          Object requestBody, Object responseBody) {
        if (isRequestLoggingEnabled()) {
            logRequestInternal("INFO", "", method, uri, statusCode, durationMs,
                    null, null, requestBody, responseBody, null);
        }
    }

    /**
     * Log a request with message.
     */
    public void logRequest(String message, String method, String uri, int statusCode, long durationMs,
                          Object requestBody, Object responseBody) {
        if (isRequestLoggingEnabled()) {
            String level = statusCode >= 500 ? "ERROR" : (statusCode >= 400 ? "WARN" : "INFO");
            logRequestInternal(level, message, method, uri, statusCode, durationMs,
                    null, null, requestBody, responseBody, null);
        }
    }

    /**
     * Log an incoming request (when request arrives).
     */
    public void logIncomingRequest(String method, String uri, Object requestBody,
                                   String remoteAddress, String userAgent) {
        if (isRequestLoggingEnabled()) {
            logRequestInternal("INFO", "Incoming request", method, uri, null, null,
                    remoteAddress, userAgent, requestBody, null, null);
        }
    }

    /**
     * Log an incoming response (when response is sent).
     */
    public void logIncomingResponse(String method, String uri, int statusCode,
                                    long durationMs, Object responseBody) {
        if (isRequestLoggingEnabled()) {
            String level = statusCode >= 500 ? "ERROR" : (statusCode >= 400 ? "WARN" : "INFO");
            logRequestInternal(level, "Incoming response", method, uri, statusCode, durationMs,
                    null, null, null, responseBody, null);
        }
    }

    /**
     * Log an outgoing request (call to external service).
     */
    public void logOutgoingRequest(String method, String uri, Object requestBody) {
        if (isRequestLoggingEnabled()) {
            logRequestInternal("INFO", "Outgoing request", method, uri, null, null,
                    null, null, requestBody, null, Map.of("direction", "outgoing"));
        }
    }

    /**
     * Log an outgoing response (response from external service).
     */
    public void logOutgoingResponse(String method, String uri, int statusCode,
                                    long durationMs, Object responseBody) {
        if (isRequestLoggingEnabled()) {
            String level = statusCode >= 500 ? "ERROR" : (statusCode >= 400 ? "WARN" : "INFO");
            logRequestInternal(level, "Outgoing response", method, uri, statusCode, durationMs,
                    null, null, null, responseBody, Map.of("direction", "outgoing"));
        }
    }

    // ==================== Internal Methods ====================

    private void logApplication(String level, String message, Throwable throwable,
                               Map<String, Object> extra) {
        setupMdc();

        LogEntry.LogEntryBuilder builder = LogEntry.builder()
                .timestamp(OffsetDateTime.now().format(TIMESTAMP_FORMATTER))
                .application(applicationName)
                .message(message)
                .loggerName(logger.getName())
                .threadName(Thread.currentThread().getName())
                .level(level)
                .levelValue(LEVEL_VALUES.getOrDefault(level, 20000))
                .type(LogType.APPLICATION.getValue())
                .correlationId(CorrelationContext.getCorrelationId())
                .extra(extra);

        if (throwable != null && properties.getApplication().isIncludeStackTrace()) {
            LogEntry.ErrorInfo errorInfo = LogEntry.ErrorInfo.builder()
                    .exceptionClass(throwable.getClass().getName())
                    .message(throwable.getMessage())
                    .stackTrace(formatStackTrace(throwable))
                    .rootCause(getRootCause(throwable))
                    .build();
            builder.error(errorInfo);
        }

        logJson(level, builder.build(), throwable);
    }

    private void logRequestInternal(String level, String message, String method, String uri,
                                   Integer statusCode, Long durationMs, String remoteAddress,
                                   String userAgent, Object requestBody, Object responseBody,
                                   Map<String, Object> extra) {
        setupMdc();

        LogEntry entry = LogEntry.builder()
                .timestamp(OffsetDateTime.now().format(TIMESTAMP_FORMATTER))
                .application(applicationName)
                .message(message)
                .loggerName(logger.getName())
                .threadName(Thread.currentThread().getName())
                .level(level)
                .levelValue(LEVEL_VALUES.getOrDefault(level, 20000))
                .type(LogType.REQUEST.getValue())
                .correlationId(CorrelationContext.getCorrelationId())
                .method(method != null ? method : "")
                .uri(uri != null ? uri : "")
                .statusCode(statusCode)
                .durationMs(durationMs)
                .remoteAddress(remoteAddress)
                .userAgent(userAgent)
                .requestBody(requestBody != null ? requestBody : Collections.emptyMap())
                .responseBody(responseBody != null ? responseBody : Collections.emptyMap())
                .extra(extra)
                .build();

        logJson(level, entry, null);
    }

    private void setupMdc() {
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, CorrelationContext.getCorrelationId());
        MDC.put("application", applicationName);
    }

    private void logJson(String level, LogEntry entry, Throwable throwable) {
        try {
            String json = objectMapper.writeValueAsString(entry);
            switch (level.toUpperCase()) {
                case "DEBUG" -> logger.debug(json);
                case "WARN" -> {
                    if (throwable != null) {
                        logger.warn(json, throwable);
                    } else {
                        logger.warn(json);
                    }
                }
                case "ERROR" -> {
                    if (throwable != null) {
                        logger.error(json, throwable);
                    } else {
                        logger.error(json);
                    }
                }
                default -> logger.info(json);
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize log entry", e);
        }
    }

    private String[] formatStackTrace(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int depth = Math.min(stackTrace.length, properties.getApplication().getMaxStackTraceDepth());
        String[] result = new String[depth];
        for (int i = 0; i < depth; i++) {
            result[i] = stackTrace[i].toString();
        }
        return result;
    }

    private String getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause != throwable ? rootCause.getClass().getName() + ": " + rootCause.getMessage() : null;
    }

    private boolean isApplicationLoggingEnabled() {
        return properties.isEnabled() && properties.getApplication().isEnabled();
    }

    private boolean isRequestLoggingEnabled() {
        return properties.isEnabled() && properties.getRequest().isEnabled();
    }
}

