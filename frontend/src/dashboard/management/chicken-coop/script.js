const API_URL = window.apiUrl || "http://localhost:8080/api";

let chickens = [];
let eggCount = 0;
let currentUserId = null;
let statusTimeout = null;

// Lecture du cookie JWT pose par le backend apres la connexion GitHub.
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

function getJwtOrThrow() {
    const jwt = getCookie("jwt");

    if (!jwt) {
        throw new Error("JWT manquant");
    }

    return jwt;
}

// Centralise les headers pour tous les appels authentifies au backend.
function getAuthHeaders(jwt) {
    return {
        "Content-Type": "application/json",
        Authorization: "Bearer " + jwt,
    };
}

// Affiche un message court sur la page puis le masque automatiquement.
function setStatus(message, type = "info") {
    const el = document.getElementById("chicken-status");
    if (!el) {
        return;
    }

    el.textContent = message;
    el.className = `chicken-status ${type}`;

    if (statusTimeout) {
        clearTimeout(statusTimeout);
    }

    statusTimeout = setTimeout(() => {
        el.textContent = "";
        el.className = "chicken-status";
    }, 3000);
}

// L'id utilisateur est charge une seule fois, puis reutilise pour les actions.
async function fetchCurrentUserId(jwt) {
    if (currentUserId !== null) {
        return currentUserId;
    }

    const response = await fetch(`${API_URL}/auth/me`, {
        headers: getAuthHeaders(jwt),
    });

    if (!response.ok) {
        throw new Error("Impossible de recuperer l'utilisateur connecte");
    }

    const user = await response.json();
    currentUserId = user.id;

    return currentUserId;
}

// Envoie une action journaliere au backend (nourrir, abreuver, soigner, nettoyer).
async function performChickenAction(chickenId, action) {
    const jwt = getJwtOrThrow();
    const userId = await fetchCurrentUserId(jwt);
    const response = await fetch(
        `${API_URL}/chickens/${chickenId}/${action}?userId=${userId}`,
        {
            method: "POST",
            headers: getAuthHeaders(jwt),
        },
    );

    if (!response.ok) {
        throw new Error(`Action ${action} impossible (${response.status})`);
    }
}

// Construit le HTML d'une carte poule a partir des donnees API.
function renderChickenCard(chicken) {
    return `
        <div class="grid-item">
            <div class="animal-title">
                <h2>${chicken.name}</h2>
            </div>

            <div class="animal-content">
                <div>
                    <span class="material-symbols-rounded">egg</span>
                    <p>${chicken.eggsLaid || 0} oeufs</p>
                </div>

                <div>
                    <span class="material-symbols-rounded">calendar_today</span>
                    <p>${chicken.age} jours</p>
                </div>
            </div>

            <div class="animal-actions">
                <button onclick="feed(${chicken.id})" class="action-button" ${
                    chicken.fedToday ? "disabled" : ""
                }>
                    Nourrir
                </button>

                <button onclick="water(${chicken.id})" class="action-button" ${
                    chicken.wateredToday ? "disabled" : ""
                }>
                    Abreuver
                </button>

                <button onclick="heal(${chicken.id})" class="action-button" ${
                    chicken.healthy ? "disabled" : ""
                }>
                    Soigner
                </button>

                <button onclick="clean(${chicken.id})" class="action-button" ${
                    chicken.clean ? "disabled" : ""
                }>
                    Nettoyer
                </button>
            </div>
        </div>
    `;
}

// Le backend renvoie toutes les poules; le front garde seulement celles du joueur.
function getChickensForCurrentUser(allChickens) {
    return allChickens.filter((chicken) => chicken.userId === currentUserId);
}

// Charge les poules, met a jour les compteurs, puis rend la grille.
async function initializeChickenCoop() {
    const container = document.querySelector(".grid-container");

    try {
        const jwt = getJwtOrThrow();
        await fetchCurrentUserId(jwt);

        const response = await fetch(`${API_URL}/chickens`, {
            headers: getAuthHeaders(jwt),
        });

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        const allChickens = await response.json();

        if (!Array.isArray(allChickens)) {
            throw new Error("Format API invalide");
        }

        chickens = getChickensForCurrentUser(allChickens);
        eggCount = 0;
        container.innerHTML = "";

        if (chickens.length === 0) {
            document.getElementById("chicken-count").textContent = "0";
            document.getElementById("egg-count").textContent = "0";
            container.innerHTML =
                '<div class="empty-state"><p>Aucune poule dans le poulailler pour le moment.</p></div>';
            return;
        }

        for (const chicken of chickens) {
            container.insertAdjacentHTML("beforeend", renderChickenCard(chicken));
            eggCount += chicken.eggsLaid || 0;
        }

        document.getElementById("chicken-count").textContent = chickens.length;
        document.getElementById("egg-count").textContent = eggCount;
    } catch (error) {
        console.error("Erreur chargement poulailler :", error);
        container.innerHTML = "<p>Erreur lors du chargement.</p>";
        setStatus("Impossible de charger le poulailler.", "error");
    }
}

// Les fonctions suivantes sont exposees sur window car les boutons sont crees en HTML.
async function feed(id) {
    try {
        await performChickenAction(id, "feed");
        setStatus("Poule nourrie.", "success");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible de nourrir la poule :", error);
        setStatus("Action nourrir impossible.", "error");
    }
}

async function water(id) {
    try {
        await performChickenAction(id, "water");
        setStatus("Poule abreuvee.", "success");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible d'abreuver la poule :", error);
        setStatus("Action abreuver impossible.", "error");
    }
}

async function heal(id) {
    try {
        await performChickenAction(id, "heal");
        setStatus("Poule soignee.", "success");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible de soigner la poule :", error);
        setStatus("Action soigner impossible.", "error");
    }
}

async function clean(id) {
    try {
        await performChickenAction(id, "clean");
        setStatus("Poule nettoyee.", "success");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible de nettoyer la poule :", error);
        setStatus("Action nettoyer impossible.", "error");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializeChickenCoop();

    const sellButton = document.querySelector(".sell-button");
    if (sellButton) {
        sellButton.disabled = true;
        sellButton.title = "Vente a brancher avec le module marche";
    }
});

window.feed = feed;
window.water = water;
window.heal = heal;
window.clean = clean;
