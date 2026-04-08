/*===========================================*/
/*  SCRIPT D'INSERTION DES DONNÉES DE TEST   */
/*  TinyFarm                                 */
/*===========================================*/
SET
    search_path TO TinyFarm;

-- ============================================
-- Utilisateurs
-- ============================================
-- u_id = 1 est réservé à la coopérative
INSERT INTO
    "user" (u_id, nom, sexe, ecus, level)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Cooperative', 'N/A', 5000, 0),
    (2, 'Alice', 'F', 1200, 5),
    (3, 'Bob', 'M', 800, 3),
    (4, 'Charlie', 'M', 1500, 7),
    (5, 'Diana', 'F', 350, 2);

-- Resynchroniser la séquence après insertion manuelle
SELECT
    setval(
        pg_get_serial_sequence('"user"', 'u_id'),
        (
            SELECT
                MAX(u_id)
            FROM
                "user"
        )
    );

-- ============================================
-- Produits
-- ============================================
INSERT INTO
    Product (productID, description, collection, price, coef)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Lapin', FALSE, 50.0, 3),
    (2, 'Oeuf', TRUE, 5.0, 1),
    (3, 'Lait', TRUE, 10.0, 2),
    (4, 'Laine', TRUE, 15.0, 2),
    (5, 'Poulet', FALSE, 30.0, 3),
    (6, 'Carotte', TRUE, 2.0, 1),
    (7, 'Blé', TRUE, 3.0, 1);

SELECT
    setval(
        pg_get_serial_sequence('Product', 'productid'),
        (
            SELECT
                MAX(productID)
            FROM
                Product
        )
    );

-- ============================================
-- Transactions
-- ============================================
INSERT INTO
    Transactions (
        t_id,
        seller,
        buyer,
        product,
        quantite,
        prix_total,
        date_transaction
    )
OVERRIDING SYSTEM VALUE
VALUES
    (1, 2, 3, 2, 10, 50.0, '2025-02-01 10:00:00'),
    (2, 2, 4, 3, 5, 50.0, '2025-02-02 11:30:00'),
    (3, 3, 5, 2, 8, 40.0, '2025-02-03 09:15:00'),
    (4, 4, 2, 1, 2, 100.0, '2025-02-05 14:00:00'),
    (5, 4, 3, 3, 3, 30.0, '2025-02-06 16:45:00'),
    (6, 5, 2, 7, 20, 60.0, '2025-02-07 08:00:00'),
    (7, 2, 5, 5, 1, 30.0, '2025-02-10 12:00:00'),
    (8, 3, 4, 6, 15, 30.0, '2025-02-12 10:30:00'),
    (9, 4, 5, 2, 12, 60.0, '2025-02-14 13:00:00'),
    (10, 5, 3, 4, 4, 60.0, '2025-02-15 17:20:00');

SELECT
    setval(
        pg_get_serial_sequence('Transactions', 't_id'),
        (
            SELECT
                MAX(t_id)
            FROM
                Transactions
        )
    );

-- ============================================
-- Animaux
-- ============================================
-- Animaux d'Alice (u_id = 2)
INSERT INTO
    Animal (u_id, a_id, clean, healthy, age, weight, gender)
OVERRIDING SYSTEM VALUE
VALUES
    (2, 1, TRUE, TRUE, 120, 2.5, 'M'),
    (2, 2, TRUE, TRUE, 90, 2.0, 'F'),
    (2, 3, FALSE, TRUE, 30, 0.5, 'F'),
    (2, 4, TRUE, FALSE, 200, 3.0, 'M');

-- Animaux de Bob (u_id = 3)
INSERT INTO
    Animal (u_id, a_id, clean, healthy, age, weight, gender)
OVERRIDING SYSTEM VALUE
VALUES
    (3, 5, TRUE, TRUE, 60, 1.8, 'F'),
    (3, 6, TRUE, TRUE, 150, 4.0, 'M'),
    (3, 7, FALSE, FALSE, 45, 1.2, 'F');

-- Animaux de Charlie (u_id = 4)
INSERT INTO
    Animal (u_id, a_id, clean, healthy, age, weight, gender)
OVERRIDING SYSTEM VALUE
VALUES
    (4, 8, TRUE, TRUE, 300, 500.0, 'F'),
    (4, 9, TRUE, TRUE, 80, 2.2, 'M'),
    (4, 10, TRUE, TRUE, 10, 0.3, 'F');

