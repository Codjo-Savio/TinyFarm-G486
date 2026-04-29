import { fetchApiWithCredentials } from "/utils/fetch.js";

const snackbarElement = document.querySelector("tf-snackbar");
let rabbits = [];

async function initializeHutch() {
    const container = document.getElementById("game-grid");

    try {
        const response = await fetchApiWithCredentials("/rabbits");

        if (!response.ok) {
            throw new Error(`Erreur API : ${response.status}`);
        }

        rabbits = await response.json();

        if (!Array.isArray(rabbits)) {
            throw new Error("Format API invalide");
        }

        renderHutch();
    } catch (error) {
        console.error("Erreur :", error);
        container.innerHTML = "<p>Erreur lors du chargement des lapins.</p>";
    }
}

function renderRabbitCard(rabbit) {
    const typeLabel = rabbit.rabbitType === "lapereau" ? "Lapereau" : "Lapin";
    const genderLabel = rabbit.gender === "F" ? "Femelle" : "Male";

    return `
        <tf-card>
            <div class="grid-item">
                <div class="animal-title">
                    ${rabbit.name}
                    <div class="badges">
                        ${!rabbit.healthy ? `<tf-pill icon="heart_broken">Malade</tf-pill>` : ""}
                        ${!rabbit.clean ? `<tf-pill icon="mop">Sale</tf-pill>` : ""}
                    </div>
                </div>
                <div class="animal-content">
                    <div class="food-state">
                        <span class="material-symbols-rounded">wheat</span>
                        <tf-progress-bar progress="${rabbit.fedToday ? 100 : 0}"></tf-progress-bar>
                    </div>
                    <div class="animal-type">
                        <span class="material-symbols-rounded">info</span>
                        <div class="animal-type-text">
                            <p>${typeLabel}</p>
                        </div>
                    </div>
                    <div class="animal-gender">
                        <span class="material-symbols-rounded">${typeLabel !== "Lapereau" ? (rabbit.gender === "F" ? "female" : "male") : "question_mark"}</span>
                        <div class="animal-gender-text">
                            <p>${typeLabel !== "Lapereau" ? genderLabel : "Sexe inconnu"}</p>
                        </div>
                    </div>
                    <div class="animal-weight">
                        <span class="material-symbols-rounded">weight</span>
                        <div class="animal-weight-text">
                            <p>${rabbit.weight ?? 0} kg</p>
                        </div>
                    </div>
                    <div class="animal-age">
                        <span class="material-symbols-rounded">calendar_today</span>
                        <div class="animal-age-text">
                            <p>${rabbit.age ?? 0} jours</p>
                        </div>
                    </div>
                </div>
                <div class="animal-actions">
                    <tf-button data-action="feed" data-rabbit-id="${rabbit.id}" ${rabbit.fedToday ? "disabled=true" : ""}>$5 Nourrir</tf-button>
                    <tf-button data-action="water" data-rabbit-id="${rabbit.id}" ${rabbit.wateredToday ? "disabled=true" : ""}>$2 Abreuver</tf-button>
                    <tf-button data-action="heal" data-rabbit-id="${rabbit.id}" ${rabbit.healthy ? "disabled=true" : ""}>$6 Soigner</tf-button>
                    <tf-button data-action="clean" data-rabbit-id="${rabbit.id}" ${rabbit.clean ? "disabled=true" : ""}>$3 Nettoyer</tf-button>
                </div>
            </div>
        </tf-card>
    `;
}

function renderHutch() {
    const container = document.getElementById("game-grid");
    let healthyCount = 0;
    let cleanCount = 0;

    container.innerHTML = "";

    for (const rabbit of rabbits) {
        if (rabbit.healthy) {
            healthyCount++;
        }
        if (rabbit.clean) {
            cleanCount++;
        }

        container.insertAdjacentHTML("beforeend", renderRabbitCard(rabbit));
    }

    document.getElementById("rabbit-count").textContent = String(
        rabbits.length,
    );
    document.getElementById("sick-count").textContent =
        (Math.round(
            ((rabbits.length - healthyCount) / (rabbits.length || 1)) * 100,
        ) || 0) + "%";
    document.getElementById("dirty-count").textContent =
        (Math.round(
            ((rabbits.length - cleanCount) / (rabbits.length || 1)) * 100,
        ) || 0) + "%";

    updateActionMenuCosts();
}

