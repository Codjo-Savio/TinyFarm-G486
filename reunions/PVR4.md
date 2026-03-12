Projet		: TinyFarm
Réunion numéro  : 4
Date de réunion : 10 Mars 2026 ( 8H07 - 9H20 )
Auteur 		: CHUPIN Noa 486M

----------------------------------------------------
# Objectif de la réunion
		
	1- Planification ( BackEnd - FrontEnd ) Savio - Mewenn - Maël 
	2- Premier regard sur les classes : Savio
	3- Répartition du travail : Savio
	4- Rappels et Conventions sur le Code : Savio
	
	
----------------------------------------------------

## 1 ) - Planification

  # Poulailler : 
    - Code des classes / tables qui concernent le poulailler
      - Entity
      - Repository
      - Servives
      - Controller
    - Pour le pouilailler besoin des classes suivantes !
      - User 
      - Produit
      - Animal
      - chicken 
    Et ainsi faire la même chose pour ces tables.
    
    Il faudra définir les conventions pour le code. 
    
  # Pour le FrontEnd : 
  
  - Besoin des conventions des chemins par le backend pour les API
  
  - En attendant : 
    - Checker pour les boutons ect pour l'interface + code javascript pour les appels à l'API

## 2 ) Premier regard sur le necéssaire pour les Entity, Repository, Services, Controller

  # User :
    - entity : Pour les personnes qui codent le backend, bien faire aattention aux @Table, @Column
    - Repository : methodes necéssaires : findById, findByName pas nécassaire pour les coders car Spring s'en charge, juste besoin de faire les signatures. @Query
    - Services : Créer le compte, gagner des écus ...
    - Controller : api/user/*...

  # Produit : (idem on reprend le même principe)
      - Entity :
      - Repository :
      - Services : 
      - Controller : 

  # Pareil pour les autres tables.
  
## 3 ) Répartition du travail 

  # BackEnd

    Dans l'idéal : 2 personnes par tables:
  
      - Pour la reflexion et la création du code.
        - User : Tyler - Noa.C
        - Produit : Balian - Aubry
        - Animal : Mewenn - Evan
        - Chicken : Killian - Tucdual
        - Test : Savio - Tyler - Mewenn ( Travail à faire en amont )


  # FrontEnd :

    - Repartition du travail prochainement.

## 4 ) Unification des conventions et rappels

  - Ecriture : camelCase
    - type : tableChicken, SuperChefDeProjetSavio
  
  - IL FAUT COMMENTER VOS CODES !!!

  - Vous pouvez utiliser l'IA pour vous aider à comprendre le code ect. MAIS ne recopiez pas bêtement le code généré !!!
    - 1 ) Car ça se voit !!! 
    - 2 ) Soucis de sécurités ET validités du code.
    - 3 ) Vous n'apprenez pas avec ça.
   Il vous faut donc, LIRE, COMPRENDRE et REECRIRE le code dans le but que le code fonctionne ! 

  - Pour les pulls requests : IL faudra d'abord montrer le code aux chef de groupes ( Mewenn et Mael ) pour une  fois vérification ==> possibilité du push sur le git.
  
  - Rappel du Prof sur le code : 
    - Au niveau de la notation pour le projet : Une partie de la note est sur la possibilité de faire évoluer le code du projet
    - Pour le niveau : On peut essayer le gérer le système de niveau mais pas besoin de trop se prendre la tête.
    
    
  - Dans l'idéal DEADLINE pour le code des classes : La semaine du 16 MARS 2026 !
