package th.co.autox.logging;

import th.co.autox.logging.autoconfigure.LoggingAutoConfiguration;
import th.co.autox.logging.config.LoggingProperties;
import th.co.autox.logging.core.AppLogger;
import th.co.autox.logging.core.AppLoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for LoggingAutoConfiguration.
 */
class LoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

    @Test
    void autoConfigurationLoadsSuccessfully() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AppLoggerFactory.class);
            assertThat(context).hasSingleBean(LoggingProperties.class);
        });
    }

    @Test
    void loggerFactoryCreatesLogger() {
        contextRunner
                .withPropertyValues("spring.application.name=test-app")
                .run(context -> {
                    AppLoggerFactory factory = context.getBean(AppLoggerFactory.class);
                    AppLogger logger = factory.getLogger(LoggingAutoConfigurationTest.class);

                    assertThat(logger).isNotNull();
                    assertThat(logger.getApplicationName()).isEqualTo("test-app");
                });
    }

    @Test
    void disabledWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("logging.structured.enabled=false")
                .run(context -> {
                    // Auto-configuration should not run when disabled
                    assertThat(context).doesNotHaveBean(AppLoggerFactory.class);
                });
    }

    @Test
    void customApplicationNameOverridesSpringApplicationName() {
        contextRunner
                .withPropertyValues(
                        "spring.application.name=spring-name",
                        "logging.structured.application-name=custom-name"
                )
                .run(context -> {
                    AppLoggerFactory factory = context.getBean(AppLoggerFactory.class);
                    assertThat(factory.getApplicationName()).isEqualTo("custom-name");
                });
    }

    @Test
    void propertiesAreConfigurable() {
        contextRunner
                .withPropertyValues(
                        "logging.structured.request.enabled=false",
                        "logging.structured.request.max-body-size=5000",
                        "logging.structured.masked-headers=Custom-Header",
                        "logging.structured.mask-value=HIDDEN"
                )
                .run(context -> {
                    LoggingProperties properties = context.getBean(LoggingProperties.class);

                    assertThat(properties.getRequest().isEnabled()).isFalse();
                    assertThat(properties.getRequest().getMaxBodySize()).isEqualTo(5000);
                    assertThat(properties.getMaskedHeaders()).contains("Custom-Header");
                    assertThat(properties.getMaskValue()).isEqualTo("HIDDEN");
                });
    }
}

