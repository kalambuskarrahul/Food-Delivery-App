-- Food Delivery App: initial schema (MySQL 8, InnoDB, utf8mb4)
-- Place at: src/main/resources/db/migration/V1__init.sql

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    phone         VARCHAR(20),
    role          ENUM('CUSTOMER','RESTAURANT_OWNER','DELIVERY_PARTNER','ADMIN') NOT NULL,
    enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE addresses (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    label      VARCHAR(30),
    line1      VARCHAR(200) NOT NULL,
    line2      VARCHAR(200),
    city       VARCHAR(80)  NOT NULL,
    state      VARCHAR(80)  NOT NULL,
    pincode    VARCHAR(10)  NOT NULL,
    latitude   DECIMAL(9,6),
    longitude  DECIMAL(9,6),
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_addresses_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE delivery_profiles (
    user_id        BIGINT PRIMARY KEY,
    vehicle_type   VARCHAR(30),
    vehicle_number VARCHAR(20),
    is_available   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_delivery_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE restaurants (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id    BIGINT       NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    cuisine     VARCHAR(100),
    phone       VARCHAR(20),
    address     VARCHAR(255) NOT NULL,
    city        VARCHAR(80)  NOT NULL,
    pincode     VARCHAR(10),
    image_url   VARCHAR(255),
    is_open     BOOLEAN      NOT NULL DEFAULT TRUE,
    status      ENUM('PENDING','APPROVED','SUSPENDED') NOT NULL DEFAULT 'PENDING',
    avg_rating  DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_restaurants_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_restaurants_city_status (city, status),
    INDEX idx_restaurants_owner (owner_id)
) ENGINE=InnoDB;

CREATE TABLE menu_categories (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT      NOT NULL,
    name          VARCHAR(100) NOT NULL,
    CONSTRAINT fk_categories_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    CONSTRAINT uq_category_per_restaurant UNIQUE (restaurant_id, name)
) ENGINE=InnoDB;

CREATE TABLE menu_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT         NOT NULL,
    category_id   BIGINT,
    name          VARCHAR(150)   NOT NULL,
    description   TEXT,
    price         DECIMAL(10,2)  NOT NULL,
    is_veg        BOOLEAN        NOT NULL DEFAULT TRUE,
    available     BOOLEAN        NOT NULL DEFAULT TRUE,
    image_url     VARCHAR(255),
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_menu_price CHECK (price >= 0),
    CONSTRAINT fk_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_category   FOREIGN KEY (category_id)   REFERENCES menu_categories(id) ON DELETE SET NULL,
    INDEX idx_items_restaurant (restaurant_id)
) ENGINE=InnoDB;

CREATE TABLE carts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    restaurant_id BIGINT,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cart_user UNIQUE (user_id),
    CONSTRAINT fk_carts_user       FOREIGN KEY (user_id)       REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_carts_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE cart_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id      BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity     INT    NOT NULL,
    CONSTRAINT chk_cart_qty CHECK (quantity > 0),
    CONSTRAINT uq_cart_item UNIQUE (cart_id, menu_item_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id)      REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number        VARCHAR(30)   NOT NULL,
    customer_id         BIGINT        NOT NULL,
    restaurant_id       BIGINT        NOT NULL,
    delivery_partner_id BIGINT,
    delivery_address    VARCHAR(500)  NOT NULL,
    status              ENUM('PLACED','ACCEPTED','REJECTED','PREPARING','READY_FOR_PICKUP',
                             'OUT_FOR_DELIVERY','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    subtotal            DECIMAL(10,2) NOT NULL,
    delivery_fee        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax                 DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total               DECIMAL(10,2) NOT NULL,
    cancel_reason       VARCHAR(255),
    placed_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_orders_number UNIQUE (order_number),
    CONSTRAINT fk_orders_customer   FOREIGN KEY (customer_id)         REFERENCES users(id),
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id)       REFERENCES restaurants(id),
    CONSTRAINT fk_orders_partner    FOREIGN KEY (delivery_partner_id) REFERENCES users(id),
    INDEX idx_orders_customer (customer_id, placed_at),
    INDEX idx_orders_restaurant_status (restaurant_id, status),
    INDEX idx_orders_partner_status (delivery_partner_id, status)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT        NOT NULL,
    menu_item_id BIGINT,
    item_name    VARCHAR(150)  NOT NULL,
    unit_price   DECIMAL(10,2) NOT NULL,
    quantity     INT           NOT NULL,
    line_total   DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_order_qty CHECK (quantity > 0),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)     REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE SET NULL,
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB;

CREATE TABLE order_status_history (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT    NOT NULL,
    status     VARCHAR(30) NOT NULL,
    changed_by BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_order FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user  FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_history_order (order_id)
) ENGINE=InnoDB;

CREATE TABLE payments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT        NOT NULL,
    method          ENUM('COD','UPI','CARD','WALLET') NOT NULL,
    status          ENUM('PENDING','SUCCESS','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    amount          DECIMAL(10,2) NOT NULL,
    transaction_ref VARCHAR(100),
    paid_at         TIMESTAMP NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payments_order UNIQUE (order_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB;

CREATE TABLE reviews (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT   NOT NULL,
    customer_id   BIGINT   NOT NULL,
    restaurant_id BIGINT   NOT NULL,
    rating        TINYINT  NOT NULL,
    comment       VARCHAR(500),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_reviews_order UNIQUE (order_id),
    CONSTRAINT fk_reviews_order      FOREIGN KEY (order_id)      REFERENCES orders(id),
    CONSTRAINT fk_reviews_customer   FOREIGN KEY (customer_id)   REFERENCES users(id),
    CONSTRAINT fk_reviews_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    INDEX idx_reviews_restaurant (restaurant_id)
) ENGINE=InnoDB;
