# Microservices Architecture Documentation

## System Overview

This repository implements a modern microservices architecture with the following components:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Application                        │
└──────────────────────┬────────────────────────────────────────────┘
                       │
                       ↓ HTTP :8080
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway                               │
│        (Spring Cloud Gateway - Request Routing & Logging)         │
│  Routes: /api/product/**, /api/order/**, /api/inventory/**      │
└──────────────────────────┬──────────────────────────────────────┐
        ┌─────────────────┘      │         └──────────────────┐
        ↓ :8081                  ↓ :8082                      ↓ :8083
┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐
│  Order Service   │  │ Product Service  │  │ Inventory Service  │
│  (JPA + Kafka)   │  │  (Stateless)     │  │  (Stock Check)     │
└──────────────────┘  └──────────────────┘  └────────────────────┘
        │                    │                       │
        ↓ HTTP calls         Eureka Discovery        ↓ Kafk Consumers
┌──────────────────┐      ┌──────────────────┐
│   PostgreSQL DB  │      │  Eureka Server   │
│   (Order Data)   │      │  (Service Reg.)  │
└──────────────────┘      └──────────────────┘
        │
        ↓
┌──────────────────┐      ┌──────────────────┐      ┌────────────────┐
│   Kafka Broker   │      │   Zipkin Trace   │      │  Prometheus +  │
│  (Event Stream)  │      │   (Tracing)      │      │  Grafana (Metrics)
└──────────────────┘      └──────────────────┘      └────────────────┘
```

## Core Services

### 1. API Gateway (Port 8080)

**Purpose**: Single entry point for all client requests

**Technology**: Spring Cloud Gateway

**Responsibilities**:
- Routes requests to appropriate microservices
- Adds X-Trace-Id header for distributed tracing
- Logs all incoming/outgoing requests
- Provides service discovery and load balancing
- Exposes health and metrics endpoints

**Key Features**:
- Dynamic routing using Spring Cloud Gateway RouteLocator
- Service discovery via Eureka
- Global logging filter with UUID trace ID generation
- Gateway management endpoints (/api/gateway/*)

**See**: [API_GATEWAY_GUIDE.md](API_GATEWAY_GUIDE.md)

### 2. Order Service (Port 8081)

**Purpose**: Manages order placement and order lifecycle

**Technology**: Spring Boot 3.2.0 + Spring Data JPA + Kafka

**Responsibilities**:
- Process order placement requests
- Verify inventory before creating orders
- Store order data in PostgreSQL database
- Publish order events to Kafka
- Handle order queries and retrieval

**Key Features**:
- JPA entities (Order, OrderLineItems) with relationships
- Order validation with inventory check (via InventoryClient)
- Kafka event producer for order placement events
- Resilience4j circuit breaker for inventory service calls
- REST API endpoints for order operations

**Database**:
- PostgreSQL 15
- Schemas: orders (id, orderNumber, status, createdAt), order_line_items (id, skuCode, price, quantity, orderId)

**Kafka Topics**:
- `notificationTopic`: Order placed events

**API Endpoints**:
- `POST /api/order` - Place new order
- `GET /api/order/{orderNumber}` - Get order by number
- `GET /api/order/id/{id}` - Get order by ID
- `GET /api/order/health` - Health check

**See**: [API_GUIDE.md](API_GUIDE.md), [README.md](README.md)

### 3. Product Service (Port 8082)

**Purpose**: Manages product catalog

**Note**: This service is included in routing configuration but implementation is outside this scope

**Proposed Technology**: Spring Boot + Spring Data JPA

**Proposed Responsibilities**:
- Maintain product catalog
- Provide product information and pricing
- Handle product search and filtering

### 4. Inventory Service (Port 8083)

**Purpose**: Manages stock/inventory levels

**Note**: This service is called by Order Service for inventory verification

**Proposed Technology**: Spring Boot + In-Memory Cache

**Proposed Responsibilities**:
- Check stock availability for SKU codes
- Update inventory on order placement
- Provide inventory levels

**Called By**: Order Service (synchronous HTTP call with circuit breaker)

## Infrastructure Services

### Eureka Service Discovery (Port 8761)

**Purpose**: Centralized service registry

**Functionality**:
- Services register themselves on startup
- Gateway queries for service instances
- Enables load balancing across service replicas

**Configuration**:
```properties
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka
```

### PostgreSQL Database (Port 5432)

**Purpose**: Persistent data storage for Order Service

**Database**: `order_db`
**User**: postgres / postgres

**Schemas**:
```sql
CREATE TABLE orders (
  id BIGINT PRIMARY KEY,
  order_number VARCHAR(255) UNIQUE,
  status VARCHAR(50),
  created_at TIMESTAMP
);

CREATE TABLE order_line_items (
  id BIGINT PRIMARY KEY,
  order_id BIGINT,
  sku_code VARCHAR(100),
  price DECIMAL(10,2),
  quantity INT,
  FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

### Kafka Message Broker (Port 29092)

**Purpose**: Event-driven communication

**Topics**:
- `notificationTopic`: Order placement events

**Configuration**:
- Broker: kafka:29092 (internal Docker network)
- Zookeeper: zookeeper:2181

**Message Format** (Order Placed Event):
```json
{
  "orderId": 1,
  "orderNumber": "ORD-001",
  "message": "Order placed successfully"
}
```

### Zipkin Distributed Tracing (Port 9411)

**Purpose**: Trace requests across microservices

**Features**:
- Visual request flow understanding
- Latency analysis
- Service dependency mapping

**Integration**:
- All services include `spring.sleuth.enabled=true`
- X-Trace-Id header propagation via GlobalLoggingFilter

**Access**: http://localhost:9411

### Prometheus Metrics (Port 9090)

**Purpose**: Metrics collection and time-series database

**Metrics Collected**:
- HTTP request counts and latencies
- Database connection pool stats
- Kafka producer/consumer metrics
- JVM metrics (memory, threads, GC)

**Configuration**:
```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
```

### Grafana Dashboards (Port 3000)

**Purpose**: Visualization of Prometheus metrics

**Default Credentials**: admin / admin

**Dashboards**:
- Request rates and latencies
- Error rates
- Database performance
- Service health overview

**Access**: http://localhost:3000

## Request Flow Example: Order Placement

```
1. Client Request
   POST http://localhost:8080/api/order
   {
     "orderLineItemsDtoList": [
       {"skuCode": "SKU123", "price": 100.00, "quantity": 2}
     ]
   }

2. API Gateway Processing
   - Extract path /api/order
   - Generate Trace-ID: abc123def456
   - Route to order-service (via Eureka discovery)
   - Add X-Trace-Id: abc123def456 header
   - Log: POST /api/order [Trace-ID: abc123def456]

3. Order Service Processing
   a) Inventory Check
      - Call InventoryClient.checkInventory(["SKU123"])
      - Resilience4j circuit breaker: 
        * If success: returns true (in stock)
        * If failure: fallback returns true (optimistic)
   
   b) Order Creation
      - Create Order entity: orderNumber: "ORD-001", status: "PENDING"
      - Create OrderLineItems: 1 item with SKU-123, quantity 2
      - Persist to PostgreSQL
      - Return Order ID
   
   c) Event Publishing
      - Publish OrderPlacedEvent to Kafka topic: notificationTopic
      - Event contains: orderId, orderNumber, "Order placed successfully"
   
   d) Response
      - Return HTTP 201 (Created)
      - Response body: {orderId: 1, orderNumber: "ORD-001", status: "PENDING"}

4. Kafka Event Consumption
   - OrderEventListener receives event from notificationTopic
   - Can trigger downstream processing (notification, analytics, etc.)

5. Monitoring
   - Prometheus collects metrics: request duration, success/failure counts
   - Zipkin records trace with all service call details
   - Grafana visualizes request metrics on dashboards
```

## Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Spring Boot | 3.2.0 | Application framework |
| Cloud | Spring Cloud | 2023.0.0 | Microservices patterns |
| Gateway | Spring Cloud Gateway | - | API routing |
| Service Discovery | Spring Cloud Eureka | - | Service registry |
| Persistence | Spring Data JPA | - | ORM for Order Service |
| Messaging | Spring Kafka | - | Event streaming |
| Resilience | Resilience4j | - | Circuit breaker pattern |
| Metrics | Micrometer Prometheus | - | Metrics collection |
| Tracing | Micrometer Tracing | - | Distributed tracing |
| Database | PostgreSQL | 15 | Order data storage |
| Message Broker | Apache Kafka | - | Event streaming |
| Container | Docker | - | Containerization |
| Orchestration | Docker Compose | 3.8 | Multi-container setup |

## Deployment Architecture

### Local Development
```bash
mvn clean install
mvn spring-boot:run  # For each service separately
```

### Docker Compose
```bash
docker-compose up -d
```

Services start in dependency order:
1. PostgreSQL (database prerequisite)
2. Kafka + Zookeeper (messaging)
3. Eureka Server (service discovery)
4. Zipkin (tracing)
5. Prometheus (metrics)
6. Grafana (dashboards)
7. Order Service (port 8081)
8. API Gateway (port 8080)

### Production Deployment

For production, consider:
- Kubernetes orchestration (replicas, self-healing)
- Spring Cloud Config for centralized configuration
- API Gateway authentication and authorization
- Persistent volumes for databases
- Service mesh (Istio) for advanced traffic management

## Configuration Management

### Profiles

#### Default Profile (application.properties)
- Local development configuration
- localhost connection strings
- Debug logging levels

#### Docker Profile (application-docker.properties)
- Docker-specific configuration
- Container service names (eureka-server, kafka, postgres)
- Elevated health check timeouts

#### Test Profile (application-test.properties)
- In-memory H2 database
- Embedded test Kafka
- Simplified configuration

### Environment Variables (for Docker)
- `SPRING_DATASOURCE_URL` - PostgreSQL connection
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka broker address
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` - Eureka server address
- `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT` - Zipkin server address
- `SPRING_APPLICATION_NAME` - Service identifier

## Monitoring and Observability

### Health Checks
```bash
curl http://localhost:8080/api/gateway/health
curl http://localhost:8081/api/order/health
```

### Metrics
```bash
curl http://localhost:8080/actuator/prometheus
curl http://localhost:8081/actuator/prometheus
```

### Distributed Tracing
```bash
# Access Zipkin
http://localhost:9411

# Search for traces by service name or trace ID
# View service dependencies and latencies
```

### Centralized Logging (Future Enhancement)
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Splunk
- CloudWatch

## Resilience Patterns Implemented

### 1. Circuit Breaker
**Service**: Order Service → Inventory Service

**Configuration**:
```properties
resilience4j.circuitbreaker.instances.inventoryService.registerHealthIndicator=true
resilience4j.circuitbreaker.instances.inventoryService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.inventoryService.minNumberOfCalls=10
```

**Behavior**:
- Closed: Normal operation
- Open: Reject calls after 50% failure rate, return fallback (optimistic: true)
- Half-open: Allow limited calls to test if service recovered

### 2. Retry
**Service**: Order Service → Inventory Service

**Configuration**:
```properties
resilience4j.retry.instances.inventoryService.maxAttempts=3
resilience4j.retry.instances.inventoryService.waitDuration=1000
```

**Behavior**: Retry failed calls up to 3 times with 1-second backoff

### 3. Timeout
**Service**: Order Service → Inventory Service

**Configuration**:
```properties
resilience4j.timelimiter.instances.inventoryService.timeoutDuration=3s
```

**Behavior**: Abandon slow calls after 3 seconds, return fallback

## API Contracts

### Order Service API

#### Place Order
```
POST /api/order
Content-Type: application/json

Request:
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "SKU123",
      "price": 100.00,
      "quantity": 2
    }
  ]
}

Response (201 Created):
{
  "orderId": 1,
  "orderNumber": "ORD-001",
  "status": "PENDING",
  "message": "Order placed successfully"
}

Response (400 Bad Request - Out of Stock):
{
  "orderId": null,
  "orderNumber": null,
  "status": "FAILED",
  "message": "Item with skuCode: SKU123 is not in stock"
}
```

#### Get Order by Number
```
GET /api/order/{orderNumber}

Response (200 OK):
{
  "id": 1,
  "orderNumber": "ORD-001",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:45Z",
  "orderLineItemsList": [...]
}

Response (404 Not Found):
{
  "orderId": null,
  "orderNumber": null,
  "status": "NOT_FOUND",
  "message": "Order not found"
}
```

### API Gateway Endpoints
See [API_GATEWAY_GUIDE.md](API_GATEWAY_GUIDE.md) for detailed endpoints.

## Testing Strategy

### Unit Tests
- Service components with Mockito
- Test business logic in isolation
- Run with: `mvn test`

### Integration Tests
- TestContainers for real database/broker
- Full request-response cycles
- Run with: `mvn verify`

### End-to-End Tests
```bash
# Start docker-compose stack
docker-compose up -d

# Test order placement through gateway
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{"orderLineItemsDtoList": [{"skuCode": "ITEM1", "price": 50, "quantity": 1}]}'

# Verify event in Kafka
docker exec kafka bash -c '
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic notificationTopic --from-beginning --max-messages 1
'

# Check traces in Zipkin
# Visit http://localhost:9411
```

## Performance Considerations

### Caching
- Implement Redis for frequently accessed data (products, inventory)
- Cache microservice discovery results

### Asynchronous Processing
- Use Kafka for non-blocking event processing
- Implement reactive streams with Project Reactor

### Database Optimization
- Index frequently queried columns (orderNumber, skuCode)
- Implement pagination for list endpoints
- Use connection pooling (HikariCP)

### Load Balancing
- Gateway automatically load balances across service instances
- Eureka detects and removes unhealthy instances

## Security Considerations (Future)

1. **Authentication**: Add OAuth2/JWT token validation in gateway
2. **Authorization**: Role-based access control per endpoint
3. **API Encryption**: Use HTTPS/TLS
4. **Rate Limiting**: Prevent abuse and DDoS attacks
5. **Input Validation**: Strict validation of all inputs
6. **SQL Injection Protection**: Using parameterized queries (ORM)
7. **CORS Policy**: Configure appropriate CORS headers

## Common Issues and Solutions

### Services unable to discover each other
- Verify Eureka server is running
- Check service names in routing config match registration names
- Verify network connectivity in Docker network

### Database connection failures
- Check PostgreSQL is running: `docker ps | grep postgres`
- Verify credentials in application-docker.properties
- Test connection: `psql -h localhost -U postgres -d order_db`

### Kafka message issues
- Verify Kafka broker is running
- Check topic exists: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`
- View messages: `docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic notificationTopic`

## Next Steps

1. **Implement Product Service**: Complete product catalog service
2. **Implement Inventory Service**: Implement stock management
3. **Add Authentication**: Implement OAuth2 in API Gateway
4. **API Documentation**: Implement OpenAPI/Swagger
5. **Performance Testing**: Load test with k6 or Apache JMeter
6. **Kubernetes Deployment**: Migrate from Docker Compose to K8s
7. **Service Mesh**: Implement Istio for advanced traffic management

## References

- Spring Cloud Gateway: https://spring.io/projects/spring-cloud-gateway
- Spring Cloud Netflix Eureka: https://spring.io/projects/spring-cloud-netflix
- Resilience4j: https://resilience4j.readme.io/
- Kafka: https://kafka.apache.org/
- Micrometer: https://micrometer.io/

