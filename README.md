# Notification Service (CNLTHD_Bai4)

Spring Boot microservice to consume order notifications from Kafka and process them.

## Tech Stack

- Spring Boot 3
- Spring Web
- Spring Kafka
- Eureka Client
- Spring Boot Actuator
- Micrometer Tracing + Zipkin
- Prometheus Registry
- Lombok

## Kafka Message Format

Topic: `notificationTopic`

```json
{
	"orderNumber": "ORD123",
	"message": "Order Placed Successfully"
}
```

## Key Endpoints

- Health check: `GET /actuator/health`
- Prometheus metrics: `GET /actuator/prometheus`
- View processed notifications: `GET /api/notifications`

## Run Locally

```bash
mvn clean spring-boot:run
```

## Run Tests

```bash
mvn test
```

## Run With Docker Compose

```bash
docker compose up --build
```

This starts:
- Zookeeper
- Kafka
- Zipkin
- Eureka Server
- Notification Service