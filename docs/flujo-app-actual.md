# ERP Lite - Flujo actual de la aplicacion

Este documento resume que estas construyendo ahora mismo, para que sirve cada parte y como deberia fluir la aplicacion cuando las capas pendientes esten conectadas.

## 1. Idea general

ERP Lite es una aplicacion tipo ERP enfocada, por ahora, en productos, pedidos, catalogos, clientes externos, almacenamiento de imagenes y auditoria.

La arquitectura apunta a DDD y arquitectura hexagonal:

- El dominio contiene las reglas del negocio sin depender de Spring, bases de datos ni HTTP.
- La aplicacion deberia coordinar casos de uso, pero actualmente esta casi vacia.
- La infraestructura contiene adaptadores para PostgreSQL, MongoDB, Redis, AWS S3/LocalStack y JSONPlaceholder.
- La API arranca Spring Boot y carga configuracion, pero todavia no expone controladores REST.

En otras palabras: ahora tienes una base de dominio e infraestructura bastante avanzada, pero aun falta la capa que conecte todo en endpoints reales.

## 2. Modulos del proyecto

### `erp-domain`

Es el corazon del sistema. Aqui viven los conceptos de negocio:

- Productos.
- Pedidos.
- Items de pedido.
- Catalogos.
- Value Objects como dinero, stock, SKU, cantidad, email e IDs.
- Eventos de dominio.
- Puertos que la infraestructura debe implementar.

Este modulo no deberia saber nada de bases de datos, controladores REST, S3 ni APIs externas. Su trabajo es proteger reglas del negocio.

### `erp-application`

Este modulo esta preparado para contener los casos de uso, pero por ahora no tiene implementacion real.

Aqui deberian vivir clases como:

- `CreateProductUseCase`.
- `CreateOrderUseCase`.
- `ConfirmOrderUseCase`.
- `UploadProductImageUseCase`.
- `FindCatalogItemsUseCase`.

Su responsabilidad seria coordinar dominio + repositorios + adaptadores. Por ejemplo, confirmar una orden, guardar cambios y publicar eventos.

### `erp-infrastructure`

Contiene las implementaciones tecnicas:

- Entidades JPA para PostgreSQL.
- Repositorios Spring Data JPA.
- Documentos MongoDB.
- Repositorios Spring Data MongoDB.
- Cliente REST para JSONPlaceholder.
- Servicio de imagenes en S3.
- Configuracion de `RestClient` y `S3Client`.

Este modulo es el puente entre el mundo exterior y el dominio.

### `erp-api`

Es la aplicacion Spring Boot que arranca todo.

Actualmente contiene:

- `ErpLiteApplication`, punto de entrada.
- `application.yaml`, configuracion de PostgreSQL, MongoDB, Redis y servidor.
- `aws/aws.yaml`, configuracion S3/LocalStack.
- `jsonplaceholder/jsonplaceholder.yaml`, configuracion de la API externa.
- Carga de YAMLs personalizados mediante `YamlConfig`.

Por ahora no hay controladores REST (`@RestController`), asi que la app puede arrancar, pero no tiene API funcional expuesta.

### `erp-common`

Existe como modulo comun, pero actualmente no contiene clases utiles. Puede servir mas adelante para utilidades compartidas que no sean dominio.

## 3. Modelo de dominio

### Producto

Clase principal: `Product`.

Representa un producto vendible del ERP.

Campos importantes:

- `ProductId`: identificador UUID.
- `SKU`: codigo unico del producto, por ejemplo `LAPTOP-001`.
- `ProductName`: nombre validado.
- `description`: descripcion.
- `Money price`: precio con moneda.
- `Stock stock`: cantidad disponible.
- `CategoryReference category`: referencia a una categoria en catalogos.
- `ProductImage image`: URL de imagen.
- `active`: indica si se puede vender.
- `AuditInfo`: informacion de creacion/actualizacion.

Comportamientos:

- `create(...)`: crea un producto activo y registra `ProductCreated`.
- `update(...)`: cambia datos principales y registra `ProductUpdated`.
- `incrementStock(...)`: suma stock y registra `StockChanged`.
- `decrementStock(...)`: resta stock, impide negativos y registra `StockChanged`.
- `changePrice(...)`: cambia precio y registra `ProductUpdated`.
- `deactivate()`: desactiva producto y registra `ProductDeactivated`.
- `activate()`: reactiva producto.
- `hasAvailableStock(...)`: valida si se puede vender una cantidad.

Para que sirve: centraliza las reglas sobre productos. Nadie deberia cambiar stock, precio o estado saltandose estos metodos.

### Pedido

Clase principal: `Order`.

Representa una orden de compra.

Campos importantes:

