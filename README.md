# Order Service - Microservice Architecture

## Overview
Order Service là một microservice được xây dựng bằng Spring Boot để quản lý đơn hàng trong hệ thống Microservices. Service này tích hợp nhiều công nghệ như Eureka, Kafka, Resilience4j, Prometheus, và Zipkin để xây dựng một hệ thống ổn định và có tính năng giám sát cao.

## Quick Start - Docker Deployment

```bash
docker-compose up -d
```

## API Examples

### Place Order
```bash
POST /api/order
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_13",
      "price": 1200.00,
      "quantity": 1
    }
  ]
}
```

### Get Order
```bash
GET /api/order/{orderNumber}
```

## Monitoring

- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/prometheus
- Eureka: http://localhost:8761
- Zipkin: http://localhost:9411
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

## Features

✅ Spring Boot 3.2.0
✅ Spring Data JPA with PostgreSQL
✅ Eureka Service Discovery
✅ Kafka Event Publishing
✅ Resilience4j Circuit Breaker
✅ Actuator & Prometheus
✅ Micrometer Tracing with Zipkin
✅ Unit & Integration Tests
✅ Docker & Docker Compose
✅ Exception Handling

## Testing

```bash
mvn test              # Unit tests
mvn verify           # Integration tests
mvn jacoco:report    # Code coverage
```

## Version

1.0.0 - Initial Release

---

Last Updated: March 22, 2024