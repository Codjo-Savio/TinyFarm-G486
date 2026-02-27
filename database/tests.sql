/*===========================================*/
/*  SCRIPT DE TESTS DE LA BASE DE DONNÉES    */
/*  TinyFarm                                 */
/*===========================================*/

-- Ce script valide le bon fonctionnement de la base de données.
-- Il doit être exécuté APRÈS database.sql, tables.sql et insert_data.sql.
-- Chaque test affiche un message et le résultat attendu vs obtenu.

/*===========================================*/
/*  1. TESTS DE STRUCTURE DES TABLES         */
/*===========================================*/

-- Test 1.1 : Vérifier que toutes les tables existent
DO $$
DECLARE
    tables_attendues TEXT[] := ARRAY[
        'User', 'Product', 'Transactions', 'Stock', 'Desktop',
        'Animal', 'Chicken', 'Rabbit', 'Cow',
        'Event', 'Market', 'Cooperative'
    ];
    t TEXT;
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 1.1 : Existence des tables ===';
    FOREACH t IN ARRAY tables_attendues LOOP
        SELECT COUNT(*) INTO nb
        FROM information_schema.tables
        WHERE table_name = lower(t);
        IF nb = 1 THEN
            RAISE NOTICE '  [OK] Table "%" existe', t;
        ELSE
            RAISE WARNING '  [ECHEC] Table "%" introuvable', t;
        END IF;
    END LOOP;
END $$;

-- Test 1.2 : Vérifier que les types ENUM existent
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 1.2 : Existence des types ENUM ===';

    SELECT COUNT(*) INTO nb FROM pg_type WHERE typname = 'chicken_type_enum';
    IF nb = 1 THEN
        RAISE NOTICE '  [OK] Type chicken_type_enum existe';
    ELSE
        RAISE WARNING '  [ECHEC] Type chicken_type_enum introuvable';
    END IF;

    SELECT COUNT(*) INTO nb FROM pg_type WHERE typname = 'rabbit_type_enum';
    IF nb = 1 THEN
        RAISE NOTICE '  [OK] Type rabbit_type_enum existe';
    ELSE
        RAISE WARNING '  [ECHEC] Type rabbit_type_enum introuvable';
    END IF;
END $$;

/*===========================================*/
/*  2. TESTS D'INTÉGRITÉ DES DONNÉES         */
/*===========================================*/

-- Test 2.1 : Vérifier le nombre d'utilisateurs insérés
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 2.1 : Nombre d''utilisateurs ===';
    SELECT COUNT(*) INTO nb FROM "User";
    IF nb = 5 THEN
        RAISE NOTICE '  [OK] 5 utilisateurs trouvés (attendu : 5)';
    ELSE
        RAISE WARNING '  [ECHEC] % utilisateurs trouvés (attendu : 5)', nb;
    END IF;
END $$;

-- Test 2.2 : Vérifier le nombre de produits insérés
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 2.2 : Nombre de produits ===';
    SELECT COUNT(*) INTO nb FROM Product;
    IF nb = 7 THEN
        RAISE NOTICE '  [OK] 7 produits trouvés (attendu : 7)';
    ELSE
        RAISE WARNING '  [ECHEC] % produits trouvés (attendu : 7)', nb;
    END IF;
END $$;

-- Test 2.3 : Vérifier le nombre d'animaux insérés
DO $$
DECLARE
    nb_animaux INT;
    nb_chickens INT;
    nb_rabbits INT;
    nb_cows INT;
BEGIN
    RAISE NOTICE '=== TEST 2.3 : Nombre d''animaux ===';
    SELECT COUNT(*) INTO nb_animaux FROM Animal;
    SELECT COUNT(*) INTO nb_chickens FROM Chicken;
    SELECT COUNT(*) INTO nb_rabbits FROM Rabbit;
    SELECT COUNT(*) INTO nb_cows FROM Cow;

    IF nb_animaux = 12 THEN
        RAISE NOTICE '  [OK] 12 animaux trouvés (attendu : 12)';
    ELSE
        RAISE WARNING '  [ECHEC] % animaux trouvés (attendu : 12)', nb_animaux;
    END IF;

    IF nb_chickens = 6 THEN
        RAISE NOTICE '  [OK] 6 poulets trouvés (attendu : 6)';
    ELSE
        RAISE WARNING '  [ECHEC] % poulets trouvés (attendu : 6)', nb_chickens;
    END IF;

    IF nb_rabbits = 4 THEN
        RAISE NOTICE '  [OK] 4 lapins trouvés (attendu : 4)';
    ELSE
        RAISE WARNING '  [ECHEC] % lapins trouvés (attendu : 4)', nb_rabbits;
    END IF;

    IF nb_cows = 2 THEN
        RAISE NOTICE '  [OK] 2 vaches trouvées (attendu : 2)';
    ELSE
        RAISE WARNING '  [ECHEC] % vaches trouvées (attendu : 2)', nb_cows;
    END IF;

    -- Vérifier que chaque animal a une sous-classe
    IF nb_chickens + nb_rabbits + nb_cows = nb_animaux THEN
        RAISE NOTICE '  [OK] Tous les animaux ont une sous-classe';
    ELSE
        RAISE WARNING '  [ECHEC] % animaux sans sous-classe', nb_animaux - (nb_chickens + nb_rabbits + nb_cows);
    END IF;
