# Getting Started with Order Service

## 🎯 What's Been Created

A fully-featured **Order Service Microservice** built with Spring Boot 3.2.0 for managing orders in a distributed microservices architecture.

## 📦 What's Included

### Core Functionality
- ✅ REST API for order management
- ✅ PostgreSQL database with Spring Data JPA
- ✅ Eureka service discovery integration
- ✅ Kafka event publishing
- ✅ Resilience4j circuit breaker pattern
- ✅ Prometheus metrics & monitoring
- ✅ Distributed tracing with Zipkin
- ✅ Docker & Docker Compose deployment
- ✅ Comprehensive unit & integration tests
- ✅ Production-ready configuration

### Documentation
- 📖 **README.md** - Project overview
- 📖 **API_GUIDE.md** - Complete API documentation
- 📖 **PROJECT_STRUCTURE.md** - Code organization guide
- 📖 **DEPLOYMENT_GUIDE.md** - Deployment instructions

### Configuration Files
- **pom.xml** - Maven dependencies
- **application.properties** - Local development config
- **application-docker.properties** - Docker environment config
- **docker-compose.yml** - All services orchestration
- **Dockerfile** - Container image definition
- **Makefile** - Build automation
- **.gitignore** - Git configuration
- **.env.example** - Environment variables template

## 🚀 Quick Start

### Option 1: Run with Docker Compose (Recommended)
```bash
cd order-service
docker-compose up -d
```

Access at: http://localhost:8080

### Option 2: Local Development
```bash
# Prerequisites: Java 17, Maven 3.9, PostgreSQL

# Build
mvn clean package

# Run
mvn spring-boot:run
```

### Option 3: Run with Make
```bash
make docker-up      # Start all services
make build          # Build project
make run            # Run locally
make test           # Run tests
```

## 📍 Service Endpoints

After startup, access:

| Service | URL | Purpose |
|---------|-----|---------|
| Order Service | http://localhost:8080 | Main API |
| Eureka | http://localhost:8761 | Service discovery |
| Zipkin | http://localhost:9411 | Distributed tracing |
| Prometheus | http://localhost:9090 | Metrics collection |
| Grafana | http://localhost:3000 | Metrics dashboard |
| PostgreSQL | localhost:5432 | Database |
| Kafka | localhost:9092 | Message broker |

## 💻 Example API Call

```bash
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderLineItemsDtoList": [
      {
        "skuCode": "iphone_13",
        "price": 1200.00,
        "quantity": 1
      }
    ]
  }'
```

Expected response:
```json
{
  "orderId": 1,
  "orderNumber": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Order Placed",
  "status": "CONFIRMED"
}
```

## 📁 Project Structure

```
order-service/
├── src/main/java/com/hdbank/orderservice/
│   ├── controller/          # REST Controllers
│   ├── service/            # Business Logic
│   ├── repository/         # Data Access (JPA)
│   ├── model/              # Database Entities
│   ├── dto/                # Data Transfer Objects
│   ├── event/              # Kafka Events
│   ├── producer/           # Event Publisher
│   ├── client/             # External Service Clients
│   ├── exception/          # Custom Exceptions
│   ├── config/             # Configuration Classes
│   └── listener/           # Event Listeners
│
├── src/test/               # Unit & Integration Tests
├── src/main/resources/     # Properties files
├── pom.xml                 # Maven POM
├── Dockerfile              # Docker configuration
├── docker-compose.yml      # Container orchestration
├── Makefile               # Build automation
└── Documentation files    # README, API_GUIDE, etc.
```

## 🔧 Configuration

### Database
- **Type**: PostgreSQL 15
- **URL**: postgresql://localhost:5432/order_db
- **Init Script**: src/main/resources/init.sql

### Kafka
- **Bootstrap Servers**: localhost:9092
- **Topics**: notificationTopic

### Eureka
- **Server**: http://localhost:8761/eureka
- **Service Name**: order-service

### Resilience4j
- **Circuit Breaker**: Failure rate 50%, window size 10
- **Retry**: Max 3 attempts, 1s delay
- **Timeout**: 3 seconds

## 🏃 Available Commands

