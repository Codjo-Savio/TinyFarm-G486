import { API_URL, fetchApiWithCredentials, loadScriptIfNeeded } from "/utils/fetch.js";

function utiliserModeFake() {
    const params = new URLSearchParams(window.location.search);
    return params.get("fake") === "1" || params.get("mockData") === "1";
}

function normaliserListe(source, cle) {
    if (Array.isArray(source)) {
        return source;
    }

    if (Array.isArray(source?.[cle])) {
        return source[cle];
    }

    return [];
}

function normaliserInventaireRecu(source) {
    if (Array.isArray(source)) {
        return {
            products: [],
            stocks: source,
        };
    }

    return {
        products: normaliserListe(source, "products"),
        stocks: normaliserListe(source, "stocks"),
    };
}

function normaliserNombre(value) {
    const nombre = Number(value);
    return Number.isFinite(nombre) ? nombre : 0;
}

function echapperPourOnclick(value) {
    return String(value).replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function obtenirIdPrix(nomProduit) {
    return `price-${nomProduit}`;
}

function obtenirIdBoutonEdition(nomProduit) {
    return `edit-${nomProduit}`;
}

function formaterPrix(prix) {
    return `$${normaliserNombre(prix).toFixed(2)}`;
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

let dialogPrixVente = null;

function obtenirDialogPrixVente() {
    if (dialogPrixVente) {
        return dialogPrixVente;
    }

    loadScriptIfNeeded("/components/tf-dialog.js");

    const dialog = document.createElement("tf-dialog");
    dialog.setAttribute("modal", "");
    dialog.setAttribute("title", "Prix de vente");
    dialog.setAttribute("title-icon", "sell");
    dialog.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:8px;">
            <label for="sale-price-input" style="font-weight:600;">Prix unitaire</label>
            <input id="sale-price-input" type="number" min="0" step="0.01" style="padding:10px 12px; border-radius:10px; border:1px solid rgba(0,0,0,.2); font-size:16px;" />
        </div>
        <tf-button slot="cancel-button" variant="secondary">Annuler</tf-button>
        <tf-button slot="confirm-button" variant="primary">Valider</tf-button>
    `;

    document.body.appendChild(dialog);
    dialogPrixVente = dialog;
    return dialog;
}

function demanderPrixViaDialog(nomProduit, valeurInitiale) {
    const dialog = obtenirDialogPrixVente();
    dialog.setAttribute("title", `Prix de vente - ${nomProduit}`);

    const input = dialog.querySelector("#sale-price-input");
    const boutonAnnuler = dialog.querySelector('[slot="cancel-button"]');
    const boutonValider = dialog.querySelector('[slot="confirm-button"]');

    if (!input || !boutonAnnuler || !boutonValider) {
        return Promise.resolve(null);
    }

    input.value = Number.isFinite(valeurInitiale)
        ? normaliserNombre(valeurInitiale).toFixed(2)
        : "";

    dialog.setAttribute("show", "");

    return new Promise((resolve) => {
        const fermer = (resultat) => {
            dialog.removeAttribute("show");
            input.removeEventListener("keydown", gererClavier);
            boutonAnnuler.removeEventListener("click", annuler);
            boutonValider.removeEventListener("click", valider);
            resolve(resultat);
        };

        const annuler = () => fermer(null);

        const valider = () => {
            const prix = Math.round(Number.parseFloat(input.value) * 100) / 100;
            if (!Number.isFinite(prix) || prix < 0) {
                window.alert("Prix invalide. Merci d'entrer un nombre positif.");
                input.focus();
                return;
            }

            fermer(prix);
        };

        const gererClavier = (event) => {
            if (event.key === "Enter") {
                event.preventDefault();
                valider();
            }

            if (event.key === "Escape") {
                event.preventDefault();
                annuler();
            }
        };

        input.addEventListener("keydown", gererClavier);
        boutonAnnuler.addEventListener("click", annuler);
        boutonValider.addEventListener("click", valider);

        requestAnimationFrame(() => {
            input.focus();
            input.select();
        });
    });
}

async function chargerInventaireFake() {
    const urlsCandidates = ["/fakeapi/assets.json", "../../../fakeapi/assets.json"];

    for (const url of urlsCandidates) {
        try {
            const response = await fetch(url);
            if (!response.ok) {
                continue;
            }

            return await response.json();
        } catch {
            // continue to next candidate URL
        }
    }

    throw new Error("Impossible de charger les donnees fake");
}

async function chargerInventaireReel() {
    const [meResponse, productsResponse] = await Promise.all([
        fetchApiWithCredentials("/auth/me"),
        fetchApiWithCredentials("/products"),
    ]);

    if (meResponse.ok) {
        const utilisateur = await meResponse.json();
        idUtilisateurCourant = Number(utilisateur?.id) || null;
    }

    let products = [];
    if (productsResponse.ok) {
        const jsonProducts = await productsResponse.json();
        products = Array.isArray(jsonProducts) ? jsonProducts : [];
    }

    let stocks = [];
    if (idUtilisateurCourant) {
        const stocksResponse = await fetchApiWithCredentials(`/stocks/user/${idUtilisateurCourant}`);
        if (stocksResponse.ok) {
            const jsonStocks = await stocksResponse.json();
            stocks = Array.isArray(jsonStocks) ? jsonStocks : [];
        }
    }

    return { products, stocks };
}

function indexerInventaire(source) {
    idProduitParNom.clear();
    stockParNom.clear();

    const produits = normaliserListe(source, "products");
    const produitsParId = new Map();
    for (const produit of produits) {
        const idProduit = normaliserNombre(produit?.id);
        const nomProduit = typeof produit?.description === "string"
            ? produit.description.trim()
            : "";

        if (!nomProduit || idProduit <= 0) {
            continue;
        }

        produitsParId.set(idProduit, produit);
        idProduitParNom.set(nomProduit, idProduit);
    }

    const stocks = normaliserListe(source, "stocks");
    for (const stock of stocks) {
        const idProduit = normaliserNombre(stock?.productId ?? stock?.id?.productID);
        const quantite = normaliserNombre(stock?.quantity);

        if (idProduit <= 0) {
            continue;
        }

        const produit = produitsParId.get(idProduit);
        const nomProduit = typeof produit?.description === "string"
            ? produit.description.trim()
            : `Produit ${idProduit}`;

        stockParNom.set(nomProduit.toLowerCase(), quantite);

        if (!idProduitParNom.has(nomProduit)) {
            idProduitParNom.set(nomProduit, idProduit);
        }
    }
}

async function initialiserBoutique() {
    const container = document.getElementById("shop-container");

    try {
        const modeFake = utiliserModeFake();
        const inventaireRecu = modeFake
            ? await chargerInventaireFake()
            : await chargerInventaireReel();
        inventaire = normaliserInventaireRecu(inventaireRecu);

        indexerInventaire(inventaire);

        const produits = normaliserListe(inventaire, "products");
        const stocks = normaliserListe(inventaire, "stocks");
        const stockParId = new Map();

        for (const stock of stocks) {
            const idProduit = normaliserNombre(stock?.productId ?? stock?.id?.productID);
            if (idProduit <= 0) {
                continue;
            }

            stockParId.set(idProduit, normaliserNombre(stock?.quantity));
        }

        container.innerHTML = "";

        const produitsAffichables = produits.length > 0
            ? produits
            : [...stockParId.keys()].map((idProduit) => ({
                id: idProduit,
                description: `Produit ${idProduit}`,
                price: 0,
            }));

        inventaire.products = produitsAffichables;

        if (produitsAffichables.length === 0) {
            renderEmptyInventoryState(container);
            return;
        }

        const catalogueHTML = [];

        for (const produit of produitsAffichables) {
            const idProduit = normaliserNombre(produit?.id);
            if (idProduit <= 0) {
                continue;
            }

            const nomProduit = typeof produit?.description === "string"
                ? produit.description.trim()
                : `Produit ${idProduit}`;
            const valeurs = {
                quantity: stockParId.get(idProduit) ?? 0,
                price: normaliserNombre(produit?.price),
            };

            prixParNom.set(nomProduit.toLowerCase(), valeurs.price);

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
                        <tf-button variant="primary" ${valeurs.quantity <= 0 ? "disabled" : ""} onclick="ajouterAuPanier('${nomEchappe}')">Ajouter</tf-button>
                    </div>
                </div>
            `;
            catalogueHTML.push(htmlProduit);
        }

        container.insertAdjacentHTML("beforeend", catalogueHTML.join(""));
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        container.innerHTML = "<p>Erreur lors du chargement des produits.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

// Partie panier
let inventaire = {
    products: [],
    stocks: [],
};
const panier = {};
let idUtilisateurCourant = null;
const idProduitParNom = new Map();
const stockParNom = new Map();
const prixParNom = new Map();

async function chargerContextePublication() {
    try {
        const [meResponse, productsResponse] = await Promise.all([
            fetchApiWithCredentials("/auth/me"),
            fetchApiWithCredentials("/products"),
        ]);

        if (meResponse.ok) {
            const utilisateur = await meResponse.json();
            idUtilisateurCourant = Number(utilisateur?.id) || null;
        }

        if (productsResponse.ok) {
            const produits = await productsResponse.json();
            if (Array.isArray(produits)) {
                for (const produit of produits) {
                    const nom = typeof produit?.description === "string"
                        ? produit.description.trim()
                        : null;
                    const productId = Number(produit?.id);
                    if (nom && Number.isFinite(productId)) {
                        idProduitParNom.set(nom, productId);
                    }
                }
            }
        }
    } catch (erreur) {
        console.warn("Contexte de publication indisponible:", erreur);
    }
}

function obtenirIdProduitParNom(nomProduit) {
    if (idProduitParNom.has(nomProduit)) {
        return idProduitParNom.get(nomProduit);
    }

    const cle = nomProduit.trim().toLowerCase();
    for (const [nom, id] of idProduitParNom.entries()) {
        if (nom.trim().toLowerCase() === cle) {
            return id;
        }
    }

    return null;
}

function trouverProduitDansInventaire(nomProduit) {
    const produits = Array.isArray(inventaire.products) ? inventaire.products : [];
    const trouve = produits.find((produit) => {
        const description = typeof produit?.description === "string"
            ? produit.description.trim().toLowerCase()
            : "";
        return description === nomProduit.trim().toLowerCase();
    }) ?? null;

    if (trouve) {
        return trouve;
    }

    const idProduit = obtenirIdProduitParNom(nomProduit);
    if (Number.isFinite(idProduit)) {
        return {
            id: idProduit,
            description: nomProduit,
        };
    }

    return null;
}

async function demanderPrixVente(nomProduit) {
    const prixDefaut = prixParNom.get(nomProduit.trim().toLowerCase());
    return await demanderPrixViaDialog(nomProduit, prixDefaut);
}

async function modifierPrix(nomProduit) {
    const cle = nomProduit.trim().toLowerCase();
    const prixCourant = prixParNom.get(cle) ?? 0;

    const prix = await demanderPrixViaDialog(nomProduit, prixCourant);
    if (prix === null) {
        return;
    }

    prixParNom.set(cle, prix);

    const elementPrix = document.getElementById(obtenirIdPrix(nomProduit));
    if (elementPrix) {
        elementPrix.textContent = formaterPrix(prix);
    }
}

function displayPanier() {
    let total = 0;

    for (const item of Object.values(panier)) {
        total += item.prixVente * item.quantite;
    }

    const totalArrondi = Math.round(total * 100) / 100;
    const totalPriceElement = document.getElementById("totalPrice");
    if (totalPriceElement) {
        totalPriceElement.textContent = `$${totalArrondi.toFixed(2)}`;
    }

    const cartListElement = document.querySelector(".cart-list");
    if (!cartListElement) {
        return;
    }

    cartListElement.innerHTML = "";

    for (const [nom, item] of Object.entries(panier)) {
        const productHTML = `
                            <div class="cart-item">
                                    <span>${nom}</span>
                                    <span>Prix unit.: $${item.prixVente.toFixed(2)}</span>
                                    <div class="qty-control">
                                        <button class="btn-qty" onclick="modifierPrixPanier('${nom.replace(/'/g, "\\'")}')">
                                            <span
                                                class="material-symbols-rounded"
                                            >
                                                edit
                                            </span>
                                        </button>
                                        <button class="btn-qty" onclick="retirerDuPanier('${nom.replace(/'/g, "\\'")}')">
                                            <span
                                                class="material-symbols-rounded"
                                            >
                                                remove_circle
                                            </span>
                                        </button>
                                        <span>${item.quantite}</span>
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
        cartListElement.insertAdjacentHTML("beforeend", productHTML);
    }
}

displayPanier();

async function ajouterAuPanier(nomProduit) {
    const produit = trouverProduitDansInventaire(nomProduit);
    const stockDisponible = stockParNom.get(nomProduit.trim().toLowerCase()) ?? Infinity;

    if (!produit) {
        window.alert("Produit introuvable.");
        return;
    }

    if (panier[nomProduit]) {
        if (panier[nomProduit].quantite >= stockDisponible) {
            window.alert("Stock insuffisant pour ajouter ce produit.");
            return;
        }

        panier[nomProduit].quantite++;
    } else {
        const prixVente = await demanderPrixVente(nomProduit);
        if (prixVente === null) {
            return;
        }

        panier[nomProduit] = {
            quantite: 1,
            prixVente: prixVente,
        };
    }

    displayPanier();
}

function retirerDuPanier(nomProduit) {
    if (panier[nomProduit]) {
        panier[nomProduit].quantite--;
        if (panier[nomProduit].quantite <= 0) {
            delete panier[nomProduit];
        }
    }

    displayPanier();
}

async function modifierPrixPanier(nomProduit) {
    const item = panier[nomProduit];
    if (!item) {
        return;
    }

    const prix = await demanderPrixViaDialog(nomProduit, item.prixVente);
    if (prix === null) {
        return;
    }

    item.prixVente = prix;
    displayPanier();
}

async function publierProduitAuMarche(payload) {
    const responseAvecUserId = await fetch(`${API_URL}/market/ad`, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
    });

    if (responseAvecUserId.ok) {
        return responseAvecUserId;
    }

    const { userId, ...payloadSansUserId } = payload;
    const responseSansUserId = await fetch(`${API_URL}/market/ad`, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(payloadSansUserId),
    });

    return responseSansUserId;
}

async function mettrePanierEnVente() {
    if (utiliserModeFake()) {
        window.alert("Mode fake actif: la publication vers le marche n'est pas disponible.");
        return;
    }

    const lignesPanier = Object.entries(panier);
    if (lignesPanier.length === 0) {
        window.alert("Le panier est vide.");
        return;
    }

    if (!idUtilisateurCourant) {
        await chargerContextePublication();
    }

    let succes = 0;
    const erreurs = [];

    for (const [nomProduit, item] of lignesPanier) {
        const productId = obtenirIdProduitParNom(nomProduit);
        if (!Number.isFinite(productId)) {
            erreurs.push(`Produit introuvable dans l'API: ${nomProduit}`);
            continue;
        }

        const payload = {
            productId: productId,
            userId: idUtilisateurCourant,
            quantity: Number(item.quantite),
            unitPrice: Number(item.prixVente),
        };

        try {
            const response = await publierProduitAuMarche(payload);
            if (!response.ok) {
                erreurs.push(`Echec publication ${nomProduit} (${response.status})`);
                continue;
            }

            succes++;
            delete panier[nomProduit];
        } catch (erreur) {
            erreurs.push(`Erreur publication ${nomProduit}`);
        }
    }

    displayPanier();

    if (succes > 0 && erreurs.length === 0) {
        window.alert("Produits publies au marche avec succes.");
        return;
    }

    if (succes > 0 && erreurs.length > 0) {
        window.alert(
            `${succes} publication(s) reussie(s), ${erreurs.length} en echec.`,
        );
        return;
    }

    window.alert("Aucune publication n'a abouti.");
}

const boutonVente = document.getElementById("sellButton") ?? document.querySelector(".btn-sell");
boutonVente?.addEventListener("click", () => {
    void mettrePanierEnVente();
});

window.ajouterAuPanier = ajouterAuPanier;
window.retirerDuPanier = retirerDuPanier;
window.modifierPrixPanier = modifierPrixPanier;
window.modifierPrix = modifierPrix;