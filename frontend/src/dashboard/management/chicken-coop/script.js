const API_URL = "http://localhost:8080/api";

let chickens = [];
let eggCount = 0;
let currentUserId = null;

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

function getAuthHeaders(jwt) {
    return {
        Authorization: "Bearer " + jwt,
    };
}

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

        chickens = await response.json();

        if (!Array.isArray(chickens)) {
            throw new Error("Format API invalide");
        }

        eggCount = 0;
        container.innerHTML = "";

        for (const chicken of chickens) {
            container.insertAdjacentHTML("beforeend", renderChickenCard(chicken));
            eggCount += chicken.eggsLaid || 0;
        }

        document.getElementById("chicken-count").textContent = chickens.length;
        document.getElementById("egg-count").textContent = eggCount;
    } catch (error) {
        console.error("Erreur chargement poulailler :", error);
        container.innerHTML = "<p>Erreur lors du chargement.</p>";
    }
}

async function feed(id) {
    try {
        await performChickenAction(id, "feed");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible de nourrir la poule :", error);
    }
}

async function water(id) {
    try {
        await performChickenAction(id, "water");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible d'abreuver la poule :", error);
    }
}

async function heal(id) {
    try {
        await performChickenAction(id, "heal");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible de soigner la poule :", error);
    }
}

async function clean(id) {
    try {
        await performChickenAction(id, "clean");
        await initializeChickenCoop();
    } catch (error) {
        console.error("Impossible de nettoyer la poule :", error);
    }
}

document.addEventListener("DOMContentLoaded", initializeChickenCoop);
