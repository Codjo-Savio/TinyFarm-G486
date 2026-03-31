API_URL = "http://localhost:8080/api";

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

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

        const meadow = await response.json();

        container.innerHTML = "";
        let healthyCount = 0;
        let cleanCount = 0;

        for (const cow of meadow.cows) {
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
                                        <div class="food-state-line" style="width: ${cow.hunger * 100}%;"></div>
                                    </div>
                                </div>
                                <div class="animal-type">
                                    <span class="material-symbols-rounded">
                                        info
                                    </span>
                                    <div class="animal-type-text">
                                        <p>${cow.type}</p>
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
                                <button class="action-button" active="${cow.hunger === 0 ? "true" : "false"}">Nourrir : <span class="material-symbols-rounded coin-icon">paid</span> 5</button>
                                <button class="action-button" active="${cow.thirst === 0 ? "true" : "false"}">Abreuver : <span class="material-symbols-rounded coin-icon">paid</span> 2</button>
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
        document.getElementById("cow-count").textContent = meadow.cows.length;
        document.getElementById("milk-count").textContent = meadow.milk;
        document.getElementById("sick-count").textContent =
            (Math.round(
                ((meadow.cows.length - healthyCount) / meadow.cows.length) *
                    100,
            ) || 0) + "%";
        document.getElementById("dirty-count").textContent =
            (Math.round(
                ((meadow.cows.length - cleanCount) / meadow.cows.length) * 100,
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

let meadow = {};

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

    if (!feedBtn || !meadow.cows) return;

    const unhealthyCows = meadow.cows.filter((cow) => !cow.healthy).length;
    const dirtyCows = meadow.cows.filter((cow) => !cow.clean).length;
    const hungryCows = meadow.cows.filter((cow) => cow.hunger === 0).length;
    const thirstyCows = meadow.cows.filter((cow) => cow.thirst === 0).length;

    feedBtn.innerHTML = `Nourrir : <span class="material-symbols-rounded coin-icon">paid</span> ${hungryCows * 5}`;
    waterBtn.innerHTML = `Abreuver : <span class="material-symbols-rounded coin-icon">paid</span> ${thirstyCows * 2}`;
    healBtn.innerHTML = `Soigner : <span class="material-symbols-rounded coin-icon">paid</span> ${unhealthyCows * 6}`;
    cleanBtn.innerHTML = `Nettoyer : <span class="material-symbols-rounded coin-icon">paid</span> ${dirtyCows * 3}`;
}

function feedAll() {
    const cost = meadow.cows.length * 5;
    if (meadow.milk >= cost) {
        meadow.milk -= cost;
        for (const cow of meadow.cows) {
            cow.hunger = 1;
        }
        document.getElementById("milk-count").textContent = meadow.milk;
        updateActionMenuCosts();
        console.log(`Nourri toutes les vaches pour ${cost} lait`);
    } else {
        console.log("Lait insuffisant pour nourrir toutes les vaches");
    }
}

function waterAll() {
    const cost = meadow.cows.length * 2;
    if (meadow.milk >= cost) {
        meadow.milk -= cost;
        for (const cow of meadow.cows) {
            cow.thirst = 1;
        }
        document.getElementById("milk-count").textContent = meadow.milk;
        updateActionMenuCosts();
        console.log(`Abreuvé toutes les vaches pour ${cost} lait`);
    } else {
        console.log("Lait insuffisant pour abreuver toutes les vaches");
    }
}

function healAll() {
    const unhealthyCows = meadow.cows.filter((cow) => !cow.healthy).length;
    const cost = unhealthyCows * 6;
    if (meadow.milk >= cost) {
        meadow.milk -= cost;
        for (const cow of meadow.cows) {
            if (!cow.healthy) {
                cow.healthy = true;
            }
        }
        document.getElementById("milk-count").textContent = meadow.milk;
        updateActionMenuCosts();
        console.log(`Soigné ${unhealthyCows} vaches pour ${cost} lait`);
    } else {
        console.log("Lait insuffisant pour soigner toutes les vaches");
    }
}

function cleanAll() {
    const dirtyCows = meadow.cows.filter((cow) => !cow.clean).length;
    const cost = dirtyCows * 3;
    if (meadow.milk >= cost) {
        meadow.milk -= cost;
        for (const cow of meadow.cows) {
            if (!cow.clean) {
                cow.clean = true;
            }
        }
        document.getElementById("milk-count").textContent = meadow.milk;
        updateActionMenuCosts();
        console.log(`Nettoyé ${dirtyCows} vaches pour ${cost} lait`);
    } else {
        console.log("Lait insuffisant pour nettoyer toutes les vaches");
    }
}
