# TinyFarm-G486

This is the repository of the group 486 for the TinyFarm game project.

## Quick start

### Requirements

- Docker

### Launch the project

**Create a Github OAuth App :**

1. Set the callback URL to `https://<your_domain>/api/auth/login/oauth2/code/github`
2. Create a new client secret
3. Copy your client ID and secret to use later in the `.env`

**Setup the compose stack and start TinyFarm :**

```
# Copy and fill the .env
cp .env.example .env
nano .env

# Start services
docker compose up -d
```

## Development

### Requirements

- JDK 21
- Maven
- Node.JS 22 & NPM

### Start the backend

```
cd backend/tinyfarm

# Run tests
mvn clean clean-install

mvn spring-boot:run
```

### Start the frontend

```
cd frontend

# Install packages
npm i

# Run tests
npm test

# Run
npm run start
# Or run with hot reload
npm run dev
```

Access the frontend at `http://localhost:3000`.

## About

## Suivi des mises a jour

Le suivi des evolutions projet est centralise ici:
- [MAJ_PROJET.md](./MAJ_PROJET.md)

### Technologies & frameworks

We use a Java backend with [Maven](https://maven.apache.org/) and [Spring Boot](https://spring.io/projects/spring-boot), with [PostgreSQL](https://www.postgresql.org/) as a database.  
The frontend is built using vanilla HTML + CSS + JS stack, and is served with [Express](https://expressjs.com/).

### Design

We created pages design and UI with [Figma](https://www.figma.com/design/nO4maMQfMmHhVG4KkLaVPc/TinyFarm?node-id=0-1&p=f&t=OhETpBXVWBqiZEUs-0).  
You can find a static version of the project in the `screens` folder, which we made before building the frontend.