END $$;

-- Test 2.4 : Vérifier le nombre de transactions
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 2.4 : Nombre de transactions ===';
    SELECT COUNT(*) INTO nb FROM Transactions;
    IF nb = 10 THEN
        RAISE NOTICE '  [OK] 10 transactions trouvées (attendu : 10)';
    ELSE
        RAISE WARNING '  [ECHEC] % transactions trouvées (attendu : 10)', nb;
    END IF;
END $$;

/*===========================================*/
/*  3. TESTS DES CONTRAINTES D'INTÉGRITÉ     */
/*===========================================*/

-- Test 3.1 : Contrainte CHECK sur quantity (Stock) — doit échouer
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.1 : Contrainte CHECK quantity >= 0 (Stock) ===';
    BEGIN
        INSERT INTO Stock (u_id, productID, quantity) VALUES (2, 1, -5);
        RAISE WARNING '  [ECHEC] L''insertion de quantité négative aurait dû échouer';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE '  [OK] Insertion de quantité négative refusée';
    END;
END $$;

-- Test 3.2 : Contrainte CHECK sur quantity (Desktop) — doit échouer
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.2 : Contrainte CHECK quantity >= 0 (Desktop) ===';
    BEGIN
        INSERT INTO Desktop (u_id, productID, quantity) VALUES (2, 1, -3);
        RAISE WARNING '  [ECHEC] L''insertion de quantité négative aurait dû échouer';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE '  [OK] Insertion de quantité négative refusée';
    END;
END $$;

-- Test 3.3 : Contrainte CHECK sur quantite (Transactions) — doit échouer
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.3 : Contrainte CHECK quantite > 0 (Transactions) ===';
    BEGIN
        INSERT INTO Transactions (t_id, seller, buyer, product, quantite, prix_total)
            VALUES (999, 2, 3, 1, 0, 0.0);
        RAISE WARNING '  [ECHEC] L''insertion de quantité 0 aurait dû échouer';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE '  [OK] Insertion de quantité 0 refusée';
    END;
END $$;

-- Test 3.4 : Contrainte FK — utilisateur inexistant dans Animal
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.4 : Contrainte FK utilisateur inexistant (Animal) ===';
    BEGIN
        INSERT INTO Animal (u_id, a_id, clean, healthy, age, weight, a_gender)
            VALUES (999, 999, TRUE, TRUE, 10, 1.0, TRUE);
        RAISE WARNING '  [ECHEC] L''insertion avec u_id=999 aurait dû échouer';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE '  [OK] Insertion avec utilisateur inexistant refusée';
    END;
END $$;

-- Test 3.5 : Contrainte FK — produit inexistant dans Stock
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.5 : Contrainte FK produit inexistant (Stock) ===';
    BEGIN
        INSERT INTO Stock (u_id, productID, quantity) VALUES (2, 999, 10);
        RAISE WARNING '  [ECHEC] L''insertion avec productID=999 aurait dû échouer';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE '  [OK] Insertion avec produit inexistant refusée';
    END;
END $$;

-- Test 3.6 : Contrainte FK — animal inexistant dans Chicken
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.6 : Contrainte FK animal inexistant (Chicken) ===';
    BEGIN
        INSERT INTO Chicken (a_id, chicken_type, fasting) VALUES (999, 'poule', 0);
        RAISE WARNING '  [ECHEC] L''insertion avec a_id=999 aurait dû échouer';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE '  [OK] Insertion avec animal inexistant refusée';
    END;
END $$;