- `OrderId`: identificador UUID.
- `OrderNumber`: numero tipo `ORD-2026-001`.
- `Customer`: snapshot simple del cliente.
- `OrderStatus`: estado del pedido.
- `List<OrderItem>`: lineas del pedido.
- `Money totalAmount`: total calculado.
- `AuditInfo`: auditoria.

Comportamientos:

- `create(...)`: crea una orden en estado `PENDING` y registra `OrderCreated`.
- `confirm()`: pasa de `PENDING` a `CONFIRMED` y registra `OrderConfirmed`.
- `ship()`: pasa de `CONFIRMED` a `SHIPPED` y registra `OrderShipped`.
- `deliver()`: pasa de `SHIPPED` a `DELIVERED` y registra `OrderDelivered`.
- `cancel(reason)`: cancela si la transicion es valida y registra `OrderCancelled`.
- `addItem(...)`: agrega items solo si el pedido esta `PENDING`.
- `removeItem(...)`: elimina items solo si el pedido esta `PENDING`.

Estados validos:

- `PENDING -> CONFIRMED` o `CANCELLED`.
- `CONFIRMED -> SHIPPED` o `CANCELLED`.
- `SHIPPED -> DELIVERED`.
- `DELIVERED` y `CANCELLED` son estados finales.

Para que sirve: protege el ciclo de vida del pedido. No permite, por ejemplo, entregar un pedido que no fue enviado.

### Item de pedido

Clase principal: `OrderItem`.

Representa una linea dentro de una orden.

Cuando se crea desde un `Product`, guarda una foto fija del producto:

- ID del producto.
- Nombre del producto en ese momento.
- Precio unitario en ese momento.
- Cantidad.
- Subtotal.

Esto es importante porque si manana cambia el nombre o precio del producto, los pedidos antiguos deben conservar el precio/nombre con el que se vendieron.

### Catalogos

Clases principales:

- `Catalog`.
- `CatalogItem`.
- `CatalogType`.

Sirven para datos de referencia:

- Categorias de productos.
- Estados de pedido.
- Metodos de pago.
- Metodos de envio.
- Paises.
- Monedas.

MongoDB contiene estos catalogos en la coleccion `catalogs`.

Para que sirve: evita quemar listas fijas en codigo cuando algunos valores pueden ser administrables o consultables desde base de datos.

## 4. Value Objects

Los Value Objects validan reglas pequenas pero importantes:

- `Money`: no permite importes negativos, obliga moneda y escala a 2 decimales.
- `Stock`: no permite stock negativo.
- `Quantity`: representa cantidades mayores que cero.
- `SKU`: valida formato de SKU.
- `ProductName`: valida longitud.
- `ProductImage`: representa URL de imagen.
- `OrderStatus`: implementa la maquina de estados del pedido.
- `OrderNumber`: representa numero de orden.
- `CustomerId`: referencia a cliente externo.
- `Email`: valida formato de email.
- `AuditInfo`: guarda quien creo y cuando se actualizo.

Para que sirven: hacen que los objetos invalidos fallen pronto. En vez de tener un `String sku` sin reglas, tienes `SKU` con reglas propias.

## 5. Eventos de dominio

Los agregados registran eventos cuando pasa algo importante:

Eventos de producto:

- `ProductCreated`.
- `ProductUpdated`.
- `StockChanged`.
- `ProductDeactivated`.

Eventos de pedido:

- `OrderCreated`.
- `OrderConfirmed`.
- `OrderShipped`.
- `OrderDelivered`.
- `OrderCancelled`.

Para que sirven: son senales internas. Por ejemplo:

- Cuando se confirma una orden, `OrderConfirmed` deberia disparar la bajada de stock.
- Cuando se cancela una orden confirmada, `OrderCancelled` deberia liberar stock.
- Cuando cambia un producto, sus eventos podrian sincronizar el documento de lectura en MongoDB.

Estado actual: los eventos se crean y se guardan dentro del agregado, pero no hay publicador/listener implementado todavia.

## 6. Persistencia actual

### PostgreSQL

PostgreSQL guarda la parte transaccional:

- `products`: productos principales.
- `orders`: cabecera de pedidos.
- `order_products`: lineas de pedido.

El script `db/postgresql/01-schema.sql` crea tablas, constraints e indices.

El script `db/postgresql/02-data.sql` carga datos de prueba:

- 20 productos.
- 15 pedidos.
- Lineas de pedido asociadas.

Las entidades JPA equivalentes estan en:

- `ProductEntity`.
- `OrderEntity`.
- `OrderProductEntity`.

Los repositorios JPA son:

- `ProductRepository`.
- `OrderRepository`.
- `OrderProductRepository`.

Estado actual: existen entidades y repositorios, pero faltan mappers entre entidades JPA y objetos de dominio, y faltan adaptadores que implementen repositorios de dominio para productos/pedidos.

