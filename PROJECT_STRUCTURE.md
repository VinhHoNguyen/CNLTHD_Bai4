# Project Structure Summary

## Root Directory Files
```
pom.xml                    - Maven dependencies and configurations
Dockerfile                 - Docker image configuration
docker-compose.yml         - Docker Compose orchestration
prometheus.yml             - Prometheus metrics configuration
Makefile                   - Build and deployment commands
.gitignore                - Git ignore rules
.env.example              - Environment variables example
README.md                 - Project documentation
```

## Source Code Organization

### src/main/java/com/hdbank/orderservice/

```
├── OrderServiceApplication.java     - Main Spring Boot application
│
├── controller/
│   └── OrderController.java         - REST API endpoints
│
├── model/
│   ├── Order.java                   - Order entity (JPA)
│   └── OrderLineItems.java          - OrderLineItems entity (JPA)
│
├── repository/
│   └── OrderRepository.java         - Spring Data JPA repository
│
├── service/
│   └── OrderService.java            - Business logic layer
│
├── dto/
│   ├── OrderLineItemsDto.java       - Order line items DTO
│   ├── OrderRequest.java            - Order request DTO
│   ├── OrderResponse.java           - Order response DTO
│   └── InventoryResponse.java       - Inventory service response
│
├── event/
│   └── OrderPlacedEvent.java        - Kafka event for order placement
│
├── producer/
│   └── OrderProducer.java           - Kafka event producer
│
├── listener/
│   └── OrderEventListener.java      - Kafka event listener
│
├── client/
│   └── InventoryClient.java         - HTTP client with Resilience4j
│
├── exception/
│   └── OutOfStockException.java     - Custom exception
│
└── config/
    ├── RestTemplateConfig.java      - RestTemplate configuration
    ├── GlobalExceptionHandler.java  - Global exception handling
```

### src/main/resources/

```
├── application.properties           - Default configuration
├── application-docker.properties    - Docker environment config
├── application-test.properties      - Test environment config
├── banner.txt                       - Application banner
└── init.sql                        - Database initialization script
```

### src/test/java/com/hdbank/orderservice/

```
├── service/
│   └── OrderServiceTest.java        - Unit tests for OrderService
│
├── controller/
│   └── OrderControllerTest.java     - Unit tests for Controller
│
└── OrderServiceIntegrationTest.java - Integration tests with TestContainers
```

## Key Features by File

### 1. REST API (OrderController.java)
- POST /api/order - Place new order (201 Created)
- GET /api/order/{orderNumber} - Get order by order number
- GET /api/order/id/{id} - Get order by ID
- GET /api/order/health - Health check endpoint

### 2. Database Models
- **Order.java**: orderNumber (unique), status, orderLineItemsList (cascade)
- **OrderLineItems.java**: skuCode, price, quantity, order (FK)

### 3. Business Logic (OrderService.java)
- Inventory verification with Resilience4j
- Order creation and persistence
- Kafka event publishing
- Error handling for out-of-stock scenarios

### 4. External Service Integration (InventoryClient.java)
- Circuit Breaker pattern with Resilience4j
- Automatic retries with exponential backoff
- Timeout handling
- Fallback mechanism

### 5. Event Publishing (OrderProducer.java)
- Kafka message sending
- topic: notificationTopic
- Payload: OrderPlacedEvent with order details

### 6. Configuration
- application.properties - Local PostgreSQL/Kafka settings
- application-docker.properties - Docker environment settings
- application-test.properties - In-memory H2 database

## Dependencies Hierarchy

```
Spring Boot 3.2.0
├── Spring Web
├── Spring Data JPA
├── Spring Cloud Eureka Client
├── Spring Cloud Circuit Breaker (Resilience4j)
├── Spring Kafka
├── Lombok
├── Spring Boot Actuator
├── Micrometer Prometheus
├── Micrometer Tracing Zipkin
├── PostgreSQL/MySQL Drivers
└── Testing Libraries
    ├── JUnit 5
    ├── Mockito
    ├── TestContainers
    └── REST Assured
```

## Database Schema

### orders table
```sql
id (BIGINT, PK, AUTO_INCREMENT)
order_number (VARCHAR, UNIQUE, NOT NULL)
status (VARCHAR, DEFAULT='PENDING')
created_at (TIMESTAMP, DEFAULT=CURRENT_TIMESTAMP)
```

### order_line_items table
```sql
id (BIGINT, PK, AUTO_INCREMENT)
order_id (BIGINT, FK to orders)
sku_code (VARCHAR, NOT NULL)
price (DECIMAL 19,2, NOT NULL)
quantity (INT, NOT NULL)
```

## Docker Services

```yaml
services:
  - postgres:15-alpine         # Database
  - kafka:7.5.0               # Message broker
  - zookeeper:7.5.0          # Kafka coordination
  - eureka-server:latest      # Service discovery
  - zipkin:latest            # Distributed tracing
  - prometheus:latest         # Metrics collection
  - grafana:latest           # Metrics visualization
  - order-service:1.0.0      # Our application
```

## Build & Deployment Commands

```bash
# Development
mvn clean package           # Build
mvn spring-boot:run        # Run locally
mvn test                   # Unit tests
mvn verify                 # Integration tests

# Docker
mvn clean package -DskipTests && docker-compose up -d

# Make commands (if Makefile available)
make build                 # Build
make run                   # Local run
make test                  # Tests
make docker-up            # Start services
make docker-down          # Stop services
```

## Configuration Properties

### Critical Settings
- `eureka.client.service-url.defaultZone=http://localhost:8761/eureka`
- `spring.kafka.bootstrap-servers=localhost:9092`
- `spring.datasource.url=jdbc:postgresql://localhost:5432/order_db`
- `resilience4j.circuitbreaker.failure-rate-threshold=50`
- `management.endpoints.web.exposure.include=health,info,metrics,prometheus`

### Resilience4j Circuit Breaker
- failure-rate-threshold: 50%
- sliding-window-size: 10 requests
- wait-duration-in-open-state: 5s
- max-retry-attempts: 3
- timeout: 3s

## Testing Coverage

Test classes created:
1. **OrderServiceTest** - Unit tests (Mockito)
2. **OrderControllerTest** - Controller tests
3. **OrderServiceIntegrationTest** - Integration with TestContainers

Target coverage: >80% (via JaCoCo)

## Deployment Architecture

```
┌─────────────────────────────────────────────┐
│         Docker Compose Orchestration         │
├─────────────────────────────────────────────┤
│                                             │
│  ┌──────────────┐  ┌──────────────┐       │
│  │   Order      │  │  PostgreSQL  │       │
│  │   Service    │  │   (port 5432)│       │
│  │   (8080)     │  │              │       │
│  └──────────────┘  └──────────────┘       │
│         │                                  │
│         ├──► Kafka (port 9092)            │
│         ├──► Eureka (port 8761)           │
│         ├──► Zipkin (port 9411)           │
│         ├──► Prometheus (port 9090)       │
│         └──► Grafana (port 3000)          │
│                                             │
└─────────────────────────────────────────────┘
```

## Next Steps

1. **Build the project**: `mvn clean package`
2. **Run locally**: `mvn spring-boot:run`
3. **Docker deployment**: `docker-compose up -d`
4. **Test API**: POST to http://localhost:8080/api/order
5. **Monitor**: Access http://localhost:3000 (Grafana)
6. **Trace**: Check http://localhost:9411 (Zipkin)

---

*Generated: March 22, 2024*
*Order Service v1.0.0 - Microservice Architecture*
