const API_URL = "http://localhost:8080/api";


// Variables globales pour stocker les données des vaches et le lait
let cows = [];
let milk = 0;
let healthyCount = 0;
let cleanCount = 0;
let milkFallback = 0;


// Fonction pour récupérer la valeur d'un cookie par son nom
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}


// Fonction d'initialisation du pré et de chargement des données des vaches depuis l'API
async function initializeMeadow() {
    const container = document.getElementById("game-grid");

    try {
        const jwt = getCookie("jwt");

        if (!jwt) throw new Error();

        const response = await fetch(`${API_URL}/cows`, {
            headers: new Headers({
                Authorization: "Bearer " + jwt,
            }),
        });
        // const response = await fetch("../../../fakeapi//meadow.json");

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
            // Cartes individuelles pour chaque vache avec leurs états et actions
            const animalCardHTML = `<div class="grid-container">
                    <div class="grid-item">
                        <div class="animal-title">
                            ${cow.name}
                            ${
                                !cow.healthy && !cow.clean
                                    ? `
                                <div class="animal-state">
                                    <span class="material-symbols-rounded">
                                        heart_broken
                                    </span>
                                    <p> Malade</p>
                                </div>
                                <div class="animal-state">
                                    <span class="material-symbols-rounded">
                                        mop
                                    </span>
                                    <p> Sale</p>
                                </div> `
                                    : ""
                            }${
                                !cow.healthy && cow.clean
                                    ? `
                                <div class="animal-state">
                                    <span class="material-symbols-rounded">
                                        heart_broken
                                    </span>
                                    <p> Malade</p>
                                </div>`
                                    : ""
                            }${
                                cow.healthy && !cow.clean
                                    ? `
                                <div class="animal-state">
                                    <span class="material-symbols-rounded">
                                        mop
                                    </span>
                                    <p> Sale</p>
                                </div>`
                                    : ""
                            }
                        </div>
                            <div class="animal-content">
                                <div class="food-state">
                                    <span class="material-symbols-rounded">
                                        nutrition
                                    </span>
                                    <div class="food-state-line-place-holder">
                                        <div class="food-state-line" style="width: ${cow.fedToday ? 100 : 0}%;"></div>
                                    </div>
                                </div>
                                <div class="animal-type">
                                    <span class="material-symbols-rounded">
                                        info
                                    </span>
                                    <div class="animal-type-text">
                                        <p>${cow.cowType === null ? "Inconnu" : cow.cowType}</p>
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
                                    <button class="action-button" active="${!cow.fedToday}">Nourrir : <span class="material-symbols-rounded coin-icon">paid</span> 5</button>
                                    <button class="action-button" active="${!cow.wateredToday}">Abreuver : <span class="material-symbols-rounded coin-icon">paid</span> 2</button>
                                <button class="action-button" active="${!cow.healthy}">Soigner : <span class="material-symbols-rounded coin-icon">paid</span> 6</button>
                                <button class="action-button" active="${!cow.clean}">Nettoyer : <span class="material-symbols-rounded coin-icon">paid</span> 3</button>
                            </div>
                        </div>
                    </div>`;
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
            (Math.round(
                ((cows.length - healthyCount) / cows.length) *
                    100,
            ) || 0) + "%";
        document.getElementById("dirty-count").textContent =
            (Math.round(
                ((cows.length - cleanCount) / cows.length) * 100,
            ) || 0) + "%";
        updateActionMenuCosts();
    } catch (error) {
        console.error("Impossible de charger le pré :", error);
        container.innerHTML = "<p>Erreur lors du chargement des vaches.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initializeMeadow);

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
            fetch(`${API_URL}/cows/${cow.id}/feed`, {
            headers: new Headers({
                Authorization: "Bearer " + jwt,
            }),
        });
            cow.fedToday = true;
        }
    }
    updateActionMenuCosts();

}

function waterAll() {
    for (const cow of cows) {
        if (!cow.wateredToday) {
            // Appel à l'API pour abreuver la vache 
            fetch(`${API_URL}/cows/${cow.id}/water`, {
            headers: new Headers({
                Authorization: "Bearer " + jwt,
            }),
        });
            cow.wateredToday = true;
        }
    }
    updateActionMenuCosts();
}

function healAll() {
    for (const cow of cows) {
        if (!cow.healthy) {
            // Appel à l'API pour soigner la vache 
            fetch(`${API_URL}/cows/${cow.id}/heal`, {
            headers: new Headers({
                Authorization: "Bearer " + jwt,
            }),
        });
            cow.healthy = true;
        }
    }
    updateActionMenuCosts();
}

function cleanAll() {
    for (const cow of cows) {
        if (!cow.clean) {
            // Appel à l'API pour nettoyer la vache 
            fetch(`${API_URL}/cows/${cow.id}/clean`, {
            headers: new Headers({
                Authorization: "Bearer " + jwt,
            }),
        });
            cow.clean = true;
        }
    }
    updateActionMenuCosts();
}
