package th.co.autox.logging.util;

import th.co.autox.logging.config.LoggingProperties;
import th.co.autox.logging.core.AppLoggerFactory;
import th.co.autox.logging.interceptor.RestTemplateLoggingInterceptor;
import th.co.autox.logging.interceptor.WebClientLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for easily configuring logging on HTTP clients.
 */
@RequiredArgsConstructor
public class LoggingClientCustomizer {

    private final AppLoggerFactory loggerFactory;
    private final LoggingProperties properties;

    /**
     * Add logging interceptor to a RestTemplate.
     */
    public RestTemplate customize(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(new RestTemplateLoggingInterceptor(loggerFactory, properties));
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    /**
     * Create a new RestTemplate with logging enabled.
     */
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return customize(restTemplate);
    }

    /**
     * Add logging filter to a WebClient.Builder.
     */
    public WebClient.Builder customize(WebClient.Builder builder) {
        return builder.filter(new WebClientLoggingFilter(loggerFactory, properties));
    }

    /**
     * Create a new WebClient with logging enabled.
     */
    public WebClient createWebClient() {
        return customize(WebClient.builder()).build();
    }

    /**
     * Create a new WebClient with logging and base URL.
     */
    public WebClient createWebClient(String baseUrl) {
        return customize(WebClient.builder())
                .baseUrl(baseUrl)
                .build();
    }
}

