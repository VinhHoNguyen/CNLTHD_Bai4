# API Gateway Guide

## Overview

The API Gateway is the central entry point for all client requests to the microservices architecture. It uses Spring Cloud Gateway to route requests to appropriate backend services, handle cross-cutting concerns, and provide service discovery.

## Architecture

```
Client Requests
      ↓
API Gateway (Port 8080)
      ↓
  ┌───┴───┬─────────┐
  ↓       ↓         ↓
Product Order  Inventory
Service Service Service
  (8082) (8081)  (8083)
```

## Routing Rules

The gateway routes requests based on path patterns:

| Pattern | Service | Description |
|---------|---------|-------------|
| `/api/product/**` | product-service | Product catalog management |
| `/api/order/**` | order-service | Order management (port 8081) |
| `/api/inventory/**` | inventory-service | Inventory/stock management |
| `/api/gateway/**` | Gateway (internal) | Gateway management endpoints |

### Service Discovery

Routes use the `lb://` (Load Balancer) prefix with service names. Spring Cloud Gateway automatically resolves service names through Eureka service discovery:

```java
.route("order-service", r -> r.path("/api/order/**").uri("lb://order-service"))
```

When a request arrives:
1. Gateway extracts service name from path
2. Queries Eureka registry for service instances
3. Load balances across available instances
4. Routes request to selected instance

## Gateway Endpoints

### 1. Health Check
```
GET /api/gateway/health
Response: "API Gateway is running"
Status: 200 OK
```

### 2. Gateway Status
```
GET /api/gateway/status
Response:
{
  "status": "UP",
  "component": "API-Gateway",
  "timestamp": "2024-01-15T10:30:45Z"
}
Status: 200 OK
```

### 3. Service Discovery
```
GET /api/gateway/services
Response:
{
  "total_services": 3,
  "services": {
    "order-service": [
      {"host": "localhost", "port": 8081}
    ],
    "product-service": [
      {"host": "localhost", "port": 8082}
    ],
    "inventory-service": [
      {"host": "localhost", "port": 8083}
    ]
  }
}
Status: 200 OK
```

## Request Flow with Examples

### Example 1: Place an Order
```bash
# Client sends request to gateway
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderLineItemsDtoList": [
      {
        "skuCode": "SKU123",
        "price": 100.00,
        "quantity": 2
      }
    ]
  }'

# Gateway routing:
# 1. Receives request at /api/order (matches /api/order/** route)
# 2. Looks up order-service in Eureka registry
# 3. Resolves to http://order-service:8080 (internal Docker network)
# 4. Forwards request with X-Trace-Id header
# 5. Returns response (201 Created with OrderResponse)
```

### Example 2: Check Product
```bash
curl -X GET http://localhost:8080/api/product/SKU123

# Gateway routing:
# 1. Receives request at /api/product/SKU123 (matches /api/product/** route)
# 2. Looks up product-service in Eureka
# 3. Routes to http://product-service:8080
# 4. Returns product details
```

## Global Features

### 1. Request/Response Logging

The `GlobalLoggingFilter` intercepts all requests and responses:

```
Incoming Request:
  Method: POST
  Path: /api/order
  Trace-Id: a8f3b9c2-4e1d-11ec-81d3-0242ac130003

Response:
  Status: 201 Created
  Trace-Id: a8f3b9c2-4e1d-11ec-81d3-0242ac130003
```

Features:
- **Unique Trace ID**: UUID generated per request for distributed tracing
- **X-Trace-Id Header**: Added to outgoing requests for end-to-end tracing
- **Async Processing**: Uses Reactor's Mono for non-blocking operations

### 2. Service Discovery Integration

The gateway integrates with Spring Cloud Eureka for dynamic service discovery:

```properties
# application-docker.properties
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lowerCaseServiceId=true
```

Benefits:
- Services register automatically on startup
- Gateway discovers services without hardcoding URLs
- Load balancing across service instances
- Automatic failover if instances go down

### 3. Gateway Discovery Locator

