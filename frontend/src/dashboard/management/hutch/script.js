const API_URL = window.apiUrl || "http://localhost:8080/api";

let rabbits = [];

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}
const MAX_RABBITS = 50;

async function initializeHutch() {
    const jwt = getCookie("jwt"); // ← manquait cette ligne !
    if (!jwt) {
        console.error("JWT introuvable : pas connecté");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/rabbits`, {
            headers: { Authorization: "Bearer " + jwt },
        });
       
        if (!response.ok) throw new Error(`Erreur API : ${response.status}`);

        const rabbits = await response.json();
        console.log("Lapins reçus :", rabbits);
        // verification de la structure des données
        if (!Array.isArray(rabbits)) {
            throw new Error("Format API invalide: tableau de lapins attendu");
        }
        // --- Compteurs dans taskbar-right ---
        const lapins = rabbits.filter((r) => r.rabbitType === "lapin");
        const lapereaux = rabbits.filter((r) => r.rabbitType === "lapereau");
        // document.getElementById("rabbit-count").textContent = lapins.length;
        // document.getElementById("baby-rabbit-count").textContent = lapereaux.length;
        const taskbarRight = document.querySelector(".taskbar-right");
        taskbarRight.innerHTML = `
            <div>
                <span class="material-symbols-rounded">cruelty_free</span>
                <p id="rabbit-count">${lapins.length}/${MAX_RABBITS}</p>
                <p>Lapins</p>
            </div>
            <div>
                <span class="material-symbols-rounded">cruelty_free</span>
                <p id="baby-rabbit-count">${lapereaux.length}/${MAX_RABBITS}</p>
                <p>Lapereaux</p>
            </div>
            <button class="sell-button">
                <span class="material-symbols-rounded">sell</span>Vendre
            </button>
            <div class="dropdown">
                <button id="more-actions-btn" class="more-actions-button">
                    <span class="material-symbols-rounded">more_horiz</span>
                </button>
                <div id="more-actions-content" class="dropdown-content">
                    <button id="feed-all-btn">Nourrir</button>
                    <button id="water-all-btn">Abreuver</button>
                    <button id="heal-all-btn">Soigner</button>
                    <button id="clean-all-btn">Nettoyer</button>
                </div>
            </div>
        `;

        //Affiche le menu déroulant
        initDropdown();
        const container = document.querySelector(".grid-container");
        if (!container) {
            return;
        }

        container.innerHTML = rabbits
            .map(
                (rabbit) => `
            <div class="grid-item">
                <div class="animal-title">
                    <h2>${rabbit.name}</h2>
                    <div class="animal-state-bar">
                        ${
                            !rabbit.healthy
                                ? `
                            <div class="animal-state">
                                <span class="material-symbols-rounded">heart_broken</span>
                                <p>Malade</p>
                            </div>`
                                : ""
                        }
                        ${
                            !rabbit.fedToday
                                ? `
                            <div class="animal-state">
                                <span class="material-symbols-rounded">no_meals</span>
                                <p>Affamé</p>
                            </div>`
                                : ""
                        }
                        ${
                            !rabbit.wateredToday
                                ? `
                            <div class="animal-state">
                                <span class="material-symbols-rounded">water_off</span>
                                <p>Assoiffé</p>
                            </div>`
                                : ""
                        }
                        ${
                            !rabbit.clean
                                ? `
                            <div class="animal-state">
                                <span class="material-symbols-rounded">cleaning_bucket</span>
                                <p>Sale</p>
                            </div>`
                                : ""
                        }
                    </div>
                </div>
                <div class="animal-content">
                    <div class="food-state">
                        <span class="material-symbols-rounded">nutrition</span>
                        <div class="food-state-line-place-holder">
                            <div class="food-state-line" style="width: ${rabbit.fedToday ? 100 : 20}%;"></div>
                        </div>
                    </div>
                    <div class="animal-type">
                        <span class="material-symbols-rounded">info</span>
                        <div class="animal-type-text">
                            <p>${rabbit.rabbitType === "lapin" ? "Lapin" : "Lapereau"}</p>
                        </div>
                    </div>
                    <div class="animal-sex">
                        <span class="material-symbols-rounded">
                            ${rabbit.gender === "F" ? "female" : "male"}
                        </span>
                        <div class="animal-sex-text">
                            <p>${rabbit.gender === "F" ? "Femelle" : "Mâle"}</p>
                        </div>
                    </div>
                </div>
                <div class="animal-actions">
                    <button class="action-button" onclick="feedRabbit(${rabbit.id})">Nourrir</button>
                    <button class="action-button" onclick="waterRabbit(${rabbit.id})">Abreuver</button>
                    <button class="action-button" onclick="healRabbit(${rabbit.id})">Soigner</button>
                    <button class="action-button" onclick="cleanRabbit(${rabbit.id})">Nettoyer</button>
                </div>
            </div>
        `,
            )
            .join("");

        console.log("Lapins affichés !");
    } catch (error) {
        console.error("Erreur lors de l'initialisation du clapier :", error);
    }
}

// Dropdown menu
function initDropdown() {
    const button = document.getElementById("more-actions-btn");
    const menu = document.getElementById("more-actions-content");

    button.addEventListener("click", (event) => {
        event.stopPropagation();
        menu.classList.toggle("grid");
    });

    menu.addEventListener("click", (event) => {
        event.stopPropagation();
    });

    document.addEventListener("click", () => {
        menu.classList.remove("grid");
    });
}

document.addEventListener("DOMContentLoaded", initializeHutch);



async function feedRabbit(rabbitId) {
    const userId = await fetchCurrentUserId();
    const jwt = getCookie("jwt");
    const response = await fetch(
        `${API_URL}/rabbits/${rabbitId}/feed?userId=${userId}`,
        {
            method: "POST",
            headers: { Authorization: "Bearer " + jwt },
        }
    );
    if (!response.ok) {
        alert(response.status === 400 
            ? "Impossible de nourrir ce lapin (pas assez d'écus ?)" 
            : `Erreur ${response.status}`);
        return;
    }
    await initializeHutch();
}

async function waterRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");
        const response = await fetch(
            `${API_URL}/rabbits/${rabbitId}/water?userId=${userId}`,
            {
                method: "POST",
                headers: { Authorization: "Bearer " + jwt },
            }
        );
        if (!response.ok) {
            alert(response.status === 400
                ? "Impossible d'abreuver ce lapin (pas assez d'écus ?)."
                : `Erreur ${response.status}`);
            return;
        }
        await initializeHutch();
    } catch (error) {
        console.error("Erreur waterRabbit :", error);
    }
}

async function healRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");
        const response = await fetch(
            `${API_URL}/rabbits/${rabbitId}/heal?userId=${userId}`,
            {
                method: "POST",
                headers: { Authorization: "Bearer " + jwt },
            }
        );
        if (!response.ok) {
            alert(response.status === 400
                ? "Impossible de soigner ce lapin (pas assez d'écus ?)."
                : `Erreur ${response.status}`);
            return;
        }
        await initializeHutch();
    } catch (error) {
        console.error("Erreur healRabbit :", error);
    }
}

async function cleanRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");
        const response = await fetch(
            `${API_URL}/rabbits/${rabbitId}/clean?userId=${userId}`,
            {
                method: "POST",
                headers: { Authorization: "Bearer " + jwt },
            }
        );
        if (!response.ok) {
            alert(response.status === 400
                ? "Impossible de nettoyer ce lapin (pas assez d'écus ?)."
                : `Erreur ${response.status}`);
            return;
        }
        await initializeHutch();
    } catch (error) {
        console.error("Erreur cleanRabbit :", error);
    }
}

async function fetchCurrentUserId() {
    const jwt = getCookie("jwt");
    const response = await fetch(`${API_URL}/auth/me`, {
        headers: { Authorization: "Bearer " + jwt },
    });
    if (!response.ok) throw new Error(`Échec : ${response.status}`);
    const user = await response.json();
    return user.id;
}