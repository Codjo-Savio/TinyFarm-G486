// 🔥 Données TEMPORAIRES (sert uniquement à tester sans backend)
// Chaque objet représente une poule avec ses infos
const chickens = [
    { name: "Jacqueline", type: "Pondeuse", weight: 1.5, food: 80 },
    { name: "Jeannette", type: "Pondeuse", weight: 1.4, food: 60 },
    { name: "Juliette", type: "Pondeuse", weight: 1.3, food: 30 },
];

// 🔧 Fonction qui crée UNE carte HTML pour une poule
function createChickenCard(chicken) {
    // Création du conteneur principal de la carte
    const div = document.createElement("div");

    // On applique la classe CSS (important pour le style)
    div.className = "grid-item";

    // On injecte tout le HTML de la carte
    div.innerHTML = `
        <!-- Nom de la poule -->
        <div class="animal-title">
            <h2>${chicken.name}</h2>
        </div>

        <!-- Contenu principal -->
        <div class="animal-content">

            <!-- Barre de nourriture -->
            <div class="food-state">
                <span class="material-symbols-rounded">nutrition</span>

                <!-- Barre grise -->
                <div class="food-state-line-place-holder">

                    <!-- Barre verte (largeur dynamique selon food %) -->
                    <div class="food-state-line" style="width: ${chicken.food}%"></div>
                </div>
            </div>

            <!-- Type de poule -->
            <div class="animal-type">
                <span class="material-symbols-rounded">info</span>
                <p>${chicken.type}</p>
            </div>

            <!-- Poids -->
            <div class="animal-weight">
                <span class="material-symbols-rounded">weight</span>
                <p>${chicken.weight} kg</p>
            </div>
        </div>

        <!-- Boutons d'action -->
        <div class="animal-actions">
            <button class="action-button">Nourrir</button>
            <button class="action-button">Abreuver</button>
            <button class="action-button">Soigner</button>
            <button class="action-button">Nettoyer</button>
        </div>
    `;

    // On retourne la carte prête à être ajoutée dans la page
    return div;
}

// 🚀 Code exécuté quand la page est chargée
document.addEventListener("DOMContentLoaded", () => {
    // On récupère le conteneur où on va mettre les cartes
    const container = document.querySelector(".grid-container");

    // Pour chaque poule dans le tableau
    chickens.forEach((chicken) => {
        // On crée une carte
        const card = createChickenCard(chicken);

        // On l'ajoute dans le DOM (affichage à l'écran)
        container.appendChild(card);
    });

    // 🔢 Mise à jour du compteur de poules en haut
    document.getElementById("chicken-count").textContent = chickens.length;

    // 🥚 Mise à jour du compteur d'œufs (exemple simple)
    document.getElementById("egg-count").textContent = chickens.length * 2;
});