-- Test 3.7 : Contrainte ENUM — type de poulet invalide
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.7 : Contrainte ENUM chicken_type invalide ===';
    BEGIN
        INSERT INTO Animal (u_id, a_id, clean, healthy, age, weight, a_gender)
            VALUES (2, 900, TRUE, TRUE, 10, 1.0, TRUE);
        INSERT INTO Chicken (a_id, chicken_type, fasting) VALUES (900, 'canard', 0);
        RAISE WARNING '  [ECHEC] L''insertion du type "canard" aurait dû échouer';
    EXCEPTION WHEN invalid_text_representation THEN
        RAISE NOTICE '  [OK] Type de poulet invalide refusé';
        -- Nettoyage
        DELETE FROM Animal WHERE a_id = 900;
    END;
END $$;

-- Test 3.8 : Contrainte PK — doublon dans Stock
DO $$
BEGIN
    RAISE NOTICE '=== TEST 3.8 : Contrainte PK doublon (Stock) ===';
    BEGIN
        INSERT INTO Stock (u_id, productID, quantity) VALUES (2, 2, 10);
        RAISE WARNING '  [ECHEC] Le doublon (u_id=2, productID=2) aurait dû échouer';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE '  [OK] Doublon dans Stock refusé';
    END;
END $$;

-- Test 3.9 : Cascade DELETE — supprimer un utilisateur supprime ses animaux
DO $$
DECLARE
    nb_animaux_avant INT;
    nb_animaux_apres INT;
BEGIN
    RAISE NOTICE '=== TEST 3.9 : CASCADE DELETE utilisateur -> animaux ===';

    -- Insérer un utilisateur temporaire avec un animal
    INSERT INTO "User" (u_id, nom, sexe, ecus, level) VALUES (100, 'Temp', 'M', 0, 1);
    INSERT INTO Animal (u_id, a_id, clean, healthy, age, weight, a_gender)
        VALUES (100, 900, TRUE, TRUE, 10, 1.0, TRUE);

    SELECT COUNT(*) INTO nb_animaux_avant FROM Animal WHERE u_id = 100;

    -- Supprimer l'utilisateur
    DELETE FROM "User" WHERE u_id = 100;

    SELECT COUNT(*) INTO nb_animaux_apres FROM Animal WHERE u_id = 100;

    IF nb_animaux_avant = 1 AND nb_animaux_apres = 0 THEN
        RAISE NOTICE '  [OK] Suppression en cascade fonctionne (1 animal supprimé)';
    ELSE
        RAISE WARNING '  [ECHEC] Avant: %, Après: %', nb_animaux_avant, nb_animaux_apres;
    END IF;
END $$;

-- Test 3.10 : Cascade DELETE — supprimer un animal supprime sa sous-classe
DO $$
DECLARE
    nb_chicken_avant INT;
    nb_chicken_apres INT;
BEGIN
    RAISE NOTICE '=== TEST 3.10 : CASCADE DELETE animal -> sous-classe ===';

    -- Insérer un animal temporaire avec sous-classe
    INSERT INTO Animal (u_id, a_id, clean, healthy, age, weight, a_gender)
        VALUES (2, 901, TRUE, TRUE, 10, 1.0, TRUE);
    INSERT INTO Chicken (a_id, chicken_type, fasting) VALUES (901, 'poule', 0);

    SELECT COUNT(*) INTO nb_chicken_avant FROM Chicken WHERE a_id = 901;

    DELETE FROM Animal WHERE a_id = 901;

    SELECT COUNT(*) INTO nb_chicken_apres FROM Chicken WHERE a_id = 901;

    IF nb_chicken_avant = 1 AND nb_chicken_apres = 0 THEN
        RAISE NOTICE '  [OK] Suppression en cascade animal -> poulet fonctionne';
    ELSE
        RAISE WARNING '  [ECHEC] Avant: %, Après: %', nb_chicken_avant, nb_chicken_apres;
    END IF;
END $$;

/*===========================================*/
/*  4. TESTS DES REQUÊTES ET FONCTIONS       */
/*===========================================*/

-- Test 4.1 : getNbUser() — ne doit pas compter la coopérative (u_id=1)
DO $$
DECLARE
    resultat INT;
BEGIN
    RAISE NOTICE '=== TEST 4.1 : getNbUser() ===';
    resultat := getNbUser();
    IF resultat = 4 THEN
        RAISE NOTICE '  [OK] getNbUser() = % (attendu : 4)', resultat;
    ELSE
        RAISE WARNING '  [ECHEC] getNbUser() = % (attendu : 4)', resultat;
    END IF;
