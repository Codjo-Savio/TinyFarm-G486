const API_URL = "http://localhost:8080/api";
let inventaire = {};
const nomsProduits = {}; // Nouveau dictionnaire pour stocker les noms

async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        // 1. On récupère les détails des produits (pour avoir la description de chacun)
        const productsResponse = await fetch(`${API_URL}/products`);
        if (productsResponse.ok) {
            const productsList = await productsResponse.json();
            // On remplit le dictionnaire : id -> description (ex: 1 -> "Botte de foin")
            productsList.forEach(p => {
                nomsProduits[p.id] = p.description;
            });
        }

        // 2. On récupère l'inventaire actuel (prix)
        const response = await fetch(`${API_URL}/cooperative`);

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        inventaire = await response.json();

        container.innerHTML = "";

        for (const [idProduit, prix] of Object.entries(inventaire)) {
            // On récupère le vrai nom du produit grâce à l'ID, ou un nom par défaut si on ne le trouve pas
            const nomAffiche = nomsProduits[idProduit] || `${idProduit}`;
            
            const productHTML = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            <span class="material-symbols-rounded">
                                store
                            </span>
                        </span>
                        <span class="prod-name">${nomAffiche}</span>
                    </div>
                    <div class="prod-action">
                        <span class="price">$${prix.toFixed(2)}</span>
                        <button class="btn-add" onclick="ajouterAuPanier('${idProduit}')">Ajouter</button>
                    </div>
                </div>
            `;

            container.insertAdjacentHTML("beforeend", productHTML);
        }
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        container.innerHTML = "<p>Erreur lors du chargement des produits.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

// Partie panier
let inventaire = {};
const panier = {};

function displayPanier() {
    // Logique pour afficher le contenu du panier
    let total = 0;

    for (const [idProduit, quantite] of Object.entries(panier)) {
        // 'inventaire[idProduit]' contient maintenant directement le prix (Float)
        const prix = inventaire[idProduit];
        if (prix !== undefined) {
            total += prix * quantite;
        }
    }

    document.getElementById("totalPrice").textContent = `$${total.toFixed(2)}`;

    document.querySelector(".cart-list").innerHTML = "";

    for (const [idProduit, quantite] of Object.entries(panier)) {
        const nomAffiche = nomsProduits[idProduit] || `${idProduit}`;
        const productHTML = `
                            <div class="cart-item">
                                    <span>${nomAffiche}</span>
                                    <div class="qty-control">
                                        <button class="btn-qty" onclick="retirerDuPanier('${idProduit}')">
                                            <span
                                                class="material-symbols-rounded"
                                            >
                                                remove_circle
                                            </span>
                                        </button>
                                        <span>${quantite}</span>
                                        <button class="btn-qty" onclick="ajouterAuPanier('${idProduit}')">
                                            <span
                                                class="material-symbols-rounded"
                                            >
                                                add_circle
                                            </span>
                                        </button>
                                    </div>
                                </div>
                                `;
        document
            .querySelector(".cart-list")
            .insertAdjacentHTML("beforeend", productHTML);
    }
}

displayPanier();

function ajouterAuPanier(nomProduit) {
    // Logique pour ajouter le produit au panier
    if (panier[nomProduit]) {
        panier[nomProduit]++;
    } else {
        panier[nomProduit] = 1;
    }
    console.log(panier);
    displayPanier();
}

function retirerDuPanier(nomProduit) {
    // Logique pour retirer le produit du panier
    if (panier[nomProduit]) {
        panier[nomProduit]--;
        if (panier[nomProduit] <= 0) {
            delete panier[nomProduit];
        }
    }
    console.log(panier);
    displayPanier();
}
