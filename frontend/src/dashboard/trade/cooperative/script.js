const API_URL = window.apiUrl || "http://localhost:8080/api";
let inventaire = {};
const nomsProduits = {}; 
const produitsCollectibles = {}; 
let achatsRestantsGlobal = 0;

// Helper function to get JWT from cookies
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

// Helper to fetch with JWT
async function fetchWithAuth(url, options = {}) {
    const jwt = getCookie("jwt");
    const headers = new Headers(options.headers || {});
    if (jwt) {
        headers.append("Authorization", "Bearer " + jwt);
    }
    
    const fullUrl = url.startsWith("http") ? url : `${API_URL}${url}`;
    
    return fetch(fullUrl, {
        ...options,
        headers: headers,
    });
}

function setTotalStock(totalStock) {
    const statsContainer = document.querySelector(".market-stats");
    if (statsContainer) {
        statsContainer.innerHTML = `
            <span><span class="material-symbols-rounded">shopping_cart</span> Achats restants : ${achatsRestantsGlobal}</span>
            <span><span class="material-symbols-rounded">store</span> En stock : ${totalStock}</span>
        `;
    }
}

function renderEmptyMarket(container) {
    container.innerHTML = `
        <div class="empty-market-state">
            <span class="material-symbols-rounded empty-market-icon">air</span>
            <h2>Aucun article disponible</h2>
            <p>La coopérative est vide pour le moment. Revenez un peu plus tard.</p>
        </div>
    `;
}

async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        let productsResponse = await fetchWithAuth("/products").catch(() => null);
        if (productsResponse && productsResponse.ok) {
            const productsList = await productsResponse.json();
            productsList.forEach(p => {
                nomsProduits[p.id] = p.description;
                produitsCollectibles[p.id] = p.collectible;
            });
        }

        let response = await fetchWithAuth("/cooperative").catch(() => null);

        if (!response || !response.ok) {
            response = await fetch("/fakeapi/trade/cooperative.json");
        }

        if (response && response.ok) {
            inventaire = await response.json();
            
            if (inventaire && Object.values(inventaire)[0] && typeof Object.values(inventaire)[0] === 'object') {
                const temp = {};
                for (const [k, v] of Object.entries(inventaire)) {
                    temp[k] = v.price;
                }
                inventaire = temp;
            }
        }

        try {
            const appBar = document.querySelector("app-bar");
            const user = await appBar.fetchUser();
            
            if (user && user.id) {
                localStorage.setItem("tinyfarm-user-id", user.id);
            }

            const resAchats = await fetchWithAuth(`/users/id/${user.id}/achats-restants`);
            const remaining = resAchats.ok ? await resAchats.json() : 0;
            // Fallback: If 0 remaining but level > 0, assume it's just based on level for now
            achatsRestantsGlobal = (remaining === 0 && user.level > 0) ? user.level * 12 : remaining;
        } catch (e) {
            console.error("Erreur stats :", e);
        }

        container.innerHTML = "";
        let totalStock = Object.keys(inventaire).length;

        if (totalStock === 0) {
            setTotalStock(0);
            renderEmptyMarket(container);
            return;
        }

        for (const [idProduit, prix] of Object.entries(inventaire)) {
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
        }
        setTotalStock(totalStock);
        displayPanier();
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        setTotalStock(0);
        renderEmptyMarket(container);
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

// Partie panier
const panier = {};

function getPanierTotalQuantity() {
    return Object.values(panier).reduce((total, qty) => total + qty, 0);
}

function displayPanier() {
    let total = 0;

    for (const [idProduit, quantite] of Object.entries(panier)) {
        const prix = inventaire[idProduit];
        if (prix !== undefined) {
            total += prix * quantite;
        }
    }

    const totalPriceElem = document.getElementById("totalPrice");
    if (totalPriceElem) {
        totalPriceElem.textContent = `$${total.toFixed(2)}`;
    }

    const cartList = document.querySelector(".cart-list");
    if (cartList) {
        cartList.innerHTML = "";

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
            cartList.insertAdjacentHTML("beforeend", productHTML);
        }
    }
}

