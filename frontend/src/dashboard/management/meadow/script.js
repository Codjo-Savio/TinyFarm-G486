import { fetchApiWithCredentials } from "/utils/fetch.js";

// Variables globales pour stocker les données des vaches et le lait
let cows = [];
let milk = 0;
const isFakeMode = new URLSearchParams(window.location.search).get("fake") === "1";

function createCowActionButton(action, cowId, label, isEnabled) {
    return `<tf-button data-action="${action}" data-cow-id="${cowId}"${isEnabled ? "" : " disabled=true"}>${label}</tf-button>`;
}

function renderMeadow() {
    const container = document.getElementById("game-grid");
    let healthyCount = 0;
    let cleanCount = 0;

    milk = cows.reduce((total, cow) => total + (Number(cow.milk) || 0), 0);
    container.innerHTML = "";

    for (const cow of cows) {
        let frenchCowType;
        switch (cow.cowType) {
            case "D":
                frenchCowType = "Vache laitière";
                break;
            case "C":
                frenchCowType = "Veau";
                break;
            default:
                frenchCowType = "Inconnu";
                break;
        }

        const animalCardHTML = `<tf-card>
                <div class="grid-item">
                    <div class="animal-title">
                        ${cow.name}
                        <div class="badges">
                            ${
                                !cow.healthy
                                    ? `<tf-pill icon="heart_broken">Malade</tf-pill>`
                                    : ""
                            }
                            ${
                                !cow.clean
                                    ? `<tf-pill icon="mop">Sale</tf-pill>`
                                    : ""
                            }
                        </div>
                    </div>
                        <div class="animal-content">
                            <div class="food-state">
                                <span class="material-symbols-rounded">
                                    wheat
                                </span>
                                <tf-progress-bar value="${cow.fedToday ? 100 : 0}"></tf-progress-bar>
                            </div>
                            <div class="animal-type">
                                <span class="material-symbols-rounded">
                                    info
                                </span>
                                <div class="animal-type-text">
                                    <p>${frenchCowType}</p>
                                </div>
                            </div>
                            <div class="animal-weight">
                                <span class="material-symbols-rounded">
                                    weight
                                </span>
                                <div class="animal-weight-text">
                                    <p>${cow.weight} kg</p>
                                </div>
                            </div>
                            <div class="animal-age">
                                <span class="material-symbols-rounded">
                                    calendar_today
                                </span>
                                <div class="animal-age-text">
                                    <p>${cow.age} ${cow.age != 1 ? "ans" : "an"}</p>
                                </div>
                            </div>
                            </div>
                        <div class="animal-actions">
                            ${createCowActionButton("feed", cow.id, "$5 Nourrir", !cow.fedToday)}
                            ${createCowActionButton("water", cow.id, "$2 Abreuver", !cow.wateredToday)}
                            ${createCowActionButton("heal", cow.id, "$6 Soigner", !cow.healthy)}
                            ${createCowActionButton("clean", cow.id, "$3 Nettoyer", !cow.clean)}
                        </div>
                    </div>
                </tf-card>`;
        container.insertAdjacentHTML("beforeend", animalCardHTML);

        if (cow.healthy) {
            healthyCount++;
        }
        if (cow.clean) {
            cleanCount++;
        }
    }

    document.getElementById("cow-count").textContent = cows.length;
    document.getElementById("milk-count").textContent = milk;
    document.getElementById("sick-count").textContent =
        (Math.round(((cows.length - healthyCount) / cows.length) * 100) || 0) +
        "%";
    document.getElementById("dirty-count").textContent =
        (Math.round(((cows.length - cleanCount) / cows.length) * 100) || 0) +
        "%";
    updateActionMenuCosts();
}

function normalizeCowsPayload(payload) {
    if (Array.isArray(payload)) {
        return payload;
    }

    if (payload && Array.isArray(payload.cows)) {
        return payload.cows;
    }

    throw new Error("Format API invalide: tableau de vaches attendu");
}

async function fetchMeadowData() {
    if (isFakeMode) {
        const response = await fetchApiWithCredentials("/fakeapi/meadow.json");
        if (!response.ok) {
            throw new Error(`Erreur HTTP (fake meadow) : ${response.status}`);
        }
        return normalizeCowsPayload(await response.json());
    }

    const response = await fetchApiWithCredentials("/cows");
    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }

    return normalizeCowsPayload(await response.json());
}

// Fonction d'initialisation du pré et de chargement des données des vaches depuis l'API
async function initializeMeadow() {
    const container = document.getElementById("game-grid");

    try {
        cows = await fetchMeadowData();

        console.log("Données des vaches reçues de l'API :", cows);
        renderMeadow();
    } catch (error) {
        console.error("Impossible de charger le pré :", error);
        container.innerHTML = "<p>Erreur lors du chargement des vaches.</p>";
    }
}

