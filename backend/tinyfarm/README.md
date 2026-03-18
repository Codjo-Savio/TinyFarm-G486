### Run the project
1) to compile :
    mvn clean compile

2) to compile and run the tests : 
    mvn clean install

3) to run the app in dev context (with H2)
    mvn spring-boot:run

4) to run the app in prod context (with postgres)
5) do cd docker and 
    docker compose up --build