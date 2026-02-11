Projet		: TinyFarm
Réunion numéro  : 2
Date de réunion : 11 février 2026 ( 15h30 - 16H20)
Auteur 		: CHUPIN Noa 486M

----------------------------------------------------
# Objectif de la réunion
		
	1-  Mise en commun des informations sur la BDD et répartion du travail.
	
	
----------------------------------------------------

## Mise en commun en se basant sur les travaux d'Aubry et Noa .

    - Revue de la table Animal ( présentation Aubry ) :
        - Schéma BDD > 
            - Table Animal ( Mère ) puis

            - Table Ranking ==> Vue actualisable ? 
                 Tyler propose une Table updatable.
                 Mewenn : Modification des données, il faudra forcement les faire que ce soit une vue ou une table peu importe.
                 Tyler : Faire des vues ==> rajoute du travail, rajoute une couche en plus pas forcement nécéssaire.
                 Maël et Ethan : Permet d'économisé au niveau du coût : beaucoup trop d'update !!!!

                 Accord du groupe sur les VUES actualisables.

            - Retour sur la table animal.
                Principe d'héritage sur les tables découlant de la table Animal.
                Nécéssaire de garder le type ENUM de la table Mère ???
                Il faut faire attention à l'unicité des clés primaires entre toutes les tables Animal.
                ==>Initialisation des clés dans la table Mère.

            - Aubry et Mewenn : Nouveau principe de message pour l'utilisateur : Nouvelle Table Evenement:
                Suppression des messages une fois lu par l'utilisateur. ( Pas besoin de les garder en mémoire)


            - Table Bureau : 
                - Différenciation entre product et objet : 
                    On garde seulement la table product + ajout d'un bouléen pour différencier objets collectables et produits vendables.

            - Tyler pose une question sur la table Evenement : 
                - Problème pour gérer la table Evenement ? Trop le bazar ?
                    - Non puisque les événements possibles sont dénombrables + la BDD n'a pas besoin de les stocker.

            
            - Retour sur la Cooperative ( transactions avec des robots ): (Savio) : 
                La coopérative à un stock.
                nb achat limité par rapport au niveau.
                la coopérative est soumise à des horaires d'ouvertures. ( contraintes temporelles )
                conditions pour acheter certains produits ( notament par rapport aux objets achetable ( par niveaux )) géré par code JAVA.


            - Table transaction : ( Aubry ) : 
                - Utilité de la table ??
                - Pas besoin d'avoir un historique des transactions.
                - Retirer la Table transaction ( cause : Inutile ).

            - Table Market ( Transactions avec les autres joueurs ) :
                - Le Marché est soumis à des horaires d'ouvertures. ( contraintes temporelles )
                

            - Ajout d'un attribut dans la Table User : niveau INTEGER. ( Necessaire pour connaître le nombre de transactions/jours et dévérouiller certaines actions.).


            Organisation : Répartition du code des tables par tous les membres backends.
            
            Répartition encore à faire.
            Noa s'occupe de faire le nouveau schéma avec les nouveautées de la réunions.

    

                    





