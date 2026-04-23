import { fetchApiWithCredentials } from "/utils/fetch.js";

const FAKE_ASSETS_URL = "/fakeapi/assets.json";
const PRICE_DECIMALS = 2;

let inventaire = {};
const panier = {};
const previousPrices = new Map();

function escapeForOnclick(value) {
    return String(value).replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function getPriceElementId(productName) {
    return `price-${productName}`;
}

function getEditButtonId(productName) {
    return `edit-${productName}`;
}

function getPriceInputId(productName) {
    return `new-price-${productName}`;
}

function getCartListElement() {
    return document.querySelector(".cart-list");
}

function formatPrice(price) {
    return `$${Number(price).toFixed(PRICE_DECIMALS)}`;
}

function renderEmptyInventoryState(container) {
    container.innerHTML = `
        <div class="empty-state">
            <span class="material-symbols-rounded empty-state-icon">inventory_2</span>
            <h2 class="empty-state-title">Aucun article disponible</h2>
            <p class="empty-state-text">La remise est vide pour le moment. Ajoute des produits pour commencer la vente.</p>
        </div>
    `;
}

function normalizePrice(rawValue) {
    const parsed = Number.parseFloat(rawValue);
    if (!Number.isFinite(parsed) || parsed < 0) {
        return null;
    }
    return Math.round(parsed * 100) / 100;
}

function shouldUseFakePreview() {
    const params = new URLSearchParams(window.location.search);
    return params.get("fake") === "1";
}

async function fetchCurrentUserId() {
    const response = await fetchApiWithCredentials("/auth/me");
    if (!response.ok) {
        throw new Error(`Impossible de recuperer l'utilisateur: ${response.status}`);
    }

    const user = await response.json();
    if (!user || user.id == null) {
        throw new Error("Reponse /auth/me invalide");
    }

    return Number(user.id);
}

function getProductDisplayName(product) {
    if (typeof product.description === "string") {
        return product.description.trim();
    }

    return `Produit ${product.id}`;
}

function normalizeApiDataToInventory(stocks, products) {
    const productById = new Map(
        products.map((product) => [Number(product.id), product]),
    );

    return stocks.reduce((acc, stock) => {
        const product = productById.get(Number(stock.productId));
        if (!product) {
            return acc;
        }

        const productName = getProductDisplayName(product);

        acc[productName] = {
            quantity: Number(stock.quantity) || 0,
            price: Number(product.price) || 0,
        };

        return acc;
    }, {});
}

async function fetchRealInventoryData() {
    const userId = await fetchCurrentUserId();

    const [productsResponse, stocksResponse] = await Promise.all([
        fetchApiWithCredentials("/products"),
        fetchApiWithCredentials(`/stocks/user/${userId}`),
    ]);

    if (!productsResponse.ok) {
        throw new Error(`Erreur produits: ${productsResponse.status}`);
    }

    if (!stocksResponse.ok) {
        throw new Error(`Erreur stocks: ${stocksResponse.status}`);
    }

    const [products, stocks] = await Promise.all([
        productsResponse.json(),
        stocksResponse.json(),
    ]);

    return normalizeApiDataToInventory(
        Array.isArray(stocks) ? stocks : [],
        Array.isArray(products) ? products : [],
    );
}

async function fetchFakeInventoryData() {
    const response = await fetch(FAKE_ASSETS_URL);

    if (!response.ok) {
        throw new Error(`Erreur fake API: ${response.status}`);
    }

    const fakePayload = await response.json();
    const products = Array.isArray(fakePayload.products)
        ? fakePayload.products
        : [];
    const stocks = Array.isArray(fakePayload.stocks) ? fakePayload.stocks : [];

    return normalizeApiDataToInventory(stocks, products);
}

async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        const forceFakePreview = shouldUseFakePreview();
        inventaire = forceFakePreview
            ? await fetchFakeInventoryData()
            : await fetchRealInventoryData();

        container.innerHTML = "";

        if (Object.keys(inventaire).length === 0) {
            renderEmptyInventoryState(container);
            return;
        }

        for (const [nom, values] of Object.entries(inventaire)) {
            const escapedNom = escapeForOnclick(nom);
            const productHTML = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            x
                            ${values.quantity}
                        </span>
                        <span class="prod-name">${nom}</span>
                    </div>
                    <div class="prod-action">
                        <button class="edit-price-btn" id="${getEditButtonId(nom)}" onclick="modifierPrix('${escapedNom}')">
                            <span class="material-symbols-rounded">
                                edit
                            </span>
                        </button>
                        <span class="price" id="${getPriceElementId(nom)}">${formatPrice(values.price)}</span>
                        <tf-button variant="primary" onclick="ajouterAuPanier('${escapedNom}')">Ajouter</tf-button>
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

function displayPanier() {
    let total = 0;

    for (const [nom, quantite] of Object.entries(panier)) {
        const produit = inventaire[nom];
        if (produit) {
            total += produit.price * quantite;
        }
    }

    const totalArrondi = Math.round(total * 100) / 100;
    document.getElementById("totalPrice").textContent = formatPrice(totalArrondi);

    const cartListElement = getCartListElement();
    cartListElement.innerHTML = "";

    if (Object.keys(panier).length === 0) {
        return;
    }

    for (const [nom, quantite] of Object.entries(panier)) {
        const escapedNom = escapeForOnclick(nom);
        const productHTML = `
                            <div class="cart-item">
                                    <span>${nom}</span>
                                    <div class="qty-control">
                                        <button class="btn-qty" onclick="retirerDuPanier('${escapedNom}')">
                                            <span class="material-symbols-rounded">
                                                remove_circle
                                            </span>
                                        </button>
                                        <span>${quantite}</span>
                                        <button class="btn-qty" onclick="ajouterAuPanier('${escapedNom}')">
                                            <span class="material-symbols-rounded">
                                                add_circle
                                            </span>
                                        </button>
                                    </div>
                                </div>
                                `;
        cartListElement.insertAdjacentHTML("beforeend", productHTML);
    }
}

displayPanier();

function ajouterAuPanier(nomProduit) {
    if (!inventaire[nomProduit]) {
        return;
    }

    if (panier[nomProduit]) {
        panier[nomProduit]++;
    } else {
        panier[nomProduit] = 1;
    }

    displayPanier();
}

function retirerDuPanier(nomProduit) {
    if (panier[nomProduit]) {
        panier[nomProduit]--;
        if (panier[nomProduit] <= 0) {
            delete panier[nomProduit];
        }
    }

    displayPanier();
}

function applyPriceChange(nomProduit, rawValue, updateDisplay = true) {
    const prix = normalizePrice(rawValue);
    if (prix === null || !inventaire[nomProduit]) {
        return false;
    }

    inventaire[nomProduit].price = prix;

    if (updateDisplay) {
        const priceSpan = document.getElementById(getPriceElementId(nomProduit));
        if (priceSpan) {
            priceSpan.textContent = formatPrice(prix);
        }
    }

    displayPanier();
    return true;
}

function restoreEditButton(nomProduit) {
    const editButton = document.getElementById(getEditButtonId(nomProduit));
    if (!editButton) {
        return;
    }

    editButton.innerHTML = `<span class="material-symbols-rounded">
                                edit
                            </span>`;
    editButton.setAttribute("onclick", `modifierPrix('${escapeForOnclick(nomProduit)}')`);
}

function annulerPrix(nomProduit) {
    const previousPrice = previousPrices.get(nomProduit);
    if (previousPrice != null && inventaire[nomProduit]) {
        inventaire[nomProduit].price = previousPrice;
    }

    const priceSpan = document.getElementById(getPriceElementId(nomProduit));
    if (priceSpan && inventaire[nomProduit]) {
        priceSpan.textContent = formatPrice(inventaire[nomProduit].price);
    }

    previousPrices.delete(nomProduit);
    restoreEditButton(nomProduit);
    displayPanier();
}

function modifierPrix(nomProduit) {
    const priceSpan = document.getElementById(getPriceElementId(nomProduit));
    if (!priceSpan) {
        return;
    }

    if (inventaire[nomProduit]) {
        previousPrices.set(nomProduit, inventaire[nomProduit].price);
    }

    const currentPrice = parseFloat(priceSpan.textContent.replace("$", ""));
    const inputHTML = `
        <input type="number" id="${getPriceInputId(nomProduit)}" value="${currentPrice.toFixed(2)}" min="0" step="0.01">
    `;
    priceSpan.innerHTML = inputHTML;

    const input = document.getElementById(getPriceInputId(nomProduit));
    input?.addEventListener("input", () => {
        applyPriceChange(nomProduit, input.value, false);
    });
    input?.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            confirmerPrix(nomProduit);
        }
        if (event.key === "Escape") {
            event.preventDefault();
            annulerPrix(nomProduit);
        }
    });
    input?.focus();
    input?.select();

    const editButton = document.getElementById(getEditButtonId(nomProduit));
    if (!editButton) {
        return;
    }

    editButton.innerHTML = `<span class="material-symbols-rounded">
                                check
                            </span>`;
    editButton.setAttribute("onclick", `confirmerPrix('${escapeForOnclick(nomProduit)}')`);
}

function confirmerPrix(nomProduit) {
    const input = document.getElementById(getPriceInputId(nomProduit));
    if (!input) {
        return;
    }

    const hasUpdatedPrice = applyPriceChange(nomProduit, input.value, true);
    if (!hasUpdatedPrice && inventaire[nomProduit]) {
        const priceSpan = document.getElementById(getPriceElementId(nomProduit));
        if (priceSpan) {
            priceSpan.textContent = formatPrice(inventaire[nomProduit].price);
        }
    }

    previousPrices.delete(nomProduit);
    restoreEditButton(nomProduit);
}

window.ajouterAuPanier = ajouterAuPanier;
window.retirerDuPanier = retirerDuPanier;
window.modifierPrix = modifierPrix;
window.confirmerPrix = confirmerPrix;
