package com.jnp.logging.filter;

import com.jnp.logging.config.LoggingProperties;
import com.jnp.logging.context.CorrelationContext;
import com.jnp.logging.core.AppLogger;
import com.jnp.logging.core.AppLoggerFactory;
import com.jnp.logging.model.RequestInfo;
import com.jnp.logging.model.ResponseInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter that logs incoming HTTP requests and responses.
 * Also manages correlation ID for request tracing.
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final AppLogger log;
    private final LoggingProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RequestLoggingFilter(AppLoggerFactory loggerFactory, LoggingProperties properties) {
        this.log = loggerFactory.getLogger(RequestLoggingFilter.class);
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Setup correlation ID
        String correlationId = request.getHeader(CorrelationContext.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationContext.generateCorrelationId();
        }
        CorrelationContext.setCorrelationId(correlationId);
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, correlationId);

        // Add correlation ID to response header
        response.setHeader(CorrelationContext.CORRELATION_ID_HEADER, correlationId);

        // Check if request should be logged
        if (!shouldLog(request)) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                cleanup();
            }
            return;
        }

        // Wrap request and response to capture body
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logIncomingRequest(wrappedRequest);

            // Process request
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // Log response
            long duration = System.currentTimeMillis() - startTime;
            logIncomingResponse(wrappedRequest, wrappedResponse, duration);

        } finally {
            // Copy response body to actual response
            wrappedResponse.copyBodyToResponse();
            cleanup();
        }
    }

    private void logIncomingRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        
        if (queryString != null) {
            uri += "?" + queryString;
        }

        RequestInfo.RequestInfoBuilder requestInfoBuilder = RequestInfo.builder();

        if (properties.getRequest().isLogHeaders()) {
            requestInfoBuilder.headers(maskHeaders(getHeaders(request)));
        }

        if (properties.getRequest().isLogBody()) {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                requestInfoBuilder.body(truncateBody(body));
            }
        }
        
        requestInfoBuilder.contentType(request.getContentType());
        if (request.getContentLengthLong() > 0) {
            requestInfoBuilder.contentLength(request.getContentLengthLong());
        }

        log.logIncomingRequest(method, uri, requestInfoBuilder.build(), 
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    private void logIncomingResponse(ContentCachingRequestWrapper request,
                                     ContentCachingResponseWrapper response,
                                     long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int statusCode = response.getStatus();

        ResponseInfo.ResponseInfoBuilder responseInfoBuilder = ResponseInfo.builder();

        if (properties.getRequest().isLogHeaders()) {
            responseInfoBuilder.headers(getResponseHeaders(response));
        }

        if (properties.getRequest().isLogResponseBody()) {
            String body = getResponseBody(response);
            if (body != null && !body.isEmpty()) {
                responseInfoBuilder.body(truncateBody(body));
            }
        }
        
        responseInfoBuilder.contentType(response.getContentType());
        if (response.getContentSize() > 0) {
            responseInfoBuilder.contentLength((long) response.getContentSize());
        }

        log.logIncomingResponse(method, uri, statusCode, duration, responseInfoBuilder.build());
    }

    private boolean shouldLog(HttpServletRequest request) {
        if (!properties.isEnabled() || !properties.getRequest().isEnabled()) {
            return false;
        }

        String uri = request.getRequestURI();

        // Check exclude patterns
        for (String pattern : properties.getRequest().getExcludePatterns()) {
            if (pathMatcher.match(pattern, uri)) {
                return false;
            }
        }

        // Check include patterns (if specified)
        if (!properties.getRequest().getIncludePatterns().isEmpty()) {
            for (String pattern : properties.getRequest().getIncludePatterns()) {
                if (pathMatcher.match(pattern, uri)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Collections.list(request.getHeaderNames())
                .forEach(name -> headers.put(name, request.getHeader(name)));
        return headers;
    }

    private Map<String, String> getResponseHeaders(ContentCachingResponseWrapper response) {
        Map<String, String> headers = new HashMap<>();
        response.getHeaderNames()
                .forEach(name -> headers.put(name, response.getHeader(name)));
        return headers;
    }

    private Map<String, String> maskHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> shouldMaskHeader(e.getKey()) ? properties.getMaskValue() : e.getValue()
                ));
    }

    private boolean shouldMaskHeader(String headerName) {
        return properties.getMaskedHeaders().stream()
                .anyMatch(masked -> masked.equalsIgnoreCase(headerName));
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String truncateBody(String body) {
        int maxSize = properties.getRequest().getMaxBodySize();
        if (body.length() > maxSize) {
            return body.substring(0, maxSize) + "... [TRUNCATED]";
        }
        return body;
    }

    private void cleanup() {
        CorrelationContext.clear();
        MDC.clear();
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
