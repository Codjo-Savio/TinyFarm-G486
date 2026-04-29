const API_URL = "http://localhost:8080/api";
let inventaire = {};
const nomsProduits = {}; // Associe chaque id produit a son nom affichable.

// Charge les produits de la cooperative et prepare leur affichage.
async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        // 1. On récupère les détails des produits (pour avoir la description de chacun)
        const productsResponse = await fetch(`${API_URL}/products`);
        if (productsResponse.ok) {
            const productsList = await productsResponse.json();
            // Dictionnaire id -> description (ex: 1 -> "Botte de foin").
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
        let totalStock = 0;

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

            totalStock += details.stock;
        }
        document.querySelector(".stock-info").innerHTML += totalStock;
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        container.innerHTML = "<p>Erreur lors du chargement des produits.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

// Partie panier
const panier = {};

// Recalcule le total et reconstruit la liste visible du panier.
function displayPanier() {
    let total = 0;

    for (const [idProduit, quantite] of Object.entries(panier)) {
        // inventaire[idProduit] contient directement le prix moyen du produit.
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

// Ajoute un produit au panier en respectant la limite de stock disponible.
function ajouterAuPanier(nomProduit) {
    if (panier[nomProduit] == inventaire[nomProduit].stock) {
        alert("Limite de stock atteinte pour ce produit.");
        return;
    }
    if (panier[nomProduit]) {
        panier[nomProduit]++;
    } else {
        panier[nomProduit] = 1;
    }
    console.log(panier);
    displayPanier();
}

// Retire une quantite, puis supprime la ligne si elle tombe a zero.
function retirerDuPanier(nomProduit) {
    if (panier[nomProduit]) {
        panier[nomProduit]--;
        if (panier[nomProduit] <= 0) {
            delete panier[nomProduit];
        }
    }
    console.log(panier);
    displayPanier();
}
