import { API_URL, fetchApiWithCredentials } from "/utils/fetch.js";

// =========================
// Constantes et état global
// =========================

const snackbarElement = document.querySelector("tf-snackbar");
const appbarElement = document.querySelector("tf-app-bar");
let markets = [];
const panier = {};

// =========================
// Chargement des données
// =========================

async function getCurrentUserId() {
    const response = await fetchApiWithCredentials("/auth/me");

    if (!response.ok) {
        throw new Error("Impossible de récuperer l'utilisateur connecté");
    }

    return (await response.json()).id;
}

async function fetchAllMarkets() {
    const response = await fetchApiWithCredentials("/market/not/me");
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

async function getCurrentBuyerId() {
    const response = await fetchApiWithCredentials("/auth/me");

    if (!response.ok) {
        throw new Error("Impossible de recuperer l'utilisateur connecte");
    }

    const user = await response.json();
    return user.id;
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

    const productMarkets = await Promise.all(
        products.map(async (product) => {
            try {
                return await fetchMarketByProductId(product.id);
            } catch {
                return null;
            }
        }),
    );

    return normalizeMarkets(productMarkets.filter(Boolean));
}

async function fetchMarketsGroupedByProduct(sourceMarkets) {
    const allMarkets = normalizeMarkets(
        Array.isArray(sourceMarkets) ? sourceMarkets : await fetchAllMarkets(),
    );

    const marketProductIds = [
        ...new Set(allMarkets.map((market) => market.productId)),
    ].sort((a, b) => a - b);

    const referenceByProduct = new Map();
    await Promise.all(
        marketProductIds.map(async (productId) => {
            try {
                const referenceMarket = await fetchMarketByProductId(productId);
                if (referenceMarket) {
                    referenceByProduct.set(productId, referenceMarket);
                }
            } catch {
                // fallback silencieux
            }
        }),
    );

    return marketProductIds.flatMap((productId) => {
        const marketGroup = allMarkets
            .filter((market) => market.productId === productId)
            .sort((a, b) => a.userId - b.userId);

        const referenceMarket = referenceByProduct.get(productId);
        if (!referenceMarket) {
            return marketGroup;
        }

        const referenceIndex = marketGroup.findIndex(
            (market) =>
                market.userId === referenceMarket.userId &&
                market.productId === referenceMarket.productId &&
                market.price ===
                    Number(
                        referenceMarket.unitPrice ?? referenceMarket.price ?? 0,
                    ) &&
                market.quantity === referenceMarket.quantity,
        );

        if (referenceIndex > 0) {
            const [firstMarket] = marketGroup.splice(referenceIndex, 1);
            marketGroup.unshift(firstMarket);
        }

        return marketGroup;
    });
}

// =========================
// Affichage des compteurs
// =========================

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

// =========================
// Initialisation du marché
// =========================

async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        const buyerId = await getCurrentBuyerId();

        if (buyerId) {
            const remainingPurchases = await fetchRemainingPurchases(buyerId);
            setRemainingPurchases(remainingPurchases);
        } else {
            setRemainingPurchases(null);
        }

        let rawMarkets = [];

        try {
            rawMarkets = normalizeMarkets(await fetchAllMarkets());
        } catch {
            rawMarkets = await fetchMarketsFromProductEndpoints();
        }

        markets = await fetchMarketsGroupedByProduct(rawMarkets);
        container.innerHTML = "";

        let totalStock = 0;

        if (markets.length === 0) {
            setTotalStock(0);
            renderEmptyMarket(container);
            return;
        }

        markets.forEach((market) => {
            const stock = Number(market.quantity);
            const productLabel = `Produit ${market.productId}`;
            const sellerLabel = `Utilisateur ${market.userId}`;
            const productHTML = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            <span class="material-symbols-rounded">
                                store
                            </span>
                            ${stock}
                        </span>
                        <span class="prod-name">${productLabel}</span><span class="seller-name">• ${sellerLabel}</span>
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
    } catch (error) {
        console.error("Impossible de charger l'inventaire :", error);
        setTotalStock(0);
        renderEmptyMarket(container);
    }
}

// =========================
// Panier
// =========================

function getPanierItemKey(productId, userId) {
    return `${productId}-${userId}`;
}

function getPanierTotalQuantity() {
    return Object.values(panier).reduce(
        (total, item) => total + item.quantity,
        0,
    );
}

function displayPanier() {
    let total = 0;

    for (const cartItem of Object.values(panier)) {
        total += cartItem.price * cartItem.quantity;
    }

    const totalPriceElement = document.getElementById("totalPrice");
    if (totalPriceElement) {
        totalPriceElement.textContent = `$${total}`;
    }

    const cartList = document.querySelector(".cart-list");
    if (!cartList) return;

    cartList.innerHTML = "";

    for (const [cartItemKey, cartItem] of Object.entries(panier)) {
        const productHTML = `
            <div class="cart-item">
                <span>Produit ${cartItem.productId} • ${cartItem.seller}</span>
                <div class="qty-control">
                    <button class="btn-qty" onclick="retirerDuPanier('${cartItemKey}')">
                        <span class="material-symbols-rounded">remove_circle</span>
                    </button>
                    <span>${cartItem.quantity}</span>
                    <button class="btn-qty" onclick="ajouterAuPanier(${cartItem.productId}, ${cartItem.userId})">
                        <span class="material-symbols-rounded">add_circle</span>
                    </button>
                </div>
            </div>
        `;

        cartList.insertAdjacentHTML("beforeend", productHTML);
    }
}

function ajouterAuPanier(productId, userId) {
    const market = markets.find(
        (marketEntry) =>
            Number(marketEntry.productId) === Number(productId) &&
            Number(marketEntry.userId) === Number(userId),
    );

    if (!market) {
        return;
    }

    const itemKey = getPanierItemKey(productId, userId);
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

function retirerDuPanier(cartItemKey) {
    if (panier[cartItemKey]) {
        panier[cartItemKey].quantity--;
        if (panier[cartItemKey].quantity <= 0) {
            delete panier[cartItemKey];
        }
    }

    displayPanier();
}

// =========================
// Paiement
// =========================

async function payerPanier() {
    const buyerId = await getCurrentBuyerId();
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

    payButton?.setAttribute("loading", "");

    try {
        let successCount = 0;
        let failureCount = 0;

        for (const cartItem of Object.values(panier)) {
            const requestBody = {
                buyerId: Number(buyerId),
                sellerId: cartItem.userId,
                productId: cartItem.productId,
                quantity: cartItem.quantity,
            };

            try {
                const buyResponse = await fetch(`${API_URL}/market/buy`, {
                    method: "POST",
                    credentials: "include",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(requestBody),
                });

                if (!buyResponse.ok) {
                    throw new Error(`Erreur buy: ${buyResponse.status}`);
                }

                successCount++;
            } catch (error) {
                console.error(
                    `Erreur achat produit ${cartItem.productId}:`,
                    error,
                );
                snackbarElement.showSnackbar(
                    `Erreur lors de l'achat du produit ${cartItem.productId}`,
                    false,
                );
                failureCount++;
            }
        }

        if (successCount > 0 && failureCount === 0) {
            snackbarElement.showSnackbar(
                `Paiement réussi ! ${successCount} achat(s) complété(s).`,
            );
            Object.keys(panier).forEach((key) => delete panier[key]);
            displayPanier();
            await initialiserBoutique();
        } else if (successCount > 0) {
            snackbarElement.showSnackbar(
                `Paiement partiel : ${successCount} achat(s) validé(s), ${failureCount} en erreur.`,
                false,
            );
            await initialiserBoutique();
        } else {
            snackbarElement.showSnackbar(
                "Aucun achat n'a pu être complété.",
                false,
            );
        }
    } catch (error) {
        console.error("Erreur paiement:", error);
        snackbarElement.showSnackbar(
            "Erreur lors du paiement. Veuillez réessayer.",
            false,
        );
    } finally {
        await appbarElement.update();
        payButton?.removeAttribute("loading");
    }
}

// =========================
// Démarrage
// =========================

document.querySelector("#pay-btn").addEventListener("click", payerPanier);
initialiserBoutique();
displayPanier();
window.ajouterAuPanier = ajouterAuPanier;
window.retirerDuPanier = retirerDuPanier;
