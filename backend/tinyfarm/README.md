### Run the project
1) to compile :
    `mvn clean compile`

2) to compile and run the tests : 
    `mvn clean install`

3) to run the app in dev context (with H2)
    `mvn spring-boot:run`

4) to run the app in prod context (with postgres)
5) do `cd docker` and 
    `docker compose up --build`

# END POINT
### Animal ("/api/animals")
    * récupérer tous les animaux                     Get("")
    * récupérer un animal par identifiant            Get("/id/{id}")
    
    * créer un animal Post("")
    
    * mettre a jour un animal par identifiant        Put("/id/{id}")
    
    * retirer un animal par identifiant              Delete("/id/{id}")

### Chicken ("/api/chickens")
    * récupérer tous les poulets Get("")
    * récupérer un poulet par identifiant            Get("/id/{id}")
    * récupérer un poulet par nom                    Get("/name/{name}")
    
    * créer un poulet                                Post("")
    * nourrire un poulet par identifiant             Post("/{id}/feed")
    * abreuver un poulet par identifiant             Post("/{id}/water")
    * laver un poulet par identifiant                Post("/{id}/clean")
    * soigner un poulet par identifiant              Post("/{id}/heal")
    * fin de journée des poulets                     Post("/endOfDay")
    
    * mettre a jour un poulet par identifiant        Put("/id/{id}")
    * mettre a jour un poulet par nom                Put("/id/{id}")
    
    * retirer un poulet par identifiant              Delete("/id/{id}")
    * retirer un poulet par nom                      Delete("/id/{id}")
    
### Cow ("/api/cows")
    * récupérer tous les vaches                      Get("")
    * récupérer une vache par identifiant            Get("/id/{id}")
    * récupérer une vache par nom                    Get("/name/{name}")
    
    * créer une vache Post("")
    
    * mettre a jour une vache par identifiant        Put("/id/{id}")
    
    * retirer une vache par identifiant              Delete("/id/{id}")
    * retirer une vache par nom                      Delete("/name/{name}")


### Rabbit ("/api/rabbits")
    * récupérer tous les lapins                      Get("")
    * récupérer un lapin par identifiant             Get("/{id}")
    * récupérer un lapin par nom                     Get("/filter/name/{name}")
    * récupérer un lapin par type                    Get("/filter/type/{rabbitType}")
    
    * créer un lapin                                 Post("")
    *nourrir un lapin par identifiant                Post("/{id}/feed")
    *abreuver un lapin par identifiant               Post("/{id}/water")
    *laver un lapin par identifiant                  Post("/{id}/clean")
    *soigner un lapin par identifiant                Post("/{id}/heal")
    *fin de journée des lapins                       Post("endOfDay")
    
    * mettre a jour un lapin par identifiant         Put("/{id}")
    
    * retirer un lapin par identifiant               Delete("/{id}")
    * retirer tous les lapins                        Delete("/all")
    
### User ("/api/users")
    * récupérer tous les utilisateurs                Get("")
    * récupérer un utilisateurs par identifiant      Get("/id/{id}")
    
    * créer un utilisateurs                          Post("")
    
    * mettre a jour un Utilisateur par identifiant   Put("/id/{id}")
    
    * supprimer un utilisateur par identifiant       Delete("/id/{id}")
    
    * ajouter des ecus                               Patch("ecus/add/id/{id}")
    * retirer des ecus                               Patch("ecus/withdraw/id/{id}")

### Product ("/api/products")
    * récupérer tous les produits                    Get("")
    * récupérer un produit par identifiant           Get("/id/{id}")
    * récupérer tous les collectible                 Get("/filter/collectible/{collectible}")
    * récupérer un produit pour un coefficient       Get("/filter/coefficient/{coefficient}")
    
    * ajouter un produit                             Post("")
    
    * mettre a jour un produit par identifiant       Put("/id/{id}")
    
    * retirer un produit par identifiant             Delete("/id/{id}")
