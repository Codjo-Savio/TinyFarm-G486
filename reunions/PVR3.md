Projet		: TinyFarm
Réunion numéro  : 3
Date de réunion : 03 Mars 2026 ( 8H13 - 9H20 )
Auteur 		: CHUPIN Noa 486M

----------------------------------------------------
# Objectif de la réunion
		
	1-  Plan d'action de Savio
	2-  Discution sur la table Ranking
	3-  Discution sur la BDD en générale
	
	
----------------------------------------------------

### Plan d'action de Savio

## DDD - Domaine Driven Developpement ( Découpage du projet en étapes ) :

  -Domaines :
    - Poulailler
    - Clapier
    - Pâturage
    - Coopérative
    - Market
    - Bureau
    - Authentification
    
    
## TDD - Test Driven Developpement ( écrire Test ) : 
  - Ecrire du test puis écrire le minimum de test pour passer les tests ( comme en TP )
      - Le backend travaille en même temps.
      
    Parenthèse sur le TDD : 
      - Le prof de TD : 
        - Il faut bien prendre le temps pour écrire les tests. faire des duos d'inputs.
          - Il faut tester tous les cas possibles, un maximum d'input possible, il faut y passer du temps.

# / ! \ Important / ! \
  - Le code push doit être fonctionnel, faire un effort sur la synthaxe !! Du code qui marche par pitié.
  - Savio invite fortement à produire du code PROPRE !
  
  
  
## Mise au point de la BDD par Mewenn

# Vue ranking ( ranking par Porduction ) 
 -Il est dans la spec, mais aucune info sur la "PRODUCTION" que faire ? 
    La garder ? 
    
    Mention dans la spec avec la production des champs ? et des potagés ? Mais on ne sait pas d'où ça vient ???
      -Il n'y a pas de champs ou de potagés....
      
        Erreur de spec selon le prof ? Reponse pendant le TP de 10h50.
        Idem pour le marché noir.
        Truc pas clair dans le sujet.
  
  - Il faut regarder la vue ranking, si elle est fonctionnelle avec le code de Tyler.
      
      
# BDD 
    - Table transaction ==> retour, nous en avons besoin. Tyler la remise le repo.
      - Essentiel pour le calcul des scores. 
        - Il faut pouvoir récupéré le nb d'item vendu par l'user. Il faut garder tout ça dans la BDD.
          ( Aubry propose de juste avoir un attribut dans USER comptant le nombre de transaction ?)
          Il faudrait avoir un attribut par items vendable.
          
          Regard sur l'attribut date : Redondance sur la date et le t_id.
          On retire T_ID.
          Clé primaire : buyer seller date.
          
          
          On garde la table transaction ! Savio. Cependant il faudra assumé le code nécéssaire pour garder la table.
      
      - Au sujet des produits besoin d'un attribut coeff de produit à ajouter dans la table Produit.
        - Pour le calcul des scores de vente de l'utilisateur.
        - Déjà ajouter dans la table par Tyler.
        - Faire des fonctions à l'exterieur pour les calculs de score.
        
        - Au niveau de l'attribut price : Pas besoin de price, parce qu le prix moyen est recalculer en boucle.
          - Seulement a garder dans le market et la coopérative.
          - Ajout d'une fonction pour le calcul du prix en fonction du marché.
          - Il va falloir faire en sorte de faire attention au nb de calcul.
          - Intervention du prof sur la compétitivité : Que se passe-t-il si deux utilisateurs achètent le produit en même temps ?   
               - Une fois que le porduit est acheté comment faire pour que le vendeur soit bien rémunéré au montant auquel il avait fixé au moment de la vente sur le marché, et non au prix auquel l'acheteur l'acheter.
      
      
      - Au sujet de la table chicken : 
        - attribut fasting : il va falloir l'update a chaque fois, a voir avec le coût :
          - INT ou DATE ?
            - On partirai sur un attribut date pour généraliser quotidiennement. les décès en cas de jeûne de plus de 4 jours.
              - Au niveau de la date seulement garder le jour, mois, année.
              - Prendre en compte le fait que si le compte est en hibernation depuis 50 jours, on supprime le compte.
      
      - Propostion de Noah pour une actualisation à chaque connexion du l'utilisateur pour allégé le calcul quotidien pour le serveur ? ( Pour le calcul des maladies etc... ).
        - Si jamais l'utilisateur se connecte après 40 jours, les calculs vont être très lourds, pas très efficace au final.
            ==> on garde l'acualisation quotidienne par le serveur pour tous les comptes. ( Il fera nuit, pas beaucoup d'utilisateur, le serveur ne sera pas surbooker ).
            
            
      - Table Lapin : ( Aubry ). 
        - Proposition de gérer par Clapier et non par lapin comme dans la spec.
        - Comment faire la différence entre les lapereaux et les lapins dans le Clapier. Regarder au niveaux des portées de lapin.
      On peut tout refaire dans un Clapier. Seuelement possible pour les lapins. Pas possible pour le poulailler.
      ( Prototype sur le Repo - Schema BDD 19_02_26.svg ) Commit d'Aubry dans la semaine sur le sujet ?
      
      
      Modification sur la Table Produit et Desktop : 
        - On retire la table Desktop : et ajout d'un booléen dans le produit "collectible" pour savoir si le produit est un produit de collection.


## Fin de réunion.
