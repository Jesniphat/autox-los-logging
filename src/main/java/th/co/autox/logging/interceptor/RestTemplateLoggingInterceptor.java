package th.co.autox.logging.interceptor;

}
    }
        return body;
        }
            return body.substring(0, maxSize) + "... [TRUNCATED]";
        if (body.length() > maxSize) {
        int maxSize = properties.getRequest().getMaxBodySize();
    private String truncateBody(String body) {

    }
        }
            log.error("Failed to log outgoing response", e);
        } catch (IOException e) {

            log.logOutgoingResponse(method, uri, statusCode, duration, responseInfoBuilder.build());

            }
                }
                    responseInfoBuilder.contentLength((long) bodyStr.length());
                    responseInfoBuilder.body(truncateBody(bodyStr));
                if (!bodyStr.isEmpty()) {
                String bodyStr = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            if (properties.getRequest().isLogResponseBody()) {

            }
                responseInfoBuilder.headers(headers);
                        headers.put(name, String.join(", ", values)));
                response.getHeaders().forEach((name, values) ->
                Map<String, String> headers = new HashMap<>();
            if (properties.getRequest().isLogHeaders()) {

                    ? response.getHeaders().getContentType().toString() : null);
            responseInfoBuilder.contentType(response.getHeaders().getContentType() != null
            ResponseInfo.ResponseInfoBuilder responseInfoBuilder = ResponseInfo.builder();

            int statusCode = response.getStatusCode().value();
        try {
    private void logOutgoingResponse(String method, String uri, ClientHttpResponse response, long duration) {

    }
        log.logOutgoingRequest(method, uri, requestInfoBuilder.build());

        }
            requestInfoBuilder.contentLength((long) body.length);
            requestInfoBuilder.body(truncateBody(bodyStr));
            String bodyStr = new String(body, StandardCharsets.UTF_8);
        if (properties.getRequest().isLogBody() && body != null && body.length > 0) {

        RequestInfo.RequestInfoBuilder requestInfoBuilder = RequestInfo.builder();
    private void logOutgoingRequest(String method, String uri, byte[] body) {

    }
        return response;

        logOutgoingResponse(method, uri, response, duration);
        long duration = System.currentTimeMillis() - startTime;
        // Log response

        ClientHttpResponse response = execution.execute(request, body);
        // Execute request

        logOutgoingRequest(method, uri, body);
        // Log outgoing request

        String uri = request.getURI().toString();
        String method = request.getMethod().name();
        long startTime = System.currentTimeMillis();

        }
            return execution.execute(request, body);
        if (!properties.isEnabled() || !properties.getRequest().isEnabled()) {

                CorrelationContext.getCorrelationId());
        request.getHeaders().add(CorrelationContext.CORRELATION_ID_HEADER,
        // Add correlation ID header

                                        ClientHttpRequestExecution execution) throws IOException {
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
    @Override

    }
        this.properties = properties;
        this.log = loggerFactory.getLogger(RestTemplateLoggingInterceptor.class);
    public RestTemplateLoggingInterceptor(AppLoggerFactory loggerFactory, LoggingProperties properties) {

    private final LoggingProperties properties;
    private final AppLogger log;

public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {
@RequiredArgsConstructor
 */
 * Interceptor for logging outgoing RestTemplate HTTP requests and responses.
/**

import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import org.springframework.util.StreamUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.HttpRequest;
import lombok.RequiredArgsConstructor;
import th.co.autox.logging.model.ResponseInfo;
import th.co.autox.logging.model.RequestInfo;
import th.co.autox.logging.core.AppLoggerFactory;
import th.co.autox.logging.core.AppLogger;
import th.co.autox.logging.context.CorrelationContext;
import th.co.autox.logging.config.LoggingProperties;

