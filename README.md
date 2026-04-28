# TinyFarm - Project G486

## About

**TinyFarm** is a website developed by students at the University of Nantes as part of the client-side web development course. This project enables students to apply the concepts they have studied throughout their Computer Science Bachelor's degree.

The project is based on the principles of the game [My e-Farm](https://api.myefarm.net/).

You can **try the game** with our [live demo](https://tinyfarm486.app).

## Quick Start

### Requirements

- **Docker** (for deployment)
- **JDK 21** (for backend development)
- **Maven 3.8+** (dependency manager)
- **Node.JS 22 or 24 & NPM** (for frontend)
- **OAuth app secrets**
  1. Go to [the dedicated settings page](https://github.com/settings/developers) and click `New OAuth App`
  2. Set the callback URL to `https://<your_domain>/api/auth/login/oauth2/code/github`
  3. Create a new client secret
  4. Keep your client ID and secret because we are going to use it later

### Development

#### Environnement setup

```bash
# Move to the resources directory
cd backend/tinyfarm/src/main/resources
# Copy the two files and rename it to suppress the .example
cp application-test.properties.example application-test.properties
cp application.properties.example application.properties

# Fill in the empty spaces by pasting the secrets that you got from Github
nano application.properties
nano application-test.properties
```

#### Launch the backend

```bash
cd backend/tinyfarm

# Install dependencies and compile
mvn clean install

# Start the application
mvn spring-boot:run
```

#### Launch the frontend

```bash
cd frontend

# Install packages
npm i

# Development mode with hot reload
npm run dev

# Or production mode
npm run start
```

### Production deployement

```bash
# Copy and fill the .env
cp .env.example .env
nano .env

# Start services
docker compose up -d --build

# Stop services
docker compose down
# Stop services and remove volumes (clear the database)
docker compose down -v
```

## Project Structure

```

TinyFarm-G486/
│
├── backend/tinyfarm/ # Backend code (Java/Spring Boot)
│ ├── src/main/java/
│ │ └── com/api/tinyfarm/
│ │ ├── controller/ # REST Endpoints
│ │ ├── model/ # JPA Classes/Entities
│ │ ├── repository/ # Database Queries
│ │ ├── service/ # Business Logic
│ │ ├── dto/ # Data Transfer Objects
│ │ └── security/ # OAuth Authentication
│ ├── src/main/resources/ # Configuration Files
│ ├── src/test/ # Unit & Integration Tests
│ └── pom.xml # Maven Configuration
│
├── frontend/ # Frontend code (HTML/CSS/JS + Express)
│ ├── public/ # Static Files
│ ├── src/ # JavaScript Source Code
│ └── package.json # Node Dependencies
│
├── database/ # SQL Schemas & Functions
├── reunions/ # Meeting Notes
├── screens/ # UI/UX Prototypes (Figma)
└── README.md # General Documentation
```

### Detailed Backend Structure

```

src/main/java/com/api/tinyfarm/
│
├── controller/ # REST Controllers
│ ├── StockController.java
│ ├── UserController.java
│ ├── TransactionController.java
│ └── ...
│
├── model/ # JPA Entities
│ ├── User.java
│ ├── Stock.java
│ ├── Transaction.java
│ ├── StockId.java (composite key)
│ └── ...
│
├── repository/ # JPA Interfaces
│ ├── UserRepository.java
│ ├── StockRepository.java
│ ├── TransactionRepository.java
│ └── ...
│
├── service/ # Business Logic
│ ├── StockService.java
│ ├── UserService.java
│ ├── TransactionService.java
│ └── ...
│
├── security/ # Authentication
│ ├── JwtRequestFilter.java
│ ├── SecurityConfig.java
│ └── ...
│
└── dto/ # Data Transfer Objects
```

### Detailed Frontend Structure

```

frontend/src/
│
├── components/
│ ├── tf-app-bar.js
│ ├── tf-button.js
│ ├── tf-dialog.js
│ └── ...
│
├── dashboard/
│ ├── management/
│ │ ├── chicken-coop/
│ │ ├── hutch/
│ │ └── meadow/
│ └── trade/
│   ├── marketplace/
│   └── cooperative/
│
├── assets/
├── utils/
├── fakeapi/
├── doc/
└── index.html
```

## Technologies & Frameworks

### Backend

| Technology        | Role                                 |
| ----------------- | ------------------------------------ |
| **Java**          | Main Language                        |
| **Spring Boot**   | Web Framework & Dependency Injection |
| **Maven**         | Dependency Manager                   |
| **JPA/Hibernate** | ORM - Object-Relational Mapping      |
| **PostgreSQL**    | Relational Database                  |
| **OAuth**         | GitHub Authentication                |
| **JWT**           | Authentication Tokens                |

### Frontend

| Technology               | Role               |
| ------------------------ | ------------------ |
| **HTML5**                | Structure          |
| **CSS3**                 | Styling            |
| **JavaScript (Vanilla)** | Interactivity      |
| **Express.js**           | Server & Routing   |
| **Node.js**              | JavaScript Runtime |

### Tools & Services

| Tool       | Usage                  |
| ---------- | ---------------------- |
| **Docker** | Containerization       |
| **Git**    | Version Control        |
| **Maven**  | Automated Build & Test |
| **NPM**    | Node Package Manager   |

---

## Design

### UI/UX Prototypes

The page designs and user interface were created with **Figma**. You can find the designs [here](https://www.figma.com/design/nO4maMQfMmHhVG4KkLaVPc/TinyFarm).

### Static Version

A static version of the project (prototypes) is available in the `screens/` folder.
This version was created before developing the interactive frontend.

---

## Additional Documentation

- **Backend**: `backend/tinyfarm/README.md`
- **Meetings**: `reunions/` (detailed meeting notes)
- **Database**: `database/` (SQL schemas and functions)

## Testing

### Run Backend Tests

```bash
cd backend/tinyfarm

# Run all tests
mvn test
```

## Getting the github stats

```bash
# Generate an index.html file in the github-stats folder
npm run github:stats
```

## Team

**Group 486** - University of Nantes  
Computer Science Bachelor's Degree - Web Development
