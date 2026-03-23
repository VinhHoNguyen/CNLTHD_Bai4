# Complete Microservices Architecture Project

## 📋 Project Overview

This project implements a complete, production-ready microservices architecture using **Spring Cloud** ecosystem with:
- **API Gateway** for central request routing and load balancing
- **Order Service** for order management with event-driven architecture
- **Service Discovery** via Eureka
- **Event Streaming** via Apache Kafka
- **Distributed Tracing** via Zipkin and Micrometer
- **Metrics Collection** via Prometheus and Grafana
- **Resilience Patterns** including circuit breaker, retry, and timeout

## 🏗️ Current Implementation Status

### ✅ Completed

#### Order Service Branch (`order`)
- Spring Boot 3.2.0 REST API with Spring Data JPA
- PostgreSQL database with Order and OrderLineItems entities
- Spring Cloud Eureka client registration
- Kafka event publishing to `notificationTopic`
- Kafka event listener for order events
- Resilience4j circuit breaker for inventory service calls with retry and timeout
- Micrometer Tracing with Zipkin integration
- Actuator health checks and Prometheus metrics exposure
- Unit tests (Mockito - 3 test methods)
- Integration tests (TestContainers - 2 test methods)
- Comprehensive documentation (5 files)
- Complete Docker setup with docker-compose (8 services)

**Access**: `git checkout order`

#### API Gateway Branch (`api-gateway`)
- Spring Cloud Gateway with dynamic routing to 4 services
- Eureka service discovery integration
- GlobalLoggingFilter for request/response tracing with UUID trace ID
- GatewayController with 3 management endpoints
- Full test coverage (7 test methods across 2 test files)
- Configuration for both local (application.properties) and Docker (application-docker.properties) environments
- Updated docker-compose.yml with gateway service on port 8080
- Comprehensive documentation (2 files)

**Access**: `git checkout api-gateway`

### ⏳ Planned (Not Yet Implemented)

- **Product Service**: Product catalog management
- **Inventory Service**: Stock/inventory management
- **API Documentation**: OpenAPI/Swagger integration
- **Authentication**: OAuth2/JWT security layer
- **Rate Limiting**: DDoS/abuse protection
- **Kubernetes Deployment**: K8s manifests for production scaling

## 📁 Project Structure

```
CNLTHD_Bai4/
├── src/
│   ├── main/
│   │   ├── java/com/hdbank/
│   │   │   ├── apigateway/              # API Gateway (current branch)
│   │   │   │   ├── ApiGatewayApplication.java
│   │   │   │   ├── config/GatewayConfig.java (routing rules)
│   │   │   │   ├── filter/GlobalLoggingFilter.java
│   │   │   │   └── controller/GatewayController.java
│   │   │   └── orderservice/            # Order Service (order branch)
│   │   │       ├── OrderServiceApplication.java
│   │   │       ├── controller/OrderController.java
│   │   │       ├── service/OrderService.java
│   │   │       ├── repository/OrderRepository.java
│   │   │       ├── model/Order.java, OrderLineItems.java
│   │   │       ├── client/InventoryClient.java (with circuit breaker)
│   │   │       ├── producer/OrderProducer.java (Kafka)
│   │   │       ├── listener/OrderEventListener.java (Kafka)
│   │   │       └── config/* (exception handlers, REST template config)
│   │   └── resources/
│   │       ├── application.properties (local config)
│   │       ├── application-docker.properties (Docker config)
│   │       ├── application-test.properties (test config)
│   │       └── banner.txt
│   └── test/
│       └── java/com/hdbank/
│           ├── ApiGatewayApplicationTest.java (4 tests)
│           ├── GatewayControllerTest.java (3 tests)
│           ├── OrderServiceTest.java (3 unit tests - order branch)
│           ├── OrderControllerTest.java (3 controller tests - order branch)
│           └── OrderServiceIntegrationTest.java (2 integration tests - order branch)
├── docker-compose.yml (8 services: postgres, kafka, zookeeper, eureka, zipkin, prometheus, grafana, order-service, api-gateway)
├── Dockerfile (multi-stage build)
├── pom.xml (Maven dependencies)
├── README.md (Order Service quick start)
├── API_GUIDE.md (Order Service API reference)
├── PROJECT_STRUCTURE.md (Order Service architecture)
├── DEPLOYMENT_GUIDE.md (Order Service deployment)
├── GETTING_STARTED.md (Order Service quick start guide)
├── API_GATEWAY_GUIDE.md (Gateway architecture and endpoints)
└── MICROSERVICES_ARCHITECTURE.md (Complete system design)
```

