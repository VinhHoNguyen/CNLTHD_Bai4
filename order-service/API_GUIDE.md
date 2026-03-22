# Order Service - API Guide

## Base URL
Default: `http://localhost:8080`

## Endpoints

### 1. Place Order ✅
**Endpoint**: `POST /api/order`

**Purpose**: Create a new order with line items

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_13",
      "price": 1200.00,
      "quantity": 1
    },
    {
      "skuCode": "ipad_pro",
      "price": 1500.00,
      "quantity": 2
    }
  ]
}
```

**Success Response** (201 Created):
```json
{
  "orderId": 1,
  "orderNumber": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Order Placed",
  "status": "CONFIRMED"
}
```

**Error Response** (400 Bad Request - Out of Stock):
```json
{
  "orderId": null,
  "orderNumber": null,
  "message": "One or more items are out of stock",
  "status": "FAILED"
}
```

**cURL Example**:
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

**HTTPie Example**:
```bash
http POST localhost:8080/api/order \
  orderLineItemsDtoList:='[
    {
      "skuCode": "iphone_13",
      "price": 1200,
      "quantity": 1
    }
  ]'
```

---

### 2. Get Order by Order Number 🔍
**Endpoint**: `GET /api/order/{orderNumber}`

**Purpose**: Retrieve order details by order number

**Path Parameters**:
- `orderNumber` (string): The unique order number

**Success Response** (200 OK):
```json
{
  "id": 1,
  "orderNumber": "550e8400-e29b-41d4-a716-446655440000",
  "orderLineItemsList": [
    {
      "id": 1,
      "skuCode": "iphone_13",
      "price": 1200.00,
      "quantity": 1
    },
    {
      "id": 2,
      "skuCode": "ipad_pro",
      "price": 1500.00,
      "quantity": 2
    }
  ],
  "createdAt": "2024-03-22T10:30:45.123456",
  "status": "CONFIRMED"
}
```

**Error Response** (404 Not Found):
```
Order not found
```

**cURL Example**:
```bash
curl http://localhost:8080/api/order/550e8400-e29b-41d4-a716-446655440000
```

---

### 3. Get Order by ID 🔍
**Endpoint**: `GET /api/order/id/{id}`

**Purpose**: Retrieve order details by order ID

**Path Parameters**:
- `id` (long): The order ID

**Success Response** (200 OK):
```json
{
  "id": 1,
  "orderNumber": "550e8400-e29b-41d4-a716-446655440000",
  "orderLineItemsList": [...],
  "createdAt": "2024-03-22T10:30:45.123456",
  "status": "CONFIRMED"
}
```

**Error Response** (404 Not Found):
```
Order not found with id: 999
```

**cURL Example**:
```bash
curl http://localhost:8080/api/order/id/1
```

---

### 4. Health Check ❤️
**Endpoint**: `GET /api/order/health`

**Purpose**: Check if service is running

**Response** (200 OK):
```
Order Service is running
```

**cURL Example**:
```bash
curl http://localhost:8080/api/order/health
```

---

## Actuator Endpoints

### Health
**Endpoint**: `GET /actuator/health`

**Response**:
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {...}
    },
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "version": "15.x"
      }
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

### Metrics
**Endpoint**: `GET /actuator/prometheus`

**Returns**: Prometheus-format metrics

### Info
**Endpoint**: `GET /actuator/info`

**Returns**: Application information

---

## Testing Scenarios

### Scenario 1: Successful Order Placement
```bash
# 1. Place order with available items
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

# Expected: 201 Created with order details
```

### Scenario 2: Order Not Found
```bash
# Try to get non-existent order
curl http://localhost:8080/api/order/invalid-order-number

# Expected: 404 Not Found
```

### Scenario 3: Multiple Items in Order
```bash
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderLineItemsDtoList": [
      {
        "skuCode": "iphone_13",
        "price": 1200.00,
        "quantity": 2
      },
      {
        "skuCode": "ipad_pro",
        "price": 1500.00,
        "quantity": 1
      },
      {
        "skuCode": "airpods",
        "price": 200.00,
        "quantity": 3
      }
    ]
  }'
```

---

## Error Handling

### Error Response Structure
```json
{
  "orderId": null,
  "orderNumber": null,
  "message": "Error description here",
  "status": "FAILED" or "ERROR"
}
```

### Common Errors

| Status | Scenario | Message |
|--------|----------|---------|
| 400 | Out of stock | "One or more items are out of stock" |
| 400 | Inventory check failed | "Failed to verify inventory: ..." |
| 404 | Order not found | "Order not found with number/id: ..." |
| 500 | Server error | "Internal server error: ..." |

---

## Request/Response Examples

### Full Order Placement With Response

**Request**:
```bash
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderLineItemsDtoList": [
      {
        "skuCode": "iphone_13",
        "price": 1200.00,
        "quantity": 1
      },
      {
        "skuCode": "ipad_pro",
        "price": 1500.00,
        "quantity": 2
      }
    ]
  }'
```

**Response** (201 Created):
```json
{
  "orderId": 42,
  "orderNumber": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "message": "Order Placed",
  "status": "CONFIRMED"
}
```

**Get Order Details**:
```bash
curl http://localhost:8080/api/order/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Response** (200 OK):
```json
{
  "id": 42,
  "orderNumber": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "orderLineItemsList": [
    {
      "id": 101,
      "skuCode": "iphone_13",
      "price": 1200.00,
      "quantity": 1
    },
    {
      "id": 102,
      "skuCode": "ipad_pro",
      "price": 1500.00,
      "quantity": 2
    }
  ],
  "createdAt": "2024-03-22T15:45:30.123456",
  "status": "CONFIRMED"
}
```

---

## API Testing Tools

### Using Postman
1. Create new POST request to `http://localhost:8080/api/order`
2. Add header: `Content-Type: application/json`
3. Paste JSON body from examples above
4. Click Send

### Using curl
```bash
# Store in file: order.json
{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_13",
      "price": 1200.00,
      "quantity": 1
    }
  ]
}

# Send request
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d @order.json
```

### Using Python
```python
import requests
import json

url = "http://localhost:8080/api/order"
headers = {"Content-Type": "application/json"}
data = {
    "orderLineItemsDtoList": [
        {
            "skuCode": "iphone_13",
            "price": 1200.00,
            "quantity": 1
        }
    ]
}

response = requests.post(url, json=data, headers=headers)
print(response.status_code)
print(response.json())
```

### Using JavaScript/Node.js
```javascript
const axios = require('axios');

const data = {
  orderLineItemsDtoList: [
    {
      skuCode: "iphone_13",
      price: 1200.00,
      quantity: 1
    }
  ]
};

axios.post('http://localhost:8080/api/order', data)
  .then(response => {
    console.log('Order Placed:', response.data);
  })
  .catch(error => {
    console.error('Error:', error.response.data);
  });
```

---

## API Rate Limits & Performance

- **Default Timeout**: 3 seconds per request
- **Circuit Breaker**: Opens after 50% failure rate
- **Retry**: Maximum 3 attempts with 1s delay
- **Concurrent Requests**: Limited by database connection pool

---

## Monitoring API Usage

Check metrics at: `http://localhost:9090` (Prometheus)

Key metrics:
- `http_server_requests_seconds` - Response time
- `http_server_requests_total` - Total requests
- `resilience4j_circuitbreaker_state` - Circuit breaker status

---

**Last Updated**: March 22, 2024
**API Version**: 1.0.0
