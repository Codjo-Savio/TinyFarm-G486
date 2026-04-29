import { fetchApiWithCredentials } from "/utils/fetch.js";

let chickens = [];
let currentUserId = null;

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
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

async function performChickenAction(chickenId, action) {
    const userId = await fetchCurrentUserId();
    const response = await fetchApiWithCredentials(
        `/chickens/${chickenId}/${action}?userId=${userId}`,
        "POST",
    );

    if (!response.ok) {
        throw new Error(`Action ${action} impossible (${response.status})`);
    }
}

function createChickenActionButton(action, chickenId, label, isEnabled) {
    return `<tf-button data-action="${action}" data-chicken-id="${chickenId}"${isEnabled ? "" : " disabled=true"}>${label}</tf-button>`;
}

function isChickenFed(chicken) {
    return Boolean(chicken?.fedToday);
}

function renderChickenCoop() {
    const container = document.getElementById("game-grid");
    container.innerHTML = "";

    let healthyCount = 0;
    let cleanCount = 0;
    let eggCount = 0;

    for (const chicken of chickens) {
        eggCount += Number(chicken.eggsLaid) || 0;

        const chickenTypeLabel =
            chicken.chickenType === "C"
                ? "Poussin"
                : chicken.chickenType === "H"
                  ? "Poule"
                  : chicken.chickenType === "R"
                    ? "Coq"
                    : chicken.chickenType === "L"
                      ? "Poule pondeuse"
                      : chicken.chickenType === "B"
                        ? "Coq reproducteur"
                        : "Inconnu";

        const chickenCardHTML = `<tf-card>
                <div class="grid-item">
                    <div class="animal-title">
                        ${chicken.name || "Poule sans nom"}
                        <div class="badges">
                            ${!chicken.healthy ? `<tf-pill icon="heart_broken">Malade</tf-pill>` : ""}
                            ${!chicken.clean ? `<tf-pill icon="mop">Sale</tf-pill>` : ""}
                        </div>
                    </div>
                    <div class="animal-content">
                        <div class="food-state">
                            <span class="material-symbols-rounded">wheat</span>
                            <tf-progress-bar progress="${isChickenFed(chicken) ? 100 : 0}"></tf-progress-bar>
                        </div>
                        <div class="animal-type">
                            <span class="material-symbols-rounded">info</span>
                            <div class="animal-type-text">
                                <p>${chickenTypeLabel}</p>
                            </div>
                        </div>
                        <div class="animal-weight">
                            <span class="material-symbols-rounded">weight</span>
                            <div class="animal-weight-text">
                                <p>${Number(chicken.weight || 0).toFixed(2)} kg</p>
                            </div>
                        </div>
                        <div class="animal-age">
                            <span class="material-symbols-rounded">calendar_today</span>
                            <div class="animal-age-text">
                                <p>${chicken.age || 0} jours</p>
                            </div>
                        </div>
                    </div>
                    <div class="animal-actions">
                        ${createChickenActionButton("feed", chicken.id, "$3 Nourrir", !isChickenFed(chicken))}
                        ${createChickenActionButton("water", chicken.id, "$1 Abreuver", !chicken.wateredToday)}
                        ${createChickenActionButton("heal", chicken.id, "$6 Soigner", !chicken.healthy)}
                        ${createChickenActionButton("clean", chicken.id, "$3 Nettoyer", !chicken.clean)}
                    </div>
                </div>
            </tf-card>`;

        container.insertAdjacentHTML("beforeend", chickenCardHTML);

        if (chicken.healthy) {
            healthyCount += 1;
        }
        if (chicken.clean) {
            cleanCount += 1;
        }
    }

    document.getElementById("chicken-count").textContent = String(
        chickens.length,
    );
    document.getElementById("egg-count").textContent = String(eggCount);
    document.getElementById("sick-count").textContent =
        (Math.round(
            ((chickens.length - healthyCount) / (chickens.length || 1)) * 100,
        ) || 0) + "%";
    document.getElementById("dirty-count").textContent =
        (Math.round(
            ((chickens.length - cleanCount) / (chickens.length || 1)) * 100,
        ) || 0) + "%";

    updateActionMenuCosts();
}

async function fetchChickenData() {
    const response = await fetchApiWithCredentials("/chickens");

    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }

    const payload = await response.json();

    if (!Array.isArray(payload)) {
        throw new Error("Format API invalide");
    }

    return payload;
}

async function initializeChickenCoop() {
    const container = document.getElementById("game-grid");

    try {
        await fetchCurrentUserId();
        chickens = await fetchChickenData();
        renderChickenCoop();
    } catch (error) {
        console.error("Impossible de charger le poulailler :", error);
        container.innerHTML = "<p>Erreur lors du chargement des poules.</p>";
    }
}

