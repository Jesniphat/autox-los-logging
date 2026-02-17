package th.co.autox.logging.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Model representing a structured log entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "@timestamp", "@version", "application", "message", "logger_name", "thread_name",
    "level", "level_value", "type", "correlation_id", "method", "uri", "status_code",
    "duration_ms", "remote_address", "user_agent", "request_body", "response_body", "error"
})
public class LogEntry {

    @JsonProperty("@timestamp")
    private String timestamp;

    @JsonProperty("@version")
    @Builder.Default
    private String version = "1";

    @JsonProperty("application")
    private String application;

    @JsonProperty("message")
    @Builder.Default
    private String message = "";

    @JsonProperty("logger_name")
    private String loggerName;

    @JsonProperty("thread_name")
    private String threadName;

    @JsonProperty("level")
    private String level;

    @JsonProperty("level_value")
    private Integer levelValue;

    @JsonProperty("type")
    private String type;

    @JsonProperty("correlation_id")
    private String correlationId;

    // HTTP-specific fields (moved out from body)
    @JsonProperty("method")
    @Builder.Default
    private String method = "";

    @JsonProperty("uri")
    @Builder.Default
    private String uri = "";

    @JsonProperty("status_code")
    private Integer statusCode;

    @JsonProperty("duration_ms")
    private Long durationMs;

    @JsonProperty("remote_address")
    private String remoteAddress;

    @JsonProperty("user_agent")
    private String userAgent;

    // Request and response body
    @JsonProperty("request_body")
    @Builder.Default
    private Object requestBody = Collections.emptyMap();

    @JsonProperty("response_body")
    @Builder.Default
    private Object responseBody = Collections.emptyMap();

    // Error information
    @JsonProperty("error")
    private ErrorInfo error;

    // Additional custom fields
    @JsonProperty("extra")
    private Map<String, Object> extra;

    /**
     * Helper method to set type from LogType enum.
     */
    public void setType(LogType logType) {
        this.type = logType.getValue();
    }

    /**
     * Error information for exception logging.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        @JsonProperty("class")
        private String exceptionClass;

        @JsonProperty("message")
        private String message;

        @JsonProperty("stack_trace")
        private String[] stackTrace;

        @JsonProperty("root_cause")
        private String rootCause;
    }
}