```bash
# Build & Test
mvn clean package          # Build project
mvn test                   # Run unit tests
mvn verify                 # Run integration tests
mvn jacoco:report         # Coverage report

# Docker
docker-compose up -d      # Start all services
docker-compose down       # Stop all services
docker-compose logs -f    # View logs

# Make
make build                # Build
make run                  # Run locally
make test                 # Run tests
make docker-up           # Start services
make docker-down         # Stop services
make help                # All commands
```

## 🧪 Testing

- **Unit Tests**: OrderServiceTest, OrderControllerTest
- **Integration Tests**: OrderServiceIntegrationTest
- **Coverage Target**: >80%

Run tests:
```bash
mvn test              # Unit tests only
mvn verify           # Including integration tests
mvn jacoco:report    # Generate coverage report
```

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

### Dashboard
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -aon | findstr :8080  # Windows

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -U postgres -h localhost -d order_db
```

### Kafka Not Available
```bash
# Start Kafka with Docker
docker-compose up kafka -d

# Check Kafka health
kafka-broker-api-versions --bootstrap-server localhost:9092
```

## 📚 Documentation

- **README.md** - Project overview & features
- **API_GUIDE.md** - Complete API endpoints documentation
- **PROJECT_STRUCTURE.md** - Architecture & code organization
- **DEPLOYMENT_GUIDE.md** - Production deployment & scaling
- **GETTING_STARTED.md** - This file

## 🔄 Git Branch

You're currently on the **order** branch:
```bash
git checkout order      # Switch to order branch
git status             # Check current status
git log --oneline      # View commits
```

## 📋 Next Steps

1. **Explore the API**: Check API_GUIDE.md
2. **Read the documentation**: See PROJECT_STRUCTURE.md
3. **Run tests**: Execute `mvn test`
4. **Deploy locally**: Run `docker-compose up -d`
5. **Monitor**: Open Grafana at http://localhost:3000

## 🚀 Deploy to Production

See DEPLOYMENT_GUIDE.md for:
- Kubernetes deployment
- CI/CD pipeline setup
- Environment configuration
- Backup & recovery strategies
- Performance optimization
- Scaling guidelines

## 💡 Key Features Implemented

### 1. REST API ✅
- POST /api/order - Create order (201 Created)
- GET /api/order/{orderNumber} - Get order details
- GET /api/order/id/{id} - Get by ID
- GET /api/order/health - Health check

### 2. Database Integration ✅
- Order entity with cascade delete
- OrderLineItems with foreign key
- Automatic schema creation
- Hibernat ORM mapping

### 3. Event-Driven Architecture ✅
- Kafka producer for order events
- Event listener for notifications
- Topic: notificationTopic
- JSON serialization

### 4. Resilience Patterns ✅
- Circuit Breaker (Resilience4j)
- Automatic retry mechanism
- Timeout handling
- Fallback implementation

### 5. Observability ✅
- Prometheus metrics endpoint
- Actuator health checks
- Zipkin distributed tracing
- Request/response logging

### 6. Container Ready ✅
- Multi-stage Dockerfile
- Docker Compose orchestration
- Health checks configured
- Environment-based config

### 7. Testing ✅
- Unit tests with Mockito
- Integration tests with TestContainers
- Controller tests with MockMvc
- Code coverage tracking

## 📞 Support & Tips

### Common Tasks

**View all dependencies:**
```bash
mvn dependency:tree
```

**Format code:**
```bash
mvn spotless:apply
```

**Check for vulnerabilities:**
```bash
mvn org.owasp:dependency-check-maven:check
```

**Generate JavaDoc:**
```bash
mvn javadoc:javadoc
```

## 🎓 Learning Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Resilience4j](https://resilience4j.readme.io/)
- [Kafka Spring Integration](https://spring.io/guides/gs/messaging-kafka/)
- [Micrometer Monitoring](https://micrometer.io/)

## 📝 License

This project is licensed under the MIT License.

---

**Version**: 1.0.0
**Last Updated**: March 22, 2024
**Branch**: order
**Status**: ✅ Ready for Development

For detailed information, check the documentation files in the project root.
