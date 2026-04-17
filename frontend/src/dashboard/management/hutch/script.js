const API_URL = window.apiUrl || "http://localhost:8080/api";

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

async function initializeHutch() {
    // Étape 1 : Récupérer le JWT du cookie
    const jwt = getCookie("jwt");
    if (!jwt) {
        console.error("JWT introuvable : pas connecté");
        return;
    }

    try {
        //  Récupérer les lapins depuis l'API
        const response = await fetch(`${API_URL}/rabbits`, {
            headers: new Headers({
                Authorization: "Bearer " + jwt,
            }),
        });

        if (!response.ok) {
            throw new Error(`Erreur API rabbits : ${response.status}`);
        }

        const rabbits = await response.json();
        console.log("Lapins reçus du backend :", rabbits);

        //  Afficher les lapins dans .grid-container
        const container = document.querySelector(".grid-container");
        if (!container) {
            console.error("Conteneur .grid-container introuvable en HTML");
            return;
        }

        // Remplacer le contenu HTML statique par les lapins réels
        container.innerHTML = rabbits
            .map((rabbit) => {
                return `
                    <div class="grid-item">
                        <div class="animal-title">
                            <h2>${rabbit.name}</h2>
                        </div>
                        <div class="animal-content">
                            <div class="food-state">
                                <span class="material-symbols-rounded">nutrition</span>
                                <div class="food-state-line-place-holder">
                                    <div class="food-state-line" style="width: 50%;"></div>
                                </div>
                            </div>
                            <div class="animal-type">
                                <span class="material-symbols-rounded">info</span>
                                <div class="animal-type-text">
                                    <p>${rabbit.rabbitType}</p>
                                </div>
                            </div>
                            <div class="animal-sex">
                                <span class="material-symbols-rounded">${rabbit.gender === "F" ? "female" : "male"}</span>
                                <div class="animal-sex-text">
                                    <p>${rabbit.gender === "F" ? "Femelle" : "Mâle"}</p>
                                </div>
                            </div>
                        </div>
                        <div class="animal-actions">
                            <button class="action-button" onclick="feedRabbit(${rabbit.id})">Nourrir</button>
                            <button class="action-button">Abreuver</button>
                            <button class="action-button">Soigner</button>
                            <button class="action-button">Nettoyer</button>
                        </div>
                    </div>
                `;
            })
            .join("");

        console.log("Lapins affichés avec succès");
    } catch (error) {
        console.error("Erreur lors du chargement du clapier :", error);
    }
}

// Appeler initializeHutch() dès que la page HTML est chargée
document.addEventListener("DOMContentLoaded", initializeHutch);
