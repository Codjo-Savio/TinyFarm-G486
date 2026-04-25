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
                        <tf-progress-bar progress="${rabbit.fedToday ? 100 : 0}"></tf-progress-bar>
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
    const feedBtn = document.getElementById("feed-btn");
    const waterBtn = document.getElementById("water-btn");
    const healBtn = document.getElementById("heal-btn");
    const cleanBtn = document.getElementById("clean-btn");

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
