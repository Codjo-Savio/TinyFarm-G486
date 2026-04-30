CREATE TABLE IF NOT EXISTS "user" (
    uid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(20),
    email VARCHAR(100),
    gender VARCHAR(20) CHECK (gender IN ('M', 'F')),
    ecus FLOAT DEFAULT 1500 CHECK (ecus >= 0),
    hibernation BOOLEAN DEFAULT FALSE,
    hibernation_date TIMESTAMP,
    level INTEGER DEFAULT 1 CHECK (level >= 1),
    remaining_purchases INTEGER DEFAULT 12 CHECK (remaining_purchases BETWEEN 0 AND 12)
);

CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description TEXT,
    collection BOOLEAN DEFAULT FALSE,
    coef INTEGER DEFAULT 1 CHECK (coef >= 1)
);

CREATE TABLE IF NOT EXISTS animal (
    aid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid BIGINT NOT NULL,
    clean BOOLEAN DEFAULT TRUE,
    healthy BOOLEAN DEFAULT TRUE,
    age INTEGER DEFAULT 0 CHECK (age >= 0),
    weight FLOAT DEFAULT 1 CHECK (weight >= 0),
    fed_today BOOLEAN DEFAULT FALSE,
    watered_today BOOLEAN DEFAULT FALSE,
    gender VARCHAR(20) CHECK (gender IN ('M', 'F')),
    CONSTRAINT fk_animal_user
        FOREIGN KEY (uid) REFERENCES "user" (uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS chicken (
    aid BIGINT PRIMARY KEY,
    name VARCHAR(50),
    chicken_type VARCHAR(20) DEFAULT 'C' CHECK (chicken_type IN ('C', 'H', 'R', 'L', 'B')),
    fasting_days INTEGER DEFAULT 0 CHECK (fasting_days >= 0),
    sick_days INTEGER DEFAULT 0 CHECK (sick_days >= 0),
    CONSTRAINT fk_chicken_animal
        FOREIGN KEY (aid) REFERENCES animal (aid)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS rabbit (
    aid BIGINT PRIMARY KEY,
    name VARCHAR(50),
    rabbit_type VARCHAR(20) DEFAULT 'lapereau' CHECK (rabbit_type IN ('lapereau', 'lapin')),
    CONSTRAINT fk_rabbit_animal
        FOREIGN KEY (aid) REFERENCES animal (aid)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS cow (
    aid BIGINT PRIMARY KEY,
    name VARCHAR(20),
    cow_type VARCHAR(20) DEFAULT 'C' CHECK (cow_type IN ('D', 'C')),
    hay_today BOOLEAN DEFAULT FALSE,
    sick_days INTEGER DEFAULT 0 CHECK (sick_days >= 0),
    milk INTEGER DEFAULT 0 CHECK (milk >= 0),
    milking BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_cow_animal
        FOREIGN KEY (aid) REFERENCES animal (aid)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS stock (
    uid BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER DEFAULT 0 CHECK (quantity >= 0),
    collectible BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (uid, product_id),
    CONSTRAINT fk_stock_user
        FOREIGN KEY (uid) REFERENCES "user" (uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_stock_product
        FOREIGN KEY (product_id) REFERENCES product (product_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS market (
    uid BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    unit_price FLOAT NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (uid, product_id),
    CONSTRAINT fk_market_user
        FOREIGN KEY (uid) REFERENCES "user" (uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_market_product
        FOREIGN KEY (product_id) REFERENCES product (product_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS cooperative (
    uid BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    price FLOAT CHECK (price >= 0),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    PRIMARY KEY (uid, product_id),
    CONSTRAINT fk_cooperative_user
        FOREIGN KEY (uid) REFERENCES "user" (uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_cooperative_product
        FOREIGN KEY (product_id) REFERENCES product (product_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS transaction (
    tid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    seller BIGINT NOT NULL,
    buyer BIGINT NOT NULL,
    product BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_price FLOAT NOT NULL CHECK (total_price >= 0),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_transaction_distinct_parties CHECK (seller <> buyer),
    CONSTRAINT fk_transaction_seller
        FOREIGN KEY (seller) REFERENCES "user" (uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_transaction_buyer
        FOREIGN KEY (buyer) REFERENCES "user" (uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_transaction_product
        FOREIGN KEY (product) REFERENCES product (product_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
