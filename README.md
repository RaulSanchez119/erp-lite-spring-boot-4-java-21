# ERP Lite

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-6DB33F?logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-9.4.1-02303A?logo=gradle&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-8-47A248?logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-AMQP-FF6600?logo=rabbitmq&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS-S3%20(LocalStack)-232F3E?logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

API backend de un ERP ligero construida con **DDD**, **Arquitectura Hexagonal** y **CQRS**, usando PostgreSQL como modelo de escritura y MongoDB como modelo de lectura.

**Índice rápido:** [Swagger / Demo](#swagger--demo) · [Arquitectura](#arquitectura) · [Cómo ejecutarlo](#como-ejecutar-en-local) · [Endpoints](#endpoints)

---

## Swagger / Demo

> 🔗 **Swagger UI en vivo:** `PENDIENTE — añadir aquí el link cuando esté desplegado`
>
> Si aún no lo has desplegado, prueba con [Render](https://render.com) o [Railway](https://railway.app) (ambos con capa gratuita y soporte para Docker). Recomendado: usar un perfil `demo` con solo PostgreSQL para simplificar el despliegue público.

En local, una vez levantado el proyecto (ver más abajo), Swagger está disponible en:

```text
http://localhost:9090/swagger-ui.html
http://localhost:9090/v3/api-docs
```

## Descripción

ERP Lite es una API backend construida con Java 21, Gradle y Spring Boot 4.0.0. El proyecto está organizado como una aplicación multi-módulo con una separación clara entre dominio, casos de uso, infraestructura y API HTTP.

La aplicación modela un ERP ligero centrado en productos, pedidos, catálogos, clientes externos, imágenes de producto, cache y eventos. La estructura apunta a DDD, arquitectura hexagonal y un patrón CQRS sencillo: PostgreSQL se usa como modelo transaccional de escritura y MongoDB como modelo de lectura/catálogos.

## Módulos

| Módulo | Responsabilidad |
| --- | --- |
| `erp-common` | Código compartido entre módulos. Actualmente contiene constantes de cache y el enum `CatalogType`. |
| `erp-domain` | Modelo de dominio, agregados, value objects, eventos y puertos. No depende de Spring ni de infraestructura. |
| `erp-application` | Casos de uso, comandos, queries, excepciones de aplicación y configuración de seguridad/JWT. |
| `erp-infrastructure` | Adaptadores para PostgreSQL/JPA, MongoDB, Redis, LocalStack/S3, RabbitMQ, Gmail y JSONPlaceholder. |
| `erp-api` | Aplicación Spring Boot, controladores REST, configuración HTTP, OpenAPI, carga de YAMLs y manejo global de errores. |

## Stack

- Java 21 mediante Gradle toolchain.
- Gradle Wrapper con Gradle `9.4.1`.
- Spring Boot `4.0.0`.
- Spring Web MVC.
- Spring Security con JWT.
- Spring Data JPA y PostgreSQL.
- Spring Data MongoDB.
- Spring Data Redis y Spring Cache.
- RabbitMQ mediante Spring AMQP.
- AWS SDK S3, configurado para LocalStack.
- Java Mail Sender con Gmail SMTP.
- MapStruct y Lombok.
- Springdoc OpenAPI.
- JUnit 5.

## Arquitectura

El flujo general separa escritura y lectura (CQRS):

- Los comandos de productos y pedidos trabajan con agregados de dominio y guardan en PostgreSQL.
- Las consultas de productos y catálogos leen desde MongoDB, usando Redis como cache cuando hay datos disponibles.
- La creación de productos publica el evento `ProductCreated` en RabbitMQ.
- Las imágenes de productos se suben a S3 mediante el adaptador `AwsImageStorageService`; en local se usa LocalStack.
- La creación de pedidos consulta clientes en JSONPlaceholder y envía un correo mediante `GmailAdapter`.

```text
                     ┌──────────────────────┐
                     │        erp-api        │  Controllers REST + OpenAPI
                     └──────────┬───────────┘
                                │
                     ┌──────────▼───────────┐
                     │    erp-application     │  Casos de uso, comandos, queries
                     └──────────┬───────────┘
                                │
                     ┌──────────▼───────────┐
                     │      erp-domain        │  Agregados, VOs, eventos, puertos
                     └──────────┬───────────┘
                                │
                     ┌──────────▼───────────┐
                     │   erp-infrastructure   │  Adaptadores: JPA, Mongo, Redis, S3, RabbitMQ
                     └──────────┬───────────┘
                                │
        ┌──────────┬───────────┼───────────┬──────────┐
   PostgreSQL   MongoDB      Redis      RabbitMQ    S3 / LocalStack
   (escritura)  (lectura)   (cache)    (eventos)   (imágenes)
```

## Dominio

### Productos

El agregado principal es `ProductRoot`. Gestiona:

- SKU único.
- Nombre, descripción, precio, stock y categoría.
- Imagen de producto.
- Activación/desactivación.
- Auditoría básica.
- Eventos de dominio: `ProductCreated`, `ProductUpdated`, `StockChanged` y `ProductDeactivated`.

Reglas destacadas:

- El SKU se valida con el value object `SKU`.
- El precio se representa con `Money`.
- El stock se representa con `Stock` y no puede quedar negativo.
- El producto puede desactivarse como baja lógica.

### Pedidos

El agregado principal es `OrderRoot`. Gestiona:

- Número de pedido.
- Cliente.
- Items de pedido.
- Estado.
- Total.
- Eventos de dominio: `OrderCreated`, `OrderConfirmed`, `OrderShipped`, `OrderDelivered` y `OrderCancelled`.

Estados soportados:

- `PENDING`
- `CONFIRMED`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

Transiciones implementadas desde los casos de uso:

- `PENDING -> CONFIRMED`
- `CONFIRMED -> SHIPPED`
- `SHIPPED -> DELIVERED`
- Cancelación mediante flujo separado.

### Value Objects

El dominio incluye value objects para encapsular reglas pequeñas pero importantes:

`Money` · `Quantity` · `Email` · `CustomerId` · `ProductId` · `ProductName` · `ProductImage` · `SKU` · `Stock` · `OrderId` · `OrderItemId` · `OrderNumber` · `AuditInfo`

## Persistencia

### PostgreSQL

PostgreSQL guarda el modelo transaccional:

- `products`
- `orders`
- `order_products`

Scripts:

- `db/postgresql/01-schema.sql`
- `db/postgresql/02-data.sql` (carga 20 productos y 15 pedidos de prueba)

Adaptadores principales: `ProductRepositoryAdapter`, `OrderRepositoryAdapter`
Repositorios Spring Data: `ProductRepository`, `OrderRepository`, `OrderProductRepository`

### MongoDB

MongoDB guarda catálogos y documentos de lectura:

- `catalogs`
- `product_documents`
- `audit_logs`

Script de inicialización: `db/mongodb/init-mongo.js`

Catálogos iniciales: `PRODUCT_CATEGORIES`, `ORDER_STATUSES`, `PAYMENT_METHODS`, `SHIPPING_METHODS`, `COUNTRIES`, `CURRENCIES`

Adaptadores principales: `CatalogRepositoryAdapter`, `ProductCatalogRepositoryAdapter`

### Redis

Redis se configura como cache y también se usa directamente mediante `RedisTemplate<String, Object>`.

Constantes de cache usadas: productos por id, productos por SKU, productos por categoría, productos activos, catálogos por tipo, items de catálogo.

### S3 / LocalStack

Las imágenes se guardan con el puerto `ImageStorageServicePort` y el adaptador `AwsImageStorageService`.

Configuración local:

- Endpoint: `http://localhost:4566`
- Region: `us-east-1`
- Bucket: `erp-products-images`
- Path-style access: habilitado.

Configuración en `erp-api/src/main/resources/aws/aws.yaml`.

### RabbitMQ

RabbitMQ publica eventos mediante `RabbitMQEventPublisherAdapter`.

- Exchange: `erp.events`
- Queue: `erp.product.created`
- Routing key: `product.created`

Actualmente el publicador soporta `ProductCreated`.

### JSONPlaceholder

Integración externa de clientes:

- Base URL: `https://jsonplaceholder.typicode.com`
- Endpoint: `/users/{id}`

Adaptador: `JsonPlaceholderCustomerProviderServiceAdapter`. El método `findById` está implementado. El método `existsById` existe pero actualmente devuelve `false`.

### Gmail

El envío de email usa `GmailAdapter`, `JavaMailSender` y una plantilla HTML: `erp-infrastructure/src/main/resources/templates/email-order-confirm-template.html`

La configuración SMTP está en `application.yaml` y espera variables de entorno: `EMAIL_USERNAME`, `EMAIL_PASSWORD`.

En desarrollo pueden configurarse desde IntelliJ como variables de entorno de la configuración de ejecución.

## Servicios locales

`docker-compose.yml` levanta:

| Servicio | Puerto local | Notas |
| --- | ---: | --- |
| PostgreSQL 17 | `5432` | DB `erp_db` — credenciales de ejemplo, ver `.env.example` |
| MongoDB 8 | `27017` | DB `erp_catalog_db` — credenciales de ejemplo, ver `.env.example` |
| Redis 7 | `6379` | Password de ejemplo, ver `.env.example` |
| Redis Commander | `8081` | UI de administración de Redis |
| LocalStack | `4566` | Servicio S3 |
| RabbitMQ | `5672` | Credenciales de ejemplo, ver `.env.example` |
| RabbitMQ Management | `15672` | UI de administración de RabbitMQ |

```powershell
docker compose up -d
```

PostgreSQL y MongoDB inicializan datos desde la carpeta `db`.

> ⚠️ **Nota de seguridad:** las credenciales del `docker-compose.yml` son valores de ejemplo pensados **solo** para desarrollo local. No reutilices estos valores en ningún entorno real y no subas archivos `.env` con credenciales reales al repositorio (usa `.env.example` como plantilla y añade `.env` a `.gitignore`).

## Configuración de la API

Configuración principal:

- `erp-api/src/main/resources/application.yaml`
- `erp-api/src/main/resources/aws/aws.yaml`
- `erp-api/src/main/resources/jsonplaceholder/jsonplaceholder.yaml`

La API arranca en:

```text
http://localhost:9090
```

## Seguridad

Login expuesto en:

```http
POST /auth/login
```

Body de ejemplo:

```json
{
  "username": "admin",
  "password": "<contraseña de ejemplo, ver entorno local>"
}
```

Roles definidos en memoria:

| Usuario | Rol |
| --- | --- |
| `admin` | `ADMIN` |
| `manager` | `MANAGER` |
| `employee` | `USER` |

Reglas de acceso:

- `/auth/**` es público.
- `/swagger-ui/**` y `/v3/api-docs/**` son públicos.
- `GET /api/queries/products/**` es público.
- `/api/commands/**` requiere rol `ADMIN`.
- `/api/queries/catalogs/**` requiere rol `ADMIN` o `MANAGER`.
- El resto de rutas requiere autenticación.

Para llamar endpoints protegidos:

```http
Authorization: Bearer <token>
```

Los controladores de comandos usan versionado por header con valor por defecto `1`:

```http
X-Api-Version: 1
```

## Endpoints

### Autenticación

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/auth/login` | Autentica usuario y devuelve JWT. |

### Comandos de productos

Base path: `/api/commands/products`

| Método | Ruta | Body | Descripción |
| --- | --- | --- | --- |
| `POST` | `/api/commands/products` | `multipart/form-data` con `product` e `image` | Crea producto, sube imagen si llega, guarda en PostgreSQL y publica `ProductCreated`. |
| `PUT` | `/api/commands/products/{id}` | `multipart/form-data` con `product` e `image` | Actualiza producto e imagen. |
| `PATCH` | `/api/commands/products/{id}/deactivate` | Sin body | Desactiva producto. |
| `PATCH` | `/api/commands/products/{id}/stock` | JSON | Incrementa o decrementa stock según el signo de `quantity`. |

Ejemplo de `product` para crear producto:

```json
{
  "sku": "LAPTOP-003",
  "name": "Laptop Lenovo ThinkPad",
  "description": "Business laptop",
  "price": 1299.99,
  "currency": "USD",
  "stock": 10,
  "categoryId": "cat-electronics",
  "createdBy": "admin"
}
```

Ejemplo para actualizar stock:

```json
{
  "quantity": -2,
  "reason": "SALE"
}
```

### Comandos de pedidos

Base path: `/api/commands/orders`

| Método | Ruta | Body | Descripción |
| --- | --- | --- | --- |
| `POST` | `/api/commands/orders` | JSON | Crea un pedido, consulta cliente externo, toma snapshot de productos y envía email. |
| `PATCH` | `/api/commands/orders/{id}/status?status=CONFIRMED` | Query param | Cambia estado a `CONFIRMED`, `SHIPPED` o `DELIVERED`. |
| `PATCH` | `/api/commands/orders/{id}/cancel?reason=...` | Query param | Cancela pedido con motivo. |

Ejemplo para crear pedido:

```json
{
  "customerId": 1,
  "items": [
    {
      "productId": "11111111-1111-1111-1111-111111111111",
      "quantity": 1
    }
  ],
  "createdBy": "admin"
}
```

### Consultas de productos

Base path: `/api/queries/products`

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/queries/products/{id}` | Busca producto por id en el modelo de lectura. |
| `GET` | `/api/queries/products?sku=LAPTOP-001` | Busca producto por SKU. |
| `GET` | `/api/queries/products/active` | Lista productos activos. |
| `GET` | `/api/queries/products/search?text=laptop` | Busca productos por texto. |
| `GET` | `/api/queries/products?category=cat-electronics` | Lista productos por categoría. |

Las respuestas exitosas usan `BaseResponseWrapper`:

```json
{
  "data": {},
  "date": "2026-07-06T13:00:00"
}
```

### Consultas de catálogos

Base path: `/api/queries/catalogs`

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/api/queries/catalogs/{type}` | Obtiene un catálogo completo. |
| `GET` | `/api/queries/catalogs/{type}/items` | Obtiene los items de un catálogo. |
| `GET` | `/api/queries/catalogs/{type}/items?code=ELECTRONICS` | Obtiene un item por código. |

Tipos válidos: `PRODUCT_CATEGORIES`, `ORDER_STATUSES`, `PAYMENT_METHODS`, `SHIPPING_METHODS`, `COUNTRIES`, `CURRENCIES`

## Cómo ejecutar en local

1. Levantar infraestructura:

```powershell
docker compose up -d
```

2. Configurar variables de entorno en IntelliJ o en la terminal:

```powershell
$env:EMAIL_USERNAME="tu-email"
$env:EMAIL_PASSWORD="tu-password-o-app-password"
```

3. Crear el bucket de S3 en LocalStack antes de subir imágenes:

```powershell
aws --endpoint-url=http://localhost:4566 s3 mb s3://erp-products-images
```

4. Arrancar la API:

```powershell
.\gradlew.bat :erp-api:bootRun
```

5. Abrir Swagger:

```text
http://localhost:9090/swagger-ui.html
```

## Comandos útiles

```powershell
# Compilar la API
.\gradlew.bat :erp-api:compileJava

# Ejecutar build completo
.\gradlew.bat build

# Ejecutar tests de dominio
.\gradlew.bat :erp-domain:test
```

## Para qué sirve este proyecto

ERP Lite sirve como ejemplo práctico de una API empresarial modular donde las reglas de negocio no viven en los controladores ni en la base de datos, sino en el dominio. El proyecto permite trabajar con productos, inventario, pedidos, estados de pedido, catálogos de referencia y clientes externos, integrando varias tecnologías habituales en un backend real.

También funciona como base de aprendizaje para practicar:

- DDD con agregados, value objects y eventos de dominio.
- Arquitectura hexagonal con puertos y adaptadores.
- Separación entre comandos y consultas (CQRS).
- Persistencia relacional con PostgreSQL.
- Modelo de lectura y catálogos con MongoDB.
- Cache con Redis.
- Mensajería con RabbitMQ.
- Almacenamiento de archivos en S3 usando LocalStack.
- Seguridad con JWT y roles.
- Integración con APIs externas mediante `RestClient`.
- Envío de correo con plantillas HTML.

## Estructura del proyecto

```text
erp-lite/
├── db/
│   ├── mongodb/
│   │   └── init-mongo.js
│   └── postgresql/
│       ├── 01-schema.sql
│       └── 02-data.sql
├── docs/
├── erp-api/
│   └── src/main/java/com/raulsanchez/erp_lite/
│       ├── configs/
│       ├── controllers/
│       ├── dtos/
│       └── paths/
├── erp-application/
│   └── src/main/java/com/raulsanchez/erp_lite/
│       ├── commands/
│       ├── queries/
│       ├── security/
│       └── use_cases/
├── erp-common/
│   └── src/main/java/com/raulsanchez/erp_lite/
│       ├── constants/
│       └── enums/
├── erp-domain/
│   └── src/main/java/com/raulsanchez/erp_lite/domain/
│       ├── common/
│       ├── entities/
│       ├── exceptions/
│       ├── ports/
│       ├── shared/
│       └── views/
├── erp-infrastructure/
│   └── src/main/java/com/raulsanchez/erp_lite/persistence/
│       ├── aws/
│       ├── jpa/
│       ├── mail/
│       ├── mongo/
│       ├── rabbitt/
│       ├── redis/
│       └── rest/
├── docker-compose.yml
├── build.gradle
└── settings.gradle
```

Cada capa tiene una responsabilidad concreta:

- `erp-domain` define el negocio.
- `erp-application` coordina casos de uso.
- `erp-infrastructure` conecta con tecnologías externas.
- `erp-api` expone la aplicación por HTTP.
- `erp-common` agrupa elementos compartidos que no pertenecen al dominio.

## Postman

El repositorio incluye una colección lista para importar:

```text
ERP-lite.postman_collection.json
```

## Roadmap / próximas mejoras

- [ ] Completar `existsById` en `JsonPlaceholderCustomerProviderServiceAdapter`.
- [ ] Añadir más eventos de dominio al publicador de RabbitMQ (actualmente solo `ProductCreated`).
- [ ] Añadir tests de integración para los adaptadores de infraestructura.
- [ ] Desplegar demo pública con Swagger accesible.

## Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [`LICENSE`](./LICENSE) para más detalles.

---

*Desarrollado como proyecto de aprendizaje práctico de arquitectura backend moderna con Spring Boot 4 y Java 25.*
