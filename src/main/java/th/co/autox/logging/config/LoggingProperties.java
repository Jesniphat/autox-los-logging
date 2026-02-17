package th.co.autox.logging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for structured logging.
 */
@Data
@ConfigurationProperties(prefix = "logging.structured")
public class LoggingProperties {

    /**
     * Enable or disable the logging library.
     */
    private boolean enabled = true;

    /**
     * Application name to be included in logs.
     */
    private String applicationName;

    /**
     * Configuration for request logging.
     */
    private RequestLoggingConfig request = new RequestLoggingConfig();

    /**
     * Configuration for application logging.
     */
    private ApplicationLoggingConfig application = new ApplicationLoggingConfig();

    /**
     * Headers to mask in logs (for security).
     */
    private List<String> maskedHeaders = new ArrayList<>(List.of(
            "Authorization",
            "X-Api-Key",
            "Cookie",
            "Set-Cookie"
    ));

    /**
     * Fields to mask in request/response body.
     */
    private List<String> maskedFields = new ArrayList<>(List.of(
            "password",
            "secret",
            "token",
            "creditCard",
            "ssn"
    ));

    /**
     * Mask value to use for sensitive data.
     */
    private String maskValue = "***MASKED***";

    /**
     * Request logging configuration.
     */
    @Data
    public static class RequestLoggingConfig {

        /**
         * Enable request logging.
         */
        private boolean enabled = true;

        /**
         * Log request headers.
         */
        private boolean logHeaders = true;

        /**
         * Log request body.
         */
        private boolean logBody = true;

        /**
         * Log response body.
         */
        private boolean logResponseBody = true;

        /**
         * Maximum body size to log (in bytes).
         */
        private int maxBodySize = 10240;

        /**
         * URL patterns to exclude from logging.
         */
        private List<String> excludePatterns = new ArrayList<>(List.of(
                "/actuator/**",
                "/health/**",
                "/favicon.ico"
        ));

        /**
         * URL patterns to include for logging (if empty, all are included).
         */
        private List<String> includePatterns = new ArrayList<>();
    }

    /**
     * Application logging configuration.
     */
    @Data
    public static class ApplicationLoggingConfig {

        /**
         * Enable application logging.
         */
        private boolean enabled = true;

        /**
         * Include stack trace in error logs.
         */
        private boolean includeStackTrace = true;

        /**
         * Maximum stack trace depth.
         */
        private int maxStackTraceDepth = 50;
    }
}

