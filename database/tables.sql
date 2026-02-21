/*CREATE THE TABLES HERE*/

CREATE IF NOT EXISTS TABLE Stock
(
    u_id INTEGER REFERENCES (User.id),
    productID INTEGER REFERENCES (Product.productID),
    quantity INTEGER
);

CREATE IF NOT EXISTS TABLE Desktop
(
    u_id INTEGER REFERENCES (User.id),
    productID INTEGER REFERENCES (Product.productID),
    quantity INTEGER
);

CREATE IF NOT EXISTS TABLE Animal
(
    u_id INTEGER REFERENCES (User.id),
    a_id INTEGER PRIMARY KEY,
    clean BOOLEAN,
    healthy BOOLEAN,
    age INTEGER,
    weight FLOAT,
    a_gender BOOL
);

CREATE IF NOT EXISTS TABLE Product
(
    productID INTEGER PRIMARY KEY,
    description TEXT,
    collection BOOLEAN,
    price FLOAT
);

CREATE IF NOT EXISTS TABLE Event
(
    u_id INTEGER REFERENCES (User.id),
    e_id INTEGER REFERENCES (User.id),
    text VARCHAR(200)
);

CREATE IF NOT EXISTS TABLE Chicken
(
    a_id INTEGER PRIMARY KEY,
    chicken_type ENUM,
    fasting BOOL
);


CREATE IF NOT EXISTS TABLE Rabbit
(
    a_id INTEGER PRIMARY KEY,
    chicken_type ENUM
);

CREATE IF NOT EXISTS TABLE Cow
(
    a_id INTEGER PRIMARY KEY
);

CREATE IF NOT EXISTS TABLE User
(
    u_id INTEGER PRIMARY KEY,
    nom VARCHAR(20),
    sexe VARCHAR(20),
    ecus INTEGER,
    level INTEGER
);

CREATE IF NOT EXISTS TABLE Market
(
    u_id INTEGER REFERENCES (User.id),
    productID INTEGER REFERENCES (Product.productID)
);

CREATE IF NOT EXISTS TABLE Cooperative
(
    u_id INTEGER REFERENCES (User.id),
    productID INTEGER REFERENCES (Product.productID),
    is_open BOOLEAN
);
