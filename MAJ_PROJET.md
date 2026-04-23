# Suivi des MAJ du projet TinyFarm

Ce document centralise les mises a jour importantes du projet.

Objectif:
- garder une trace claire des changements techniques
- aider l'equipe a comprendre rapidement ce qui a bouge
- faciliter les retours en arriere et les revues

## Comment mettre a jour ce document

1. Ajouter une nouvelle entree en haut de la section `Historique des MAJ`.
2. Donner la date, la branche, les commits et un resume court.
3. Indiquer les impacts (`Frontend`, `Backend`, `Config locale`, `Tests`).
4. Ajouter une section `A faire ensuite` si necessaire.

## Historique des MAJ

### 2026-04-23 - Branch `front-chicken-coop`

Contexte:
- Deblocage des pages commerce pour avancer sur les retours de Savio.

Changements principaux:
- `trade/cooperative/script.js`:
- correction d'une erreur JS bloquante (`inventaire` declare 2 fois).
- `backend/.../MarketController.java`:
- ajout de l'endpoint `GET /api/market` pour recuperer la liste complete des offres.
- `trade/marketplace/script.js`:
- suppression du fake API local (`fakeapi/trade/marketplace.json`).
- chargement des offres reelles depuis `GET /api/market`.
- chargement des noms de produits via `GET /api/products`.
- recuperation de l'utilisateur connecte via `GET /api/auth/me`.
- panier dynamique (ajout/retrait/total) base sur les offres reelles.
- paiement branche sur backend:
- creation de transaction (`POST /api/transaction`)
- execution achat (`POST /api/stocks/buy/{tid}`)
- nettoyage de l'offre achetee (`DELETE /api/market/{userId}/{productId}`)

Tests executes:
- `node --check frontend/src/dashboard/trade/cooperative/script.js`: OK
- `node --check frontend/src/dashboard/trade/marketplace/script.js`: OK
- `npm test` (frontend): PASS
- `mvnw -DskipTests compile` (backend): OK

Etat obtenu:
- cooperative de nouveau executable (plus de crash JS immediat).
- marche branche aux endpoints backend principaux, sans dependance fake JSON.

### 2026-04-23 - Branch `front-chicken-coop`

Contexte:
- Amelioration fonctionnelle et UX du clapier avant prochaine PR.

Changements principaux:
- `hutch/script.js`:
- API base utilise maintenant `window.apiUrl` (plus de hardcode local).
- filtrage des lapins par `currentUserId` pour n'afficher que les donnees du joueur.
- correction de la structure de rendu des cards (plus de wrapper grille inutile par carte).
- ajout d'un feedback utilisateur (`hutch-status`) pour les actions et erreurs.
- bouton `Vendre` explicitement desactive tant que le flux n'est pas branche.
- `hutch/index.html`: ajout de la zone de statut (`#hutch-status`).
- `hutch/style.css`: styles associes au statut + ajustements visuels.

Tests executes:
- Frontend: `npm test` execute apres les changements, resultat `PASS` sur tous les scenarios.

Etat obtenu:
- clapier plus fidele, plus robuste et plus explicite pour l'utilisateur.
- base prete pour commit/push apres validation finale.

### 2026-04-22 - Branch `front-chicken-coop`

Contexte:
- Consolidation qualite avant push: verification backend et mise en place de tests frontend.

Changements principaux:
- Ajout d'une suite de tests frontend executable localement sans framework externe.
- Refactor de `frontend/server.js` pour permettre le demarrage du serveur en mode test (fonction exportee).
- Mise a jour du script `npm test` du frontend.

Tests executes:
- Backend: `mvn test` execute avec JDK 21, resultat `177 tests, 0 echec, 0 erreur`.
- Frontend: `npm test` execute, resultat `3 scenarios PASS`.
- Le 3e scenario frontend contient une passe data-driven de `18 checks` routes/assets + `1 check 404`.

Commit ajoute:
- `bcc26fa` - ajoute une suite de tests frontend pour les routes et assets

Etat obtenu:
- Base de tests frontend en place et stable.
- Verification backend complete sans regression detectee.

### 2026-04-21 - Branch `front-chicken-coop`

Contexte:
- Stabilisation de la branche frontend et finalisation de l'integration locale.

Changements principaux:
- Clapier (`hutch`) aligne sur le design attendu et les composants partages.
- Script du `hutch` branche sur l'API pour les actions de gestion.
- Script du `chicken-coop` corrige pour respecter le flux backend authentifie.
- Merge de `origin/main` dans `front-chicken-coop` avec resolution de conflits sur les pages de management.

Correctifs d'authentification locale (backend):
- Autorisation explicite des routes OAuth dans la securite Spring.
- Ajustement du callback OAuth gere cote backend pour correspondre au flux local utilise.
- Correction du handler de succes OAuth:
- cookie JWT compatible en local HTTP (`Secure` conditionne au protocole)
- construction d'URL de redirection fiabilisee

Config locale (non versionnee dans git):
- `backend/tinyfarm/src/main/resources/application.properties` complete pour les tests locaux:
- scope GitHub avec `user:email`
- secret JWT suffisamment long pour HS256
- URL frontend locale coherente

Commits cles sur la branche:
- `1d3f77d` - aligne la page clapier sur le design et les composants partages
- `0d9b5b5` - branche le clapier sur l api et ajoute les actions de gestion
- `682773a` - corrige les actions du poulailler avec le flux authentifie du backend
- `027e4ee` - fusionne origin/main dans front-chicken-coop et adapte les pages en conflit
- `88dcc21` - corrige le flux oauth local et le cookie jwt de connexion

Etat obtenu:
- login GitHub local fonctionnel
- acces au dashboard local valide apres authentification
- base de branche prete a etre poussee sur `origin/front-chicken-coop`

## Modele d'entree (a copier-coller)

### YYYY-MM-DD - Branch `nom-de-branche`

Contexte:
- ...

Changements principaux:
- ...
- ...

Impacts:
- Frontend: ...
- Backend: ...
- Config locale: ...
- Tests/Verification: ...

Commits:
- `hash` - message
- `hash` - message

A faire ensuite:
- ...
