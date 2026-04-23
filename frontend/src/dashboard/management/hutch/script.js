import { fetchApiWithCredentials } from "/utils/fetch.js";

const API_URL = window.apiUrl || "http://localhost:8080/api";

let rabbits = [];
let currentUserId = null;
let statusTimeout = null;

function setStatus(message, type = "info") {
    const el = document.getElementById("hutch-status");
    if (!el) {
        return;
    }

    el.textContent = message;
    el.className = `hutch-status ${type}`;

    if (statusTimeout) {
        clearTimeout(statusTimeout);
    }

    statusTimeout = setTimeout(() => {
        el.textContent = "";
        el.className = "hutch-status";
    }, 3000);
}

async function fetchCurrentUserId() {
    if (currentUserId !== null) {
        return currentUserId;
    }

    const response = await fetchApiWithCredentials("/auth/me");

    if (!response.ok) {
        throw new Error("Impossible de recuperer l'utilisateur connecte");
    }

    const user = await response.json();
    currentUserId = user.id;

    return currentUserId;
}

async function performRabbitAction(rabbitId, action) {
    const userId = await fetchCurrentUserId();
    const response = await fetchApiWithCredentials(
        `/rabbits/${rabbitId}/${action}?userId=${userId}`,
        "POST",
    );

    if (!response.ok) {
        throw new Error(`Action ${action} impossible (${response.status})`);
    }
}

async function performBulkRabbitAction(action, predicate) {
    const targets = rabbits.filter(predicate);

    if (targets.length === 0) {
        setStatus("Aucun lapin concerne par cette action.", "info");
        return;
    }

    for (const rabbit of targets) {
        await performRabbitAction(rabbit.id, action);
    }

    await initializeHutch();
}

function renderRabbitCard(rabbit) {
    const typeLabel = rabbit.rabbitType === "lapereau" ? "Lapereau" : "Lapin";
    const genderLabel = rabbit.gender === "F" ? "Femelle" : "Male";

    return `
        <div class="grid-item">
            <div class="animal-title">
                <h2>${rabbit.name}</h2>
                <div class="animal-state-bar">
                    ${
                        !rabbit.healthy
                            ? '<div class="animal-state"><span class="material-symbols-rounded">heart_broken</span><p>Malade</p></div>'
                            : ""
                    }
                    ${
                        !rabbit.clean
                            ? '<div class="animal-state"><span class="material-symbols-rounded">mop</span><p>Sale</p></div>'
                            : ""
                    }
                </div>
            </div>

            <div class="animal-content">
                <div class="food-state">
                    <span class="material-symbols-rounded">nutrition</span>
                    <div class="food-state-line-place-holder">
                        <div class="food-state-line" style="width: ${
                            rabbit.fedToday ? 100 : 20
                        }%;"></div>
                    </div>
                </div>

                <div>
                    <span class="material-symbols-rounded">info</span>
                    <p>${typeLabel}</p>
                </div>

                <div>
                    <span class="material-symbols-rounded">${
                        rabbit.gender === "F" ? "female" : "male"
                    }</span>
                    <p>${genderLabel}</p>
                </div>

                <div>
                    <span class="material-symbols-rounded">weight</span>
                    <p>${rabbit.weight} kg</p>
                </div>

                <div>
                    <span class="material-symbols-rounded">calendar_today</span>
                    <p>${rabbit.age} jours</p>
                </div>
            </div>

            <div class="animal-actions">
                <button onclick="feed(${rabbit.id})" ${
                    rabbit.fedToday ? "disabled" : ""
                }>Nourrir</button>
                <button onclick="water(${rabbit.id})" ${
                    rabbit.wateredToday ? "disabled" : ""
                }>Abreuver</button>
                <button onclick="heal(${rabbit.id})" ${
                    rabbit.healthy ? "disabled" : ""
                }>Soigner</button>
                <button onclick="clean(${rabbit.id})" ${
                    rabbit.clean ? "disabled" : ""
                }>Nettoyer</button>
            </div>
        </div>
    `;
}

function getRabbitsForCurrentUser(allRabbits) {
    return allRabbits.filter((rabbit) => rabbit.userId === currentUserId);
}

