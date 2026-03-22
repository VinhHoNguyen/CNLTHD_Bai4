# Order Service - Deployment Guide

## Local Development Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- Git
- Docker & Docker Compose (optional)
- PostgreSQL 15 (optional if using Docker)
- Kafka (optional if using Docker)

### Step 1: Clone Repository
```bash
git clone <repository-url>
cd order-service
git checkout order
```

### Step 2: Build Project
```bash
mvn clean package -DskipTests
```

### Step 3: Configure Database
Create PostgreSQL database:
```bash
# Using Docker
docker run -d \
  --name postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=order_db \
  -p 5432:5432 \
  postgres:15-alpine
```

Or use local PostgreSQL:
```sql
CREATE DATABASE order_db;
```

### Step 4: Start Dependencies (Optional)

**Start Zookeeper & Kafka**:
```bash
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:7.5.0
docker run -d --name kafka \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -p 9092:9092 \
  confluentinc/cp-kafka:7.5.0
```

**Start Eureka Server**:
```bash
docker run -d --name eureka -p 8761:8761 springcloud/eureka-server:latest
```

**Start Zipkin**:
```bash
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin
```

### Step 5: Run Application
```bash
mvn spring-boot:run
```

Application will be available at: `http://localhost:8080`

---

## Docker Deployment

### Quick Start with Docker Compose

```bash
# Navigate to project directory
cd order-service

# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f order-service

# Stop all services
docker-compose down
```

### Access Services
After `docker-compose up -d`:
- **Order Service**: http://localhost:8080
- **Eureka**: http://localhost:8761
- **Zipkin**: http://localhost:9411
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **PostgreSQL**: localhost:5432

### Building Docker Image Manually

```bash
# Build image
docker build -t order-service:1.0.0 .

# Run container
docker run -d \
  --name order-service \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/order_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka:8761/eureka \
  order-service:1.0.0
```

---

## Production Deployment

### Pre-deployment Checklist
- [ ] Code review completed
- [ ] All tests passed (coverage > 80%)
- [ ] Security credentials secured in environment variables
- [ ] Database backups configured
- [ ] Monitoring and alerting setup
- [ ] Load testing completed
- [ ] Disaster recovery plan in place

### Environment Variables for Production

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/order_db
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka1:8761/eureka,http://eureka2:8761/eureka

# Zipkin
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=production
LOGGING_LEVEL_ROOT=INFO
```

### Kubernetes Deployment

Create `k8s/deployment.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  namespace: microservices
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: registry.example.com/order-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: order-config
              key: db.url
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          valueFrom:
            configMapKeyRef:
              name: order-config
              key: kafka.servers
        resources:
          requests:
            cpu: 500m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1024Mi
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: order-service
  namespace: microservices
spec:
  selector:
    app: order-service
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

Deploy to Kubernetes:
```bash
kubectl apply -f k8s/deployment.yaml
kubectl get pods -n microservices
kubectl logs -f pod/order-service-xxxxx -n microservices
```

---

## CI/CD Pipeline

### GitHub Actions Example

Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy Order Service

on:
  push:
    branches: [ main, production ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up Java
      uses: actions/setup-java@v2
      with:
        java-version: '17'
    
    - name: Build with Maven
      run: mvn clean package
    
    - name: Run tests
      run: mvn test
    
    - name: Generate coverage report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v2
    
    - name: Build Docker image
      if: github.event_name == 'push'
      run: docker build -t order-service:${{ github.sha }} .
    
    - name: Push to Docker Registry
      if: github.event_name == 'push'
      run: |
        docker tag order-service:${{ github.sha }} registry.example.com/order-service:latest
        docker push registry.example.com/order-service:latest
    
    - name: Deploy to Kubernetes
      if: github.ref == 'refs/heads/production'
      run: kubectl set image deployment/order-service order-service=registry.example.com/order-service:latest
```

---

## Monitoring & Health Checks

### Health Check Endpoint
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {...}
}
```

### Prometheus Metrics
```bash
curl http://localhost:9090/api/v1/targets
```

### Grafana Dashboard
1. Access http://localhost:3000
2. Login with admin/admin
3. Add Prometheus data source: http://prometheus:9090
4. Import dashboard ID: 11378 (Spring Boot)

### Log Aggregation
Configure ELK Stack or Splunk:
```bash
# Shipping logs to ELK
docker run -d \
  -e ELASTICSEARCH_HOSTS=elasticsearch:9200 \
  -v /var/lib/docker/containers:/var/lib/docker/containers:ro \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  elastic/filebeat:latest
```

---

## Performance Optimization

### Database Optimization
```sql
-- Create indexes
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created ON orders(created_at);
CREATE INDEX idx_line_items_order ON order_line_items(order_id);

-- Analyze query plans
EXPLAIN ANALYZE SELECT * FROM orders WHERE status = 'CONFIRMED';
```

### Connection Pooling
Update `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Caching
Add Redis for caching:
```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

### Request Optimization
```properties
# Enable compression
server.compression.enabled=true
server.compression.min-response-size=1024

# Connection timeout
server.tomcat.connection-timeout=60000
```

---

## Scaling

### Horizontal Scaling
```bash
# Scale deployment in Kubernetes
kubectl scale deployment order-service --replicas=5 -n microservices

# Load balancing via service
kubectl get svc order-service -n microservices
```

### Database Replication
```sql
-- Setup PostgreSQL replication
-- Primary: postgres1
-- Standbys: postgres2, postgres3
```

### Kafka Partitioning
```bash
# Create topic with multiple partitions
kafka-topics --create --topic notificationTopic \
  --partitions 3 --replication-factor 3 \
  --bootstrap-server kafka:9092
```

---

## Backup & Recovery

### Database Backup
```bash
# PostgreSQL backup
pg_dump -U postgres -h localhost order_db > backup.sql

# Restore
psql -U postgres -h localhost order_db < backup.sql
```

### Configuration Backup
```bash
# Backup config files
tar -czf config-backup.tar.gz \
  application.properties \
  application-docker.properties \
  prometheus.yml
```

---

## Rollback Strategy

### Docker Rollback
```bash
# Rollback to previous image
docker-compose down
docker-compose up -d  # Uses previous image from docker-compose history
```

### Kubernetes Rollback
```bash
# View rollout history
kubectl rollout history deployment/order-service -n microservices

# Rollback to previous version
kubectl rollout undo deployment/order-service -n microservices
```

---

## Troubleshooting

### Service Won't Start
```bash
# Check logs
docker logs order-service

# Check database connection
psql -U postgres -h localhost -d order_db

# Check Kafka
kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Out of Memory
```bash
# Increase JVM memory
export JAVA_OPTS="-Xmx512m -Xms256m"
mvn spring-boot:run
```

### High Latency
1. Check database query performance
2. Review Prometheus metrics
3. Check circuit breaker status
4. Verify Kafka throughput

---

## Security Checklist

- [ ] Database credentials in environment variables
- [ ] Enable HTTPS/TLS
- [ ] Add authentication middleware
- [ ] Rate limiting configured
- [ ] CORS configured
- [ ] SQL injection prevention (using ORM)
- [ ] Secrets management (HashiCorp Vault)
- [ ] Security scanning (SonarQube, OWASP)

---

## Maintenance

### Scheduled Tasks
- Database maintenance (VACUUM, ANALYZE)
- Log rotation
- Backup verification
- Security patches
- Dependency updates

### Monitoring Dashboards
- Error rate
- Response time
- Database connection pool
- JVM memory usage
- Circuit breaker state

---

**Last Updated**: March 22, 2024
**Order Service v1.0.0**
