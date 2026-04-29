import { fetchApiWithCredentials } from "/utils/fetch.js";

const FAKE_MARKET_URL = "/fakeapi/trade/marketplace.json";
const snackbarElement = document.querySelector("tf-snackbar");
const appbarElement = document.querySelector("tf-app-bar");

// Recupere toutes les offres disponibles sur le marche.
async function fetchAllMarkets() {
    const response = await fetchApiWithCredentials("/market");
    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }
    const data = await response.json();
    return Array.isArray(data) ? data : [];
}

// Donnees de previsualisation, utiles uniquement avec ?fake=1 ou ?mockData=1.
async function fetchFakeMarkets() {
    const response = await fetch(FAKE_MARKET_URL);
    if (!response.ok) {
        return [];
    }

    const data = await response.json();
    return Array.isArray(data) ? data : [];
}

// Permet de tester la page sans backend quand on force le mode mock.
function shouldUseFakePreview() {
    const params = new URLSearchParams(window.location.search);
    return params.get("fake") === "1" || params.get("mockData") === "1";
}

// Recupere une offre de reference pour un produit donne.
async function fetchMarketByProductId(productId) {
    const response = await fetchApiWithCredentials(
        `/market/product/${productId}`,
    );
    if (!response.ok) {
        return null;
    }
    return response.json();
}

// Trie les offres pour garder les memes produits groupes dans l'affichage.
async function fetchMarketsGroupedByProduct(sourceMarkets) {
    const allMarkets = Array.isArray(sourceMarkets)
        ? sourceMarkets
        : await fetchAllMarkets();

    // Même produit à la suite
    const productIds = [...new Set(allMarkets.map((m) => m.productId))].sort(
        (a, b) => a - b,
    );

    // On utilise /product/{id} pour récupérer une offre de référence par produit.
    // Comme l'endpoint renvoie un seul Market, on conserve /api/market pour toutes les offres.
    const referenceByProduct = new Map();
    await Promise.all(
        productIds.map(async (productId) => {
            try {
                const ref = await fetchMarketByProductId(productId);
                if (ref) {
                    referenceByProduct.set(productId, ref);
                }
            } catch (erreur) {
                // fallback silencieux
            }
        }),
    );

    return productIds.flatMap((productId) => {
        const group = allMarkets
            .filter((m) => m.productId === productId)
            .sort((a, b) => a.userId - b.userId);

        // Si une référence est trouvée via /product/{id}, la mettre en tête
        const ref = referenceByProduct.get(productId);
        if (!ref) return group;

        const idx = group.findIndex(
            (m) =>
                m.userId === ref.userId &&
                m.productId === ref.productId &&
                m.price === ref.price &&
                m.quantity === ref.quantity,
        );

        if (idx > 0) {
            const [item] = group.splice(idx, 1);
            group.unshift(item);
        }

        return group;
    });
}

function setTotalStock(totalStock) {
    const stockInfo = document.querySelector(".stock-info");
    if (!stockInfo) return;

    stockInfo.innerHTML = `
        <span class="material-symbols-rounded">store</span>
        En stock : ${totalStock}
    `;
}

// Affiche un etat vide propre si aucune offre n'est disponible.
function renderEmptyMarket(container) {
    container.innerHTML = `
        <div class="empty-market-state">
            <span class="material-symbols-rounded empty-market-icon">air</span>
            <h2>Aucun article disponible</h2>
            <p>Le marché est vide pour le moment. Revenez un peu plus tard.</p>
        </div>
    `;
}