### MongoDB

MongoDB guarda datos de lectura/catalogo:

- `catalogs`: catalogos de referencia.
- `product_documents`: documentos enriquecidos de producto para consultas.
- `audit_logs`: logs de auditoria.

El script `db/mongodb/init-mongo.js` inicializa colecciones e indices.

Documentos principales:

- `CatalogDocument`.
- `ProductCatalogDocument`.
- `AuditLogDocument`.

Repositorios Mongo:

- `CatalogRepository`.
- `ProductCatalogRepository`.
- `AuditLogRepository`.

Estado actual: existen documentos y repositorios, pero falta conectar el puerto de dominio `CatalogRepository` con el repositorio Mongo.

### Redis

Redis esta configurado en `docker-compose.yml` y en `application.yaml`.

Para que podria servir:

- Cache de catalogos.
- Cache de productos leidos frecuentemente.
- Sesiones o datos temporales.

Estado actual: esta configurado, pero no hay uso de cache implementado todavia.

## 7. Integraciones externas

### JSONPlaceholder como sistema externo de clientes

Puerto de dominio:

- `CustomerProvider`.

Implementacion:

- `JsonPlaceholderCustomerProviderAdapter`.

DTOs:

- `UserDTO`.
- `AddressDTO`.
- `GeoDTO`.
- `CompanyDTO`.

Mapper:

- `CustomerMapper`.

Flujo esperado:

1. Un caso de uso necesita validar o consultar un cliente.
2. Llama a `CustomerProvider.findById(id)`.
3. La infraestructura usa `RestClient` contra JSONPlaceholder.
4. El DTO externo se transforma a `CustomerInfo`.
5. El dominio o aplicacion trabaja con un modelo propio, no con el DTO externo.

Estado actual:

- `findById` esta implementado.
- `existsById` devuelve siempre `false`, falta completarlo.

### AWS S3 / LocalStack para imagenes

Puerto de dominio:

- `ImageStorageService`.

Implementacion:

- `AwsImageStorageService`.

Configuracion:

- `S3BucketConfig`.
- `AwsConfigModel`.
- `aws/aws.yaml`.
- `localstack` en `docker-compose.yml`.

Flujo esperado:

1. La API recibe una imagen.
2. Un caso de uso llama a `ImageStorageService.upload(...)`.
3. El adaptador sube bytes a S3/LocalStack.
4. Devuelve un `ProductImage` con URL.
5. El producto guarda esa URL.

Estado actual: el servicio tiene `upload`, `delete` y `download`, pero no hay endpoint ni caso de uso que lo consuma.

## 8. Flujo funcional esperado

### Crear producto

Flujo ideal:

1. API recibe datos del producto.
2. Caso de uso valida categoria consultando catalogos.
3. Si hay imagen, la sube a S3.
4. Crea `Product` con Value Objects.
5. Guarda en PostgreSQL.
6. Publica `ProductCreated`.
7. Sincroniza `ProductCatalogDocument` en MongoDB para consultas.

Estado actual: el dominio puede crear el producto, JPA puede guardarlo como entidad, S3 puede subir imagenes, pero falta el caso de uso, mapper y controlador.

### Crear pedido

Flujo ideal:

1. API recibe cliente e items.
2. Caso de uso consulta cliente externo con `CustomerProvider`.
3. Busca productos en PostgreSQL.
4. Crea `OrderItem` desde cada producto.
5. Crea `Order` en estado `PENDING`.
6. Guarda orden y lineas en PostgreSQL.
7. Publica `OrderCreated`.

Estado actual: el dominio puede construir una orden valida, las tablas existen, pero falta el caso de uso, repositorio de dominio, mapper y endpoint.

### Confirmar pedido

Flujo ideal:

1. API pide confirmar orden.
2. Caso de uso carga `Order`.
3. Llama `order.confirm()`.
4. Se registra `OrderConfirmed`.
5. El sistema decrementa stock de los productos.
6. Guarda cambios.

Estado actual: `Order.confirm()` existe y valida transicion, pero no hay orquestacion ni persistencia de dominio conectada.

### Enviar y entregar pedido

Flujo ideal:

1. `ship()` solo si esta `CONFIRMED`.
2. `deliver()` solo si esta `SHIPPED`.
3. Cada cambio registra su evento.

Estado actual: la regla existe en dominio, pero falta API/casos de uso.

### Cancelar pedido

Flujo ideal:

1. `cancel(reason)` cancela desde estados permitidos.
2. Si estaba confirmado, se libera stock.
3. Se guarda evento de cancelacion y cambios.

Estado actual: el evento existe, pero falta listener/caso de uso para devolver stock.

## 9. Infraestructura local

`docker-compose.yml` levanta:

