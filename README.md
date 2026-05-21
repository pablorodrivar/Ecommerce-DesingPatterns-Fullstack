# Ecommerce Platform — Spring Boot Microservices

Plataforma de e-commerce construida con arquitectura de microservicios, mensajería asíncrona con Kafka, búsqueda con Elasticsearch y persistencia relacional con PostgreSQL.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal |
| Spring Boot 3.2 | Framework base de cada microservicio |
| Spring Data JPA | Persistencia relacional |
| Spring Kafka | Mensajería asíncrona |
| Spring Data Elasticsearch | Motor de búsqueda |
| Spring Validation | Validación de DTOs |
| PostgreSQL 15 | Base de datos relacional |
| Apache Kafka | Bus de eventos |
| Elasticsearch 8.11 | Índice de búsqueda de productos |
| Kibana 8.11 | Dashboard de Elasticsearch |
| Lombok | Reducción de boilerplate |
| MapStruct | Mapeo entre entidades y DTOs |
| Docker + Docker Compose | Infraestructura local |
| Maven (multi-módulo) | Gestión de dependencias y build |

## Arquitectura

```
Cliente
   │
   ▼
API Gateway (Spring Cloud Gateway + JWT)   [puerto 8080]
   │
   ├──▶ Order Service        [puerto 8081]  ──▶ PostgreSQL (orders_db)
   │         │
   │         └──▶ Kafka: order.created
   │                   │
   │                   ├──▶ Inventory Service   [puerto 8082]  ──▶ PostgreSQL (inventory_db)
   │                   └──▶ Notification Service [puerto 8084]
   │
   ├──▶ Inventory Service    [puerto 8082]
   └──▶ Search Service       [puerto 8083]  ──▶ Elasticsearch
```

### Flujo principal

1. El cliente crea un pedido via `POST /api/v1/orders`
2. `order-service` persiste el pedido y publica el evento `order.created` en Kafka
3. `inventory-service` consume el evento y descuenta el stock de cada producto
4. `notification-service` consume el evento y envía confirmación al cliente
5. Los productos se indexan en Elasticsearch para búsqueda full-text

## Patrones de diseño aplicados

- **DTO pattern** — separación entre entidades JPA y objetos de transferencia de datos
- **Global Exception Handler** — manejo centralizado de errores con `@RestControllerAdvice`
- **Strategy pattern** — canales de notificación intercambiables (`EmailNotificationChannel`, `LogNotificationChannel`)
- **Repository pattern** — abstracción de acceso a datos con Spring Data
- **Dependency Injection** — inyección limpia via constructor con `@RequiredArgsConstructor`
- **Pessimistic Locking** — control de concurrencia en actualizaciones de stock

## Estructura del proyecto

```
ecommerce-platform/
├── common/                          # DTOs, excepciones y respuestas compartidas
│   └── src/main/java/com/ecommerce/common/
│       ├── dto/
│       ├── exception/
│       │   ├── BusinessException.java
│       │   └── GlobalExceptionHandler.java
│       └── response/
│           └── ApiResponse.java
├── order-service/                   # Gestión de pedidos
│   └── src/main/java/com/ecommerce/order/
│       ├── controller/OrderController.java
│       ├── service/OrderService.java
│       ├── repository/OrderRepository.java
│       ├── entity/Order.java
│       ├── entity/OrderItem.java
│       ├── entity/OrderStatus.java
│       ├── dto/
│       └── mapper/OrderMapper.java
├── inventory-service/               # Gestión de productos y stock
│   └── src/main/java/com/ecommerce/inventory/
│       ├── controller/InventoryController.java
│       ├── service/InventoryService.java
│       ├── repository/ProductRepository.java
│       ├── entity/Product.java
│       ├── consumer/OrderCreatedConsumer.java
│       └── dto/
├── search-service/                  # Búsqueda con Elasticsearch
│   └── src/main/java/com/ecommerce/search/
│       ├── controller/SearchController.java
│       ├── service/ProductSearchService.java
│       ├── repository/ProductSearchRepository.java
│       ├── document/ProductDocument.java
│       └── dto/
├── notification-service/            # Notificaciones (Strategy pattern)
│   └── src/main/java/com/ecommerce/notification/
│       ├── consumer/OrderEventConsumer.java
│       ├── service/NotificationChannel.java
│       ├── service/EmailNotificationChannel.java
│       ├── service/LogNotificationChannel.java
│       └── service/NotificationService.java
├── docker-compose.yml
├── init-db.sql
└── pom.xml
```

