import { fetchApiWithCredentials } from "/utils/fetch.js";

const snackbarElement = document.querySelector("tf-snackbar");
const appbarElement = document.querySelector("tf-app-bar");

let products = [];
let cooperativePrices = {};
let currentUserId = null;
const panier = {};

async function getCurrentUserId() {
    if (currentUserId !== null) {
        return currentUserId;
    }

    const response = await fetchApiWithCredentials("/auth/me");

    if (!response.ok) {
        throw new Error("Impossible de recuperer l'utilisateur connecte");
    }

    const user = await response.json();
    currentUserId = user.id;

    return currentUserId;
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

async function fetchProducts() {
    const response = await fetchApiWithCredentials("/products");

    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }

    const data = await response.json();
    return Array.isArray(data) ? data : [];
}

async function fetchCooperativeProducts() {
    const response = await fetchApiWithCredentials("/cooperative");

    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }

    const data = await response.json();
    return data && typeof data === "object" ? data : {};
}

async function fetchCooperativeOpenState() {
    const response = await fetchApiWithCredentials("/cooperative/isOpen");

    if (!response.ok) {
        throw new Error(`Erreur HTTP : ${response.status}`);
    }

    return Boolean(await response.json());
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

function setCooperativeStatus(isOpen) {
    const statusElement = document.getElementById("coop-status");

    if (!statusElement) {
        return;
    }

    statusElement.textContent = isOpen ? "Ouverte" : "Fermee";
}

function renderEmptyCooperative(container, message) {
    container.innerHTML = `
        <div class="empty-market-state">
            <span class="material-symbols-rounded empty-market-icon">storefront</span>
            <h2>Cooperative indisponible</h2>
            <p>${message}</p>
        </div>
    `;
}

function getPanierItemKey(productId) {
    return String(productId);
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
                <span>${cartItem.description}</span>
                <div class="qty-control">
                    <button class="btn-qty" onclick="retirerDuPanier('${cartItemKey}')">
                        <span class="material-symbols-rounded">remove_circle</span>
                    </button>
                    <span>${cartItem.quantity}</span>
                    <button class="btn-qty" onclick="ajouterAuPanier(${cartItem.productId})">
                        <span class="material-symbols-rounded">add_circle</span>
                    </button>
                </div>
            </div>
        `;

        cartList.insertAdjacentHTML("beforeend", productHTML);
    }
}

function renderProducts() {
    const container = document.getElementById("shop-container");

    if (!container) {
        return;
    }

    const availableProducts = products
        .filter((product) => cooperativePrices[product.id] !== undefined)
        .map((product) => ({
            productId: Number(product.id),
            description: product.description,
            price: Number(cooperativePrices[product.id]),
        }))
        .filter(
            (product) =>
                Number.isFinite(product.productId) &&
                Number.isFinite(product.price),
        );

    if (availableProducts.length === 0) {
        setTotalStock(0);
        renderEmptyCooperative(
            container,
            "Aucun article disponible pour le moment.",
        );
        return;
    }

    container.innerHTML = "";

    availableProducts.forEach((product) => {
        const productHTML = `
            <div class="product-row">
                <div class="prod-info">
                    <span class="stock-badge">
                        <span class="material-symbols-rounded">
                            store
                        </span>
                    </span>
                    <span class="prod-name">${product.description}</span>
                </div>
                <div class="prod-action">
                    <span class="price">$${product.price}</span>
                    <tf-button onclick="ajouterAuPanier(${product.productId})">Ajouter</tf-button>
                </div>
            </div>
        `;

        container.insertAdjacentHTML("beforeend", productHTML);
    });

    setTotalStock(availableProducts.length);
}

function ajouterAuPanier(productId) {
    const selectedProduct = products.find(
        (product) => Number(product.id) === Number(productId),
    );

    if (!selectedProduct) {
        return;
    }

    const price = Number(cooperativePrices[selectedProduct.id]);

    if (!Number.isFinite(price)) {
        return;
    }

    const itemKey = getPanierItemKey(productId);

    if (panier[itemKey]) {
        panier[itemKey].quantity++;
    } else {
        panier[itemKey] = {
            productId: Number(selectedProduct.id),
            description: selectedProduct.description,
            price,
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

async function initialiserCooperative() {
    const container = document.getElementById("shop-container");

    try {
        const buyerId = await getCurrentUserId();

        if (buyerId) {
            const remainingPurchases = await fetchRemainingPurchases(buyerId);
            setRemainingPurchases(remainingPurchases);
        } else {
            setRemainingPurchases(null);
        }

        const isOpen = await fetchCooperativeOpenState();
        setCooperativeStatus(isOpen);

        if (!isOpen) {
            setTotalStock(0);
            renderEmptyCooperative(
                container,
                "La cooperative est fermee pour le moment.",
            );
            return;
        }

        [products, cooperativePrices] = await Promise.all([
            fetchProducts(),
            fetchCooperativeProducts(),
        ]);

        renderProducts();
    } catch (error) {
        console.error("Impossible de charger la cooperative :", error);
        setTotalStock(0);
        renderEmptyCooperative(
            container,
            "Une erreur est survenue lors du chargement.",
        );
    }
}

async function payerPanier() {
    const buyerId = await getCurrentUserId();
    const payButton = document.querySelector("#pay-btn");

    if (!buyerId) {
        snackbarElement?.showSnackbar(
            "Utilisateur non identifie. Impossible de proceder.",
            false,
        );
        return;
    }

    if (Object.keys(panier).length === 0) {
        snackbarElement?.showSnackbar("Votre panier est vide.", false);
        return;
    }

    payButton?.setAttribute("loading", "");

    try {
        let successCount = 0;
        let failureCount = 0;

        for (const cartItem of Object.values(panier)) {
            const requestBody = {
                buyerId: Number(buyerId),
                sellerId: Number(buyerId),
                productId: Number(cartItem.productId),
                quantity: Number(cartItem.quantity),
            };

            try {
                const buyResponse = await fetchApiWithCredentials(
                    "/cooperative/buy",
                    "POST",
                    requestBody,
                );

                if (!buyResponse.ok) {
                    throw new Error(`Erreur buy: ${buyResponse.status}`);
                }

                successCount++;
            } catch (error) {
                console.error(
                    `Erreur achat produit ${cartItem.productId}:`,
                    error,
                );
                snackbarElement?.showSnackbar(
                    `Erreur lors de l'achat du produit ${cartItem.description}`,
                    false,
                );
                failureCount++;
            }
        }

        if (successCount > 0 && failureCount === 0) {
            snackbarElement?.showSnackbar(
                `Paiement reussi ! ${successCount} achat(s) complete(s).`,
            );
            Object.keys(panier).forEach((key) => delete panier[key]);
            displayPanier();
            await initialiserCooperative();
        } else if (successCount > 0) {
            snackbarElement?.showSnackbar(
                `Paiement partiel : ${successCount} achat(s) valide(s), ${failureCount} en erreur.`,
                false,
            );
            await initialiserCooperative();
        } else {
            snackbarElement?.showSnackbar(
                "Aucun achat n'a pu etre complete.",
                false,
            );
        }
    } catch (error) {
        console.error("Erreur paiement:", error);
        snackbarElement?.showSnackbar(
            "Erreur lors du paiement. Veuillez reessayer.",
            false,
        );
    } finally {
        await appbarElement?.update();
        payButton?.removeAttribute("loading");
    }
}

document.querySelector("#pay-btn")?.addEventListener("click", payerPanier);
initialiserCooperative();
displayPanier();
window.ajouterAuPanier = ajouterAuPanier;
window.retirerDuPanier = retirerDuPanier;
