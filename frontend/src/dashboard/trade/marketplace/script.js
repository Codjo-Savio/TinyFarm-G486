import { API_URL, fetchApiWithCredentials } from "/utils/fetch.js";

const FAKE_MARKET_URL = "/fakeapi/trade/marketplace.json";
const snackbarElement = document.querySelector("tf-snackbar");
const appbarElement = document.querySelector("tf-app-bar");

async function fetchAllMarkets() {
    const response = await fetchApiWithCredentials("/market");
    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }
    const data = await response.json();
    return Array.isArray(data) ? data : [];
}

function normalizeMarkets(marketsRaw) {
    return (Array.isArray(marketsRaw) ? marketsRaw : []).map((market) => ({
        ...market,
        userId: Number(market.userId),
        productId: Number(market.productId),
        quantity: Number(market.quantity),
        // Le backend expose unitPrice; on garde aussi price pour le rendu actuel.
        price: Number(market.unitPrice ?? market.price ?? 0),
    }));
}

async function fetchProducts() {
    const response = await fetchApiWithCredentials("/products");
    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }
    const data = await response.json();
    return Array.isArray(data) ? data : [];
}

async function fetchFakeMarkets() {
    const response = await fetch(FAKE_MARKET_URL);
    if (!response.ok) {
        return [];
    }

    const data = await response.json();
    return Array.isArray(data) ? data : [];
}

function shouldUseFakePreview() {
    const params = new URLSearchParams(window.location.search);
    return params.get("fake") === "1" || params.get("mockData") === "1";
}

function getCurrentBuyerId() {
    return localStorage.getItem("tinyfarm-user-id");
}

async function fetchRemainingPurchases(userId) {
    const response = await fetchApiWithCredentials(
        `/users/remainingPurchases/id/${userId}`,
    );
    if (!response.ok) {
        return null;
    }
    const data = await response.json();
    return Number.isFinite(Number(data)) ? Number(data) : null;
}

async function fetchMarketByProductId(productId) {
    const response = await fetchApiWithCredentials(
        `/market/product/${productId}`,
    );
    if (!response.ok) {
        return null;
    }
    return response.json();
}

async function fetchMarketsFromProductEndpoints() {
    const products = await fetchProducts();
    if (products.length === 0) {
        return [];
    }

    const markets = await Promise.all(
        products.map(async (product) => {
            try {
                return await fetchMarketByProductId(product.id);
            } catch {
                return null;
            }
        }),
    );

    return normalizeMarkets(markets.filter(Boolean));
}

async function fetchMarketsGroupedByProduct(sourceMarkets) {
    const allMarkets = normalizeMarkets(
        Array.isArray(sourceMarkets) ? sourceMarkets : await fetchAllMarkets(),
    );

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
                m.price === Number(ref.unitPrice ?? ref.price ?? 0) &&
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

function setRemainingPurchases(remainingPurchases) {
    const purchaseInfo = document.querySelector(".purchase-info");
    if (!purchaseInfo) return;

    const value =
        remainingPurchases === null || remainingPurchases === undefined
            ? "-"
            : remainingPurchases;
    purchaseInfo.innerHTML = `
        <span class="material-symbols-rounded">shopping_cart</span>
        Achats restants : ${value}
    `;
}

function renderEmptyMarket(container) {
    container.innerHTML = `
        <div class="empty-market-state">
            <span class="material-symbols-rounded empty-market-icon">air</span>
            <h2>Aucun article disponible</h2>
            <p>Le marché est vide pour le moment. Revenez un peu plus tard.</p>
        </div>
    `;
}

async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        const buyerId = getCurrentBuyerId();

        if (buyerId) {
            const remainingPurchases = await fetchRemainingPurchases(buyerId);
            setRemainingPurchases(remainingPurchases);
        } else {
            setRemainingPurchases(null);
        }

        const forceFakePreview = shouldUseFakePreview();
        let sourceMarkets = [];

        if (forceFakePreview) {
            sourceMarkets = normalizeMarkets(await fetchFakeMarkets());
        } else {
            try {
                // 1) Endpoint idéal (non exposé sur tous les environnements)
                sourceMarkets = normalizeMarkets(await fetchAllMarkets());
            } catch {
                // 2) Fallback faisable avec les APIs actuelles
                sourceMarkets = await fetchMarketsFromProductEndpoints();
            }
        }

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

// Partie panier
let markets = [];
const panier = {};

function cartItemKey(productId, userId) {
    return `${productId}-${userId}`;
}

function getPanierTotalQuantity() {
    return Object.values(panier).reduce(
        (total, item) => total + item.quantity,
        0,
    );
}

function displayPanier() {
    // Logique pour afficher le contenu du panier
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

initialiserBoutique();
displayPanier();

function ajouterAuPanier(productId, userId) {
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

    displayPanier();
}

function retirerDuPanier(itemKey) {
    // Logique pour retirer le produit du panier
    if (panier[itemKey]) {
        panier[itemKey].quantity--;
        if (panier[itemKey].quantity <= 0) {
            delete panier[itemKey];
        }
    }
    displayPanier();
}

async function payerPanier() {
    // Récupérer l'ID de l'utilisateur courant (acheteur)
    const buyerId = getCurrentBuyerId();
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
        let successCount = 0;
        let failureCount = 0;

        // Une seule action métier par article du panier : /api/market/buy
        for (const [key, item] of Object.entries(panier)) {
            const requestBody = {
                buyerId: Number(buyerId),
                sellerId: item.userId,
                productId: item.productId,
                quantity: item.quantity,
            };

            try {
                const buyResponse = await fetch(
                    `${API_URL}/market/buy`,
                    {
                        method: "POST",
                        credentials: "include",
                        headers: {
                            "Content-Type": "application/json",
                        },
                        body: JSON.stringify(requestBody),
                    },
                );

                if (!buyResponse.ok) {
                    throw new Error(`Erreur buy: ${buyResponse.status}`);
                }

                successCount++;
            } catch (err) {
                console.error(
                    `Erreur achat produit ${item.productId}:`,
                    err,
                );
                snackbarElement.showSnackbar(
                    `Erreur lors de l'achat du produit ${item.productId}`,
                    false,
                );
                failureCount++;
                continue;
            }
        }

        // Afficher le résultat et vider le panier si tout est passé
        if (successCount > 0 && failureCount === 0) {
            snackbarElement.showSnackbar(
                `Paiement réussi ! ${successCount} achat(s) complété(s).`,
            );
            Object.keys(panier).forEach((key) => delete panier[key]);
            displayPanier();
            await initialiserBoutique(); // Actualiser la liste des produits
        } else if (successCount > 0) {
            snackbarElement.showSnackbar(
                `Paiement partiel : ${successCount} achat(s) validé(s), ${failureCount} en erreur.`,
                false,
            );
            await initialiserBoutique();
        } else {
            snackbarElement.showSnackbar("Aucun achat n'a pu être complété.", false);
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
