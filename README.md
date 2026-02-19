# AutoX LOS Logging

A Spring Boot Starter library for structured JSON logging with two main log types: **request** and **application**.

## Features

- 📝 **Structured JSON Logging** - All logs are output in a consistent JSON format
- 🔗 **Correlation ID** - Automatic correlation ID propagation across services
- 📥 **Request Logging** - Automatic logging of incoming HTTP requests and responses
- 📤 **Outgoing Request Logging** - Log calls to external services via RestTemplate/WebClient
- 🔒 **Security** - Automatic masking of sensitive headers and fields
- ⚙️ **Configurable** - Extensive configuration options via application properties
- 🚀 **Auto-configuration** - Works out of the box with Spring Boot

## Installation

### Local Development

1. **Publish the library to Maven Local** (ที่โปรเจกต์ logging-libs):

```bash
./gradlew publishToMavenLocal
```

Artifact จะถูก publish ไปที่ `~/.m2/repository/th/co/autox/logging-libs/1.0.0/`

2. **เพิ่ม `mavenLocal()` ใน `build.gradle` ของ microservice**:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'th.co.autox:logging-libs:1.0.0'
}
```

> **Note:** ทุกครั้งที่แก้โค้ดใน logging-libs ต้องรัน `./gradlew publishToMavenLocal` ใหม่

### Production

สำหรับ production ควรใช้ **Private Maven Repository** (เช่น JFrog Artifactory, Sonatype Nexus, GitHub Packages) แทน `mavenLocal()`

1. **Publish library ไปที่ private repository** (ตั้งค่าใน CI/CD pipeline):

```bash
./gradlew publish
```

2. **เพิ่ม repository URL ใน `build.gradle` ของ microservice**:

```groovy
repositories {
    maven { url 'https://your-company-repo.example.com/maven-releases' }
    mavenCentral()
}

dependencies {
    implementation 'th.co.autox:logging-libs:1.0.0'
}
```

> **Important:** ไม่ควรใช้ `mavenLocal()` บน production เพราะไม่มี checksum verification และทำให้ build ไม่ reproducible

### Dependency Snippet Reference

#### Gradle

```groovy
implementation 'th.co.autox:logging-libs:1.0.0'
```

#### Maven

```xml
<dependency>
    <groupId>th.co.autox</groupId>
    <artifactId>logging-libs</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Add the dependency

Add the library to your project as shown above.

### 2. Configure your application

```yaml
# application.yml
spring:
  application:
    name: my-service

logging:
  structured:
    enabled: true
    request:
      enabled: true
      log-headers: true
      log-body: true
```

### 3. Use the logger in your code

```java
import th.co.autox.logging.core.AppLogger;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    // Simple usage - no injection needed!
    private final AppLogger log = new AppLogger(MyService.class);

    public void doSomething() {
        // Application logs
        log.info("Processing started");
        log.debug("Debug information");
        log.warn("Warning message");
        log.error("Error occurred", exception);
        
        // With additional fields
        log.info("User action", Map.of(
            "userId", "12345",
            "action", "LOGIN"
        ));
    }
}
```

#### Alternative: Using Factory Injection

```java
import th.co.autox.logging.core.AppLogger;
import th.co.autox.logging.core.AppLoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    private final AppLogger log;

    public MyService(AppLoggerFactory loggerFactory) {
        this.log = loggerFactory.getLogger(MyService.class);
    }

    public void doSomething() {
        log.info("Processing started");
    }
}
```

## Log Types

### 1. Request Logs (`type: "request"`)

Used for logging HTTP request/response information:

```json
{
    "@timestamp": "2026-02-09T10:15:30.123+07:00",
    "@version": "1",
    "application": "my-service",
    "message": "Incoming request: POST /api/users",
    "logger_name": "th.co.autox.logging.filter.RequestLoggingFilter",
    "thread_name": "http-nio-8080-exec-1",
    "level": "INFO",
    "level_value": 20000,
    "type": "request",
    "correlation_id": "abc123xyz",
    "method": "POST",
    "uri": "/api/users",
    "status_code": 200,
    "duration_ms": 150,
    "remote_address": "127.0.0.1",
    "user_agent": "Mozilla/5.0",
    "request_body": {
        "headers": {...},
        "body": {...}
    },
    "response_body": {
        "headers": {...},
        "body": {...}
    }
}
```

### 2. Application Logs (`type: "application"`)

Used for general application logging:

```json
{
    "@timestamp": "2026-02-09T10:15:30.123+07:00",
    "@version": "1",
    "application": "my-service",
    "message": "User login successful",
    "logger_name": "com.example.service.UserService",
    "thread_name": "http-nio-8080-exec-1",
    "level": "INFO",
    "level_value": 20000,
    "type": "application",
    "correlation_id": "abc123xyz",
    "method": "",
    "uri": "",
    "request_body": {},
    "response_body": {}
}
```

## Configuration

### Full Configuration Example