END $$;

-- Test 4.2 : getEcusTotaux() — somme des écus sans la coopérative
DO $$
DECLARE
    resultat INT;
    attendu INT;
BEGIN
    RAISE NOTICE '=== TEST 4.2 : getEcusTotaux() ===';
    -- Alice=1200 + Bob=800 + Charlie=1500 + Diana=350 = 3850
    attendu := 3850;
    resultat := getEcusTotaux();
    IF resultat = attendu THEN
        RAISE NOTICE '  [OK] getEcusTotaux() = % (attendu : %)', resultat, attendu;
    ELSE
        RAISE WARNING '  [ECHEC] getEcusTotaux() = % (attendu : %)', resultat, attendu;
    END IF;
END $$;

-- Test 4.3 : getEcusUser() — écus d'un utilisateur spécifique
DO $$
DECLARE
    resultat INT;
BEGIN
    RAISE NOTICE '=== TEST 4.3 : getEcusUser() ===';

    resultat := getEcusUser(2);
    IF resultat = 1200 THEN
        RAISE NOTICE '  [OK] getEcusUser(2) = % (attendu : 1200 pour Alice)', resultat;
    ELSE
        RAISE WARNING '  [ECHEC] getEcusUser(2) = % (attendu : 1200)', resultat;
    END IF;

    resultat := getEcusUser(4);
    IF resultat = 1500 THEN
        RAISE NOTICE '  [OK] getEcusUser(4) = % (attendu : 1500 pour Charlie)', resultat;
    ELSE
        RAISE WARNING '  [ECHEC] getEcusUser(4) = % (attendu : 1500)', resultat;
    END IF;
END $$;

-- Test 4.4 : getCoef() — coefficient d'un produit
DO $$
DECLARE
    resultat INT;
BEGIN
    RAISE NOTICE '=== TEST 4.4 : getCoef() ===';

    resultat := getCoef(1); -- Lapin, coef = 3
    IF resultat = 3 THEN
        RAISE NOTICE '  [OK] getCoef(1) = % (attendu : 3 pour Lapin)', resultat;
    ELSE
        RAISE WARNING '  [ECHEC] getCoef(1) = % (attendu : 3)', resultat;
    END IF;

    resultat := getCoef(2); -- Oeuf, coef = 1
    IF resultat = 1 THEN
        RAISE NOTICE '  [OK] getCoef(2) = % (attendu : 1 pour Oeuf)', resultat;
    ELSE
        RAISE WARNING '  [ECHEC] getCoef(2) = % (attendu : 1)', resultat;
    END IF;
END $$;

-- Test 4.5 : getVentesTotal() — total des ventes d'un produit
DO $$
DECLARE
    resultat INT;
    attendu INT;
BEGIN
    RAISE NOTICE '=== TEST 4.5 : getVentesTotal() ===';
    -- Oeufs (productID=2) : transaction 1 (10) + 3 (8) + 9 (12) = 30
    attendu := 30;
    resultat := getVentesTotal(2);
    IF resultat = attendu THEN
        RAISE NOTICE '  [OK] getVentesTotal(2) = % (attendu : % pour Oeufs)', resultat, attendu;
    ELSE
        RAISE WARNING '  [ECHEC] getVentesTotal(2) = % (attendu : %)', resultat, attendu;
    END IF;
END $$;

-- Test 4.6 : getVentesUserTotal() — ventes d'un produit pour un utilisateur
DO $$
DECLARE
    resultat INT;
    attendu INT;
BEGIN
    RAISE NOTICE '=== TEST 4.6 : getVentesUserTotal() ===';
    -- Alice (u_id=2) a vendu des Oeufs (productID=2) : transaction 1 (10) = 10
    attendu := 10;
    resultat := getVentesUserTotal(2, 2);
    IF resultat = attendu THEN
        RAISE NOTICE '  [OK] getVentesUserTotal(2, 2) = % (attendu : %)', resultat, attendu;
    ELSE
        RAISE WARNING '  [ECHEC] getVentesUserTotal(2, 2) = % (attendu : %)', resultat, attendu;
    END IF;
END $$;

/*===========================================*/
/*  5. TESTS DE REQUÊTES COURANTES           */
/*===========================================*/

