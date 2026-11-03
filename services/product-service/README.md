# StockPilot Product Service

Spring Boot microservice for product catalogue management.

## Requirements

- JDK 17 or later
- Maven Wrapper (included in the parent backend repository)

## Run locally

From this directory:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The service listens on `http://localhost:8082`.

## Local database

The default profile uses an in-memory H2 database. Data is cleared when the application stops.

H2 console: `http://localhost:8082/h2-console`

- JDBC URL: `jdbc:h2:mem:stockpilot_product`
- User: `sa`
- Password: blank

## MySQL profile

Once MySQL is running and the `stockpilot_product` database is available, configure `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then run:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=mysql"
```

`ddl-auto=update` is for local development only. Use Flyway migrations and schema validation for production.

## API endpoints

- `POST /api/v1/products`
- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `PUT /api/v1/products/{id}`
- `DELETE /api/v1/products/{id}`

List query parameters: `q`, `page`, `size`, `sortBy`, `sortDirection`.
