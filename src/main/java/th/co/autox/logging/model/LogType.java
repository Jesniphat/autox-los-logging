package th.co.autox.logging.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the type of log entry.
 */
@Getter
@RequiredArgsConstructor
public enum LogType {
    /**
     * Request log type - used for HTTP request/response logging
     * (incoming requests and outgoing calls to other services)
     */
    REQUEST("request"),

    /**
     * Application log type - used for general application logging
     * (debug, info, warn, error messages)
     */
    APPLICATION("application");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}