function updateActionMenuCosts() {
    const feedBtn = document.getElementById("feed-btn");
    const waterBtn = document.getElementById("water-btn");
    const healBtn = document.getElementById("heal-btn");
    const cleanBtn = document.getElementById("clean-btn");

    if (!feedBtn || !waterBtn || !healBtn || !cleanBtn) return;

    const hungryCount = rabbits.filter((r) => !r.fedToday).length;
    const thirstyCount = rabbits.filter((r) => !r.wateredToday).length;
    const sickCount = rabbits.filter((r) => !r.healthy).length;
    const dirtyCount = rabbits.filter((r) => !r.clean).length;

    feedBtn.textContent = `$${hungryCount * 5} Nourrir`;
    if (hungryCount <= 0) feedBtn.setAttribute("disabled", "");

    waterBtn.textContent = `$${thirstyCount * 2} Abreuver`;
    if (thirstyCount <= 0) waterBtn.setAttribute("disabled", "");

    healBtn.textContent = `$${sickCount * 6} Soigner`;
    if (sickCount <= 0) healBtn.setAttribute("disabled", "");

    cleanBtn.textContent = `$${dirtyCount * 3} Nettoyer`;
    if (dirtyCount <= 0) cleanBtn.setAttribute("disabled", "");
}

async function performRabbitAction(rabbitId, action) {
    const userId = await fetchCurrentUserId();
    const response = await fetchApiWithCredentials(
        `/rabbits/${rabbitId}/${action}?userId=${userId}`,
        "POST",
    );

    if (!response.ok) {
        snackbarElement.showSnackbar(`Impossible d'appliquer l'action.`, false);
        throw new Error(`Action ${action} impossible (${response.status})`);
    }

    window.dispatchEvent(new CustomEvent("refresh-user-data"));
    snackbarElement.showSnackbar(`Action appliquée avec succès.`);
}

async function fetchCurrentUserId() {
    const response = await fetchApiWithCredentials("/auth/me");
    if (!response.ok) throw new Error(`Échec : ${response.status}`);
    const user = await response.json();
    return user.id;
}

async function feedRabbit(rabbitId) {
    try {
        await performRabbitAction(rabbitId, "feed");
        await initializeHutch();
    } catch (error) {
        console.error("Erreur feedRabbit :", error);
    }
}

async function waterRabbit(rabbitId) {
    try {
        await performRabbitAction(rabbitId, "water");
        await initializeHutch();
    } catch (error) {
        console.error("Erreur waterRabbit :", error);
    }
}

async function healRabbit(rabbitId) {
    try {
        await performRabbitAction(rabbitId, "heal");
        await initializeHutch();
    } catch (error) {
        console.error("Erreur healRabbit :", error);
    }
}

async function cleanRabbit(rabbitId) {
    try {
        await performRabbitAction(rabbitId, "clean");
        await initializeHutch();
    } catch (error) {
        console.error("Erreur cleanRabbit :", error);
    }
}

async function feedAll() {
    try {
        await Promise.all(
            rabbits
                .filter((rabbit) => !rabbit.fedToday)
                .map((rabbit) => performRabbitAction(rabbit.id, "feed")),
        );
        await initializeHutch();
    } catch (error) {
        console.error("Erreur feedAll :", error);
    }
}

async function waterAll() {
    try {
        await Promise.all(
            rabbits
                .filter((rabbit) => !rabbit.wateredToday)
                .map((rabbit) => performRabbitAction(rabbit.id, "water")),
        );
        await initializeHutch();
    } catch (error) {
        console.error("Erreur waterAll :", error);
    }
}

async function healAll() {
    try {
        await Promise.all(
            rabbits
                .filter((rabbit) => !rabbit.healthy)
                .map((rabbit) => performRabbitAction(rabbit.id, "heal")),
        );
        await initializeHutch();
    } catch (error) {
        console.error("Erreur healAll :", error);
    }
}

async function cleanAll() {
    try {
        await Promise.all(
            rabbits
                .filter((rabbit) => !rabbit.clean)
                .map((rabbit) => performRabbitAction(rabbit.id, "clean")),
        );
        await initializeHutch();
    } catch (error) {
        console.error("Erreur cleanAll :", error);
    }
}

async function onRabbitActionClick(event) {
    const actionButton = event.target.closest(
        "tf-button[data-action][data-rabbit-id]",
    );

    if (!actionButton) {
        return;
    }

    const rabbitId = Number(actionButton.dataset.rabbitId);
    const action = actionButton.dataset.action;

    if (!Number.isFinite(rabbitId) || !action) {
        return;
    }

    switch (action) {
        case "feed":
            await feedRabbit(rabbitId);
            break;
        case "water":
            await waterRabbit(rabbitId);
            break;
        case "heal":
            await healRabbit(rabbitId);
            break;
        case "clean":
            await cleanRabbit(rabbitId);
            break;
        default:
            break;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializeHutch();

    document
        .querySelector("#game-grid")
        ?.addEventListener("click", onRabbitActionClick);
    document.querySelector("#feed-btn")?.addEventListener("click", feedAll);
    document.querySelector("#water-btn")?.addEventListener("click", waterAll);
    document.querySelector("#heal-btn")?.addEventListener("click", healAll);
    document.querySelector("#clean-btn")?.addEventListener("click", cleanAll);
});
