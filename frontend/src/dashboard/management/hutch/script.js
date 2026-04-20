const API_URL = "http://localhost:8080/api";

let rabbits = [];
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

async function performRabbitAction(rabbitId, action) {
    const jwt = getJwtOrThrow();
    const userId = await fetchCurrentUserId(jwt);
    const response = await fetch(
        `${API_URL}/rabbits/${rabbitId}/${action}?userId=${userId}`,
        {
            method: "POST",
            headers: getAuthHeaders(jwt),
        },
    );

    if (!response.ok) {
        throw new Error(`Action ${action} impossible (${response.status})`);
    }
}

async function performBulkRabbitAction(action, predicate) {
    const targets = rabbits.filter(predicate);

    if (targets.length === 0) {
        return;
    }

    for (const rabbit of targets) {
        await performRabbitAction(rabbit.id, action);
    }

    await initializeHutch();
}

function renderRabbitCard(rabbit) {
    return `
        <div class="grid-container">
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
                        <p>${rabbit.rabbitType || "Inconnu"}</p>
                    </div>

                    <div>
                        <span class="material-symbols-rounded">${
                            rabbit.gender === "F" ? "female" : "male"
                        }</span>
                        <p>${rabbit.gender === "F" ? "Femelle" : "Male"}</p>
                    </div>

                    <div>
                        <span class="material-symbols-rounded">weight</span>
                        <p>${rabbit.weight} kg</p>
                    </div>

                    <div>
                        <span class="material-symbols-rounded">calendar_today</span>
                        <p>${rabbit.age} ans</p>
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
        </div>
    `;
}

async function initializeHutch() {
    const container = document.getElementById("game-grid");

    try {
        const jwt = getJwtOrThrow();
        await fetchCurrentUserId(jwt);

        const response = await fetch(`${API_URL}/rabbits`, {
            headers: getAuthHeaders(jwt),
        });

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        rabbits = await response.json();
        container.innerHTML = "";

        if (!Array.isArray(rabbits) || rabbits.length === 0) {
            document.getElementById("rabbit-count").textContent = "0/50";
            document.getElementById("baby-rabbit-count").textContent = "0/50";
            container.innerHTML =
                '<div class="empty-state"><p>Aucun lapin dans le clapier pour le moment.</p></div>';
            return;
        }

        let babyRabbitCount = 0;

        rabbits.forEach((rabbit) => {
            if (rabbit.rabbitType === "lapereau") {
                babyRabbitCount += 1;
            }

            container.insertAdjacentHTML("beforeend", renderRabbitCard(rabbit));
        });

        document.getElementById("rabbit-count").textContent =
            `${rabbits.length}/50`;
        document.getElementById("baby-rabbit-count").textContent =
            `${babyRabbitCount}/50`;
    } catch (error) {
        console.error("Erreur lors du chargement du clapier :", error);
        container.innerHTML =
            '<div class="error-state"><p>Erreur lors du chargement du clapier.</p></div>';
    }
}

async function feed(id) {
    try {
        await performRabbitAction(id, "feed");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de nourrir le lapin :", error);
    }
}

async function water(id) {
    try {
        await performRabbitAction(id, "water");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible d'abreuver le lapin :", error);
    }
}

async function heal(id) {
    try {
        await performRabbitAction(id, "heal");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de soigner le lapin :", error);
    }
}

async function clean(id) {
    try {
        await performRabbitAction(id, "clean");
        await initializeHutch();
    } catch (error) {
        console.error("Impossible de nettoyer le lapin :", error);
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
    } catch (error) {
        console.error("Impossible de nourrir tous les lapins :", error);
    }
}

async function waterAll() {
    try {
        await performBulkRabbitAction("water", (rabbit) => !rabbit.wateredToday);
    } catch (error) {
        console.error("Impossible d'abreuver tous les lapins :", error);
    }
}

async function healAll() {
    try {
        await performBulkRabbitAction("heal", (rabbit) => !rabbit.healthy);
    } catch (error) {
        console.error("Impossible de soigner tous les lapins :", error);
    }
}

async function cleanAll() {
    try {
        await performBulkRabbitAction("clean", (rabbit) => !rabbit.clean);
    } catch (error) {
        console.error("Impossible de nettoyer tous les lapins :", error);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initializeHutch();

    document.addEventListener("click", (event) => {
        const wrapper = document.querySelector(".actions-wrapper");
        const menu = document.getElementById("actions-menu");

        if (!wrapper || !menu || wrapper.contains(event.target)) {
            return;
        }

        menu.classList.remove("open");
    });
});
