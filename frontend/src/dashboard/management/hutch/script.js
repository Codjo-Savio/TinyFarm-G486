<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2d29b5230cb0bf0817169cd7585cd028c2929f7c
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
<<<<<<< HEAD
=======
import { fetchApiWithCredentials } from "/utils/fetch.js";

let rabbits = [];
let currentUserId = null;
const snackbar = document.getElementById("hutch-snackbar");
const appbarElement = document.querySelector("tf-app-bar");

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
        snackbar?.showSnackbar("Aucun lapin concerne par cette action.", true);
        return;
    }

    await Promise.all(
        targets.map((rabbit) => performRabbitAction(rabbit.id, action)),
    );

    await initializeHutch();
}

function renderRabbitCard(rabbit) {
    const typeLabel = rabbit.rabbitType === "lapereau" ? "Lapereau" : "Lapin";
    const genderLabel = rabbit.gender === "F" ? "Femelle" : "Male";

    const individualActions = [];

    if (!rabbit.fedToday) {
        individualActions.push(
            `<tf-button data-action="feed" data-id="${rabbit.id}">Nourrir</tf-button>`,
        );
    }

    if (!rabbit.wateredToday) {
        individualActions.push(
            `<tf-button data-action="water" data-id="${rabbit.id}">Abreuver</tf-button>`,
        );
    }

    if (!rabbit.healthy) {
        individualActions.push(
            `<tf-button data-action="heal" data-id="${rabbit.id}">Soigner</tf-button>`,
        );
    }

    if (!rabbit.clean) {
        individualActions.push(
            `<tf-button data-action="clean" data-id="${rabbit.id}">Nettoyer</tf-button>`,
        );
    }

    return `
        <tf-card>
            <div class="grid-item">
                <div class="animal-title">
                    ${rabbit.name}
                    <div class="badges">
                        ${
                            !rabbit.healthy
                                ? `<tf-pill icon="heart_broken">Malade</tf-pill>`
                                : ""
                        }
                        ${
                            !rabbit.clean
                                ? `<tf-pill icon="mop">Sale</tf-pill>`
                                : ""
                        }
                    </div>
                </div>

                <div class="animal-content">
                    <div class="food-state">
                        <span class="material-symbols-rounded">nutrition</span>
                        <tf-progress-bar value="${rabbit.fedToday ? 100 : 0}"></tf-progress-bar>
                    </div>

                    <div class="animal-type">
                        <span class="material-symbols-rounded">info</span>
                        <div class="animal-type-text">
                            <p>${typeLabel}</p>
                        </div>
                    </div>

                    <div class="animal-gender">
                        <span class="material-symbols-rounded">${
                            rabbit.gender === "F" ? "female" : "male"
                        }</span>
                        <div class="animal-gender-text">
                            <p>${genderLabel}</p>
                        </div>
                    </div>

                    <div class="animal-weight">
                        <span class="material-symbols-rounded">weight</span>
                        <div class="animal-weight-text">
                            <p>${rabbit.weight} kg</p>
                        </div>
                    </div>

                    <div class="animal-age">
                        <span class="material-symbols-rounded">calendar_today</span>
                        <div class="animal-age-text">
                            <p>${rabbit.age} jours</p>
                        </div>
                    </div>
                </div>

                <div class="animal-actions">
                    ${individualActions.join("")}
                </div>
            </div>
        </tf-card>
    `;
}

function getRabbitsForCurrentUser(allRabbits) {
    return allRabbits.filter((rabbit) => rabbit.userId === currentUserId);
}

function updateActionMenuCounts() {
>>>>>>> main
=======
>>>>>>> 2d29b5230cb0bf0817169cd7585cd028c2929f7c
    const feedBtn = document.getElementById("feed-btn");
    const waterBtn = document.getElementById("water-btn");
    const healBtn = document.getElementById("heal-btn");
    const cleanBtn = document.getElementById("clean-btn");

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 2d29b5230cb0bf0817169cd7585cd028c2929f7c
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
<<<<<<< HEAD
}
=======
    if (!feedBtn || !waterBtn || !healBtn || !cleanBtn) {
        return;
    }

    const hungryRabbits = rabbits.filter((rabbit) => !rabbit.fedToday).length;
    const thirstyRabbits = rabbits.filter(
        (rabbit) => !rabbit.wateredToday,
    ).length;
    const unhealthyRabbits = rabbits.filter((rabbit) => !rabbit.healthy).length;
    const dirtyRabbits = rabbits.filter((rabbit) => !rabbit.clean).length;

    feedBtn.textContent = `Nourrir (${hungryRabbits})`;
    waterBtn.textContent = `Abreuver (${thirstyRabbits})`;
    healBtn.textContent = `Soigner (${unhealthyRabbits})`;
    cleanBtn.textContent = `Nettoyer (${dirtyRabbits})`;
}

function bindIndividualActions() {
    const actionButtons = document.querySelectorAll(
        "tf-button[data-action][data-id]",
    );

    for (const button of actionButtons) {
        button.addEventListener("click", async () => {
            const action = button.dataset.action;
            const rabbitId = Number(button.dataset.id);

            if (!action || Number.isNaN(rabbitId)) {
                return;
            }

            try {
                await performRabbitAction(rabbitId, action);
                await appbarElement.update();
                await initializeHutch();
            } catch (error) {
                console.error(
                    `Impossible d'executer l'action ${action} :`,
                    error,
                );
                snackbar?.showSnackbar(`Action ${action} impossible.`, false);
            }
        });
    }
}