## 🚀 Quick Start Guide

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Option 1: Run with Docker Compose (Recommended)

```bash
# Clone and navigate
cd CNLTHD_Bai4

# Build and start all services
docker-compose up -d

# Wait for services to be healthy (30-40 seconds)
sleep 30

# Verify all services started
docker-compose ps
```

**All services will be running:**
- API Gateway: http://localhost:8080
- Order Service: http://localhost:8081
- Eureka Server: http://localhost:8761
- Kafka: localhost:9092
- Zipkin: http://localhost:9411
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

### Option 2: Run Locally with Maven

```bash
# Ensure you have the order branch code
git checkout order

# Build
mvn clean install

# Start Order Service (requires PostgreSQL, Kafka, Eureka running)
mvn spring-boot:run

# In separate terminal, start API Gateway
git checkout api-gateway
mvn spring-boot:run
```

## 🔄 Understanding the Architecture

### Request Flow: Place an Order
```
Client
  │
  ↓ POST /api/order
API Gateway (port 8080)
  │ Routes via Eureka discovery
  ↓
Order Service (port 8081)
  │ Checks inventory
  ├─→ InventoryClient (with circuit breaker)
  │
  ├─→ Creates Order in PostgreSQL
  │
  ├─→ Publishes OrderPlacedEvent to Kafka
  │
  └─→ Returns 201 Created response
```

### Core Components

#### API Gateway (Port 8080)
- **Role**: Single entry point, request routing, load balancing
- **Routing**: `/api/product/**, /api/order/**, /api/inventory/**`
- **Features**: Global logging, distributed tracing, service discovery
- **See**: [API_GATEWAY_GUIDE.md](API_GATEWAY_GUIDE.md)