-- Animaux de Diana (u_id = 5)
INSERT INTO
    Animal (u_id, a_id, clean, healthy, age, weight, gender)
OVERRIDING SYSTEM VALUE
VALUES
    (5, 11, TRUE, TRUE, 40, 1.5, 'F'),
    (5, 12, FALSE, TRUE, 20, 0.8, 'M');

SELECT
    setval(
        pg_get_serial_sequence('Animal', 'a_id'),
        (
            SELECT
                MAX(a_id)
            FROM
                Animal
        )
    );

-- ============================================
-- Poulets (sous-classe Chicken)
-- ============================================
INSERT INTO
    Chicken (a_id, chickenType, fasting)
VALUES
    (1, 'R', 0),
    (2, 'H', 0),
    (3, 'C', 2),
    (5, 'H', 0),
    (10, 'C', 0),
    (11, 'H', 1);

-- ============================================
-- Lapins (sous-classe Rabbit)
-- ============================================
INSERT INTO
    Rabbit (a_id, rabbitType)
VALUES
    (4, 'lapin'),
    (7, 'lapereau'),
    (9, 'lapin'),
    (12, 'lapereau');

-- ============================================
-- Vaches (sous-classe Cow)
-- ============================================
INSERT INTO
    Cow (a_id)
VALUES
    (6),
    (8);

-- ============================================
-- Stock (inventaire des utilisateurs)
-- ============================================
INSERT INTO
    Stock (u_id, productID, quantity)
VALUES
    (2, 2, 25),
    (2, 3, 10),
    (2, 7, 50),
    (3, 2, 15),
    (3, 6, 30),
    (4, 1, 3),
    (4, 3, 8),
    (4, 5, 2),
    (5, 4, 6),
    (5, 7, 20);

-- ============================================
-- Desktop (étalage / vitrine des utilisateurs)
-- ============================================
INSERT INTO
    Desktop (u_id, productID, quantity)
VALUES
    (2, 2, 5),
    (2, 3, 3),
    (3, 2, 4),
    (3, 6, 10),
    (4, 1, 1),
    (4, 3, 2),
    (5, 4, 2),
    (5, 7, 8);

-- ============================================
-- Market (produits mis en vente avec prix libre)
-- ============================================
INSERT INTO
    Market (u_id, productID, price)
VALUES
    (2, 2, 6.0),
    (2, 7, 4.0),
    (3, 6, 2.5),
    (4, 1, 55.0),
    (4, 3, 12.0),
    (5, 4, 18.0);

-- ============================================
-- Cooperative (produits dans la coopérative)
-- ============================================
INSERT INTO
    Cooperative (u_id, productID, is_open)
VALUES
    (1, 2, TRUE),
    (1, 3, TRUE),
    (1, 6, FALSE),
    (1, 7, TRUE);

-- ============================================
-- Événements (avec date)
-- ============================================
INSERT INTO
    Event (e_id, u_id, text, date_event)
OVERRIDING SYSTEM VALUE
VALUES
    (
        1,
        2,
        'Alice a vendu 10 oeufs à Bob',
        '2025-02-01 10:00:00'
    ),
    (
        2,
        3,
        'Bob a acheté 10 oeufs à Alice',
        '2025-02-01 10:00:00'
    ),
    (
        3,
        4,
        'Charlie a vendu 2 lapins à Alice',
        '2025-02-05 14:00:00'
    ),
    (
        4,
        2,
        'La poule d''Alice a pondu 5 oeufs',
        '2025-02-08 07:30:00'
    ),
    (
        5,
        5,
        'Diana a mis de la laine en vente',
        '2025-02-09 09:00:00'
    ),
    (
        6,
        3,
        'Le lapereau de Bob est malade',
        '2025-02-11 16:00:00'
    ),
    (
        7,
        4,
        'Charlie a atteint le niveau 7',
        '2025-02-13 12:00:00'
    ),
    (
        8,
        2,
        'Le poussin d''Alice jeûne depuis 2 jours',
        '2025-02-14 08:00:00'
    );

SELECT
    setval(
        pg_get_serial_sequence('Event', 'e_id'),
        (
            SELECT
                MAX(e_id)
            FROM
                Event
        )
    );
