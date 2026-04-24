import { API_URL, fetchApiWithCredentials } from "/utils/fetch.js";

const FAKE_ASSETS_URL = "/fakeapi/assets.json";
const PRICE_DECIMALS = 2;

let inventaire = {};
const panier = {};
const prixPrecedents = new Map();

// === Utilitaires ===

function echapperPourOnclick(valeur) {
    return String(valeur).replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function obtenirIdPrix(nomProduit) {
    return `price-${nomProduit}`;
}

function obtenirIdBoutonEdition(nomProduit) {
    return `edit-${nomProduit}`;
}

function obtenirIdSaisiePrix(nomProduit) {
    return `new-price-${nomProduit}`;
}

function obtenirListePanier() {
    return document.querySelector(".cart-list");
}

function formaterPrix(prix) {
    return `$${Number(prix).toFixed(PRICE_DECIMALS)}`;
}

function afficherEtatInventaireVide(conteneur) {
    conteneur.innerHTML = `
        <div class="empty-state">
            <span class="material-symbols-rounded empty-state-icon">inventory_2</span>
            <h2 class="empty-state-title">Aucun article disponible</h2>
            <p class="empty-state-text">La remise est vide pour le moment. Ajoute des produits pour commencer la vente.</p>
        </div>
    `;
}

function normaliserPrix(valeurBrute) {
    const prix = Number.parseFloat(valeurBrute);
    if (!Number.isFinite(prix) || prix < 0) {
        return null;
    }

    return Math.round(prix * 100) / 100;
}

function doitUtiliserApercuFictif() {
    const params = new URLSearchParams(window.location.search);
    return params.get("fake") === "1";
}

function obtenirNomProduitAffichage(produit) {
    if (typeof produit.description === "string") {
        return produit.description.trim();
    }

    return `Produit ${produit.id}`;
}

function convertirApiEnInventaire(stocks, produits) {
    const produitsParId = new Map(
        produits.map((produit) => [Number(produit.id), produit]),
    );

    return stocks.reduce((inventaireCourant, stock) => {
        const produit = produitsParId.get(Number(stock.productId));
        if (!produit) {
            return inventaireCourant;
        }

        const nomProduit = obtenirNomProduitAffichage(produit);

        inventaireCourant[nomProduit] = {
            id: Number(produit.id),
            description: produit.description ?? nomProduit,
            collectible: Boolean(produit.collectible),
            coefficient: Number(produit.coefficient) || 1,
            quantity: Number(stock.quantity) || 0,
            price: Number(produit.price) || 0,
        };

        return inventaireCourant;
    }, {});
}

// === Chargement des données ===

async function recupererIdUtilisateurCourant() {
    const response = await fetchApiWithCredentials("/auth/me");
    if (!response.ok) {
        throw new Error(`Impossible de recuperer l'utilisateur: ${response.status}`);
    }

    const utilisateur = await response.json();
    if (!utilisateur || utilisateur.id == null) {
        throw new Error("Reponse /auth/me invalide");
    }

    return Number(utilisateur.id);
}

async function chargerInventaireReel() {
    const userId = await recupererIdUtilisateurCourant();

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

    const [produits, stocks] = await Promise.all([
        productsResponse.json(),
        stocksResponse.json(),
    ]);

    return convertirApiEnInventaire(
        Array.isArray(stocks) ? stocks : [],
        Array.isArray(produits) ? produits : [],
    );
}

async function chargerInventaireFictif() {
    const response = await fetch(FAKE_ASSETS_URL);

    if (!response.ok) {
        throw new Error(`Erreur fake API: ${response.status}`);
    }

    const donneesFactices = await response.json();
    const produits = Array.isArray(donneesFactices.products)
        ? donneesFactices.products
        : [];
    const stocks = Array.isArray(donneesFactices.stocks)
        ? donneesFactices.stocks
        : [];

    return convertirApiEnInventaire(stocks, produits);
}

// === Rendu de la boutique ===

async function initialiserBoutique() {
    const conteneurBoutique = document.getElementById("shop-container");
    if (!conteneurBoutique) {
        return;
    }

    try {
        inventaire = doitUtiliserApercuFictif()
            ? await chargerInventaireFictif()
            : await chargerInventaireReel();

        conteneurBoutique.innerHTML = "";

        if (Object.keys(inventaire).length === 0) {
            afficherEtatInventaireVide(conteneurBoutique);
            return;
        }

        for (const [nomProduit, valeurs] of Object.entries(inventaire)) {
            const nomEchappe = echapperPourOnclick(nomProduit);
            const htmlProduit = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            x
                            ${valeurs.quantity}
                        </span>
                        <span class="prod-name">${nomProduit}</span>
                    </div>
                    <div class="prod-action">
                        <button class="edit-price-btn" id="${obtenirIdBoutonEdition(nomProduit)}" onclick="modifierPrix('${nomEchappe}')">
                            <span class="material-symbols-rounded">
                                edit
                            </span>
                        </button>
                        <span class="price" id="${obtenirIdPrix(nomProduit)}">${formaterPrix(valeurs.price)}</span>
                        <tf-button variant="primary" onclick="ajouterAuPanier('${nomEchappe}')">Ajouter</tf-button>
                    </div>
                </div>
            `;
            conteneurBoutique.insertAdjacentHTML("beforeend", htmlProduit);
        }
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        conteneurBoutique.innerHTML = "<p>Erreur lors du chargement des produits.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

// === Gestion du panier ===

function afficherPanier() {
    let total = 0;

    for (const [nomProduit, quantite] of Object.entries(panier)) {
        const produit = inventaire[nomProduit];
        if (produit) {
            total += produit.price * quantite;
        }
    }

    const totalArrondi = Math.round(total * 100) / 100;
    const totalElement = document.getElementById("totalPrice");
    if (totalElement) {
        totalElement.textContent = formaterPrix(totalArrondi);
    }

    const listePanier = obtenirListePanier();
    if (!listePanier) {
        return;
    }

    listePanier.innerHTML = "";

    if (Object.keys(panier).length === 0) {
        return;
    }

    for (const [nomProduit, quantite] of Object.entries(panier)) {
        const nomEchappe = echapperPourOnclick(nomProduit);
        const htmlProduit = `
            <div class="cart-item">
                <span>${nomProduit}</span>
                <div class="qty-control">
                    <button class="btn-qty" onclick="retirerDuPanier('${nomEchappe}')">
                        <span class="material-symbols-rounded">
                            remove_circle
                        </span>
                    </button>
                    <span>${quantite}</span>
                    <button class="btn-qty" onclick="ajouterAuPanier('${nomEchappe}')">
                        <span class="material-symbols-rounded">
                            add_circle
                        </span>
                    </button>
                </div>
            </div>
        `;
        listePanier.insertAdjacentHTML("beforeend", htmlProduit);
    }
}

afficherPanier();

function ajouterAuPanier(nomProduit) {
    if (!inventaire[nomProduit]) {
        return;
    }

    panier[nomProduit] = (panier[nomProduit] || 0) + 1;
    afficherPanier();
}

function retirerDuPanier(nomProduit) {
    if (panier[nomProduit]) {
        panier[nomProduit]--;
        if (panier[nomProduit] <= 0) {
            delete panier[nomProduit];
        }
    }

    afficherPanier();
}

// === Gestion des prix ===

function appliquerPrix(nomProduit, valeurBrute, mettreAJourAffichage = true) {
    const prix = normaliserPrix(valeurBrute);
    if (prix === null || !inventaire[nomProduit]) {
        return false;
    }

    inventaire[nomProduit].price = prix;

    if (mettreAJourAffichage) {
        const elementPrix = document.getElementById(obtenirIdPrix(nomProduit));
        if (elementPrix) {
            elementPrix.textContent = formaterPrix(prix);
        }
    }

    afficherPanier();
    return true;
}

async function sauvegarderPrixProduit(nomProduit) {
    const produit = inventaire[nomProduit];
    if (!produit || produit.id == null) {
        throw new Error(`Produit introuvable: ${nomProduit}`);
    }

    const response = await fetch(`${API_URL}/products/id/${produit.id}`, {
        method: "PUT",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            id: produit.id,
            description: produit.description,
            collectible: produit.collectible,
            price: produit.price,
            coefficient: produit.coefficient,
        }),
    });

    if (!response.ok) {
        throw new Error(`Erreur sauvegarde prix: ${response.status}`);
    }

    const produitMisAJour = await response.json();
    if (produitMisAJour && produitMisAJour.price != null) {
        const prixNormalise = normaliserPrix(produitMisAJour.price);
        if (prixNormalise != null) {
            inventaire[nomProduit].price = prixNormalise;
        }
    }
}

function mettreAJourBoutonEdition(nomProduit, nomFonction, icone) {
    const boutonEdition = document.getElementById(obtenirIdBoutonEdition(nomProduit));
    if (!boutonEdition) {
        return;
    }

    boutonEdition.innerHTML = `<span class="material-symbols-rounded">${icone}</span>`;
    boutonEdition.setAttribute("onclick", `${nomFonction}('${echapperPourOnclick(nomProduit)}')`);
}

function restaurerPrix(nomProduit) {
    const prixPrecedent = prixPrecedents.get(nomProduit);
    if (prixPrecedent != null && inventaire[nomProduit]) {
        inventaire[nomProduit].price = prixPrecedent;
    }

    const elementPrix = document.getElementById(obtenirIdPrix(nomProduit));
    if (elementPrix && inventaire[nomProduit]) {
        elementPrix.textContent = formaterPrix(inventaire[nomProduit].price);
    }

    prixPrecedents.delete(nomProduit);
    mettreAJourBoutonEdition(nomProduit, "modifierPrix", "edit");
    afficherPanier();
}

function modifierPrix(nomProduit) {
    const elementPrix = document.getElementById(obtenirIdPrix(nomProduit));
    if (!elementPrix) {
        return;
    }

    if (inventaire[nomProduit]) {
        prixPrecedents.set(nomProduit, inventaire[nomProduit].price);
    }

    const prixActuel = normaliserPrix(elementPrix.textContent.replace("$", ""));
    const valeurInitiale = prixActuel == null ? 0 : prixActuel.toFixed(2);

    elementPrix.innerHTML = `
        <input type="number" id="${obtenirIdSaisiePrix(nomProduit)}" value="${valeurInitiale}" min="0" step="0.01">
    `;

    const saisiePrix = document.getElementById(obtenirIdSaisiePrix(nomProduit));
    saisiePrix?.addEventListener("input", () => {
        appliquerPrix(nomProduit, saisiePrix.value, false);
    });
    saisiePrix?.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            confirmerPrix(nomProduit);
        }

        if (event.key === "Escape") {
            event.preventDefault();
            restaurerPrix(nomProduit);
        }
    });
    saisiePrix?.focus();
    saisiePrix?.select();

    mettreAJourBoutonEdition(nomProduit, "confirmerPrix", "check");
}

async function confirmerPrix(nomProduit) {
    const saisiePrix = document.getElementById(obtenirIdSaisiePrix(nomProduit));
    if (!saisiePrix) {
        return;
    }

    const prixMisAJour = appliquerPrix(nomProduit, saisiePrix.value, true);
    if (!prixMisAJour && inventaire[nomProduit]) {
        const elementPrix = document.getElementById(obtenirIdPrix(nomProduit));
        if (elementPrix) {
            elementPrix.textContent = formaterPrix(inventaire[nomProduit].price);
        }
    }

    if (prixMisAJour) {
        try {
            await sauvegarderPrixProduit(nomProduit);
            const elementPrix = document.getElementById(obtenirIdPrix(nomProduit));
            if (elementPrix && inventaire[nomProduit]) {
                elementPrix.textContent = formaterPrix(inventaire[nomProduit].price);
            }
        } catch (erreur) {
            console.error("Impossible de sauvegarder le prix:", erreur);
            restaurerPrix(nomProduit);
            return;
        }
    }

    prixPrecedents.delete(nomProduit);
    mettreAJourBoutonEdition(nomProduit, "modifierPrix", "edit");
}

// === Exposition globale ===

window.ajouterAuPanier = ajouterAuPanier;
window.retirerDuPanier = retirerDuPanier;
window.modifierPrix = modifierPrix;
window.confirmerPrix = confirmerPrix;