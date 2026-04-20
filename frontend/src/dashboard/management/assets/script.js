async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        const response = await fetch("../../../fakeapi/assets.json");

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        inventaire = await response.json();

        container.innerHTML = "";

        for (const [nom, details] of Object.entries(inventaire)) {
            const productHTML = `
                <div class="category-row">
                    <h2 class="category-title">${nom}</h2>
                    <div class="products-container">
                        `;

            container.insertAdjacentHTML("beforeend", productHTML);
            for (const [key, values] of Object.entries(details)) {
                const productHTML = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            x
                            ${values.quantity}
                        </span>
                        <span class="prod-name">${key}</span>
                    </div>
                    <div class="prod-action">
                        <button class="edit-price-btn" id="edit-${key.replace(/'/g, "\\'")}" onclick="modifierPrix('${key.replace(/'/g, "\\'")}')">
                            <span class="material-symbols-rounded">
                                edit
                            </span>
                        </button>
                        <span class="price" id="price-${key.replace(/'/g, "\\'")}">$${values.price}</span>
                        <button class="btn-add" onclick="ajouterAuPanier('${key.replace(/'/g, "\\'")}')">Ajouter</button>
                    </div>
                </div>
            `;
                container.insertAdjacentHTML("beforeend", productHTML);
            }
            const productHTML2 = `</div> </div>`;
            container.insertAdjacentHTML("beforeend", productHTML2);
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
        for (const category of Object.values(inventaire)) {
            if (category[nom]) {
                const produit = category[nom];
                total += produit.price * quantite;
                break; // Sortir de la boucle une fois le produit trouvé
            }
        }
    }

    // Arrondir à 2 décimales pour éviter les erreurs de précision des flottants
    const totalArrondi = Math.round(total * 100) / 100;
    document.getElementById("totalPrice").textContent = `$${totalArrondi.toFixed(2)}`;

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

function modifierPrix(nomProduit) {
    // Il faut enlever le boutons d'édition du prix le span du prix et le remplacer par un champ de saisie
    const priceSpan = document.getElementById(
        `price-${nomProduit.replace(/'/g, "\\'")}`,
    );
    const currentPrice = parseFloat(priceSpan.textContent.replace("$", ""));
    const inputHTML = `
        <input type="number" id="new-price-${nomProduit.replace(/'/g, "\\'")}" value="${currentPrice.toFixed(2)}" min="0" step="0.01">
    `;
    priceSpan.innerHTML = inputHTML;
    document.getElementById(
        "edit-" + nomProduit.replace(/'/g, "\\'"),
    ).innerHTML = `<span class="material-symbols-rounded">
                                check
                            </span>`; // Changer l'icône du bouton pour indiquer la confirmation
    document
        .getElementById("edit-" + nomProduit.replace(/'/g, "\\'"))
        .setAttribute(
            "onclick",
            `confirmerPrix('${nomProduit.replace(/'/g, "\\'")}')`,
        ); // Changer la fonction onclick pour confirmer le nouveau prix
}

function confirmerPrix(nomProduit) {
    const input = document.getElementById(
        `new-price-${nomProduit.replace(/'/g, "\\'")}`,
    );
    const nouveauPrix = input.value;
    if (nouveauPrix !== null) {
        for (const category of Object.values(inventaire)) {
            if (category[nomProduit]) {
                // Arrondir à 2 décimales pour éviter les erreurs de précision
                const prixArrondi = Math.round(parseFloat(nouveauPrix) * 100) / 100;
                category[nomProduit].price = prixArrondi;
                document.getElementById(
                    `price-${nomProduit.replace(/'/g, "\\'")}`,
                ).textContent = `$${prixArrondi.toFixed(2)}`;
                break; // Sortir de la boucle une fois le produit trouvé
            }
        }
    }
    document.getElementById(
        "edit-" + nomProduit.replace(/'/g, "\\'"),
    ).innerHTML = `<span class="material-symbols-rounded">
                                edit
                            </span>`; // Changer l'icône du bouton pour indiquer la confirmation
    document
        .getElementById("edit-" + nomProduit.replace(/'/g, "\\'"))
        .setAttribute(
            "onclick",
            `modifierPrix('${nomProduit.replace(/'/g, "\\'")}')`,
        ); // Changer la fonction onclick pour confirmer le nouveau prix
    displayPanier(); // Mettre à jour le panier pour refléter les changements de prix
}
