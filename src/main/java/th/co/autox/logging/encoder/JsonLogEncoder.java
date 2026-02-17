package th.co.autox.logging.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import th.co.autox.logging.context.CorrelationContext;
import th.co.autox.logging.model.LogType;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Logback encoder that outputs logs in structured JSON format.
 */
public class JsonLogEncoder extends EncoderBase<ILoggingEvent> {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static final Map<String, Integer> LEVEL_VALUES = Map.of(
            "TRACE", 5000,
            "DEBUG", 10000,
            "INFO", 20000,
            "WARN", 30000,
            "ERROR", 40000
    );

    private final ObjectMapper objectMapper;

    @Setter
    private String applicationName = "application";

    public JsonLogEncoder() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        Map<String, Object> logEntry = new LinkedHashMap<>();

        // Timestamp
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(event.getTimeStamp()),
                ZoneId.systemDefault()
        );
        logEntry.put("@timestamp", timestamp.format(TIMESTAMP_FORMATTER));
        logEntry.put("@version", "1");

        // Application info
        String appName = event.getMDCPropertyMap().getOrDefault("application", applicationName);
        logEntry.put("application", appName);

        // Message
        logEntry.put("message", event.getFormattedMessage());

        // Logger info
        logEntry.put("logger_name", event.getLoggerName());
        logEntry.put("thread_name", event.getThreadName());

        // Level
        String level = event.getLevel().toString();
        logEntry.put("level", level);
        logEntry.put("level_value", LEVEL_VALUES.getOrDefault(level, 20000));

        // Type (from MDC or default to application)
        String type = event.getMDCPropertyMap().getOrDefault("type", LogType.APPLICATION.getValue());
        logEntry.put("type", type);

        // Correlation ID
        String correlationId = event.getMDCPropertyMap().get(CorrelationContext.CORRELATION_ID_MDC_KEY);
        logEntry.put("correlation_id", correlationId != null ? correlationId : "");

        // HTTP fields (empty for standard logs)
        logEntry.put("method", "");
        logEntry.put("uri", "");

        // Request and response body (empty for standard encoder - these are set by AppLogger)
        logEntry.put("request_body", Collections.emptyMap());
        logEntry.put("response_body", Collections.emptyMap());

        // Add exception info if present
        if (event.getThrowableProxy() != null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("class", event.getThrowableProxy().getClassName());
            error.put("message", event.getThrowableProxy().getMessage());
            logEntry.put("error", error);
        }

        try {
            String json = objectMapper.writeValueAsString(logEntry);
            return (json + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            return ("Failed to encode log: " + e.getMessage() + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }
}

