package th.co.autox.logging;

import th.co.autox.logging.config.LoggingProperties;
import th.co.autox.logging.core.AppLogger;
import th.co.autox.logging.core.AppLoggerFactory;
import th.co.autox.logging.model.RequestInfo;
import th.co.autox.logging.model.ResponseInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for AppLogger.
 */
class AppLoggerTest {

    private AppLogger logger;
    private LoggingProperties properties;

    @BeforeEach
    void setUp() {
        properties = new LoggingProperties();
        AppLoggerFactory factory = new AppLoggerFactory("test-app", properties);
        logger = factory.getLogger(AppLoggerTest.class);
    }

    @Test
    void loggerIsCreated() {
        assertThat(logger).isNotNull();
        assertThat(logger.getApplicationName()).isEqualTo("test-app");
    }

    @Test
    void loggerCachesInstances() {
        AppLoggerFactory factory = new AppLoggerFactory("test-app", properties);
        AppLogger logger1 = factory.getLogger(AppLoggerTest.class);
        AppLogger logger2 = factory.getLogger(AppLoggerTest.class);

        assertThat(logger1).isSameAs(logger2);
    }

    @Test
    void infoLogDoesNotThrow() {
        logger.info("Test message");
        logger.info("Test with fields", Map.of("key", "value"));
    }

    @Test
    void debugLogDoesNotThrow() {
        logger.debug("Debug message");
        logger.debug("Debug with fields", Map.of("key", "value"));
    }

    @Test
    void warnLogDoesNotThrow() {
        logger.warn("Warning message");
        logger.warn("Warning with exception", new RuntimeException("Test"));
    }

    @Test
    void errorLogDoesNotThrow() {
        logger.error("Error message");
        logger.error("Error with exception", new RuntimeException("Test"));
        logger.error("Error with all", new RuntimeException("Test"), Map.of("key", "value"));
    }

    @Test
    void requestLogDoesNotThrow() {
        // Test logRequest with method, uri, status code and duration
        logger.logRequest("GET", "/api/test", 200, 100L,
                Map.of("data", "test"),
                Map.of("result", "success"));

        // Test logRequest with message
        logger.logRequest("Test request", "GET", "/api/test", 200, 100L,
                Map.of("data", "test"),
                Map.of("result", "success"));

        // Test incoming request with RequestInfo
        RequestInfo requestInfo = RequestInfo.builder()
                .headers(Map.of("Content-Type", "application/json"))
                .body(Map.of("data", "test"))
                .build();
        logger.logIncomingRequest("GET", "/api/test", requestInfo, "127.0.0.1", "Mozilla/5.0");

        // Test incoming response with ResponseInfo
        ResponseInfo responseInfo = ResponseInfo.builder()
                .headers(Map.of("Content-Type", "application/json"))
                .body(Map.of("result", "success"))
                .build();
        logger.logIncomingResponse("GET", "/api/test", 200, 100, responseInfo);

        // Test outgoing request
        logger.logOutgoingRequest("POST", "http://external.com/api", requestInfo);

        // Test outgoing response
        logger.logOutgoingResponse("POST", "http://external.com/api", 201, 50, responseInfo);
    }

    @Test
    void simpleConstructorWorks() {
        // Test that simple constructor works
        AppLogger simpleLogger = new AppLogger(AppLoggerTest.class);
        assertThat(simpleLogger).isNotNull();
        simpleLogger.info("Test from simple constructor");
    }
}

