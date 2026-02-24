-- Nombre d'utilisateur
CREATE FUNCTION getNbUser() RETURNS INT
    SELECT COUNT(*) AS result
    FROM User
    WHERE u_id != 1 -- pour ne pas prendre la coopérative
    LANGUAGE SQL;

-- Nombre d'ecus totaux
CREATE FUNCTION getEcusTotaux() RETURNS INT
    SELECT SUM(ecus) AS result
    FROM Users
    WHERE u_id != 1 -- pour ne pas prendre les écus de la coopérative
    LANGUAGE SQL;

-- Nombre d'ecus d'un utilisateur
CREATE FUNCTION getEcusUser(user int) RETURNS INT
    SELECT ecus AS result
    FROM Users u
    WHERE u.u_id = user
    LANGUAGE SQL;

-- Score d'écus
CREATE FUNCTION getScoreEcus(user int) RETURNS INT
    DECLARE EcusU INT;
    DECLARE EcusT INT;
    DECLARE NbU INT;
    BEGIN
        SET EcusU = getEcusUser(user);
        SET EcusT = getEcusTotaux();
        SET NbU = getNbUser();
        RETURN (EcusU/EcusT)*100*(30*NbU)
    LANGUAGE SQL;

-- Coefficient du score d'un produit
CREATE FUNCTION getCoef(prod INT) RETURNS INT
    SELECT coef AS result
    FROM Products
    WHERE p-id = prod
    LANGUAGE SQL;

-- /!\ NE FONCTIONNE PAS A PARTIR D'ICI /!\ ----------------------------------------------

-- Total des ventes du produit
CREATE FUNCTION getVentesTotal(prod INT) RETURNS INT
    SELECT SUM(quantite) AS result
    FROM Transactions
    WHERE product = prod
    LANGUAGE SQL;

-- Total des ventes du produit pour l'utilisateur
CREATE FUNCTION getVentesTotal(prod INT, user INT) RETURNS INT
    SELECT SUM(quantite) AS result
    FROM Transactions
    WHERE product = prod
    AND seller = user
    LANGUAGE SQL;

-- Score de ventes pour un produit et un utilisateur
CREATE FUNCTION getScoreProduit(prod INT,user INT) RETURNS INT
    DECLARE VenteU INT;
    DECLARE VenteT INT;
    DECLARE coef INT;
    DECLARE NbU INT;
    BEGIN
        SET VenteU = getEcusUser(prod, user);
        SET VenteT = getEcusTotaux(prod);
        SET coef = getCoef(prod);
        SET NbU = getNbUser();
        RETURN (VenteU/VenteT)*100*(coef*NbU)
    LANGUAGE SQL;

-- Score de ventes pour un utilisateur
CREATE FUNCTION getScoreVente(user INT) RETURNS INT
    DECLARE ScoreL INT;
    DECLARE ScoreP INT;
    DECLARE ScoreV INT;
    BEGIN
        SET ScoreL = getScoreProduit(1, user); -- remplacer 1 par le p_id des lapins
        SET ScoreP = getScoreProduit(2, user); -- remplacer 2 par le p_id des oeufs
        SET ScoreV = getScoreProduit(3, user); -- remplacer 1 par le p_id du lait
        RETURN ScoreL + ScoreP + ScoreL
    LANGUAGE SQL;

-- Rang dans le classement des écus d'un utilisateur
CREATE FUNCTION getRankEcus(user INT) RETURNS INT
    SELECT COUNT(*) AS result
    FROM Ranking r
    WHERE r.scoreEcus > (SELECT scoreEcus
                         FROM Ranking
                         WHERE u_id = user);
    LANGUAGE SQL;

-- Rang dans le classement des ventes d'un utilisateur
CREATE FUNCTION getRankVente(user INT) RETURNS INT
    SELECT COUNT(*) AS result
    FROM Ranking r
    WHERE r.scoreVente > (SELECT scoreVente
                         FROM Ranking
                         WHERE u_id = user);
    LANGUAGE SQL;

-- Rang global de l'utilisateur
CREATE FUNCTION getRank(user INT) RETURNS INT
    DECLARE RankV INT;
    DECLARE RankE INT;
    BEGIN
        SET RankE = getRankEcus(user);
        SET RankV = getRankVente(user);
        RETURN (RankE + RankV)/2 -- Possibilité d'égalité, à résoudre ?
    LANGUAGE SQL;

-- la vue Ranking
CREATE VIEW Ranking AS
    SELECT u_id, getScoreEcus(u_id) AS scoreEcus, getScoreVente(u_id) AS scoreVente
    FROM Users;