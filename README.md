# FoodExpress – Food Delivery Application

A full-stack food delivery web application where customers order from restaurants, restaurant owners manage menus and orders, delivery partners pick up and deliver, and an admin approves restaurants. It is built with **Spring Boot**, **Angular** and **MariaDB**.

## Features

**Customer**
- Register, log in, and browse approved restaurants and their menus
- Add dishes to a cart, check out with a delivery address, and pay by COD or simulated online payment
- Track an order live (the page refreshes automatically) and rate the order after delivery

**Restaurant owner**
- Add and manage restaurants, menu categories and dishes
- Accept or reject incoming orders, then move them through Preparing and Ready for pickup

**Delivery partner**
- Go online, see orders that are ready for pickup, and accept one
- Mark the order as picked up and delivered

**Admin**
- Approve or reject new restaurants
- Monitor users, restaurants and orders

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17+, Spring Boot 4.1, Spring Web MVC, Spring Security, Spring Data JPA (Hibernate), Bean Validation, Lombok |
| Authentication | Stateless JWT (access and refresh tokens), BCrypt password hashing, role-based access |
| Database | MariaDB (XAMPP) |
| Frontend | Angular 20 (standalone components, lazy-loaded routes, signals, HTTP interceptor, route guards) |
| API docs | springdoc-openapi (Swagger UI) |
| Build tools | Maven, npm |

## Architecture

```
Angular (port 4200)  --HTTP/JSON + JWT-->  Spring Boot REST API (port 8080)  --JPA-->  MariaDB (port 3307)
```

The backend follows a layered design: **Controller → Service → Repository → Database**, with DTOs for requests and responses and a global exception handler that returns clean JSON errors.

### Order workflow

```
PLACED → ACCEPTED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
   ├── REJECTED (by restaurant)
   └── CANCELLED (by customer)
```

A single `OrderWorkflow` class defines the allowed transitions and every status change is stored in an order status history table.

### Design decisions
- **Price snapshot:** each order item stores the dish name and price at order time, so later menu changes never alter old orders.
- **Row locking:** when riders claim an order, the order row is locked in a transaction, so only one rider can win.
- **Token refresh:** the Angular interceptor adds the JWT to every request and refreshes it once when it receives a 401.
- **Fees:** delivery fee of ₹40 for subtotals under ₹500, plus 5% tax.

## Project Structure

```
Food-Delivery/
├── food-delivery-backend/     Spring Boot REST API (Maven)
│   ├── database/schema.sql    Database tables (13 tables)
│   └── src/main/java/...      controller, service, repository, entity, dto, security, config
└── food-delivery-frontend/    Angular application
    └── src/app/...            core (guards, interceptor, config), pages, services
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven (or an IDE such as Spring Tool Suite with Maven support)
- Node.js and npm
- XAMPP with MariaDB

### 1. Database
1. Start **MySQL/MariaDB** from the XAMPP Control Panel. This project expects it on port **3307**. If yours uses another port, change the URL in `application.yml`.
2. Open phpMyAdmin and create a database named `food_delivery`.
3. Select that database, open the **Import** tab and import `food-delivery-backend/database/schema.sql`.

### 2. Backend
1. Open `food-delivery-backend` in your IDE as a Maven project.
2. Run `FoodDeliveryApplication`, or from the terminal:
   ```bash
   cd food-delivery-backend
   mvn spring-boot:run
   ```
3. The API runs at `http://localhost:8080` and Swagger UI is at `http://localhost:8080/swagger-ui.html`.

On first start the app seeds an admin account, demo users and sample restaurants (controlled by `app.seed-demo-data` in `application.yml`).

### 3. Frontend
```bash
cd food-delivery-frontend
npm install
npm start
```
Open `http://localhost:4200`. The API address is set in `src/app/core/config.ts`.

## Demo Logins (for local testing only)

| Role | Email | Password |
|---|---|---|
| Admin | admin@fooddelivery.com | Admin@12345 |
| Restaurant owner | owner@demo.com | Demo@1234 |
| Customer | customer@demo.com | Demo@1234 |
| Delivery partner | rider@demo.com | Demo@1234 |

## Configuration

Defaults in `application.yml` are for local development only. Override them with environment variables for any real deployment:

| Variable | Purpose | Default |
|---|---|---|
| `DB_USERNAME` | Database user | `root` |
| `DB_PASSWORD` | Database password | empty |
| `JWT_SECRET` | Secret used to sign tokens | development value, change it |
| `ADMIN_PASSWORD` | Password for the seeded admin | development value, change it |

## Try the Full Flow

1. Log in as the **customer**, add dishes to the cart and place an order.
2. Log in as the **owner**, open Orders, then Accept → Start preparing → Mark ready for pickup.
3. Log in as the **rider**, go online, accept the delivery, then Picked up → Mark as delivered.
4. Back as the **customer**, view the delivered order and rate it.

New restaurants are created by owners and must be approved by the **admin** before customers can see them.

## Future Improvements
- Real payment gateway (Razorpay or Stripe)
- Live rider tracking on a map with WebSockets
- Email, SMS and push notifications
- Coupons and restaurant offers
- Automated tests, Docker and CI/CD
- Android app, dish-level search and image upload

## Author

**Rahul Kalambuskar**
GitHub: [your-username](https://github.com/your-username)