- PostgreSQL en `localhost:5432`.
- MongoDB en `localhost:27017`.
- Redis en `localhost:6379`.
- Redis Commander en `localhost:8081`.
- LocalStack en `localhost:4566`.

La API esta configurada para arrancar en:

- `http://localhost:9090`.

## 10. Tests actuales

Hay tests en `erp-domain` para:

- `Money`.
- `Quantity`.
- `Email`.
- `SKU`.
- `Stock`.
- `Product`.
- `ProductName`.
- `ProductImage`.
- `Order`.
- `OrderStatus`.
- `OrderItem`.
- `CatalogItem`.

Esto confirma que se esta cuidando bien la logica de dominio.

Hay un test basico de Spring Boot en `erp-api`.

Nota de verificacion: `./gradlew.bat build` compila, pero el test de `erp-api` falla si PostgreSQL no esta levantado en `localhost:5432`, porque `@SpringBootTest` intenta crear el contexto completo con JPA y validar la conexion.

## 11. Estado real del proyecto ahora mismo

Lo que ya esta bastante avanzado:

- Modelo de dominio de productos y pedidos.
- Reglas de estados de pedidos.
- Reglas de stock, precios y snapshots de items.
- Eventos de dominio.
- Scripts de PostgreSQL con datos de prueba.
- Scripts de MongoDB para catalogos y lectura.
- Configuracion Docker local.
- Adaptador externo para clientes.
- Adaptador S3 para imagenes.

Lo que falta para que sea una API usable:

- Controladores REST.
- DTOs de entrada/salida para la API.
- Casos de uso en `erp-application`.
- Repositorios de dominio para productos y pedidos.
- Mappers dominio <-> JPA.
- Adaptador Mongo para el puerto `CatalogRepository`.
- Publicacion y manejo de eventos de dominio.
- Uso real de Redis/cache.
- Endpoints para subir/descargar imagenes.
- Manejo global de errores HTTP.

## 12. Alertas tecnicas encontradas

Estas no son criticas como idea de negocio, pero conviene revisarlas:

- `Entity` no permite ID nulo, pero algunos constructores protegidos de agregados llaman a `super(null)`. Si un framework invoca esos constructores, fallarian.
- `domain.repositories.CatalogRepository` importa `javax.xml.catalog.Catalog` en vez de `com.raulsanchez.erp_lite.domain.catalog.Catalog`.
- `AuditLogRepository` extiende `MongoRepository<AuditLogRepository, ObjectId>` y deberia apuntar a `AuditLogDocument`.
- `JsonPlaceholderCustomerProviderAdapter.existsById` siempre devuelve `false`.
- `S3BucketConfig` construye el `S3Client`, pero no aplica explicitamente el endpoint de LocalStack al builder. Con LocalStack normalmente hace falta `endpointOverride`.
- Hay textos con caracteres raros en algunos comentarios, probablemente por encoding.
- `erp-application` y `erp-common` estan practicamente vacios.
- La descripcion del `build.gradle` dice Java 25, pero la toolchain usa Java 21.

## 13. Que estas creando y por que

Estas creando un ERP pequeno pero bien separado por capas.

El objetivo no parece ser solo CRUD. La estructura va hacia un sistema donde:

- El dominio manda y protege reglas.
- PostgreSQL guarda lo transaccional.
- MongoDB sirve para catalogos y consultas enriquecidas.
- Redis queda disponible para cache.
- S3/LocalStack guarda imagenes.
- JSONPlaceholder simula un sistema externo de clientes.
- Los eventos de dominio permiten reaccionar a cambios importantes sin ensuciar las entidades.

La razon de cada pieza:

- DDD: para que las reglas vivan en objetos ricos, no dispersas por controladores.
- Arquitectura hexagonal: para poder cambiar infraestructura sin romper dominio.
- PostgreSQL: para consistencia en productos/pedidos.
- MongoDB: para catalogos y vistas de lectura flexibles.
- Redis: para acelerar lecturas frecuentes.
- S3: para separar imagenes del modelo relacional.
- JSONPlaceholder: para practicar integracion con sistemas externos.
- Eventos: para coordinar efectos secundarios como stock, auditoria o sincronizacion.

## 14. Siguiente paso recomendado

El siguiente paso mas natural es implementar una primera vertical completa, no muchas piezas sueltas.

Recomendacion:

1. Crear endpoints basicos de productos.
2. Crear `ProductRepositoryPort` en dominio/aplicacion.
3. Crear adaptador JPA que convierta `ProductEntity` <-> `Product`.
4. Crear casos de uso `CreateProduct`, `GetProduct`, `ListProducts`.
5. Exponerlos con un `ProductController`.
6. Probar la app de punta a punta contra PostgreSQL.

Despues de eso, repetir el mismo patron con pedidos.
