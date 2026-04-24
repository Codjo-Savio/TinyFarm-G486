const API_URL = "http://localhost:8080/api";
let inventaire = {};
const nomsProduits = {}; 

async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        let productsResponse = await fetch(`${API_URL}/products`).catch(() => null);
        if (productsResponse && productsResponse.ok) {
            const productsList = await productsResponse.json();
            productsList.forEach(p => {
                nomsProduits[p.id] = p.description;
            });
        }

        let response = await fetch(`${API_URL}/cooperative`).catch(() => null);

        if (!response || !response.ok) {
            response = await fetch("/fakeapi/trade/cooperative.json");
        }

        inventaire = await response.json();
        
        if (inventaire && Object.values(inventaire)[0] && typeof Object.values(inventaire)[0] === 'object') {
            const temp = {};
            for (const [k, v] of Object.entries(inventaire)) {
                temp[k] = v.price;
            }
            inventaire = temp;
        }


        try {
            const appBar = document.querySelector("app-bar");
            const user = await appBar.fetchUser();
            
            const resAchats = await fetch(`${API_URL}/users/id/${user.id}/achats-restants`);
            const achatsRestants = resAchats.ok ? await resAchats.json() : user.level * 12;
            const enStock = Object.keys(inventaire).length;
            
            const statsContainer = document.querySelector(".market-stats");
            if (statsContainer) {
                statsContainer.innerHTML = `
                    <span><span class="material-symbols-rounded">shopping_cart</span> Achats restants : ${achatsRestants}</span>
                    <span><span class="material-symbols-rounded">store</span> En stock : ${enStock}</span>
                `;
            }
        } catch (e) {
            console.error("Erreur stats :", e);
        }

        container.innerHTML = "";

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
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        container.innerHTML = "<p>Erreur lors du chargement des produits.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

// Partie panier
const panier = {};

function displayPanier() {
    let total = 0;

    for (const [idProduit, quantite] of Object.entries(panier)) {
        const prix = inventaire[idProduit];
        if (prix !== undefined) {
            total += prix * quantite;
        }
    }

    document.getElementById("totalPrice").textContent = `$${total.toFixed(2)}`;

    document.querySelector(".cart-list").innerHTML = "";

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
        document
            .querySelector(".cart-list")
            .insertAdjacentHTML("beforeend", productHTML);
    }
}

displayPanier();

function ajouterAuPanier(nomProduit) {
    if (panier[nomProduit]) {
        panier[nomProduit]++;
    } else {
        panier[nomProduit] = 1;
    }
    console.log(panier);
    displayPanier();
}

function retirerDuPanier(nomProduit) {
    if (panier[nomProduit]) {
        panier[nomProduit]--;
        if (panier[nomProduit] <= 0) {
            delete panier[nomProduit];
        }
    }
    console.log(panier);
    displayPanier();
}
