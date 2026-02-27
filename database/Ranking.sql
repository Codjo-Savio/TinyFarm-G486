-- Nombre d'utilisateurs
CREATE FUNCTION getNbUser() RETURNS INT AS $$
    SELECT COUNT(*)::INT
    FROM "User"
    WHERE u_id != 1; -- pour ne pas prendre la coopérative
$$ LANGUAGE SQL;

-- Nombre d'ecus totaux
CREATE FUNCTION getEcusTotaux() RETURNS INT AS $$
    SELECT SUM(ecus)::INT
    FROM "User"
    WHERE u_id != 1; -- pour ne pas prendre les écus de la coopérative
$$ LANGUAGE SQL;

-- Nombre d'ecus d'un utilisateur
CREATE FUNCTION getEcusUser(p_user INT) RETURNS INT AS $$
    SELECT ecus
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
    RETURN (EcusU * 100 / EcusT) * (30 * NbU);
END;
$$ LANGUAGE plpgsql;

-- Coefficient du score d'un produit
CREATE FUNCTION getCoef(prod INT) RETURNS INT AS $$
    SELECT coef
    FROM Product
    WHERE productID = prod;
$$ LANGUAGE SQL;

-- Total des ventes du produit
CREATE FUNCTION getVentesTotal(prod INT) RETURNS INT AS $$
    SELECT SUM(quantite)::INT
    FROM Transactions
    WHERE product = prod;
$$ LANGUAGE SQL;

-- Total des ventes du produit pour l'utilisateur
CREATE FUNCTION getVentesUserTotal(prod INT, p_user INT) RETURNS INT AS $$
    SELECT SUM(quantite)::INT
    FROM Transactions
    WHERE product = prod
    AND seller = p_user;
$$ LANGUAGE SQL;

-- Score de ventes pour un produit et un utilisateur
CREATE FUNCTION getScoreProduit(prod INT, p_user INT) RETURNS INT AS $$
DECLARE
    VenteU INT;
    VenteT INT;
    coef INT;
    NbU INT;
BEGIN
    VenteU := getVentesUserTotal(prod, p_user);
    VenteT := getVentesTotal(prod);
    coef := getCoef(prod);
    NbU := getNbUser();
    RETURN (VenteU * 100 / VenteT) * (coef * NbU);
END;
$$ LANGUAGE plpgsql;

-- Score de ventes pour un utilisateur
CREATE FUNCTION getScoreVente(p_user INT) RETURNS INT AS $$
DECLARE
    ScoreL INT;
    ScoreP INT;
    ScoreV INT;
BEGIN
    ScoreL := getScoreProduit(1, p_user); -- remplacer 1 par le productID des lapins
    ScoreP := getScoreProduit(2, p_user); -- remplacer 2 par le productID des oeufs
    ScoreV := getScoreProduit(3, p_user); -- remplacer 3 par le productID du lait
    RETURN ScoreL + ScoreP + ScoreV;
END;
$$ LANGUAGE plpgsql;

-- la vue Ranking
CREATE VIEW Ranking AS
    SELECT u_id, getScoreEcus(u_id) AS scoreEcus, getScoreVente(u_id) AS scoreVente
    FROM "User";

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