-- Test 5.1 : Lister les animaux d'un utilisateur avec leur type
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 5.1 : Animaux d''Alice avec sous-classes ===';
    SELECT COUNT(*) INTO nb
    FROM Animal a
    LEFT JOIN Chicken c ON a.a_id = c.a_id
    LEFT JOIN Rabbit r ON a.a_id = r.a_id
    LEFT JOIN Cow co ON a.a_id = co.a_id
    WHERE a.u_id = 2;

    IF nb = 4 THEN
        RAISE NOTICE '  [OK] Alice a % animaux (attendu : 4)', nb;
    ELSE
        RAISE WARNING '  [ECHEC] Alice a % animaux (attendu : 4)', nb;
    END IF;
END $$;

-- Test 5.2 : Vérifier les poulets qui jeûnent
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 5.2 : Poulets qui jeûnent ===';
    SELECT COUNT(*) INTO nb
    FROM Chicken
    WHERE fasting > 0;

    IF nb = 2 THEN
        RAISE NOTICE '  [OK] % poulets jeûnent (attendu : 2)', nb;
    ELSE
        RAISE WARNING '  [ECHEC] % poulets jeûnent (attendu : 2)', nb;
    END IF;
END $$;

-- Test 5.3 : Vérifier le stock total d'un utilisateur
DO $$
DECLARE
    total INT;
BEGIN
    RAISE NOTICE '=== TEST 5.3 : Stock total d''Alice ===';
    -- Alice : (2,25) + (3,10) + (7,50) = 85
    SELECT SUM(quantity) INTO total
    FROM Stock
    WHERE u_id = 2;

    IF total = 85 THEN
        RAISE NOTICE '  [OK] Stock total d''Alice = % (attendu : 85)', total;
    ELSE
        RAISE WARNING '  [ECHEC] Stock total d''Alice = % (attendu : 85)', total;
    END IF;
END $$;

-- Test 5.4 : Vérifier les animaux malades
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 5.4 : Animaux malades ===';
    SELECT COUNT(*) INTO nb
    FROM Animal
    WHERE healthy = FALSE;

    IF nb = 2 THEN
        RAISE NOTICE '  [OK] % animaux malades (attendu : 2)', nb;
    ELSE
        RAISE WARNING '  [ECHEC] % animaux malades (attendu : 2)', nb;
    END IF;
END $$;

-- Test 5.5 : Produits disponibles dans la coopérative ouverte
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 5.5 : Produits en coopérative ouverte ===';
    SELECT COUNT(*) INTO nb
    FROM Cooperative
    WHERE is_open = TRUE;

    IF nb = 3 THEN
        RAISE NOTICE '  [OK] % produits ouverts en coopérative (attendu : 3)', nb;
    ELSE
        RAISE WARNING '  [ECHEC] % produits ouverts en coopérative (attendu : 3)', nb;
    END IF;
END $$;

-- Test 5.6 : Vérifier le chiffre d'affaires total des transactions
DO $$
DECLARE
    total FLOAT;
    attendu FLOAT;
BEGIN
    RAISE NOTICE '=== TEST 5.6 : Chiffre d''affaires total ===';
    -- 50 + 50 + 40 + 100 + 30 + 60 + 30 + 30 + 60 + 60 = 510
    attendu := 510.0;
    SELECT SUM(prix_total) INTO total FROM Transactions;

    IF total = attendu THEN
        RAISE NOTICE '  [OK] CA total = % (attendu : %)', total, attendu;
    ELSE
        RAISE WARNING '  [ECHEC] CA total = % (attendu : %)', total, attendu;
    END IF;
END $$;

-- Test 5.7 : Nombre de produits en vente sur le Market
DO $$
DECLARE
    nb INT;
BEGIN
    RAISE NOTICE '=== TEST 5.7 : Produits sur le Market ===';
    SELECT COUNT(*) INTO nb FROM Market;

    IF nb = 6 THEN
        RAISE NOTICE '  [OK] % produits sur le Market (attendu : 6)', nb;
    ELSE
        RAISE WARNING '  [ECHEC] % produits sur le Market (attendu : 6)', nb;
    END IF;
END $$;

/*===========================================*/
/*  6. RÉSUMÉ                                */
/*===========================================*/

DO $$
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE '  TESTS TERMINÉS                         ';
    RAISE NOTICE '  Vérifiez les messages ci-dessus.       ';
    RAISE NOTICE '  [OK] = test réussi                     ';
    RAISE NOTICE '  [ECHEC] = test échoué (WARNING)        ';
    RAISE NOTICE '==========================================';
END $$;