// Charge les offres, construit les lignes produit et met a jour le stock total.
async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        const forceFakePreview = shouldUseFakePreview();
        const sourceMarkets = forceFakePreview
            ? await fetchFakeMarkets()
            : await fetchAllMarkets();

        markets = await fetchMarketsGroupedByProduct(sourceMarkets);

        container.innerHTML = "";
        let totalStock = 0;

        if (markets.length === 0) {
            setTotalStock(0);
            renderEmptyMarket(container);
            return;
        }

        markets.forEach((market) => {
            const stock = Number(market.quantity);
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
                        <button class="btn-add" onclick="ajouterAuPanier(${market.productId}, ${market.userId})">Ajouter</button>
                    </div>
                </div>
            `;

            container.insertAdjacentHTML("beforeend", productHTML);

            totalStock += stock;
        });
        setTotalStock(totalStock);
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        setTotalStock(0);
        renderEmptyMarket(container);
    }
}

initialiserBoutique();

// Partie panier
let markets = [];
const panier = {};

// Une ligne de panier depend du produit et du vendeur.
function cartItemKey(productId, userId) {
    return `${productId}-${userId}`;
}

// Le panier est limite a 10 achats pour respecter la regle du marche.
function getPanierTotalQuantity() {
    return Object.values(panier).reduce(
        (total, item) => total + item.quantity,
        0,
    );
}

// Reconstruit le panier et son total apres chaque ajout/retrait.
function displayPanier() {
    let total = 0;

    for (const item of Object.values(panier)) {
        total += item.price * item.quantity;
    }

    document.getElementById("totalPrice").textContent = `$${total}`;

    document.querySelector(".cart-list").innerHTML = "";

    for (const [key, item] of Object.entries(panier)) {
        const productHTML = `
                            <div class="cart-item">
                                    <span>Produit ${item.productId} • ${item.seller}</span>
                                    <div class="qty-control">
                                        <button class="btn-qty" onclick="retirerDuPanier('${key}')">
                                            <span
                                                class="material-symbols-rounded"
                                            >
                                                remove_circle
                                            </span>
                                        </button>
                                        <span>${item.quantity}</span>
                                        <button class="btn-qty" onclick="ajouterAuPanier(${item.productId}, ${item.userId})">
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

function ajouterAuPanier(productId, userId) {
    // On recupere l'offre exacte pour utiliser le bon prix et le bon stock.
    const market = markets.find(
        (m) =>
            Number(m.productId) === Number(productId) &&
            Number(m.userId) === Number(userId),
    );

    if (!market) {
        return;
    }

    const itemKey = cartItemKey(productId, userId);
    const stock = Number(market.quantity);

    if (panier[itemKey] && panier[itemKey].quantity >= stock) {
        snackbarElement.showSnackbar(
            "Limite de stock atteinte pour ce produit.",
            false,
        );
        return;
    }

    if (getPanierTotalQuantity() >= 10) {
        snackbarElement.showSnackbar(
            "Limite de 10 achats dans le panier atteinte.",
            false,
        );
        return;
    }

    if (panier[itemKey]) {
        panier[itemKey].quantity++;
    } else {
        panier[itemKey] = {
            productId: Number(market.productId),
            userId: Number(market.userId),
            seller: `Utilisateur ${market.userId}`,
            price: Number(market.price) || 0,
            quantity: 1,
        };
    }

    console.log(panier);
    displayPanier();
}

function retirerDuPanier(itemKey) {
    // Si la quantite tombe a zero, on retire l'article du panier.
    if (panier[itemKey]) {
        panier[itemKey].quantity--;
        if (panier[itemKey].quantity <= 0) {
            delete panier[itemKey];
        }
    }
    console.log(panier);
    displayPanier();
}

// Cree les transactions, puis demande au backend d'executer les transferts.
async function payerPanier() {
    // Récupérer l'ID de l'utilisateur courant (acheteur)
    const buyerId = localStorage.getItem("tinyfarm-user-id");
    const payButton = document.querySelector(
        ".cart-actions tf-button[icon='payment']",
    );

    if (!buyerId) {
        snackbarElement.showSnackbar(
            "Utilisateur non identifié. Impossible de procéder.",
            false,
        );
        return;
    }

    if (Object.keys(panier).length === 0) {
        snackbarElement.showSnackbar("Votre panier est vide.", false);
        return;
    }

    // Active le mode loading du composant tf-button
    payButton?.setAttribute("loading", "");

    try {
        let totalTransactions = 0;
        let successCount = 0;
        const transactionIds = [];

        // 1. Créer une transaction pour chaque article du panier
        for (const [key, item] of Object.entries(panier)) {
            const transaction = {
                seller: item.userId,
                buyer: Number(buyerId),
                product: item.productId,
                quantity: item.quantity,
                totalPrice: item.price * item.quantity,
            };

            try {
                const createResponse = await fetchApiWithCredentials(
                    "/transaction",
                    "POST",
                    JSON.stringify(transaction),
                );

                if (!createResponse.ok) {
                    throw new Error(
                        `Erreur création transaction: ${createResponse.status}`,
                    );
                }

                const createdTransaction = await createResponse.json();
                transactionIds.push(createdTransaction.id);
                totalTransactions++;
            } catch (err) {
                console.error(
                    `Erreur transaction produit ${item.productId}:`,
                    err,
                );
                snackbarElement.showSnackbar(
                    `Erreur lors de la création de la transaction pour le produit ${item.productId}`,
                    false,
                );
                continue;
            }
        }

        // 2. Exécuter les transferts de stock (sell + buy)
        for (const tid of transactionIds) {
            try {
                // Appel sell: le vendeur perd le stock
                const sellResponse = await fetchApiWithCredentials(
                    `/stocks/sell/${tid}`,
                    "POST",
                );

                if (!sellResponse.ok) {
                    throw new Error(`Erreur sell: ${sellResponse.status}`);
                }

                // Appel buy: l'acheteur reçoit le stock
                const buyResponse = await fetchApiWithCredentials(
                    `/stocks/buy/${tid}`,
                    "POST",
                );

                if (!buyResponse.ok) {
                    throw new Error(`Erreur buy: ${buyResponse.status}`);
                }

                successCount++;
            } catch (err) {
                console.error(
                    `Erreur transfert stock pour transaction ${tid}:`,
                    err,
                );
                snackbarElement.showSnackbar(
                    `Erreur lors du transfert de stock pour la transaction ${tid}`,
                    false,
                );
                continue;
            }
        }

        // 3. Afficher le résultat et vider le panier si succès
        if (successCount === totalTransactions && totalTransactions > 0) {
            snackbarElement.showSnackbar(
                `Paiement réussi ! ${successCount} transaction(s) complétée(s).`,
            );
            Object.keys(panier).forEach((key) => delete panier[key]);
            displayPanier();
            await initialiserBoutique(); // Actualiser la liste des produits
        } else if (successCount > 0) {
            snackbarElement.showSnackbar(
                `Paiement partiel : ${successCount}/${totalTransactions} transaction(s) complétée(s).`,
                false,
            );
        } else {
            snackbarElement.showSnackbar(
                "Aucune transaction n'a pu être complétée.",
                false,
            );
        }
    } catch (err) {
        console.error("Erreur paiement:", err);
        snackbarElement.showSnackbar(
            "Erreur lors du paiement. Veuillez réessayer.",
            false,
        );
    } finally {
        // Met à jour les écus de l'appbar et désactive le mode loading du composant tf-button
        await appbarElement.update();
        payButton?.removeAttribute("loading");
    }
}

document.querySelector("#pay-btn").addEventListener("click", payerPanier);