#### Order Service (Port 8081)
- **Role**: Order placement and lifecycle management
- **Database**: PostgreSQL
- **Events**: Kafka `notificationTopic`
- **Resilience**: Circuit breaker for inventory calls
- **See**: [API_GUIDE.md](API_GUIDE.md), [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

#### Supporting Infrastructure
- **Eureka**: Service discovery and registration (port 8761)
- **PostgreSQL**: Order data persistence (port 5432)
- **Kafka**: Event streaming (port 9092)
- **Zipkin**: Distributed tracing (port 9411)
- **Prometheus**: Metrics collection (port 9090)
- **Grafana**: Metrics visualization (port 3000)

**See**: [MICROSERVICES_ARCHITECTURE.md](MICROSERVICES_ARCHITECTURE.md) for complete details

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Order Placement Through Gateway
```bash
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderLineItemsDtoList": [
      {
        "skuCode": "ITEM1",
        "price": 100.00,
        "quantity": 2
      }
    ]
  }'
```

### Test Gateway Discovery
```bash
curl http://localhost:8080/api/gateway/services
```

## 📊 Monitoring Dashboards

| Service | URL | Purpose |
|---------|-----|---------|
| Eureka Registry | http://localhost:8761 | View registered services |
| Prometheus | http://localhost:9090 | Query metrics |
| Grafana | http://localhost:3000 | Visualize metrics (admin/admin) |
| Zipkin | http://localhost:9411 | Distributed tracing |

## 🌳 Git Branches

### `main` (or `master`)
Initial project setup

### `order`
**Order Service Implementation**
- Complete Spring Boot microservice
- REST API for order management
- Spring Data JPA with PostgreSQL
- Kafka event publishing
- Resilience4j circuit breaker
- Eureka service discovery
- 13 test methods (unit and integration)
- Comprehensive documentation

```bash
git checkout order
mvn clean install
mvn spring-boot:run
```

### `api-gateway`
**API Gateway Implementation**
- Spring Cloud Gateway setup
- Routing to 4 backend services
- Global logging and tracing
- Service discovery integration
- 7 test methods
- Complete infrastructure configuration

```bash
git checkout api-gateway
mvn clean install
mvn spring-boot:run
```

### `inventory-service`
Previously started (minimal implementation)

## 📚 Documentation

### Quick References
- [API_GUIDE.md](API_GUIDE.md) - Order Service REST API endpoints
- [API_GATEWAY_GUIDE.md](API_GATEWAY_GUIDE.md) - Gateway routing and endpoints
- [GETTING_STARTED.md](GETTING_STARTED.md) - Quick start guide for Order Service

### In-Depth Guides
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Order Service architecture
- [MICROSERVICES_ARCHITECTURE.md](MICROSERVICES_ARCHITECTURE.md) - Complete system design
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Production deployment

## 🔧 Configuration

### Environment Profiles
```properties
# application.properties (local development)
spring.datasource.url=jdbc:postgresql://localhost:5432/order_db

# application-docker.properties (Docker environment)
spring.datasource.url=jdbc:postgresql://postgres:5432/order_db

# application-test.properties (testing)
spring.datasource.url=jdbc:h2:mem:test
```

### Key Properties
```properties
# API Gateway
server.port=8080
spring.cloud.gateway.discovery.locator.enabled=true

# Order Service
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/order_db
spring.kafka.bootstrap-servers=localhost:9092

# Service Discovery (both)
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Monitoring
management.endpoints.web.exposure.include=health,metrics,prometheus
```

## 🏭 Build and Deployment

### Build Docker Images
```bash
# Automatically done by docker-compose
docker-compose up -d

# Or manually
docker build -t hdbank/api-gateway:1.0 .
```

### Docker Compose Services
The [docker-compose.yml](docker-compose.yml) defines:
1. **postgres** - PostgreSQL 15 database
2. **kafka** - Apache Kafka broker
3. **zookeeper** - Kafka Zookeeper
4. **eureka-server** - Eureka service registry
5. **zipkin** - Distributed tracing
6. **prometheus** - Metrics collection
7. **grafana** - Metrics dashboard
8. **order-service** - Order microservice (port 8081)
9. **api-gateway** - API Gateway (port 8080)

### Health Checks
All services include health checks:
```bash
curl http://localhost:8080/actuator/health   # API Gateway
curl http://localhost:8081/actuator/health   # Order Service
```

## 🔐 Security Considerations

Currently implemented:
- ✅ Input validation
- ✅ Exception handling
- ✅ Request/response logging
- ✅ Distributed tracing

Recommended for production:
- ❌ OAuth2/JWT authentication
- ❌ CORS policy configuration
- ❌ API rate limiting
- ❌ HTTPS/TLS encryption
- ❌ Role-based access control

## 📈 Performance Optimization

Implemented:
- ✅ Connection pooling (HikariCP)
- ✅ Distributed caching via Eureka
- ✅ Asynchronous event processing (Kafka)
- ✅ Circuit breaker pattern (Resilience4j)
- ✅ Load balancing (Spring Cloud Gateway)

Future enhancements:
- ❌ Redis cache layer
- ❌ Database query optimization
- ❌ Service mesh (Istio)
- ❌ Kubernetes auto-scaling

## 🤝 Contributing

To develop new features:

1. Create new feature branch:
```bash
git checkout -b feature/your-feature-name
```

2. Make changes and test
3. Commit with clear messages:
```bash
git commit -m "feat: Add new feature description"
```

4. Push and create pull request

## 📞 Support and Troubleshooting

### Common Issues

**Services can't discover each other**
- Verify Eureka is running: http://localhost:8761
- Check service names in gateway routing match registration names

**Database connection failed**
- Verify PostgreSQL is running: `docker-compose ps`
- Reset database: `docker-compose down -v && docker-compose up`

**Kafka message issues**
- Check Kafka health: `docker-compose logs kafka`
- List topics: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`

**Port conflicts**
- Check running services: `docker-compose ps`
- Change ports in docker-compose.yml

## 📝 License

This is an educational project for learning microservices architecture.

## 🎯 Learning Objectives

This project demonstrates:
1. **Microservices Architecture**: Decoupled, independently deployable services
2. **API Gateway Pattern**: Centralized request routing and load balancing
3. **Service Discovery**: Dynamic service registration with Eureka
4. **Event-Driven Architecture**: Asynchronous communication via Kafka
5. **Resilience Patterns**: Circuit breaker, retry, timeout
6. **Distributed Tracing**: Request correlation across services
7. **Observability**: Metrics, logging, health checks
8. **Containerization**: Docker and Docker Compose for local development
9. **Testing Strategy**: Unit, integration, and end-to-end tests

## 📖 Further Reading

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j](https://resilience4j.readme.io/)
- [Apache Kafka](https://kafka.apache.org/)
- [Micrometer Documentation](https://micrometer.io/)

---

**Last Updated**: January 2024  
**Branches**: `main`, `order`, `api-gateway`, `inventory-service`  
**Technologies**: Spring Boot 3.2.0, Spring Cloud 2023.0.0, Docker Compose 3.8