## Requisitos previos

- Java 21
- Maven 3.9+
- Docker Desktop

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/pablorodrivar/ecommerce-platform.git
cd ecommerce-platform
```

### 2. Levantar la infraestructura

```bash
docker-compose up -d
```

Esto inicia PostgreSQL, Kafka, Zookeeper, Elasticsearch y Kibana.

Crear las bases de datos (solo la primera vez):

```bash
docker exec -it ecommerce-postgres psql -U postgres -c "CREATE DATABASE orders_db;"
docker exec -it ecommerce-postgres psql -U postgres -c "CREATE DATABASE inventory_db;"
```

### 3. Compilar el proyecto

```bash
mvn clean install -DskipTests
```

### 4. Arrancar los servicios

Abrir una terminal por servicio:

```bash
cd order-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd search-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

## Endpoints principales

### Order Service — `localhost:8081`

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/orders` | Crear pedido |
| GET | `/api/v1/orders/{id}` | Obtener pedido por ID |
| GET | `/api/v1/orders/customer/{customerId}` | Pedidos de un cliente |
| PATCH | `/api/v1/orders/{id}/status?status=CONFIRMED` | Actualizar estado |

### Inventory Service — `localhost:8082`

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/products` | Crear producto |
| GET | `/api/v1/products` | Listar todos los productos |
| GET | `/api/v1/products/{id}` | Obtener producto por ID |
| GET | `/api/v1/products/category/{category}` | Filtrar por categoría |
| PATCH | `/api/v1/products/{id}/stock` | Actualizar stock |
| GET | `/api/v1/products/low-stock?threshold=10` | Productos con stock bajo |

### Search Service — `localhost:8083`

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/search/index` | Indexar producto en Elasticsearch |
| GET | `/api/v1/search?query=laptop` | Búsqueda por nombre |
| GET | `/api/v1/search?category=Electronics` | Filtrar por categoría |
| GET | `/api/v1/search?minPrice=100&maxPrice=500` | Filtrar por precio |
| GET | `/api/v1/search/advanced?q=laptop` | Búsqueda avanzada multi-campo |
| DELETE | `/api/v1/search/{productId}` | Eliminar del índice |

## Ejemplos de uso

### Crear un producto

```bash
curl -X POST http://localhost:8082/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell XPS",
    "description": "Laptop de alta gama",
    "price": 1299.99,
    "stock": 50,
    "category": "Electronics"
  }'
```

### Crear un pedido

```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "productName": "Laptop Dell XPS",
        "quantity": 2,
        "unitPrice": 1299.99
      }
    ]
  }'
```

### Indexar y buscar en Elasticsearch

```bash
curl -X POST http://localhost:8083/api/v1/search/index \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "name": "Laptop Dell XPS",
    "description": "Laptop de alta gama",
    "category": "Electronics",
    "price": 1299.99,
    "stock": 48
  }'

curl "http://localhost:8083/api/v1/search?query=Laptop"
```

## Formato de respuesta

Todos los endpoints devuelven una respuesta uniforme:

```json
{
  "success": true,
  "message": "OK",
  "data": { ... },
  "timestamp": "2026-05-21T13:52:05"
}
```

En caso de error:

```json
{
  "success": false,
  "message": "Order with id 99 not found",
  "errors": "ORDER_NOT_FOUND",
  "timestamp": "2026-05-21T13:52:05"
}
```

## Puertos de la infraestructura

| Servicio | Puerto |
|---|---|
| PostgreSQL | 5432 |
| Kafka | 9092 |
| Elasticsearch | 9200 |
| Kibana | 5601 |