function renderRabbits(rabbitsToRender) {
    const container = document.getElementById("game-grid");
    container.innerHTML = "";

    if (!Array.isArray(rabbitsToRender) || rabbitsToRender.length === 0) {
        document.getElementById("rabbit-count").textContent = "0/50";
        document.getElementById("baby-rabbit-count").textContent = "0/50";
        container.innerHTML =
            '<div class="empty-state"><p>Aucun lapin dans le clapier pour le moment.</p></div>';
        return;
    }

    let babyRabbitCount = 0;

    for (const rabbit of rabbitsToRender) {
        if (rabbit.rabbitType === "lapereau") {
            babyRabbitCount += 1;
        }

        container.insertAdjacentHTML("beforeend", renderRabbitCard(rabbit));
    }

    document.getElementById("rabbit-count").textContent =
        `${rabbitsToRender.length}/50`;
    document.getElementById("baby-rabbit-count").textContent =
        `${babyRabbitCount}/50`;
}

async function initializeHutch() {
    const container = document.getElementById("game-grid");

    try {
        await fetchCurrentUserId();

        const response = await fetchApiWithCredentials("/rabbits");

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        const allRabbits = await response.json();
        rabbits = getRabbitsForCurrentUser(allRabbits);
        renderRabbits(rabbits);
    } catch (error) {
        console.error("Erreur lors du chargement du clapier :", error);
        container.innerHTML =
            '<div class="error-state"><p>Erreur lors du chargement du clapier.</p></div>';
        setStatus("Impossible de charger le clapier.", "error");
    }
}

async function feed(id) {
    try {
        await performRabbitAction(id, "feed");
        setStatus("Lapin nourri.", "success");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de nourrir le lapin :", error);
        setStatus("Action nourrir impossible.", "error");
    }
}

async function water(id) {
    try {
        await performRabbitAction(id, "water");
        setStatus("Lapin abreuve.", "success");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible d'abreuver le lapin :", error);
        setStatus("Action abreuver impossible.", "error");
    }
}

async function heal(id) {
    try {
        await performRabbitAction(id, "heal");
        setStatus("Lapin soigne.", "success");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de soigner le lapin :", error);
        setStatus("Action soigner impossible.", "error");
    }
}

async function clean(id) {
    try {
        await performRabbitAction(id, "clean");
        setStatus("Lapin nettoye.", "success");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de nettoyer le lapin :", error);
        setStatus("Action nettoyer impossible.", "error");
    }
}

function toggleActionsMenu() {
    const menu = document.getElementById("actions-menu");

    if (!menu) {
        return;
    }

    menu.classList.toggle("open");
}

async function feedAll() {
    try {
        await performBulkRabbitAction("feed", (rabbit) => !rabbit.fedToday);
        setStatus("Tous les lapins concernes ont ete nourris.", "success");
    } catch (error) {
        console.error("Impossible de nourrir tous les lapins :", error);
        setStatus("Action nourrir tout impossible.", "error");
    }
}

async function waterAll() {
    try {
        await performBulkRabbitAction(
            "water",
            (rabbit) => !rabbit.wateredToday,
        );
        setStatus("Tous les lapins concernes ont ete abreuves.", "success");
    } catch (error) {
        console.error("Impossible d'abreuver tous les lapins :", error);
        setStatus("Action abreuver tout impossible.", "error");
    }
}

async function healAll() {
    try {
        await performBulkRabbitAction("heal", (rabbit) => !rabbit.healthy);
        setStatus("Tous les lapins concernes ont ete soignes.", "success");
    } catch (error) {
        console.error("Impossible de soigner tous les lapins :", error);
        setStatus("Action soigner tout impossible.", "error");
    }
}

async function cleanAll() {
    try {
        await performBulkRabbitAction("clean", (rabbit) => !rabbit.clean);
        setStatus("Tous les lapins concernes ont ete nettoyes.", "success");
    } catch (error) {
        console.error("Impossible de nettoyer tous les lapins :", error);
        setStatus("Action nettoyer tout impossible.", "error");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializeHutch();

    const sellButton = document.querySelector(".sell-button");
    if (sellButton) {
        sellButton.disabled = true;
        sellButton.title = "Vente a brancher avec le module marche";
    }

    document.addEventListener("click", (event) => {
        const wrapper = document.querySelector(".actions-wrapper");
        const menu = document.getElementById("actions-menu");

        if (!wrapper || !menu || wrapper.contains(event.target)) {
            return;
        }

        menu.classList.remove("open");
    });
});

window.feed = feed;
window.water = water;
window.heal = heal;
window.clean = clean;
window.feedAll = feedAll;
window.waterAll = waterAll;
window.healAll = healAll;
window.cleanAll = cleanAll;
window.toggleActionsMenu = toggleActionsMenu;