function updateActionMenuCosts() {
    const feedBtn = document.getElementById("feed-btn");
    const waterBtn = document.getElementById("water-btn");
    const healBtn = document.getElementById("heal-btn");
    const cleanBtn = document.getElementById("clean-btn");

    if (!feedBtn || !waterBtn || !healBtn || !cleanBtn || !cows) {
        return;
    }

    const unhealthyCows = cows.filter((cow) => !cow.healthy).length;
    const dirtyCows = cows.filter((cow) => !cow.clean).length;
    const hungryCows = cows.filter((cow) => !cow.fedToday).length;
    const thirstyCows = cows.filter((cow) => !cow.wateredToday).length;

    feedBtn.textContent = `$${hungryCows * 5} Nourrir`;
    waterBtn.textContent = `$${thirstyCows * 2} Abreuver`;
    healBtn.textContent = `$${unhealthyCows * 6} Soigner`;
    cleanBtn.textContent = `$${dirtyCows * 3} Nettoyer`;
}

async function callCowActionApi(cow, action) {
    if (isFakeMode) {
        return;
    }

    const endpointAction = action === "feed" ? "hay" : action;
    const userIdQuery = cow.userId ? `?userId=${encodeURIComponent(cow.userId)}` : "";
    const response = await fetchApiWithCredentials(
        `/cows/${cow.id}/${endpointAction}${userIdQuery}`,
        "POST",
    );

    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }
}

async function applyActionToCow(cowId, action) {
    const cow = cows.find((item) => item.id === cowId);
    if (!cow) {
        return;
    }

    let canApply = false;
    let applyLocalState;

    switch (action) {
        case "feed":
            canApply = !cow.fedToday;
            applyLocalState = () => {
                cow.fedToday = true;
            };
            break;
        case "water":
            canApply = !cow.wateredToday;
            applyLocalState = () => {
                cow.wateredToday = true;
            };
            break;
        case "heal":
            canApply = !cow.healthy;
            applyLocalState = () => {
                cow.healthy = true;
            };
            break;
        case "clean":
            canApply = !cow.clean;
            applyLocalState = () => {
                cow.clean = true;
            };
            break;
        default:
            return;
    }

    if (!canApply) {
        return;
    }

    try {
        if (isFakeMode) {
            applyLocalState();
        } else {
            await callCowActionApi(cow, action);
            applyLocalState();
        }
    } catch (error) {
        console.error(`Impossible d'appliquer l'action ${action} sur ${cow.id} :`, error);
    }

    renderMeadow();
}

async function onCowActionClick(event) {
    const actionButton = event.target.closest("tf-button[data-action][data-cow-id]");
    if (!actionButton) {
        return;
    }

    const cowId = Number(actionButton.dataset.cowId);
    const action = actionButton.dataset.action;

    if (!Number.isFinite(cowId) || !action) {
        return;
    }

    await applyActionToCow(cowId, action);
}

async function feedAll() {
    for (const cow of cows) {
        if (!cow.fedToday) {
            try {
                if (isFakeMode) {
                    cow.fedToday = true;
                } else {
                    await callCowActionApi(cow, "feed");
                    cow.fedToday = true;
                }
            } catch (error) {
                console.error(`Impossible de nourrir la vache ${cow.id} :`, error);
            }
        }
    }
    renderMeadow();
}

async function waterAll() {
    for (const cow of cows) {
        if (!cow.wateredToday) {
            try {
                if (isFakeMode) {
                    cow.wateredToday = true;
                } else {
                    await callCowActionApi(cow, "water");
                    cow.wateredToday = true;
                }
            } catch (error) {
                console.error(
                    `Impossible d'abreuver la vache ${cow.id} :`,
                    error,
                );
            }
        }
    }
    renderMeadow();
}

async function healAll() {
    for (const cow of cows) {
        if (!cow.healthy) {
            try {
                if (isFakeMode) {
                    cow.healthy = true;
                } else {
                    await callCowActionApi(cow, "heal");
                    cow.healthy = true;
                }
            } catch (error) {
                console.error(`Impossible de soigner la vache ${cow.id} :`, error);
            }
        }
    }
    renderMeadow();
}

async function cleanAll() {
    for (const cow of cows) {
        if (!cow.clean) {
            try {
                if (isFakeMode) {
                    cow.clean = true;
                } else {
                    await callCowActionApi(cow, "clean");
                    cow.clean = true;
                }
            } catch (error) {
                console.error(
                    `Impossible de nettoyer la vache ${cow.id} :`,
                    error,
                );
            }
        }
    }
    renderMeadow();
}

initializeMeadow();

document.querySelector("#game-grid").addEventListener("click", onCowActionClick);

document.querySelector("#feed-btn").addEventListener("click", feedAll);
document.querySelector("#water-btn").addEventListener("click", waterAll);
document.querySelector("#heal-btn").addEventListener("click", healAll);
document.querySelector("#clean-btn").addEventListener("click", cleanAll);
