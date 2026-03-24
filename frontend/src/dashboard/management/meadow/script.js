async function initialiserPre() {
    const container = document.getElementById("game-grid");

    try {
        // const response = await fetch("/api/cows");
        const response = await fetch("../../../fakeapi//meadow.json");

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        pre = await response.json();

        container.innerHTML = "";

        for (const cow of pre.cows) {
            const productHTML = `<div class="grid-container">
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
                        </div>
                        <div class="animal-actions">
                            <button class="action-button" active="${cow.hunger === 0 ? "true" : "false"}">Nourir</button>
                            <button class="action-button" active="${cow.thirst === 0 ? "true" : "false"}">Abreuver</button>
                            <button class="action-button" active="${!cow.healthy}">Soigner</button>
                            <button class="action-button" active="${!cow.clean}">Nettoyer</button>
                        </div>
                    </div>`;
            container.insertAdjacentHTML("beforeend", productHTML);
        }
        document.getElementById("cow-count").textContent = pre.cows.length;
        document.getElementById("milk-count").textContent = pre.milk;
    } catch (erreur) {
        console.error("Impossible de charger le pré :", erreur);
        container.innerHTML = "<p>Erreur lors du chargement des vaches.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserPre);

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

let pre = {};

function actions() {
    const menu = document.getElementById("actions-menu");

    if (!menu) {
        return;
    }

    menu.classList.toggle("open");
}

function feedAll() {
    console.log("Nourrir toutes les vaches");
    // Implémenter la logique pour nourrir toutes les vaches
}

function waterAll() {
    console.log("Abreuver toutes les vaches");
    // Implémenter la logique pour abreuver toutes les vaches
}

function healAll() {
    console.log("Soigner toutes les vaches");
    // Implémenter la logique pour soigner toutes les vaches
}

function cleanAll() {
    console.log("Nettoyer toutes les vaches");
    // Implémenter la logique pour nettoyer toutes les vaches
}