function ajouterAuPanier(idProduit) {
    if (getPanierTotalQuantity() >= achatsRestantsGlobal) {
        alert("Limite d'achats restants atteinte.");
        return;
    }

    if (panier[idProduit]) {
        panier[idProduit]++;
    } else {
        panier[idProduit] = 1;
    }
    displayPanier();
}

function retirerDuPanier(idProduit) {
    if (panier[idProduit]) {
        panier[idProduit]--;
        if (panier[idProduit] <= 0) {
            delete panier[idProduit];
        }
    }
    displayPanier();
}

async function updateBuyerStock(buyerId, idProduit) {
    try {
        const res = await fetchWithAuth(`/stocks/user/${buyerId}/product/${idProduit}`);
        if (res.ok) {
            const stock = await res.json();
            await fetchWithAuth(`/stocks/user/${buyerId}/product/${idProduit}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    quantity: stock.quantity + 1,
                    collectible: stock.collectible
                })
            });
        } else {
            const isCollectible = produitsCollectibles[idProduit] || false;
            await fetchWithAuth(`/stocks`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    id: { uid: parseInt(buyerId), productID: parseInt(idProduit) },
                    quantity: 1,
                    collectible: isCollectible
                })
            });
        }
    } catch (e) {
        console.error("Erreur mise à jour stock :", e);
    }
}

async function payerPanier() {
    const buyerId = localStorage.getItem("tinyfarm-user-id");
    const payButton = document.querySelector(".btn-pay");

    if (!buyerId) {
        alert("Utilisateur non identifié. Impossible de procéder.");
        return;
    }

    if (Object.keys(panier).length === 0) {
        alert("Votre panier est vide.");
        return;
    }

    payButton?.setAttribute("loading", "");

    try {
        let totalTransactions = 0;
        let successCount = 0;

        for (const [idProduit, quantite] of Object.entries(panier)) {
            const description = nomsProduits[idProduit] || idProduit;

            for (let i = 0; i < quantite; i++) {
                totalTransactions++;
                try {
                    const url = `/cooperative/${buyerId}/${encodeURIComponent(description)}`;
                    const response = await fetchWithAuth(url, {
                        method: "DELETE"
                    });

                    if (!response.ok) {
                        const errorText = await response.text().catch(() => "Pas de détails");
                        throw new Error(`Erreur HTTP ${response.status} : ${errorText}`);
                    }

                    // Manually update stock as backend logic is restricted
                    await updateBuyerStock(buyerId, idProduit);

                    successCount++;
                } catch (err) {
                    console.error(`Erreur achat produit ${description}:`, err);
                }
            }
        }

        if (successCount === totalTransactions && totalTransactions > 0) {
            alert(`Paiement réussi ! ${successCount} article(s) acheté(s).`);
            Object.keys(panier).forEach((key) => delete panier[key]);
            displayPanier();
            await initialiserBoutique();
        } else if (successCount > 0) {
            alert(`Paiement partiel : ${successCount}/${totalTransactions} article(s) acheté(s).`);
            displayPanier();
            await initialiserBoutique();
        } else {
            alert("Aucune transaction n'a pu être complétée. Vérifiez que le serveur backend est lancé.");
        }
    } catch (err) {
        console.error("Erreur paiement globale:", err);
        alert("Erreur lors du paiement. Veuillez réessayer.");
    } finally {
        const appBar = document.querySelector("app-bar");
        if (appBar && appBar.render) {
            await appBar.render();
        }
        payButton?.removeAttribute("loading");
    }
}

// Global scope attachment for onclick
window.ajouterAuPanier = ajouterAuPanier;
window.retirerDuPanier = retirerDuPanier;

// Initialize event listeners
const payBtn = document.querySelector(".btn-pay");
if (payBtn) {
    payBtn.addEventListener("click", payerPanier);
}