function updateActionMenuCosts() {
    const feedBtn = document.getElementById("feed-btn");
    const waterBtn = document.getElementById("water-btn");
    const healBtn = document.getElementById("heal-btn");
    const cleanBtn = document.getElementById("clean-btn");

    if (!feedBtn || !waterBtn || !healBtn || !cleanBtn || !chickens) {
        return;
    }

    const hungryChickens = chickens.filter(
        (chicken) => !isChickenFed(chicken),
    ).length;
    const thirstyChickens = chickens.filter(
        (chicken) => !chicken.wateredToday,
    ).length;
    const unhealthyChickens = chickens.filter(
        (chicken) => !chicken.healthy,
    ).length;
    const dirtyChickens = chickens.filter((chicken) => !chicken.clean).length;

    feedBtn.textContent = `$${hungryChickens * 3} Nourrir`;
    if (hungryChickens <= 0) feedBtn.setAttribute("disabled", "");

    waterBtn.textContent = `$${thirstyChickens * 1} Abreuver`;
    if (thirstyChickens <= 0) waterBtn.setAttribute("disabled", "");

    healBtn.textContent = `$${unhealthyChickens * 6} Soigner`;
    if (unhealthyChickens <= 0) healBtn.setAttribute("disabled", "");

    cleanBtn.textContent = `$${dirtyChickens * 3} Nettoyer`;
    if (dirtyChickens <= 0) cleanBtn.setAttribute("disabled", "");
}

async function applyActionToChicken(chickenId, action) {
    const chicken = chickens.find((item) => item.id === chickenId);
    if (!chicken) {
        return;
    }

    try {
        await performChickenAction(chickenId, action);
        if (action === "feed") {
            chicken.fedToday = true;
        } else if (action === "water") {
            chicken.wateredToday = true;
        } else if (action === "heal") {
            chicken.healthy = true;
        } else if (action === "clean") {
            chicken.clean = true;
        }
    } catch (error) {
        console.error(
            `Impossible d'appliquer l'action ${action} sur ${chickenId} :`,
            error,
        );
        throw error;
    }

    renderChickenCoop();
    window.dispatchEvent(new CustomEvent("refresh-user-data"));
}

async function onChickenActionClick(event) {
    const actionButton = event.target.closest(
        "tf-button[data-action][data-chicken-id]",
    );
    if (!actionButton) {
        return;
    }

    const chickenId = Number(actionButton.dataset.chickenId);
    const action = actionButton.dataset.action;

    if (!Number.isFinite(chickenId) || !action) {
        return;
    }

    await applyActionToChicken(chickenId, action);
}

async function feedAll() {
    const eligibleChickens = chickens.filter(
        (chicken) => !isChickenFed(chicken),
    );

    if (eligibleChickens.length === 0) {
        return;
    }

    for (const chicken of eligibleChickens) {
        await performChickenAction(chicken.id, "feed");
        chicken.fedToday = true;
    }

    renderChickenCoop();
    window.dispatchEvent(new CustomEvent("refresh-user-data"));
}

async function waterAll() {
    const eligibleChickens = chickens.filter(
        (chicken) => !chicken.wateredToday,
    );

    if (eligibleChickens.length === 0) {
        return;
    }

    for (const chicken of eligibleChickens) {
        await performChickenAction(chicken.id, "water");
        chicken.wateredToday = true;
    }

    renderChickenCoop();
    window.dispatchEvent(new CustomEvent("refresh-user-data"));
}

async function healAll() {
    const eligibleChickens = chickens.filter((chicken) => !chicken.healthy);

    if (eligibleChickens.length === 0) {
        return;
    }

    for (const chicken of eligibleChickens) {
        await performChickenAction(chicken.id, "heal");
        chicken.healthy = true;
    }

    renderChickenCoop();
    window.dispatchEvent(new CustomEvent("refresh-user-data"));
}

async function cleanAll() {
    const eligibleChickens = chickens.filter((chicken) => !chicken.clean);

    if (eligibleChickens.length === 0) {
        return;
    }

    for (const chicken of eligibleChickens) {
        await performChickenAction(chicken.id, "clean");
        chicken.clean = true;
    }

    renderChickenCoop();
    window.dispatchEvent(new CustomEvent("refresh-user-data"));
}

function setupActions() {
    document
        .querySelector("#game-grid")
        ?.addEventListener("click", onChickenActionClick);
    document.querySelector("#feed-btn")?.addEventListener("click", feedAll);
    document.querySelector("#water-btn")?.addEventListener("click", waterAll);
    document.querySelector("#heal-btn")?.addEventListener("click", healAll);
    document.querySelector("#clean-btn")?.addEventListener("click", cleanAll);
}

document.addEventListener("DOMContentLoaded", () => {
    initializeChickenCoop();
    setupActions();
});
