.PHONY: help build run test clean docker-build docker-up docker-down

help:
	@echo "Order Service - Available Commands"
	@echo "=================================="
	@echo "make build           - Build the project with Maven"
	@echo "make run             - Run the application locally"
	@echo "make test            - Run unit tests"
	@echo "make integration-test - Run integration tests"
	@echo "make coverage        - Generate code coverage report"
	@echo "make clean           - Clean build artifacts"
	@echo "make docker-build    - Build Docker image"
	@echo "make docker-up       - Start all services with Docker Compose"
	@echo "make docker-down     - Stop all services"
	@echo "make docker-logs     - View Docker logs"
	@echo "make sonar           - Run SonarQube analysis"

build:
	mvn clean package -DskipTests
	@echo "✅ Build completed successfully"

run:
	mvn spring-boot:run
	@echo "✅ Application is running on http://localhost:8080"

test:
	mvn test
	@echo "✅ Unit tests completed"

integration-test:
	mvn verify
	@echo "✅ Integration tests completed"

coverage:
	mvn jacoco:report
	@echo "✅ Coverage report generated: target/site/jacoco/index.html"

clean:
	mvn clean
	docker-compose down -v
	@echo "✅ Cleanup completed"

docker-build:
	docker-compose build --no-cache
	@echo "✅ Docker image built"

docker-up:
	docker-compose up -d
	@echo "✅ All services started"
	@echo "📍 Order Service: http://localhost:8080"
	@echo "📍 Eureka: http://localhost:8761"
	@echo "📍 Zipkin: http://localhost:9411"
	@echo "📍 Prometheus: http://localhost:9090"
	@echo "📍 Grafana: http://localhost:3000"

docker-down:
	docker-compose down
	@echo "✅ All services stopped"

docker-logs:
	docker-compose logs -f order-service

install-deps:
	mvn dependency:resolve
	mvn dependency:resolve-plugins
	@echo "✅ Dependencies installed"

format:
	mvn spotless:apply
	@echo "✅ Code formatted"

lint:
	mvn checkstyle:check
	@echo "✅ Code linting completed"

sonar:
	mvn clean verify sonar:sonar
	@echo "✅ SonarQube analysis completed"

push-docker:
	docker build -t hdbank/order-service:latest .
	docker push hdbank/order-service:latest
	@echo "✅ Docker image pushed"

deploy-k8s:
	kubectl apply -f k8s/
	@echo "✅ Deployed to Kubernetes"

.DEFAULT_GOAL := help
