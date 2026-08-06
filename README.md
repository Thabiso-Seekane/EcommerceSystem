# EcommerceSystem — Order Service

A RESTful Spring Boot e-commerce backend implementing product catalogue management, order placement, order cancellation, and sales reporting.

---

## Requirements

- Java 17+
- PostgreSQL 15+
- Maven 3.9+ (or use the included `mvnw` wrapper)

---

## Database Setup

Connect to PostgreSQL as a superuser and run:

```sql
CREATE USER thabiso WITH PASSWORD 'crypto007#';
CREATE DATABASE ecommerce_db OWNER thabiso;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO thabiso;
GRANT ALL ON SCHEMA public TO thabiso;
```

---

## Installation

```bash
git clone https://github.com/Thabiso-Seekane/EcommerceSystem.git
cd EcommerceSystem
```

---

## Running

```bash
./mvnw spring-boot:run
```

The application starts on **http://localhost:8081**.

On startup, Spring automatically executes:
- `schema.sql` — drops and recreates all tables
- `data.sql` — seeds 10 supermarket products

---

## Testing

```bash
./mvnw test
```

11 tests across:
- `ProductRepositoryTest` — category and name search
- `OrderRepositoryTest` — status filtering and persistence
- `OrderServiceTest` — order placement, insufficient stock, cancellation, already-cancelled guard
- `OrderControllerIT` — full HTTP integration tests against live PostgreSQL

---

## API Endpoints

### Products

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/products` | Create a product |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products?page=0&size=20&sort=name&category=Dairy&name=milk` | List products with filtering and pagination |

**Create product request:**
```json
{
  "name": "Full Cream Milk 2L",
  "description": "Fresh full cream milk",
  "price": 32.99,
  "stockQuantity": 120,
  "category": "Dairy"
}
```

---

### Orders

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/orders` | Place an order |
| GET | `/api/orders/{id}` | Get order with items and totals |
| POST | `/api/orders/{id}/cancel` | Cancel an order and restore stock |

**Place order request:**
```json
{
  "items": [
    { "productId": 1, "quantity": 3 },
    { "productId": 4, "quantity": 1 }
  ]
}
```

---

### Reports

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/reports/top-products?startDate=2024-01-01&endDate=2024-12-31&limit=10` | Top-selling products by quantity |

---

## Error Responses

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-08-06T18:00:00",
  "status": 400,
  "error": "Insufficient Stock",
  "message": "Insufficient stock for product 'Brown Bread'. Available: 2, requested: 5"
}
```
