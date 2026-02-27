/*===========================================*/
/*  FONCTIONS DE CLASSEMENT - TinyFarm       */
/*===========================================*/

SET search_path TO TinyFarm;

-- Nombre d'utilisateurs (sans la coopérative)
CREATE FUNCTION getNbUser() RETURNS INT AS $$
    SELECT COUNT(*)::INT
    FROM "User"
    WHERE u_id != 1; -- pour ne pas prendre la coopérative
$$ LANGUAGE SQL;

-- Nombre d'ecus totaux (sans la coopérative)
CREATE FUNCTION getEcusTotaux() RETURNS INT AS $$
    SELECT COALESCE(SUM(ecus), 0)::INT
    FROM "User"
    WHERE u_id != 1; -- pour ne pas prendre les écus de la coopérative
$$ LANGUAGE SQL;

-- Nombre d'ecus d'un utilisateur
CREATE FUNCTION getEcusUser(p_user INT) RETURNS INT AS $$
    SELECT COALESCE(ecus, 0)
    FROM "User" u
    WHERE u.u_id = p_user;
$$ LANGUAGE SQL;

-- Score d'écus
CREATE FUNCTION getScoreEcus(p_user INT) RETURNS INT AS $$
DECLARE
    EcusU INT;
    EcusT INT;
    NbU INT;
BEGIN
    EcusU := getEcusUser(p_user);
    EcusT := getEcusTotaux();
    NbU := getNbUser();
    IF EcusT = 0 OR NbU = 0 THEN
        RETURN 0;
    END IF;
    RETURN (EcusU * 100 / EcusT) * (30 * NbU);
END;
$$ LANGUAGE plpgsql;

-- Coefficient du score d'un produit
CREATE FUNCTION getCoef(prod INT) RETURNS INT AS $$
    SELECT COALESCE(coef, 1)
    FROM Product
    WHERE productID = prod;
$$ LANGUAGE SQL;

-- Total des ventes du produit
CREATE FUNCTION getVentesTotal(prod INT) RETURNS INT AS $$
    SELECT COALESCE(SUM(quantite), 0)::INT
    FROM Transactions
    WHERE product = prod;
$$ LANGUAGE SQL;

-- Total des ventes du produit pour l'utilisateur
CREATE FUNCTION getVentesUserTotal(prod INT, p_user INT) RETURNS INT AS $$
    SELECT COALESCE(SUM(quantite), 0)::INT
    FROM Transactions
    WHERE product = prod
    AND seller = p_user;
$$ LANGUAGE SQL;

-- Score de ventes pour un produit et un utilisateur
CREATE FUNCTION getScoreProduit(prod INT, p_user INT) RETURNS INT AS $$
DECLARE
    VenteU INT;
    VenteT INT;
    v_coef INT;
    NbU INT;
BEGIN
    VenteU := getVentesUserTotal(prod, p_user);
    VenteT := getVentesTotal(prod);
    v_coef := getCoef(prod);
    NbU := getNbUser();
    IF VenteT = 0 OR NbU = 0 THEN
        RETURN 0;
    END IF;
    RETURN (VenteU * 100 / VenteT) * (v_coef * NbU);
END;
$$ LANGUAGE plpgsql;

-- Score de ventes pour un utilisateur (dynamique sur tous les produits)
CREATE FUNCTION getScoreVente(p_user INT) RETURNS INT AS $$
DECLARE
    total_score INT := 0;
    prod RECORD;
BEGIN
    FOR prod IN SELECT productID FROM Product LOOP
        total_score := total_score + getScoreProduit(prod.productID, p_user);
    END LOOP;
    RETURN total_score;
END;
$$ LANGUAGE plpgsql;

-- la vue Ranking (exclut la coopérative u_id=1)
CREATE VIEW Ranking AS
    SELECT u_id, getScoreEcus(u_id) AS scoreEcus, getScoreVente(u_id) AS scoreVente
    FROM "User"
    WHERE u_id != 1;

-- Rang dans le classement des écus d'un utilisateur
CREATE FUNCTION getRankEcus(p_user INT) RETURNS INT AS $$
    SELECT COUNT(*)::INT
    FROM Ranking r
    WHERE r.scoreEcus > (SELECT scoreEcus
                         FROM Ranking
                         WHERE u_id = p_user);
$$ LANGUAGE SQL;

-- Rang dans le classement des ventes d'un utilisateur
CREATE FUNCTION getRankVente(p_user INT) RETURNS INT AS $$
    SELECT COUNT(*)::INT
    FROM Ranking r
    WHERE r.scoreVente > (SELECT scoreVente
                         FROM Ranking
                         WHERE u_id = p_user);
$$ LANGUAGE SQL;

-- Rang global de l'utilisateur
CREATE FUNCTION getRank(p_user INT) RETURNS INT AS $$
DECLARE
    RankV INT;
    RankE INT;
BEGIN
    RankE := getRankEcus(p_user);
    RankV := getRankVente(p_user);
    RETURN (RankE + RankV) / 2; -- Possibilité d'égalité, à résoudre ?
END;
$$ LANGUAGE plpgsql;
