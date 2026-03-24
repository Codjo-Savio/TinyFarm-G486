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
                            <h2> ${cow.name} </h2>
                            <div class="animal-state-bar">
                                <div class="animal-state">
                                    <span class="material-symbols-rounded">
                                        format_paint
                                    </span>
                                    <p>${cow.healthy ? "Saine" : "Malade"}</p>
                                </div>
                            </div>
                        </div>
                        <div class="animal-content">
                            <div class="food-state">
                                <span class="material-symbols-rounded">
                                    nutrition
                                </span>
                                <div class="food-state-line-place-holder">
                                    <div class="food-state-line" style="width: 20%;"></div>
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
                            <button class="action-button">Nourir</button>
                            <button class="action-button">Abreuver</button>
                            <button class="action-button">Soigner</button>
                            <button class="action-button">Nettoyer</button>
                        </div>
                    </div>`;
            container.insertAdjacentHTML("beforeend", productHTML);
        }
    } catch (erreur) {
        console.error("Impossible de charger le pré :", erreur);
        container.innerHTML = "<p>Erreur lors du chargement des vaches.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserPre);

let pre = {};
