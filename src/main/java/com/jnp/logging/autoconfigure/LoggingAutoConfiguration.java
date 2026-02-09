package com.jnp.logging.autoconfigure;

import com.jnp.logging.config.LoggingProperties;
import com.jnp.logging.core.AppLogger;
import com.jnp.logging.core.AppLoggerFactory;
import com.jnp.logging.filter.RequestLoggingFilter;
import com.jnp.logging.interceptor.RestTemplateLoggingInterceptor;
import com.jnp.logging.interceptor.WebClientLoggingFilter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configuration for structured logging.
 */
@AutoConfiguration
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "logging.structured", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

    private final String applicationName;
    private final LoggingProperties properties;

    public LoggingAutoConfiguration(
            @Value("${spring.application.name:application}") String applicationName,
            LoggingProperties properties) {
        this.applicationName = applicationName;
        this.properties = properties;
    }

    /**
     * Configure AppLogger defaults for simple constructor usage.
     * This allows: new AppLogger(MyService.class)
     */
    @PostConstruct
    public void configureDefaults() {
        String effectiveName = (properties.getApplicationName() != null && !properties.getApplicationName().isBlank())
                ? properties.getApplicationName()
                : applicationName;
        AppLogger.configureDefaults(effectiveName, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AppLoggerFactory appLoggerFactory() {
        return new AppLoggerFactory(applicationName, properties);
    }

    /**
     * Servlet-based web application configuration.
     */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "logging.structured.request", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class ServletLoggingConfiguration {

        @Bean
        public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter(
                AppLoggerFactory loggerFactory,
                LoggingProperties properties) {
            FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new RequestLoggingFilter(loggerFactory, properties));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
            registration.addUrlPatterns("/*");
            registration.setName("requestLoggingFilter");
            return registration;
        }
    }

    /**
     * RestTemplate interceptor configuration.
     */
    @Configuration
    @ConditionalOnClass(RestTemplate.class)
    public static class RestTemplateLoggingConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public RestTemplateLoggingInterceptor restTemplateLoggingInterceptor(
                AppLoggerFactory loggerFactory,
                LoggingProperties properties) {
            return new RestTemplateLoggingInterceptor(loggerFactory, properties);
        }
    }

    /**
     * WebClient filter configuration.
     */
    @Configuration
    @ConditionalOnClass(WebClient.class)
    public static class WebClientLoggingConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public WebClientLoggingFilter webClientLoggingFilter(
                AppLoggerFactory loggerFactory,
                LoggingProperties properties) {
            return new WebClientLoggingFilter(loggerFactory, properties);
        }
    }
}