```yaml
logging:
  structured:
    enabled: true
    application-name: my-service  # Override spring.application.name
    
    # Request logging settings
    request:
      enabled: true
      log-headers: true
      log-body: true
      log-response-body: true
      max-body-size: 10240  # 10KB
      exclude-patterns:
        - /actuator/**
        - /health/**
        - /favicon.ico
      include-patterns: []  # Empty = include all
    
    # Application logging settings
    application:
      enabled: true
      include-stack-trace: true
      max-stack-trace-depth: 50
    
    # Security - mask sensitive data
    masked-headers:
      - Authorization
      - X-Api-Key
      - Cookie
    masked-fields:
      - password
      - secret
      - token
      - creditCard
    mask-value: "***MASKED***"
```

### Configuration Properties

| Property | Type | Default | Description |
| -------- | ---- | ------- | ----------- |
| `logging.structured.enabled` | Boolean | `true` | Enable/disable the logging library |
| `logging.structured.application-name` | String | spring.application.name | Application name in logs |
| `logging.structured.request.enabled` | Boolean | `true` | Enable request logging |
| `logging.structured.request.log-headers` | Boolean | `true` | Log request/response headers |
| `logging.structured.request.log-body` | Boolean | `true` | Log request body |
| `logging.structured.request.log-response-body` | Boolean | `true` | Log response body |
| `logging.structured.request.max-body-size` | Integer | `10240` | Max body size to log (bytes) |
| `logging.structured.request.exclude-patterns` | List | actuator, health | URL patterns to exclude |
| `logging.structured.application.enabled` | Boolean | `true` | Enable application logging |
| `logging.structured.application.include-stack-trace` | Boolean | `true` | Include stack traces |
| `logging.structured.masked-headers` | List | Auth headers | Headers to mask |
| `logging.structured.masked-fields` | List | password, etc. | Fields to mask in body |

## Logging Outgoing Requests

### RestTemplate

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateLoggingInterceptor interceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(interceptor);
        return restTemplate;
    }
}
```

Or use the customizer:

```java
@Service
public class MyService {

    private final RestTemplate restTemplate;

    public MyService(LoggingClientCustomizer customizer) {
        this.restTemplate = customizer.createRestTemplate();
    }
}
```

### WebClient

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClientLoggingFilter loggingFilter) {
        return WebClient.builder()
                .filter(loggingFilter)
                .build();
    }
}
```

Or use the customizer:

```java
@Service
public class MyService {

    private final WebClient webClient;

    public MyService(LoggingClientCustomizer customizer) {
        this.webClient = customizer.createWebClient("https://api.example.com");
    }
}
```

## Correlation ID

The library automatically propagates correlation IDs:

1. **Incoming requests**: Reads `X-Correlation-ID` header or generates a new one
2. **Outgoing requests**: Automatically adds `X-Correlation-ID` header
3. **All logs**: Include the correlation ID for request tracing

### Manual Correlation ID Management

```java
import th.co.autox.logging.context.CorrelationContext;

// Get current correlation ID
String correlationId = CorrelationContext.getCorrelationId();

// Set a custom correlation ID
CorrelationContext.setCorrelationId("my-custom-id");

// Generate a new correlation ID
String newId = CorrelationContext.generateCorrelationId();
```

## Logback Configuration

Include the provided Logback configuration in your `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="logback-structured.xml"/>
</configuration>
```

Or create a custom configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name"/>

    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="th.co.autox.logging.encoder.JsonLogEncoder">
            <applicationName>${APP_NAME}</applicationName>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
```

## Examples

### Complete Service Example

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final AppLogger log;
    private final OrderService orderService;

    public OrderController(AppLoggerFactory loggerFactory, OrderService orderService) {
        this.log = loggerFactory.getLogger(OrderController.class);
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Creating new order", Map.of("customerId", request.getCustomerId()));
        
        try {
            Order order = orderService.create(request);
            log.info("Order created successfully", Map.of(
                "orderId", order.getId(),
                "total", order.getTotal()
            ));
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Failed to create order", e, Map.of(
                "customerId", request.getCustomerId()
            ));
            throw e;
        }
    }
}
```

### Manual Request Logging

```java
@Service
public class PaymentService {

    private final AppLogger log;
    private final RestTemplate restTemplate;

    public PaymentService(AppLoggerFactory loggerFactory, RestTemplate restTemplate) {
        this.log = loggerFactory.getLogger(PaymentService.class);
        this.restTemplate = restTemplate;
    }

    public PaymentResult processPayment(PaymentRequest request) {
        log.logRequest("Processing payment", request, null);
        
        PaymentResult result = restTemplate.postForObject(
            "https://payment.example.com/process",
            request,
            PaymentResult.class
        );
        
        log.logRequest("Payment processed", request, result);
        return result;
    }
}
```

## Building

```bash
# Build the library
./gradlew clean build

# Skip tests
./gradlew clean build -x test

# Publish to Maven Local (for local development)
./gradlew publishToMavenLocal

# Publish to remote repository (for production)
./gradlew publish
```

## License

MIT License
