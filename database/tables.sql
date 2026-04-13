/* Enum types for animal subtypes */
CREATE TYPE chickenTypeEnum AS ENUM ('poussin', 'poule', 'coq', 'pondeuse', 'reproducteur');
CREATE TYPE rabbitTypeEnum AS ENUM ('lapereau', 'lapin');
CREATE TYPE cowTypeEnum AS ENUM ('Boeuf', 'vache', 'veau');
CREATE TYPE genderEnum AS ENUM ('M', 'F');


CREATE TABLE IF NOT EXISTS "user"
(
    uid INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(20),
    email VARCHAR(100),
    gender genderEnum,
    ecus INTEGER,
    hibernation BOOLEAN DEFAULT FALSE,
    level INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS product
(
    productID INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description TEXT,
    collection BOOLEAN,
    price FLOAT,
    coef INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS transaction
(
    tid INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    seller INTEGER NOT NULL REFERENCES "user"(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    buyer INTEGER NOT NULL REFERENCES "user"(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    product INTEGER NOT NULL REFERENCES product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    totalPrice FLOAT NOT NULL CHECK (totalPrice >= 0),
    transactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (seller != buyer)
);

CREATE TABLE IF NOT EXISTS stock
(
    uid INTEGER NOT NULL REFERENCES "user"(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    productID INTEGER NOT NULL REFERENCES product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    quantity INTEGER DEFAULT 0 CHECK (quantity >= 0),
    collectible BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (uid, productID)
);

CREATE TABLE IF NOT EXISTS animal
(
    aid INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid INTEGER NOT NULL REFERENCES "user"(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    clean BOOLEAN DEFAULT TRUE,
    healthy BOOLEAN DEFAULT TRUE,
    age INTEGER,
    weight FLOAT,
    gender genderEnum
);

CREATE TABLE IF NOT EXISTS chicken
(
    aid INTEGER PRIMARY KEY REFERENCES animal(aid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    name VARCHAR(50),
    chickenType chickenTypeEnum,
    fasting BOOLEAN DEFAULT FALSE,
    fasting_days INTEGER,
    sick_days INTEGER
);

CREATE TABLE IF NOT EXISTS rabbit
(
    aid INTEGER PRIMARY KEY REFERENCES animal(aid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    name VARCHAR(50),
    rabbitType rabbitTypeEnum
);

CREATE TABLE IF NOT EXISTS cow
(
    aid INTEGER PRIMARY KEY REFERENCES animal(aid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    name VARCHAR(50),
    cowType cowTypeEnum,
    milking BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS event
(
    eid INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid INTEGER NOT NULL REFERENCES "user"(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    text VARCHAR(200) NOT NULL,
    eventDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS market
(
    uid INTEGER NOT NULL REFERENCES "user"(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    productID INTEGER NOT NULL REFERENCES product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    price FLOAT NOT NULL,
    PRIMARY KEY (uid, productID)
);

CREATE TABLE IF NOT EXISTS cooperative
(
    uid INTEGER NOT NULL REFERENCES "user"(uid)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    productID INTEGER NOT NULL REFERENCES product(productID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    isOpen BOOLEAN,
    PRIMARY KEY (uid, productID)
);
