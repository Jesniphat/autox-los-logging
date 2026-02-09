# JNP Spring Logs - Starter

This module provides the main Spring Boot Starter functionality for structured JSON logging.

## Package Structure

```
com.jnp.logging
├── autoconfigure/          # Spring Boot Auto-configuration
├── config/                 # Configuration properties
├── context/                # Correlation ID and log context
├── core/                   # Core logger classes
├── encoder/                # Logback JSON encoder
├── filter/                 # Servlet filters
├── interceptor/            # HTTP client interceptors
├── model/                  # Log entry models
└── util/                   # Utility classes
```

## Main Components

### JnpLoggerFactory
Factory for creating `JnpLogger` instances. Inject this bean to get loggers.

### JnpLogger
Main logger class with methods for both application and request logging.

### RequestLoggingFilter
Servlet filter that automatically logs incoming HTTP requests/responses.

### RestTemplateLoggingInterceptor
Interceptor for logging outgoing RestTemplate calls.

### WebClientLoggingFilter
Exchange filter for logging outgoing WebClient calls.

### CorrelationContext
ThreadLocal-based correlation ID management.

### JnpJsonEncoder
Custom Logback encoder for JSON output format.
