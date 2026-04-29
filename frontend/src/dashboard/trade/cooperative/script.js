import { fetchApiWithCredentials } from "/utils/fetch.js";

// --- État global ---
let cart = []; // { productId, description, price, quantity }
let currentUserId = null;
let products = [];
let cooperativePrices = {};

// --- Récupère l'userId connecté ---
async function fetchCurrentUserId() {
    if (currentUserId !== null) return currentUserId;
    const response = await fetchApiWithCredentials("/auth/me");
    if (!response.ok) throw new Error("Impossible de récupérer l'utilisateur");
    const user = await response.json();
    currentUserId = user.id;
    return currentUserId;
}

// --- Initialisation ---
async function initializeCooperative() {
    try {
        await fetchCurrentUserId();

        // Vérifier si la coopérative est ouverte
        const isOpenRes = await fetchApiWithCredentials("/cooperative/isOpen");
        const isOpen = await isOpenRes.json();

        if (!isOpen) {
            document.getElementById("shop-container").innerHTML =
                '<div class="empty-state"><p>La coopérative est fermée pour le moment.</p></div>';
            document.querySelector(".btn-pay").disabled = true;
            return;
        }

        // Charger produits et prix en parallèle
        const [productsRes, coopRes] = await Promise.all([
            fetchApiWithCredentials("/products"),
            fetchApiWithCredentials("/cooperative"),
        ]);

        products = await productsRes.json();
        cooperativePrices = await coopRes.json();

        // Stock total
        const stockCount = Object.keys(cooperativePrices).length;
        const stockEl = document.querySelector(".stock-info");
        if (stockEl)
            stockEl.innerHTML = `
            <span class="material-symbols-rounded">store</span>
            En stock : ${stockCount}
        `;

        renderProducts();
        renderCart();
    } catch (error) {
        console.error("Erreur chargement coopérative :", error);
        document.getElementById("shop-container").innerHTML =
            '<div class="empty-state"><p>Erreur lors du chargement.</p></div>';
    }
}

// --- Affiche les produits ---
function renderProducts() {
    const container = document.getElementById("shop-container");

    const available = products.filter(
        (p) => cooperativePrices[p.id] !== undefined,
    );

    if (available.length === 0) {
        container.innerHTML =
            '<div class="empty-state"><p>Aucun produit disponible.</p></div>';
        return;
    }

    container.innerHTML = available
        .map((product) => {
            const price = cooperativePrices[product.id];
            return `
            <div class="product-row">
                <div class="prod-info">
                    <span class="stock-badge">
                        <span class="material-symbols-rounded">store</span>
                    </span>
                    <span class="prod-name">${product.description}</span>
                </div>
                <div class="prod-action">
                    <span class="price">${price} écus</span>
                    <button class="btn-add" onclick="addToCart(${product.id}, '${product.description}', ${price})">
                        Ajouter
                    </button>
                </div>
            </div>
        `;
        })
        .join("");
}

// --- Panier ---
window.addToCart = function (productId, description, price) {
    const existing = cart.find((item) => item.productId === productId);
    if (existing) {
        existing.quantity += 1;
    } else {
        cart.push({ productId, description, price, quantity: 1 });
    }
    renderCart();
};

window.removeFromCart = function (productId) {
    const existing = cart.find((item) => item.productId === productId);
    if (!existing) return;
    if (existing.quantity > 1) {
        existing.quantity -= 1;
    } else {
        cart = cart.filter((item) => item.productId !== productId);
    }
    renderCart();
};

function renderCart() {
    const cartList = document.querySelector(".cart-list");
    const totalEl = document.getElementById("totalPrice");

    if (cart.length === 0) {
        cartList.innerHTML = '<p class="empty-cart">Votre panier est vide</p>';
        if (totalEl) totalEl.textContent = "0 écus";
        return;
    }

    let total = 0;
    cartList.innerHTML = cart
        .map((item) => {
            total += item.price * item.quantity;
            return `
            <div class="cart-item">
                <span>${item.description}</span>
                <div class="qty-control">
                    <button class="btn-qty" onclick="removeFromCart(${item.productId})">
                        <span class="material-symbols-rounded">remove_circle</span>
                    </button>
                    <span>${item.quantity}</span>
                    <button class="btn-qty" onclick="addToCart(${item.productId}, '${item.description}', ${item.price})">
                        <span class="material-symbols-rounded">add_circle</span>
                    </button>
                </div>
            </div>
        `;
        })
        .join("");

    if (totalEl) totalEl.textContent = `${total.toFixed(2)} écus`;
}

// --- Paiement ---
document.querySelector(".btn-pay")?.addEventListener("click", async () => {
    if (cart.length === 0) {
        alert("Votre panier est vide !");
        return;
    }

    try {
        const userId = await fetchCurrentUserId();

        for (const item of cart) {
            const response = await fetchApiWithCredentials(
                "/cooperative/buy",
                "POST",
                {
                    buyerId: userId,
                    sellerId: userId,
                    productId: item.productId,
                    quantity: item.quantity,
                },
            );

            if (!response.ok) {
                const status = response.status;
                if (status === 400) {
                    alert(
                        `Achat impossible pour "${item.description}" — vérifiez votre solde ou le stock.`,
                    );
                } else {
                    alert(
                        `Erreur ${status} lors de l'achat de "${item.description}".`,
                    );
                }
                return;
            }
        }

        cart = [];
        await initializeCooperative();
        alert("Achat effectué avec succès !");
    } catch (error) {
        console.error("Erreur paiement :", error);
        alert("Erreur réseau lors du paiement.");
    }
});

// --- Lancement ---
initializeCooperative();
