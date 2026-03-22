# CNLTHD_Bai4

## Git ignore and push

This repo contains multiple Maven services. Build outputs in `target/` are large and must not be committed.

If you already tracked build artifacts, remove them from Git history and push again:

```bash
git rm -r --cached **/target
git add .
git commit -m "Remove build outputs"
git push
```

If you need to keep large binaries, use Git LFS instead of committing them directly.

## Setup for the team

### Prerequisites

- JDK 17+ (check with `java -version`)
- Maven 3.8+ (check with `mvn -version`)
- Docker Desktop (optional, for running services with Docker)

### Build all services

From the repo root:

```bash
mvn -q -DskipTests clean package
```

### Run a service (example)

Pick a module and run it:

```bash
cd discovery-server
mvn spring-boot:run
```

Repeat for `inventory-service`, `order-service`, or `product-service` as needed.