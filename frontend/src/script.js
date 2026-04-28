// Ce fichier contient le code JavaScript pour la page de classement des joueurs.

import { fetchApiWithCredentials, API_URL } from "/utils/fetch.js";

// usersData est un tableau qui stocke les données des utilisateurs récupérées depuis l'API ou le fichier de secours.
// c'est une variable globale.
// Chaque élément de usersData est un objet représentant un utilisateur, avec les propriétés suivantes :
// - rank: integer
// - name: string
// - production: number
// - capacity: number
// - ecus: number
let usersData = [];
const rankingTable = document.querySelector(".rankingTableContent");

/**
 * Met à jour l'état de chargement du tableau de classement
 * @param {boolean} isLoading
 */
function setLoadingState(isLoading) {
    if (!rankingTable) {
        return;
    }
    rankingTable.classList.toggle("is-loading", isLoading);
    rankingTable.setAttribute("aria-busy", String(isLoading));
}

/**
 * Affiche les utilisateurs dans le tableau de classement
 * @param {Array} users
 * users correspond aux 10 (ou moins) meilleurs utilisateurs à afficher, triés par rang décroissant.
 * users contient des objets avec les propriétés suivantes :
 * - rank: integer
 * - name: string
 * - production: number
 * - capacity: number
 * - ecus: number
 */
function displayUsers(users) {
    for (let i = 0; i < 10; i++) {
        const user = i < users.length ? users[i] : null;
        // Récupérer les cellules de la ligne i (0 à 4) sous la forme "i-col"
        const rowCells = [0, 1, 2, 3, 4].map((col) =>
            document.getElementById(i + "-" + col),
        );

        // Si aucun utilisateur n'existe pour cette ligne, afficher des cellules vides avec un style de placeholder
        if (!user) {
            rowCells.forEach((cell) => {
                cell.textContent = "";
                cell.classList.add("is-placeholder");
            });
            continue;
        }

        // Sinon, remplir les cellules avec les données de l'utilisateur et enlever le style de placeholder
        rowCells.forEach((cell) => cell.classList.remove("is-placeholder"));

        // Afficher le rang avec une icône de couronne pour les rangs 1 à 3, et une icône de premium pour les rangs 4 et plus
        if (user.rank < 4) {
            rowCells[0].innerHTML =
                user.rank +
                ' <span class="material-symbols-rounded">crown</span>';
        } else {
            rowCells[0].innerHTML =
                user.rang.current +
                ' <span class="material-symbols-rounded">workspace_premium</span>';
        }

        // Afficher les autres données de l'utilisateur
        rowCells[1].textContent = user.name;
        rowCells[2].textContent = user.production;
        rowCells[3].textContent = user.capacity;
        rowCells[4].textContent = user.ecus;
    }
}

/**
 * Trie le tableau de classement par une colonne spécifique
 * @param {string} column
 * @param {number} order
 * column correspond à la colonne à trier : "rank", "name", "production", "capacity" ou "ecus"
 * order correspond à l'ordre de tri : 1 pour croissant, -1 pour décroissant
 */
function sortTable(column, order) {
    // Mettre à jour l'icône du bouton de tri actif sans modifier le libellé du champ
    const activeButton = document.getElementById(column + "Button");
    const activeIcon = activeButton?.querySelector(".material-symbols-rounded");
    if (activeIcon) {
        activeIcon.textContent =
            order === -1 ? "arrow_upward_alt" : "arrow_downward_alt";
    }
    activeButton?.setAttribute("active", "true");

    // Réinitialiser les autres boutons de tri
    const columns = ["rank", "name", "production", "capacity", "ecus"];
    columns.forEach((col) => {
        if (col !== column) {
            const button = document.getElementById(col + "Button");
            const icon = button?.querySelector(".material-symbols-rounded");
            if (icon) {
                icon.textContent = "unfold_more";
            }
            button?.setAttribute("active", "none");
        }
    });

    // Si usersData n'est pas un tableau ou est vide, ne rien faire
    if (!Array.isArray(usersData) || usersData.length === 0) {
        return;
    }

    // Trier les données des utilisateurs en fonction de la colonne et de l'ordre spécifiés
    usersData.sort((a, b) => {
        // Pour la colonne "rank", on compare les propriétés "current" des objets "rank"
        // Pour les autres colonnes, on compare directement les propriétés correspondantes
        const valA = column === "rang" ? a.rang.current : a[column];
        const valB = column === "rang" ? b.rang.current : b[column];
        if (valA < valB) {
            // Pour le tri par rang, on veut que les rangs plus petits (meilleurs) soient en haut, donc on inverse l'ordre
            return order;
        }
        if (valA > valB) {
            // Pour le tri par rang, on veut que les rangs plus petits (meilleurs) soient en haut, donc on inverse l'ordre
            return -order;
        }
        return 0;
    });
    // Après le tri, mettre à jour l'affichage du tableau avec les données triées
    displayUsers(usersData);
}

function setRelease() {
    const releaseDate = new Date(window.release.date);
    const prettyReleaseDate =
        releaseDate.getDate().toString().padStart(2, "0") +
        "/" +
        (releaseDate.getMonth() + 1).toString().padStart(2, "0") +
        " " +
        releaseDate.getHours().toString().padStart(2, "0") +
        ":" +
        releaseDate.getMinutes().toString().padStart(2, "0");
    const releaseElmt = document.querySelector("#release>tf-pill");
    releaseElmt.textContent = `${window.release.name} • ${prettyReleaseDate}`;
    releaseElmt.parentElement.href = window.release.url;
}

setRelease();

// Au chargement de la page, récupérer les données des utilisateurs depuis l'API et les afficher dans le tableau de classement
setLoadingState(true);
fetchApiWithCredentials("/ranking")
    .then((response) => {
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        return response.json();
    })
    // Si la récupération des données depuis l'API réussit, stocker les données dans usersData et les afficher dans le tableau de classement
    .then((data) => {
        usersData = Array.isArray(data) ? data : [];
        displayUsers(usersData);
    })
    // Si la récupération des données depuis l'API échoue
    .catch((error) => {
        for (let i = 0; i < 10; i++) {
            // Récupérer les cellules de la ligne i (0 à 4) sous la forme "i-col"
            const rowCells = [0, 1, 2, 3, 4].map((col) =>
                document.getElementById(i + "-" + col),
            );

            // Afficher des cellules vides avec un style de placeholder
                rowCells.forEach((cell) => {
                    cell.textContent = "";
                    cell.classList.add("is-placeholder");
                });
        }
    })
    // Quel que soit le résultat de la récupération des données, désactiver l'état de chargement du tableau de classement
    .finally(() => {
        setLoadingState(false);
    });

// Section de code pour la connexion avec Github
async function auth() {
    document.querySelector("#github")?.setAttribute("loading", "");
    window.location.href = `${API_URL}/auth/login/oauth2/authorization/github`;
}

document.querySelector("#github").addEventListener("click", auth);
