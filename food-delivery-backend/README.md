# FoodExpress backend (Spring Boot 4.1, Java 17+, MariaDB)

1. Start MySQL (MariaDB) in XAMPP, port 3307. Database `food_delivery` with the tables from `database/schema.sql`.
2. Import into STS / Eclipse: File > Import > Maven > Existing Maven Projects > select this folder.
3. Run `FoodDeliveryApplication` (Run As > Spring Boot App).
4. Swagger UI: http://localhost:8080/swagger-ui.html

Defaults (src/main/resources/application.yml): DB user `root` with empty password, port 3307.
Override with environment variables DB_USERNAME, DB_PASSWORD, JWT_SECRET, ADMIN_PASSWORD.

Demo logins (created on first start): admin@fooddelivery.com / Admin@12345,
owner@demo.com, customer@demo.com, rider@demo.com / Demo@1234.

## Versions
Spring Boot 4.1.1 (Spring Framework 7, Spring Security 7, Hibernate 7, Jackson 3), Java 17 or newer (up to Java 26),
springdoc-openapi 3.1.1, jjwt 0.13.0. Maven 3.6.3 or newer (STS bundles one).
