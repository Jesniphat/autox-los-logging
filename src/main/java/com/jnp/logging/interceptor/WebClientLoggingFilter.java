package com.jnp.logging.interceptor;

import com.jnp.logging.config.LoggingProperties;
import com.jnp.logging.context.CorrelationContext;
import com.jnp.logging.core.AppLogger;
import com.jnp.logging.core.AppLoggerFactory;
import com.jnp.logging.model.ResponseInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Exchange filter function for logging WebClient HTTP requests and responses.
 */
@RequiredArgsConstructor
public class WebClientLoggingFilter implements ExchangeFilterFunction {

    private final AppLogger log;
    private final LoggingProperties properties;

    public WebClientLoggingFilter(AppLoggerFactory loggerFactory, LoggingProperties properties) {
        this.log = loggerFactory.getLogger(WebClientLoggingFilter.class);
        this.properties = properties;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (!properties.isEnabled() || !properties.getRequest().isEnabled()) {
            return next.exchange(addCorrelationHeader(request));
        }

        long startTime = System.currentTimeMillis();
        String correlationId = CorrelationContext.getCorrelationId();
        String method = request.method().name();
        String uri = request.url().toString();

        // Log outgoing request
        log.logOutgoingRequest(method, uri, null);

        // Add correlation ID and execute
        return next.exchange(addCorrelationHeader(request))
                .doOnNext(response -> {
                    // Restore correlation context in reactive chain
                    CorrelationContext.setCorrelationId(correlationId);
                    long duration = System.currentTimeMillis() - startTime;
                    logOutgoingResponse(method, uri, response, duration);
                })
                .doOnError(error -> {
                    CorrelationContext.setCorrelationId(correlationId);
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Outgoing request failed: " + method + " " + uri + " after " + duration + "ms", error);
                });
    }

    private ClientRequest addCorrelationHeader(ClientRequest request) {
        return ClientRequest.from(request)
                .header(CorrelationContext.CORRELATION_ID_HEADER, CorrelationContext.getCorrelationId())
                .build();
    }

    private void logOutgoingResponse(String method, String uri, ClientResponse response, long duration) {
        int statusCode = response.statusCode().value();

        ResponseInfo.ResponseInfoBuilder responseInfoBuilder = ResponseInfo.builder();

        if (properties.getRequest().isLogHeaders()) {
            Map<String, String> headers = new HashMap<>();
            response.headers().asHttpHeaders().forEach((name, values) -> 
                    headers.put(name, String.join(", ", values)));
            responseInfoBuilder.headers(headers);
        }

        log.logOutgoingResponse(method, uri, statusCode, duration, responseInfoBuilder.build());
    }

    /**
     * Create an ExchangeFilterFunction for use with WebClient.
     */
    public static ExchangeFilterFunction create(AppLoggerFactory loggerFactory, LoggingProperties properties) {
        return new WebClientLoggingFilter(loggerFactory, properties);
    }
}
