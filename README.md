# Inventory Service 

Tai lieu nay huong dan chay va tu kiem tra `inventory-service` 

## 1) Yeu cau moi truong

- Java 21
- Maven 3.9+
- Docker Desktop
- PowerShell (Windows)

## 2) Chay nhanh che do solo

Che do nay chi chay 1 container `inventory-service`, dung H2 profile standalone.

```powershell
Set-Location "D:\Projects\inventory-service-standalone"
docker compose -f docker-compose.solo.yml down
docker rm -f inventory-service-solo
docker compose -f docker-compose.solo.yml up --build -d
docker compose -f docker-compose.solo.yml ps
```

### Kiem tra API

```powershell
Invoke-WebRequest -Uri "http://localhost:8083/api/inventory?skuCode=iphone_13&skuCode=iphone_13_red&skuCode=iphone_18" -UseBasicParsing | Select-Object -ExpandProperty Content
```

Ket qua mong doi (tuong tu):

```json
[{"skuCode":"iphone_13","isInStock":true},{"skuCode":"iphone_13_red","isInStock":false},{"skuCode":"iphone_18","isInStock":false}]
```

### Kiem tra Actuator / Prometheus

```powershell
(Invoke-RestMethod -Uri "http://localhost:8083/actuator/health").status
(Invoke-WebRequest -Uri "http://localhost:8083/actuator/prometheus" -UseBasicParsing).StatusCode
```

Mong doi:
- Health: `UP`
- Prometheus status code: `200`

## 4) Chay test + coverage

```powershell
Set-Location "D:\Projects\inventory-service-standalone"
mvn clean verify
Get-Content "target\site\jacoco\jacoco.csv"
```

Muc tieu: test pass va coverage >= 80%.

## 5) Dung he thong

Solo:

```powershell
Set-Location "D:\Projects\inventory-service-standalone"
docker compose -f docker-compose.solo.yml down
```

