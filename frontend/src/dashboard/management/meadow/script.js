import { fetchApiWithCredentials } from "/utils/fetch.js";

// Variables globales pour stocker les données des vaches et le lait
let cows = [];
let milk = 0;
let healthyCount = 0;
let cleanCount = 0;
let milkFallback = 0;

// Fonction d'initialisation du pré et de chargement des données des vaches depuis l'API
async function initializeMeadow() {
    const container = document.getElementById("game-grid");

    try {
        const response = await fetchApiWithCredentials("/cows");

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        cows = await response.json();

        // Vérification de la structure des données reçues
        if (!Array.isArray(cows)) {
            throw new Error("Format API invalide: tableau de vaches attendu");
        }

        // Initialisation du lait à partir de l'API
        const milkElement = document.getElementById("milk-count");
        milk = milkElement ? Number(milkElement.textContent) || 0 : 0;

        container.innerHTML = "";

        for (const cow of cows) {
            let frenchCowType;
            switch (cow.cowType) {
                case "D":
                    frenchCowType = "Vache laitière";
                    break;
                case "B":
                    frenchCowType = "Bœuf";
                    break;
                case "C":
                    frenchCowType = "Veau";
                    break;
                default:
                    frenchCowType = "Inconnu";
                    break;
            }

            // Cartes individuelles pour chaque vache avec leurs états et actions
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
                                        nutrition
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
                                        <p>${cow.age} ans</p>
                                    </div>
                                </div>
                                </div>
                            <div class="animal-actions">
                                ${!cow.fedToday ? "<tf-button>$5 Nourrir</tf-button>" : ""}
                                ${!cow.wateredToday ? "<tf-button>$2 Abreuver</tf-button>" : ""}
                                ${!cow.healthy ? "<tf-button>$6 Soigner</tf-button>" : ""}
                                ${!cow.clean ? "<tf-button>$3 Nettoyer</tf-button>" : ""}
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
        document.getElementById("milk-count").textContent = milkFallback;
        document.getElementById("sick-count").textContent =
            (Math.round(((cows.length - healthyCount) / cows.length) * 100) ||
                0) + "%";
        document.getElementById("dirty-count").textContent =
            (Math.round(((cows.length - cleanCount) / cows.length) * 100) ||
                0) + "%";
        updateActionMenuCosts();
    } catch (error) {
        console.error("Impossible de charger le pré :", error);
        container.innerHTML = "<p>Erreur lors du chargement des vaches.</p>";
    }
}

function toggleActionsMenu() {
    const menu = document.getElementById("actions-menu");

    if (!menu) {
        return;
    }

    menu.classList.toggle("open");
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

    feedBtn.innerHTML = `Nourrir : <span class="material-symbols-rounded coin-icon">paid</span> ${hungryCows * 5}`;
    waterBtn.innerHTML = `Abreuver : <span class="material-symbols-rounded coin-icon">paid</span> ${thirstyCows * 2}`;
    healBtn.innerHTML = `Soigner : <span class="material-symbols-rounded coin-icon">paid</span> ${unhealthyCows * 6}`;
    cleanBtn.innerHTML = `Nettoyer : <span class="material-symbols-rounded coin-icon">paid</span> ${dirtyCows * 3}`;
}

function feedAll() {
    for (const cow of cows) {
        if (!cow.fedToday) {
            // Appel à l'API pour nourrir la vache
            fetchApiWithCredentials(`/cows/${cow.id}/feed`);
            cow.fedToday = true;
        }
    }
    updateActionMenuCosts();
}

function waterAll() {
    for (const cow of cows) {
        if (!cow.wateredToday) {
            // Appel à l'API pour abreuver la vache
            fetchApiWithCredentials(`/cows/${cow.id}/water`);
            cow.wateredToday = true;
        }
    }
    updateActionMenuCosts();
}

function healAll() {
    for (const cow of cows) {
        if (!cow.healthy) {
            // Appel à l'API pour soigner la vache
            fetchApiWithCredentials(`/cows/${cow.id}/heal`);
            cow.healthy = true;
        }
    }
    updateActionMenuCosts();
}

function cleanAll() {
    for (const cow of cows) {
        if (!cow.clean) {
            // Appel à l'API pour nettoyer la vache
            fetchApiWithCredentials(`/cows/${cow.id}/clean`);
            cow.clean = true;
        }
    }
    updateActionMenuCosts();
}

initializeMeadow();

document.addEventListener("click", (event) => {
    const wrapper = document.querySelector(".actions-wrapper");
    const menu = document.getElementById("actions-menu");

    if (!wrapper || !menu) {
        return;
    }

    if (!wrapper.contains(event.target)) {
        menu.classList.remove("open");
    }
});

document
    .querySelector("button.menu-actions")
    .addEventListener("click", toggleActionsMenu);
