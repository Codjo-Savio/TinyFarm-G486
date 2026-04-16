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
6) to check test failure causes
    `mvn test -pl . -Dtest=JwtRequestFilterTest -e 2>&1 | grep "Caused by"`
7) to test the authentication : http://localhost:8080/oauth2/authorization/github
8) then press F12 - go to Applications - cookies - copy the token - ten go to Postman
and test http://localhost:8080/api/auth/me (or any protected route) with
Authorization - Bearer - and paste the token that was copied


In src/main/resources you have application.properties.example and application-test.properties.example

Before runing the tests and the mvn app, create two files nammed application.properties and application-test.properties
in which you will copy and paste the content of the two exemple (respectively) by filling the empty spaces with your own secrets
# END POINT
### Animal ("/api/animals")
    * récupérer tous les animaux                     Get("")
    * récupérer un animal par identifiant            Get("/id/{id}")
    
    * créer un animal Post("")
    
    * mettre à jour un animal par identifiant        Put("/id/{id}")
    
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
    
    * mettre à jour un poulet par identifiant        Put("/id/{id}")
    * mettre à jour un poulet par nom                Put("/id/{id}")
    
    * retirer un poulet par identifiant              Delete("/id/{id}")
    * retirer un poulet par nom                      Delete("/id/{id}")
    
### Cow ("/api/cows")
    * récupérer tous les vaches                      Get("")
    * récupérer une vache par identifiant            Get("/id/{id}")
    * récupérer une vache par nom                    Get("/name/{name}")
    
    * créer une vache Post("")
    
    * mettre à jour une vache par identifiant        Put("/id/{id}")
    
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
    
    * mettre à jour un lapin par identifiant         Put("/{id}")
    
    * retirer un lapin par identifiant               Delete("/{id}")
    * retirer tous les lapins                        Delete("/all")
    
### User ("/api/users")
    * récupérer tous les utilisateurs                Get("")
    * récupérer un utilisateurs par identifiant      Get("/id/{id}")
    
    * créer un utilisateurs                          Post("")
    
    * mettre à jour un Utilisateur par identifiant   Put("/id/{id}")
    
    * supprimer un utilisateur par identifiant       Delete("/id/{id}")
    
    * ajouter des ecus                               Patch("ecus/add/id/{id}")
    * retirer des ecus                               Patch("ecus/withdraw/id/{id}")

### Product ("/api/products")
    * récupérer tous les produits                    Get("")
    * récupérer un produit par identifiant           Get("/id/{id}")
    * récupérer tous les collectible                 Get("/filter/collectible/{collectible}")
    * récupérer un produit pour un coefficient       Get("/filter/coefficient/{coefficient}")
    
    * ajouter un produit                             Post("")
    
    * mettre à jour un produit par identifiant       Put("/id/{id}")
    
    * retirer un produit par identifiant             Delete("/id/{id}")


### Market ("api/market")


  * récupérer un market par UserId                   Get("/id/{id}")
  * récupérer un market par ProductId                Get("/product/{productId}")
  * récupérer un market par price                    Get("/price/{price}")
  * récupérer un market par quantity                 Get("/quantity/{quantity})

  * ajouter un market                                Post("")
    
  
  * mettre à jour un market par UserId               Put("/id/{id}")
  
  
  * retirer un market par UserId et ProductId        Delete("/{userId}/{productId}")
  * retirer un market par UserId                     Delete("/id/{uid}")
  
  ### Transaction ("api/transaction")
  
  * récupérer une transaction par Id                Get("/id/{id})
  * récupérer une transaction par Buyer             Get("/buyer/{buyer})
  * récupérer une transaction par Seller            Get("/seller/{seller})
  * récupérer une transaction par Product           Get("/product/{product})
  
  * ajouter une transaction                         Post("")
  
  * mettre à jour une transaction                   Put("id/{id}")

  * retirer une transaction par Id                  Delete("id/{id}")

  ### Stock ("api/stocks")
  
  * récupérer tous les stocks                       Get("")
  * récupérer un stock par UserId et ProductId      Get("/user/{userId}/product/{productId}")
  * récupérer un stock par UserId                   Get("/user/{userId}")
  * récupérer tous les stocks                       Get("/product/{productId}")

  * ajouter un stock                                Post("")
  // {tid} ==> transactionId
  * vendre un produit                               Post("/sell/{tid}")
  * acheter un produit                              Post("/buy/{tid}")
  
  * mettre à jour un stock                          Put("/user/{userId}/product/{productId}")
  
  * retirer un stock par UserId et ProductId        Delete("/user/{userId}/product/{productId}")
  * retirer un stock par UserId                     Delete("/user/{userId}")
  * retirer un stock par ProductId                  Delete("/product/{productId}")

### Event ("/api/event")
    * récupérer les events par id, et les supprimer de la database Get("/id/{id}")