Enables automatic route creation for discovered services:

```properties
spring.cloud.gateway.discovery.locator.enabled=true
```

This allows routes like:
- http://localhost:8080/order-service/** → routes to order-service
- http://localhost:8080/product-service/** → routes to product-service

## Metrics and Monitoring

### Health Checks
```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "discoveryClient": {
      "status": "UP",
      "details": {
        "services": ["order-service", "product-service"]
      }
    }
  }
}
```

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

Key metrics:
- `spring_cloud_gateway_requests_total` - Total requests per route
- `spring_cloud_gateway_request_seconds` - Request duration

## Configuration

### application.properties (Default/Local)
```properties
server.port=8080
spring.application.name=api-gateway
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lowerCaseServiceId=true
management.endpoints.web.exposure.include=health,metrics,prometheus
logging.level.org.springframework.cloud.gateway=DEBUG
```

### application-docker.properties (Docker Environment)
```properties
server.port=8080
spring.application.name=api-gateway
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka
spring.cloud.gateway.discovery.locator.enabled=true
management.endpoints.web.exposure.include=health,metrics,prometheus
```

## Running the Gateway

### Local Development
```bash
mvn clean install
mvn spring-boot:run
```

Gateway will start on `http://localhost:8080`

### Docker Compose
```bash
docker-compose up -d
```

The gateway will:
1. Build from Dockerfile
2. Wait for Eureka server to be healthy
3. Register as `api-gateway` service
4. Listen on port 8080

### Port Mapping
- **External**: 8080 (client access)
- **Internal**: 8080 (container)

Other services (for reference):
- **Order Service**: External 8081 → Internal 8080
- **Product Service**: External 8082 → Internal 8080
- **Inventory Service**: External 8083 → Internal 8080

## Testing

### Unit Tests
```bash
mvn test -Dtest=ApiGatewayApplicationTest
mvn test -Dtest=GatewayControllerTest
```

### Integration Testing
```bash
# Start docker-compose stack
docker-compose up -d

# Test routing to order service
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{"orderLineItemsDtoList": [{"skuCode": "SKU123", "price": 100, "quantity": 1}]}'

# Verify service discovery
curl http://localhost:8080/api/gateway/services

# Check logs for X-Trace-Id propagation
docker logs api_gateway
```

## Troubleshooting

### Gateway not routing requests
1. Check Eureka server is running: `curl http://localhost:8761`
2. Verify services are registered: `http://localhost:8761/` (web UI)
3. Check gateway logs: `docker logs api_gateway`

### Service instances not discovered
1. Ensure services have `@EnableDiscoveryClient`
2. Verify `eureka.client.service-url.defaultZone` points to Eureka
3. Check service names match route definitions (case-sensitive)

### Requests timing out
1. Check if target service is running
2. Verify network connectivity between containers
3. Increase timeout in properties: `spring.cloud.gateway.httpclient.connect-timeout=5000`

## Best Practices

1. **Trace Request Paths**: Use X-Trace-Id header correlation across services
2. **Health Checks**: Regularly monitor `/api/gateway/health`
3. **Load Balancing**: Gateway automatically distributes load across service instances
4. **Rate Limiting**: Can be added via filter for DDoS protection
5. **Authentication**: Consider adding auth filter for production

## Key Components

### ApiGatewayApplication.java
Main entry point enabling Eureka discovery client.

### GatewayConfig.java
Defines routing rules using RouteLocator bean with:
- Route predicates (path matching)
- Route URIs (load-balanced service names)
- Built-in filters

### GlobalLoggingFilter.java
Implements GlobalFilter for:
- Request/response logging
- Trace ID generation and propagation
- Distributed tracing support

### GatewayController.java
Provides gateway management endpoints:
- Health status
- Gateway operational status
- Service registry visibility

## Next Steps

- Implement authentication filters (OAuth2, JWT)
- Add rate limiting filters
- Configure retry policies for failed requests
- Set up circuit breaker patterns
- Implement API versioning strategies
