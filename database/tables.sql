/* Enum types for animal subtypes */
CREATE TYPE chicken_type_enum AS ENUM ('poussin', 'poule', 'coq');
CREATE TYPE rabbit_type_enum AS ENUM ('lapereau', 'lapin');

CREATE TABLE IF NOT EXISTS "User"
(
    u_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom VARCHAR(20),
    sexe VARCHAR(20),
    ecus INTEGER,
    level INTEGER
);

CREATE TABLE IF NOT EXISTS Product
(
    productID INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description TEXT,
    collection BOOLEAN,
    price FLOAT,
    coef INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS Transactions
(
    t_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    seller INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    buyer INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    product INTEGER NOT NULL REFERENCES Product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    quantite INTEGER NOT NULL CHECK (quantite > 0),
    prix_total FLOAT NOT NULL CHECK (prix_total >= 0),
    date_transaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (seller != buyer)
);

CREATE TABLE IF NOT EXISTS Stock
(
    u_id INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    productID INTEGER NOT NULL REFERENCES Product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    quantity INTEGER DEFAULT 0 CHECK (quantity >= 0),
    PRIMARY KEY (u_id, productID)
);

CREATE TABLE IF NOT EXISTS Desktop
(
    u_id INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    productID INTEGER NOT NULL REFERENCES Product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    PRIMARY KEY (u_id, productID)
);

CREATE TABLE IF NOT EXISTS Animal
(
    a_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    u_id INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    clean BOOLEAN,
    healthy BOOLEAN,
    age INTEGER,
    weight FLOAT,
    a_gender BOOL
);

CREATE TABLE IF NOT EXISTS Chicken
(
    a_id INTEGER PRIMARY KEY REFERENCES Animal(a_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    chicken_type chicken_type_enum,
    fasting INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Rabbit
(
    a_id INTEGER PRIMARY KEY REFERENCES Animal(a_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    rabbit_type rabbit_type_enum
);

CREATE TABLE IF NOT EXISTS Cow
(
    a_id INTEGER PRIMARY KEY REFERENCES Animal(a_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Event
(
    e_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    u_id INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    text VARCHAR(200) NOT NULL,
    date_event TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Market
(
    u_id INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    productID INTEGER NOT NULL REFERENCES Product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    price FLOAT NOT NULL,
    PRIMARY KEY (u_id, productID)
);

CREATE TABLE IF NOT EXISTS Cooperative
(
    u_id INTEGER NOT NULL REFERENCES "User"(u_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    productID INTEGER NOT NULL REFERENCES Product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    is_open BOOLEAN,
    PRIMARY KEY (u_id, productID)
);
