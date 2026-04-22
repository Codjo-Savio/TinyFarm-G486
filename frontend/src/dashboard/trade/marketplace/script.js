async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        // const response = await fetch("/api/market");
        const response = await fetch("../../../fakeapi/trade/marketplace.json");

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        markets = await response.json();

        container.innerHTML = "";
        let totalStock = 0;

        markets.forEach(market => {
            const stock = 5; // Valeur arbitraire pour le stock
            const nom = `Produit ${market.productId}`; // À remplacer par vraie récupération du nom si possible
            const seller = `Utilisateur ${market.userId}`;
            const productHTML = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            <span class="material-symbols-rounded">
                                store
                            </span>
                            ${stock}
                        </span>
                        <span class="prod-name">${nom}</span><span class="seller-name">• ${seller}</span>
                    </div>
                    <div class="prod-action">
                        <span class="price">$${market.price}</span>
                        <button class="btn-add" onclick="ajouterAuPanier('${nom.replace(/'/g, "\\'")}')">Ajouter</button>
                    </div>
                </div>
            `;

            container.insertAdjacentHTML("beforeend", productHTML);

            totalStock += stock;
        });
        document.querySelector(".stock-info").innerHTML += totalStock;
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        container.innerHTML = "<p>Erreur lors du chargement des produits.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

// Partie panier
let markets = [];
const panier = {};

function displayPanier() {
    // Logique pour afficher le contenu du panier
    let total = 0;

    for (const [nom, quantite] of Object.entries(panier)) {
        const productId = nom.split(" ")[1]; // Extraire productId de "Produit X"
        const market = markets.find(m => m.productId == productId);
        if (market) {
            total += market.price * quantite;
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
    const stock = 5; // Valeur arbitraire
    if (panier[nomProduit] == stock) {
        alert("Limite de stock atteinte pour ce produit.");
        return;
    }
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
