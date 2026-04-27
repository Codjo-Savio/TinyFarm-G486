# TinyFarm - Project G486

## About

**TinyFarm** is a website developed by students at the University of Nantes as part of the client-side web development course. This project enables students to apply the concepts they have studied throughout their Computer Science Bachelor's degree.

The project is loosely based on the principles of the game *My e-Farm*.

## Quick Start

### Requirements

- **Docker** (for deployment)
- **JDK 21** (for backend development)
- **Maven 3.8+** (dependency manager)
- **Node.JS 22 & NPM** (for frontend)

### Setting up the environnement

- **JDK 21**
```bash
cd backend/tinyfarm

# Check the java version
java --version

# Install JDK 21 if the java version is different of 21
sudo apt update
sudo apt install openjdk-21-jdk
```

-  **Application Configurations**

```
Go into main/ressources and create tree files named :
- application-test.properties
- application.properties
- application-prod.properties

and copy/paste the content of their respective .example; then 
in the empty spaces, paste the secrets that are given in the secret.pdf
in the rendering archive

Warning : Without these files, the application will fail to start, and there will be lot of configuration errors
```
### Launch for Development

#### Backend

```bash
cd backend/tinyfarm

# Install dependencies and compile
mvn clean install

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

#### Frontend

```bash
cd frontend

# Install packages
npm i

# Development mode with hot reload
npm run dev

# Or production mode
npm run start
```

### Launch for prod


```bash
cd backend/tinyfarm

#build the docker

docker compose up --build
```

**Access the application**: `https://tinyfarm486.app`

---

## Project Structure

```
TinyFarm-G486/
│
├── backend/tinyfarm/          # Backend code (Java/Spring Boot)
│   ├── src/main/java/
│   │   └── com/api/tinyfarm/
│   │       ├── controller/    # REST Endpoints
│   │       ├── model/         # JPA Classes/Entities
│   │       ├── repository/    # Database Queries
│   │       ├── service/       # Business Logic
│   │       ├── dto/           # Data Transfer Objects
│   │       └── security/      # OAuth Authentication
│   ├── src/main/resources/    # Configuration Files
│   ├── src/test/              # Unit & Integration Tests
│   └── pom.xml                # Maven Configuration
│
├── frontend/                  # Frontend code (HTML/CSS/JS + Express)
│   ├── public/                # Static Files
│   ├── src/                   # JavaScript Source Code
│   └── package.json           # Node Dependencies
│
├── database/                  # SQL Schemas & Functions
├── reunions/                  # Meeting Notes
├── screens/                   # UI/UX Prototypes (Figma)
└── README.md                  # General Documentation

```

### Detailed Backend Structure

```
src/main/java/com/api/tinyfarm/
│
├── controller/                # REST Controllers
│   ├── StockController.java
│   ├── UserController.java
│   ├── TransactionController.java
│   └── ...
│
├── model/                     # JPA Entities
│   ├── User.java
│   ├── Stock.java
│   ├── Transaction.java
│   ├── StockId.java (composite key)
│   └── ...
│
├── repository/               # JPA Interfaces
│   ├── UserRepository.java
│   ├── StockRepository.java
│   ├── TransactionRepository.java
│   └── ...
│
├── service/                  # Business Logic
│   ├── StockService.java
│   ├── UserService.java
│   ├── TransactionService.java
│   └── ...
│
├── security/                 # Authentication
│   ├── JwtRequestFilter.java
│   ├── SecurityConfig.java
│   └── ...
│
└── dto/                      # Data Transfer Objects

```

## Technologies & Frameworks

### Backend

| Technology | Role |
|---|---|
| **Java 21** | Main Language |
| **Spring Boot 3** | Web Framework & Dependency Injection |
| **Maven** | Dependency Manager |
| **JPA/Hibernate** | ORM - Object-Relational Mapping |
| **PostgreSQL** | Relational Database |
| **OAuth 2.0** | GitHub Authentication |
| **JWT** | Authentication Tokens |

### Frontend

| Technology | Role |
|---|---|
| **HTML5** | Structure |
| **CSS3** | Styling |
| **JavaScript (Vanilla)** | Interactivity |
| **Express.js** | Server & Routing |
| **Node.js 22** | JavaScript Runtime |

### Tools & Services

| Tool | Usage |
|---|---|
| **Docker** | Containerization |
| **Git** | Version Control |
| **Maven** | Automated Build & Test |
| **NPM** | Node Package Manager |

---

## Design

### UI/UX Prototypes

The page designs and user interface were created with **Figma**:
- [View the Project on Figma](https://www.figma.com/design/nO4maMQfMmHhVG4KkLaVPc/TinyFarm)

### Static Version

A static version of the project (prototypes) is available in the `screens/` folder.
This version was created before developing the interactive frontend.

---

## Additional Documentation

- **Backend**: `backend/tinyfarm/README.md`
- **Meetings**: `reunions/` (detailed meeting notes)
- **Screens**: `screens/` (UI prototypes)
- **Database**: `database/` (SQL schemas and functions)

---

## Testing

### Run Backend Tests

```bash
cd backend/tinyfarm

# All tests
mvn test 
```


## Getting the github stats

```bash
# generate an index.html in the package TinyFarm-G486/github-stats
 npm run github:stats
```

## Team

**Group 486** - University of Nantes  
Computer Science Bachelor's Degree - Web Development
