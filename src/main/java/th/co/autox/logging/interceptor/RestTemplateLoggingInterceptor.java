package th.co.autox.logging.interceptor;

import th.co.autox.logging.config.LoggingProperties;
import th.co.autox.logging.context.CorrelationContext;
import th.co.autox.logging.core.AppLogger;
import th.co.autox.logging.core.AppLoggerFactory;
import th.co.autox.logging.model.RequestInfo;
import th.co.autox.logging.model.ResponseInfo;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor for logging outgoing RestTemplate HTTP requests and responses.
 */
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final AppLogger log;
    private final LoggingProperties properties;

    public RestTemplateLoggingInterceptor(AppLoggerFactory loggerFactory, LoggingProperties properties) {
        this.log = loggerFactory.getLogger(RestTemplateLoggingInterceptor.class);
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        // Add correlation ID header
        request.getHeaders().add(CorrelationContext.CORRELATION_ID_HEADER,
                CorrelationContext.getCorrelationId());

        if (!properties.isEnabled() || !properties.getRequest().isEnabled()) {
            return execution.execute(request, body);
        }

        long startTime = System.currentTimeMillis();
        String method = request.getMethod().name();
        String uri = request.getURI().toString();

        // Log outgoing request
        logOutgoingRequest(method, uri, body);

        // Execute request
        ClientHttpResponse response = execution.execute(request, body);

        // Log response
        long duration = System.currentTimeMillis() - startTime;
        logOutgoingResponse(method, uri, response, duration);

        return response;
    }

    private void logOutgoingRequest(String method, String uri, byte[] body) {
        RequestInfo.RequestInfoBuilder requestInfoBuilder = RequestInfo.builder();

        if (properties.getRequest().isLogBody() && body != null && body.length > 0) {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            requestInfoBuilder.body(truncateBody(bodyStr));
            requestInfoBuilder.contentLength((long) body.length);
        }

        log.logOutgoingRequest(method, uri, requestInfoBuilder.build());
    }

    private void logOutgoingResponse(String method, String uri, ClientHttpResponse response, long duration) {
        try {
            int statusCode = response.getStatusCode().value();

            ResponseInfo.ResponseInfoBuilder responseInfoBuilder = ResponseInfo.builder();
            responseInfoBuilder.contentType(response.getHeaders().getContentType() != null
                    ? response.getHeaders().getContentType().toString() : null);

            if (properties.getRequest().isLogHeaders()) {
                Map<String, String> headers = new HashMap<>();
                response.getHeaders().forEach((name, values) ->
                        headers.put(name, String.join(", ", values)));
                responseInfoBuilder.headers(headers);
            }

            if (properties.getRequest().isLogResponseBody()) {
                String bodyStr = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                if (!bodyStr.isEmpty()) {
                    responseInfoBuilder.body(truncateBody(bodyStr));
                    responseInfoBuilder.contentLength((long) bodyStr.length());
                }
            }

            log.logOutgoingResponse(method, uri, statusCode, duration, responseInfoBuilder.build());

        } catch (IOException e) {
            log.error("Failed to log outgoing response", e);
        }
    }

    private String truncateBody(String body) {
        int maxSize = properties.getRequest().getMaxBodySize();
        if (body.length() > maxSize) {
            return body.substring(0, maxSize) + "... [TRUNCATED]";
        }
        return body;
    }
}

