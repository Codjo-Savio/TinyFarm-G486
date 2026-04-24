-- Chicken 1: Healthy, clean, but hungry and thirsty
WITH new_animal AS (
    INSERT INTO animal (uid, clean, healthy, age, weight, fed_today, watered_today, gender)
    VALUES (1, TRUE, TRUE, 5, 2.5, FALSE, FALSE, 'F')
    RETURNING aid
)
INSERT INTO chicken (aid, name, chicken_type, fasting_days, sick_days)
SELECT aid, 'Poule de Test', 'L', 0, 0 FROM new_animal;

-- Chicken 2: Sick and dirty
WITH new_animal AS (
    INSERT INTO animal (uid, clean, healthy, age, weight, fed_today, watered_today, gender)
    VALUES (1, FALSE, FALSE, 10, 3.0, FALSE, TRUE, 'F')
    RETURNING aid
)
INSERT INTO chicken (aid, name, chicken_type, fasting_days, sick_days)
SELECT aid, 'Poule Malade', 'H', 0, 1 FROM new_animal;

-- Chicken 3: Chick (Poussin)
WITH new_animal AS (
    INSERT INTO animal (uid, clean, healthy, age, weight, fed_today, watered_today, gender)
    VALUES (1, TRUE, TRUE, 1, 0.5, TRUE, TRUE, 'M')
    RETURNING aid
)
INSERT INTO chicken (aid, name, chicken_type, fasting_days, sick_days)
SELECT aid, 'Petit Poussin', 'C', 0, 0 FROM new_animal;

-- Chicken 4: Rooster (Coq)
WITH new_animal AS (
    INSERT INTO animal (uid, clean, healthy, age, weight, fed_today, watered_today, gender)
    VALUES (1, TRUE, TRUE, 15, 3.2, FALSE, FALSE, 'M')
    RETURNING aid
)
INSERT INTO chicken (aid, name, chicken_type, fasting_days, sick_days)
SELECT aid, 'Beau Coq', 'R', 0, 0 FROM new_animal;

-- Chicken 5: Hungry Laying Hen
WITH new_animal AS (
    INSERT INTO animal (uid, clean, healthy, age, weight, fed_today, watered_today, gender)
    VALUES (1, TRUE, TRUE, 8, 2.8, FALSE, TRUE, 'F')
    RETURNING aid
)
INSERT INTO chicken (aid, name, chicken_type, fasting_days, sick_days)
SELECT aid, 'Poule Affamée', 'L', 1, 0 FROM new_animal;
