async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        const response = await fetch("../../../fakeapi/trade/cooperative.json");

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        inventaire = await response.json();

        container.innerHTML = "";

        for (const [nom, details] of Object.entries(inventaire)) {
            const productHTML = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            <span class="material-symbols-rounded">
                                store
                            </span>
                            ${details.stock}
                        </span>
                        <span class="prod-name">${nom}</span>
                    </div>
                    <div class="prod-action">
                        <span class="price">$${details.price}</span>
                        <button class="btn-add" onclick="ajouterAuPanier('${nom.replace(/'/g, "\\'")}')">Ajouter</button>
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

    for (const [nom, quantite] of Object.entries(panier)) {
        // Supposons qu'on ait un tableau des produits avec leurs prix
        // Cela devrait être remplacé par la vraie logique de récupération des prix
        const produit = inventaire[nom];
        if (produit) {
            total += produit.price * quantite;
        }
    }

    document.getElementById("totalPrice").textContent = `$${total}`;

    document.querySelector(".cart-list").innerHTML = "";

    for (const [nom, quantite] of Object.entries(panier)) {
        const productHTML = `
                            <div class="cart-item">
                                    <span>${nom}</span>
                                    <div class="qty-control">
                                        <button class="btn-qty" onclick="retirerDuPanier('${nom.replace(/'/g, "\\'")}')">
                                            <span
                                                class="material-symbols-rounded"
                                            >
                                                remove_circle
                                            </span>
                                        </button>
                                        <span>${quantite}</span>
                                        <button class="btn-qty" onclick="ajouterAuPanier('${nom.replace(/'/g, "\\'")}')">
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