function renderRabbits(rabbitsToRender) {
    const container = document.getElementById("game-grid");
    container.innerHTML = "";

    if (!Array.isArray(rabbitsToRender) || rabbitsToRender.length === 0) {
        document.getElementById("rabbit-count").textContent = "0";
        document.getElementById("sick-count").textContent = "0%";
        document.getElementById("dirty-count").textContent = "0%";
        container.innerHTML =
            '<div class="empty-state"><p>Aucun lapin dans le clapier pour le moment.</p></div>';
        updateActionMenuCounts();
        return;
    }

    let healthyCount = 0;
    let cleanCount = 0;

    for (const rabbit of rabbitsToRender) {
        if (rabbit.healthy) {
            healthyCount += 1;
        }

        if (rabbit.clean) {
            cleanCount += 1;
        }

        container.insertAdjacentHTML("beforeend", renderRabbitCard(rabbit));
    }

    bindIndividualActions();

    document.getElementById("rabbit-count").textContent = String(
        rabbitsToRender.length,
    );
    document.getElementById("sick-count").textContent =
        (Math.round(
            ((rabbitsToRender.length - healthyCount) /
                (rabbitsToRender.length || 1)) *
                100,
        ) || 0) + "%";
    document.getElementById("dirty-count").textContent =
        (Math.round(
            ((rabbitsToRender.length - cleanCount) /
                (rabbitsToRender.length || 1)) *
                100,
        ) || 0) + "%";

    updateActionMenuCounts();
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
        snackbar?.showSnackbar("Impossible de charger le clapier.", false);
    }
}

async function feed(id) {
    try {
        await performRabbitAction(id, "feed");
        snackbar?.showSnackbar("Lapin nourri.", true);
        await appbarElement.update();
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de nourrir le lapin :", error);
        snackbar?.showSnackbar("Action nourrir impossible.", false);
    }
}

async function water(id) {
    try {
        await performRabbitAction(id, "water");
        snackbar?.showSnackbar("Lapin abreuve.", true);
        await appbarElement.update();
        await initializeHutch();
    } catch (error) {
        console.error("Impossible d'abreuver le lapin :", error);
        snackbar?.showSnackbar("Action abreuver impossible.", false);
    }
}

async function heal(id) {
    try {
        await performRabbitAction(id, "heal");
        snackbar?.showSnackbar("Lapin soigne.", true);
        await appbarElement.update();
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de soigner le lapin :", error);
        snackbar?.showSnackbar("Action soigner impossible.", false);
    }
}

async function clean(id) {
    try {
        await performRabbitAction(id, "clean");
        snackbar?.showSnackbar("Lapin nettoye.", true);
        await appbarElement.update();
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de nettoyer le lapin :", error);
        snackbar?.showSnackbar("Action nettoyer impossible.", false);
    }
}

async function feedAll() {
    try {
        await performBulkRabbitAction("feed", (rabbit) => !rabbit.fedToday);
        snackbar?.showSnackbar(
            "Tous les lapins concernes ont ete nourris.",
            true,
        );
        await appbarElement.update();
    } catch (error) {
        console.error("Impossible de nourrir tous les lapins :", error);
        snackbar?.showSnackbar("Action nourrir tout impossible.", false);
    }
}

async function waterAll() {
    try {
        await performBulkRabbitAction(
            "water",
            (rabbit) => !rabbit.wateredToday,
        );
        snackbar?.showSnackbar(
            "Tous les lapins concernes ont ete abreuves.",
            true,
        );
        await appbarElement.update();
    } catch (error) {
        console.error("Impossible d'abreuver tous les lapins :", error);
        snackbar?.showSnackbar("Action abreuver tout impossible.", false);
    }
}

async function healAll() {
    try {
        await performBulkRabbitAction("heal", (rabbit) => !rabbit.healthy);
        snackbar?.showSnackbar(
            "Tous les lapins concernes ont ete soignes.",
            true,
        );
        await appbarElement.update();
    } catch (error) {
        console.error("Impossible de soigner tous les lapins :", error);
        snackbar?.showSnackbar("Action soigner tout impossible.", false);
    }
}

async function cleanAll() {
    try {
        await performBulkRabbitAction("clean", (rabbit) => !rabbit.clean);
        snackbar?.showSnackbar(
            "Tous les lapins concernes ont ete nettoyes.",
            true,
        );
        await appbarElement.update();
    } catch (error) {
        console.error("Impossible de nettoyer tous les lapins :", error);
        snackbar?.showSnackbar("Action nettoyer tout impossible.", false);
    }
}

initializeHutch();

document.querySelector("#feed-btn")?.addEventListener("click", feedAll);
document.querySelector("#water-btn")?.addEventListener("click", waterAll);
document.querySelector("#heal-btn")?.addEventListener("click", healAll);
document.querySelector("#clean-btn")?.addEventListener("click", cleanAll);
>>>>>>> main
=======
}
>>>>>>> 2d29b5230cb0bf0817169cd7585cd028c2929f7c
