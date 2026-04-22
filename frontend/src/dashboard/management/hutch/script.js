const API_URL = window.apiUrl || "http://localhost:8080/api";

let rabbits = [];

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

const MAX_RABBITS = 50;

async function initializeHutch() {
    const jwt = getCookie("jwt");
    if (!jwt) {
        console.error("JWT introuvable : pas connecté");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/rabbits`, {
            headers: { Authorization: "Bearer " + jwt },
        });

        if (!response.ok) throw new Error(`Erreur API : ${response.status}`);

        rabbits = await response.json();
        console.log("Lapins reçus :", rabbits);

        if (!Array.isArray(rabbits)) {
            throw new Error("Format API invalide");
        }

        // --- Calcul des stats ---
        const lapinCards = rabbits.filter((r) => r.rabbitType === "lapin");
        const lapereauCards = rabbits.filter((r) => r.rabbitType === "lapereau");
        const healthyCount = rabbits.filter((r) => r.healthy).length;
        const cleanCount = rabbits.filter((r) => r.clean).length;
        const sickPercent = Math.round(((rabbits.length - healthyCount) / rabbits.length) * 100) || 0;
        const dirtyPercent = Math.round(((rabbits.length - cleanCount) / rabbits.length) * 100) || 0;

        // --- Mise à jour des stats ---
        document.getElementById("sick-count").textContent = sickPercent + "%";
        document.getElementById("dirty-count").textContent = dirtyPercent + "%";
        document.getElementById("total-count").textContent = rabbits.length;

        // --- Cartes lapins ---
        const container = document.getElementById("game-grid");
        if (!container) return;

        const renderGroup = (group) => group.map((rabbit) => `
            <tf-card>
                <div class="animal-title">
                    ${rabbit.name}
                    <div class="badges">
                        ${!rabbit.healthy ? `<tf-pill icon="heart_broken">Malade</tf-pill>` : ""}
                        ${!rabbit.clean ? `<tf-pill icon="mop">Sale</tf-pill>` : ""}
                    </div>
                </div>
                <div class="animal-content">
                    <div class="food-state">
                        <span class="material-symbols-rounded">nutrition</span>
                        <tf-progress-bar progress="${rabbit.fedToday ? 100 : 20}"></tf-progress-bar>
                    </div>
                    <div>
                        <span class="material-symbols-rounded">info</span>
                        <p>${rabbit.rabbitType === "lapin" ? "Lapin" : "Lapereau"}</p>
                    </div>
                    <div>
                        <span class="material-symbols-rounded">${rabbit.gender === "F" ? "female" : "male"}</span>
                        <p>${rabbit.gender === "F" ? "Femelle" : "Mâle"}</p>
                    </div>
                </div>
                <div class="animal-actions">
                    ${!rabbit.fedToday ? `<tf-button onclick="feedRabbit(${rabbit.id})">Nourrir</tf-button>` : ""}
                    ${!rabbit.wateredToday ? `<tf-button onclick="waterRabbit(${rabbit.id})">Abreuver</tf-button>` : ""}
                    ${!rabbit.healthy ? `<tf-button onclick="healRabbit(${rabbit.id})">Soigner</tf-button>` : ""}
                    ${!rabbit.clean ? `<tf-button onclick="cleanRabbit(${rabbit.id})">Nettoyer</tf-button>` : ""}
                </div>
            </tf-card>
        `).join("");

        const lapinSick = Math.round((lapinCards.filter((r) => !r.healthy).length / lapinCards.length) * 100) || 0;
        const lapinDirty = Math.round((lapinCards.filter((r) => !r.clean).length / lapinCards.length) * 100) || 0;
        const lapereauSick = Math.round((lapereauCards.filter((r) => !r.healthy).length / lapereauCards.length) * 100) || 0;
        const lapereauDirty = Math.round((lapereauCards.filter((r) => !r.clean).length / lapereauCards.length) * 100) || 0;

        container.innerHTML = `
            ${lapereauCards.length > 0 ? `
                <div class="group-header">
                    <h2>Lapereaux</h2>
                    <tf-pill icon="cruelty_free">${lapereauCards.length}/${MAX_RABBITS}</tf-pill>
                    <tf-pill icon="heart_broken">Malade : ${lapereauSick}%</tf-pill>
                    <tf-pill icon="mop">Sale : ${lapereauDirty}%</tf-pill>
                </div>
                <div class="game-grid-group">
                    ${renderGroup(lapereauCards)}
                </div>
            ` : ""}
            ${lapinCards.length > 0 ? `
                <div class="group-header">
                    <h2>Lapins</h2>
                    <tf-pill icon="cruelty_free">${lapinCards.length}/${MAX_RABBITS}</tf-pill>
                    <tf-pill icon="heart_broken">Malade : ${lapinSick}%</tf-pill>
                    <tf-pill icon="mop">Sale : ${lapinDirty}%</tf-pill>
                </div>
                <div class="game-grid-group">
                    ${renderGroup(lapinCards)}
                </div>
            ` : ""}
        `;

        updateActionMenuCosts();
        console.log("Lapins affichés !");

    } catch (error) {
        console.error("Erreur :", error);
    }
}

function updateActionMenuCosts() {
    const feedBtn = document.getElementById("feed-btn");
    const waterBtn = document.getElementById("water-btn");
    const healBtn = document.getElementById("heal-btn");
    const cleanBtn = document.getElementById("clean-btn");

    if (!feedBtn || !waterBtn || !healBtn || !cleanBtn) return;

    const hungryCount = rabbits.filter(r => !r.fedToday).length;
    const thirstyCount = rabbits.filter(r => !r.wateredToday).length;
    const sickCount = rabbits.filter(r => !r.healthy).length;
    const dirtyCount = rabbits.filter(r => !r.clean).length;

    feedBtn.textContent = `Nourrir : ${hungryCount * 5} $`;
    waterBtn.textContent = `Abreuver : ${thirstyCount * 2} $`;
    healBtn.textContent = `Soigner : ${sickCount * 6} $`;
    cleanBtn.textContent = `Nettoyer : ${dirtyCount * 3} $`;
}

document.addEventListener("DOMContentLoaded", initializeHutch);

async function fetchCurrentUserId() {
    const jwt = getCookie("jwt");
    const response = await fetch(`${API_URL}/auth/me`, {
        headers: { Authorization: "Bearer " + jwt },
    });
    if (!response.ok) throw new Error(`Échec : ${response.status}`);
    const user = await response.json();
    return user.id;
}

async function feedRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");
        const response = await fetch(`${API_URL}/rabbits/${rabbitId}/feed?userId=${userId}`, {
            method: "POST",
            headers: { Authorization: "Bearer " + jwt },
        });
        if (!response.ok) {
            alert(response.status === 400 ? "Impossible de nourrir ce lapin (pas assez d'écus ?)." : `Erreur ${response.status}`);
            return;
        }
        await initializeHutch();
    } catch (error) {
        console.error("Erreur feedRabbit :", error);
    }
}

async function waterRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");
        const response = await fetch(`${API_URL}/rabbits/${rabbitId}/water?userId=${userId}`, {
            method: "POST",
            headers: { Authorization: "Bearer " + jwt },
        });
        if (!response.ok) {
            alert(response.status === 400 ? "Impossible d'abreuver ce lapin (pas assez d'écus ?)." : `Erreur ${response.status}`);
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
        const response = await fetch(`${API_URL}/rabbits/${rabbitId}/heal?userId=${userId}`, {
            method: "POST",
            headers: { Authorization: "Bearer " + jwt },
        });
        if (!response.ok) {
            alert(response.status === 400 ? "Impossible de soigner ce lapin (pas assez d'écus ?)." : `Erreur ${response.status}`);
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
        const response = await fetch(`${API_URL}/rabbits/${rabbitId}/clean?userId=${userId}`, {
            method: "POST",
            headers: { Authorization: "Bearer " + jwt },
        });
        if (!response.ok) {
            alert(response.status === 400 ? "Impossible de nettoyer ce lapin (pas assez d'écus ?)." : `Erreur ${response.status}`);
            return;
        }
        await initializeHutch();
    } catch (error) {
        console.error("Erreur cleanRabbit :", error);
    }
}

function toggleActionsMenu() {
    const menu = document.getElementById("actions-menu");
    if (!menu) return;
    menu.classList.toggle("open");
}

document.addEventListener("click", (event) => {
    const wrapper = document.querySelector(".actions-wrapper");
    const menu = document.getElementById("actions-menu");
    if (!wrapper || !menu) return;
    if (!wrapper.contains(event.target)) {
        menu.classList.remove("open");
    }
});

async function feedAll() {
    const jwt = getCookie("jwt");
    const userId = await fetchCurrentUserId();
    await Promise.all(
        rabbits.filter((r) => !r.fedToday).map((r) =>
            fetch(`${API_URL}/rabbits/${r.id}/feed?userId=${userId}`, {
                method: "POST",
                headers: { Authorization: "Bearer " + jwt },
            })
        )
    );
    await initializeHutch();
}

async function waterAll() {
    const jwt = getCookie("jwt");
    const userId = await fetchCurrentUserId();
    await Promise.all(
        rabbits.filter((r) => !r.wateredToday).map((r) =>
            fetch(`${API_URL}/rabbits/${r.id}/water?userId=${userId}`, {
                method: "POST",
                headers: { Authorization: "Bearer " + jwt },
            })
        )
    );
    await initializeHutch();
}

async function healAll() {
    const jwt = getCookie("jwt");
    const userId = await fetchCurrentUserId();
    await Promise.all(
        rabbits.filter((r) => !r.healthy).map((r) =>
            fetch(`${API_URL}/rabbits/${r.id}/heal?userId=${userId}`, {
                method: "POST",
                headers: { Authorization: "Bearer " + jwt },
            })
        )
    );
    await initializeHutch();
}

async function cleanAll() {
    const jwt = getCookie("jwt");
    const userId = await fetchCurrentUserId();
    await Promise.all(
        rabbits.filter((r) => !r.clean).map((r) =>
            fetch(`${API_URL}/rabbits/${r.id}/clean?userId=${userId}`, {
                method: "POST",
                headers: { Authorization: "Bearer " + jwt },
            })
        )
    );
    await initializeHutch();
}