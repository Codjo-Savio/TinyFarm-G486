const API_URL = window.apiUrl || "http://localhost:8080/api";

let rabbits = [];


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
                            <button class="action-button" onclick="waterRabbit(${rabbit.id})">Abreuver</button>
                            <button class="action-button" onclick="healRabbit(${rabbit.id})">Soigner</button>
                            <button class="action-button" onclick="cleanRabbit(${rabbit.id})">Nettoyer</button>
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


// Récupère l'userId depuis le backend via le JWT
async function fetchCurrentUserId() {
    const response = await fetch(`${API_URL}/auth/me`, {
        credentials: "include",
    });

    if (!response.ok) {
        throw new Error(`Échec récupération utilisateur : ${response.status}`);
    }

    const user = await response.json();
    return user.id;
}

//Fonctions pour les actions sur les lapins (nourrir, soigner, nettoyer, abreuver)

//Nourrir un lapin par son ID
async function feedRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");

        const response = await fetch(
            `${API_URL}/rabbits/${rabbitId}/feed?userId=${userId}`,
            {
                method: "POST",
                headers: new Headers({ Authorization: "Bearer " + jwt }),
            }
        );

        if (!response.ok) {
            alert(response.status === 400
                ? "Impossible de nourrir ce lapin."
                : `Erreur serveur ${response.status}`);
            return;
        }

        const updatedRabbit = await response.json();
        console.log(" Lapin nourri :", updatedRabbit);
        await initializeHutch();

    } catch (error) {
        console.error("Erreur feedRabbit :", error);
        alert("Impossible de nourrir ce lapin. Veuillez réessayer.");
    }
}
    


//Soigner un lapin par son ID 
async function healRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");

        const response = await fetch(
            `${API_URL}/rabbits/${rabbitId}/heal?userId=${userId}`,
            {
                method: "POST",
                headers: new Headers({ Authorization: "Bearer " + jwt }),
            }
        );

        if (!response.ok) {
            alert(response.status === 400
                ? "Impossible de soigner ce lapin."
                : `Erreur serveur ${response.status}`);
            return;
        }

        const updatedRabbit = await response.json();
        console.log(" Lapin soigné :", updatedRabbit);
        await initializeHutch();

    } catch (error) {
        console.error("Erreur healRabbit :", error);
        alert("Impossible de soigner ce lapin. Veuillez réessayer.");
    }
}

//Nettoyer un lapin par son ID
async function cleanRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");

        const response = await fetch(
            `${API_URL}/rabbits/${rabbitId}/clean?userId=${userId}`,
            {
                method: "POST",
                headers: new Headers({ Authorization: "Bearer " + jwt }),
            }
        );

        if (!response.ok) {
            alert(response.status === 400
                ? "Impossible de nettoyer ce lapin."
                : `Erreur serveur ${response.status}`);
            return;
        }

        const updatedRabbit = await response.json();
        console.log("Lapin nettoyé :", updatedRabbit);
        await initializeHutch();

    } catch (error) {
        console.error("Erreur cleanRabbit :", error);
        alert("Impossible de nettoyer ce lapin. Veuillez réessayer.");
    }
}


//Abreuver un lapin par son ID et son nom

async function waterRabbit(rabbitId) {
    try {
        const userId = await fetchCurrentUserId();
        const jwt = getCookie("jwt");

        const response = await fetch(
            `${API_URL}/rabbits/${rabbitId}/water?userId=${userId}`,
            {
                method: "POST",
                headers: new Headers({ Authorization: "Bearer " + jwt }),
            }
        );

        if (!response.ok) {
            alert(response.status === 400
                ? "Impossible d'abreuver ce lapin."
                : `Erreur serveur ${response.status}`);
            return;
        }

        const updatedRabbit = await response.json();
        console.log(" Lapin abreuvé :", updatedRabbit);
        await initializeHutch();

    } catch (error) {
        console.error("Erreur waterRabbit :", error);
        alert("Impossible d'abreuver ce lapin. Veuillez réessayer.");
    }
}

