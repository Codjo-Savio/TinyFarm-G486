import { fetchApiWithCredentials, loadScriptIfNeeded } from "/utils/fetch.js";

/* =========================
 * Etat global
 * ========================= */

const snackbarElement = document.querySelector("tf-snackbar");
let inventaire = {
    products: [],
    stocks: [],
};
const panier = {};
let idUtilisateurCourant = null;
const idProduitParNom = new Map();
const stockParNom = new Map();
const prixParNom = new Map();

let dialoguePrixVente = null;

/* =========================
 * Utilitaires
 * ========================= */

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

function estProduitReserveALaCooperative(nomProduit) {
    const description = String(nomProduit).toLowerCase();
    return description.includes("egg") || description.includes("oeuf");
}

function obtenirCanalVente(nomProduit) {
    return estProduitReserveALaCooperative(nomProduit)
        ? "cooperative"
        : "market";
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

/* =========================
 * Dialogue prix
 * ========================= */

function obtenirDialoguePrixVente() {
    if (dialoguePrixVente) {
        return dialoguePrixVente;
    }

    loadScriptIfNeeded("/components/tf-dialog.js");

    const dialogue = document.createElement("tf-dialog");
    dialogue.setAttribute("modal", "");
    dialogue.setAttribute("title", "Prix de vente");
    dialogue.setAttribute("title-icon", "sell");
    dialogue.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:8px;">
            <label for="sale-price-input" style="font-weight:600;">Prix unitaire</label>
            <input id="sale-price-input" type="number" min="0" step="0.01" style="padding:10px 12px; border-radius:10px; border:1px solid rgba(0,0,0,.2); font-size:16px;" />
        </div>
        <tf-button slot="cancel-button" variant="secondary">Annuler</tf-button>
        <tf-button slot="confirm-button" variant="primary">Valider</tf-button>
    `;

    document.body.appendChild(dialogue);
    dialoguePrixVente = dialogue;
    return dialogue;
}

function demanderPrixViaDialogue(nomProduit, valeurInitiale) {
    const dialogue = obtenirDialoguePrixVente();
    dialogue.setAttribute("title", `Prix de vente - ${nomProduit}`);

    const input = dialogue.querySelector("#sale-price-input");
    const boutonAnnuler = dialogue.querySelector('[slot="cancel-button"]');
    const boutonValider = dialogue.querySelector('[slot="confirm-button"]');

    if (!input || !boutonAnnuler || !boutonValider) {
        return Promise.resolve(null);
    }

    input.value = Number.isFinite(valeurInitiale)
        ? normaliserNombre(valeurInitiale).toFixed(2)
        : "";

    dialogue.setAttribute("show", "");

    return new Promise((resolve) => {
        const fermer = (resultat) => {
            dialogue.removeAttribute("show");
            input.removeEventListener("keydown", gererClavier);
            boutonAnnuler.removeEventListener("click", annuler);
            boutonValider.removeEventListener("click", valider);
            resolve(resultat);
        };

        const annuler = () => fermer(null);

        const valider = () => {
            const prix = Math.round(Number.parseFloat(input.value) * 100) / 100;
            if (!Number.isFinite(prix) || prix < 0) {
                window.alert(
                    "Prix invalide. Merci d'entrer un nombre positif.",
                );
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

/* =========================
 * Chargement des donnees
 * ========================= */

async function chargerInventaireReel() {
    const [meResponse, productsResponse] = await Promise.all([
        fetchApiWithCredentials("/auth/me"),
        fetchApiWithCredentials("/products"),
    ]);

    if (!meResponse.ok) {
        throw new Error(
            `Impossible de récupérer l'utilisateur connecté (${meResponse.status})`,
        );
    }

    if (!productsResponse.ok) {
        throw new Error(
            `Impossible de récupérer les produits (${productsResponse.status})`,
        );
    }

    const utilisateur = await meResponse.json();
    idUtilisateurCourant = Number(utilisateur?.id) || null;

    if (!idUtilisateurCourant) {
        throw new Error("Utilisateur connecté invalide");
    }

    const jsonProducts = await productsResponse.json();
    const products = Array.isArray(jsonProducts) ? jsonProducts : [];

    if (products.length === 0) {
        throw new Error("Aucun produit n'a été renvoyé par l'API");
    }

    const stocksResponse = await fetchApiWithCredentials(
        `/stocks/user/${idUtilisateurCourant}`,
    );
    if (!stocksResponse.ok) {
        throw new Error(
            `Impossible de récupérer les stocks (${stocksResponse.status})`,
        );
    }

    const jsonStocks = await stocksResponse.json();
    const stocks = Array.isArray(jsonStocks) ? jsonStocks : [];

    return { products, stocks };
}

/* =========================
 * Indexation des donnees
 * ========================= */

function indexerInventaire(source) {
    idProduitParNom.clear();
    stockParNom.clear();

    const produits = normaliserListe(source, "products");
    const produitsParId = new Map();
    for (const produit of produits) {
        const idProduit = normaliserNombre(produit?.id);
        const nomProduit =
            typeof produit?.description === "string"
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
        const idProduit = normaliserNombre(
            stock?.productId ?? stock?.id?.productID,
        );
        const quantite = normaliserNombre(stock?.quantity);

        if (idProduit <= 0) {
            continue;
        }

        const produit = produitsParId.get(idProduit);
        const nomProduit =
            typeof produit?.description === "string"
                ? produit.description.trim()
                : `Produit ${idProduit}`;

        stockParNom.set(nomProduit.toLowerCase(), quantite);

        if (!idProduitParNom.has(nomProduit)) {
            idProduitParNom.set(nomProduit, idProduit);
        }
    }
}

/* =========================
 * Affichage catalogue
 * ========================= */

async function initialiserBoutique() {
    const conteneur = document.getElementById("shop-container");

    try {
        const inventaireRecu = await chargerInventaireReel();
        inventaire = normaliserInventaireRecu(inventaireRecu);

        indexerInventaire(inventaire);

        const produits = normaliserListe(inventaire, "products");
        const stocks = normaliserListe(inventaire, "stocks");
        const stockParId = new Map();

        for (const stock of stocks) {
            const idProduit = normaliserNombre(
                stock?.productId ?? stock?.id?.productID,
            );
            if (idProduit <= 0) {
                continue;
            }

            stockParId.set(idProduit, normaliserNombre(stock?.quantity));
        }

        conteneur.innerHTML = "";

        const produitsAffichables =
            produits.length > 0
                ? produits
                : [...stockParId.keys()].map((idProduit) => ({
                      id: idProduit,
                      description: `Produit ${idProduit}`,
                      price: 0,
                  }));

        inventaire.products = produitsAffichables;

        if (produitsAffichables.length === 0) {
            afficherEtatInventaireVide(conteneur);
            return;
        }

        const catalogueHTML = [];

        for (const produit of produitsAffichables) {
            const idProduit = normaliserNombre(produit?.id);
            if (idProduit <= 0) {
                continue;
            }

            const nomProduit =
                typeof produit?.description === "string"
                    ? produit.description.trim()
                    : `Produit ${idProduit}`;
            const valeurs = {
                quantity: stockParId.get(idProduit) ?? 0,
                price: normaliserNombre(produit?.price),
                canal: obtenirCanalVente(nomProduit),
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
                        <span class="prod-market">${
                            valeurs.canal === "cooperative"
                                ? " • Coopérative"
                                : " • Marché"
                        }</span>
                    </div>
                    <div class="prod-action">
                        <tf-button variant="primary" ${valeurs.quantity <= 0 ? "disabled" : ""} onclick="ajouterAuPanier('${nomEchappe}')">Ajouter</tf-button>
                    </div>
                </div>
            `;
            catalogueHTML.push(htmlProduit);
        }

        conteneur.insertAdjacentHTML("beforeend", catalogueHTML.join(""));
    } catch (erreur) {
        console.error("Impossible de charger l'inventaire :", erreur);
        if (snackbarElement) {
            snackbarElement.showSnackbar(
                "Erreur lors du chargement des données de gestion.",
                false,
            );
        }
        conteneur.innerHTML = "<p>Erreur lors du chargement des produits.</p>";
    }
}

document.addEventListener("DOMContentLoaded", initialiserBoutique);

/* =========================
 * Contexte publication
 * ========================= */

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
                    const nom =
                        typeof produit?.description === "string"
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

/* =========================
 * Metier panier
 * ========================= */

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
    const produits = Array.isArray(inventaire.products)
        ? inventaire.products
        : [];
    const trouve =
        produits.find((produit) => {
            const description =
                typeof produit?.description === "string"
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
    return await demanderPrixViaDialogue(nomProduit, prixDefaut);
}

function afficherPanier() {
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
        const estCooperatif = item.canal === "cooperative";
        const productHTML = `
                            <div class="cart-item">
                                    <span>${nom}</span>
                                    <span>${
                                        estCooperatif
                                            ? "Prix coopérative: fixé automatiquement"
                                            : `Prix unit.: $${item.prixVente.toFixed(2)}`
                                    }</span>
                                    <div class="qty-control">
                                        ${
                                            estCooperatif
                                                ? ""
                                                : `<button class="btn-qty" onclick="modifierPrixPanier('${nom.replace(/'/g, "\\'")}')">
                                            <span class="material-symbols-rounded">edit</span>
                                        </button>`
                                        }
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

afficherPanier();

async function ajouterAuPanier(nomProduit) {
    const produit = trouverProduitDansInventaire(nomProduit);
    const stockDisponible =
        stockParNom.get(nomProduit.trim().toLowerCase()) ?? Infinity;

    if (!produit) {
        snackbarElement.showSnackbar("Produit introuvable.", false);
        return;
    }

    if (panier[nomProduit]) {
        if (panier[nomProduit].quantite >= stockDisponible) {
            snackbarElement.showSnackbar(
                "Stock insuffisant pour ajouter ce produit.",
                false,
            );
            return;
        }

        panier[nomProduit].quantite++;
    } else {
        const canal = obtenirCanalVente(nomProduit);
        const prixVente =
            canal === "cooperative"
                ? null
                : await demanderPrixVente(nomProduit);

        if (canal !== "cooperative" && prixVente === null) {
            return;
        }

        panier[nomProduit] = {
            quantite: 1,
            prixVente: prixVente,
            canal: canal,
        };
    }

    afficherPanier();
}

function retirerDuPanier(nomProduit) {
    if (panier[nomProduit]) {
        panier[nomProduit].quantite--;
        if (panier[nomProduit].quantite <= 0) {
            delete panier[nomProduit];
        }
    }

    afficherPanier();
}

async function modifierPrixPanier(nomProduit) {
    const item = panier[nomProduit];
    if (!item) {
        return;
    }

    if (item.canal === "cooperative") {
        snackbarElement.showSnackbar(
            "Le prix des ventes à la coopérative est fixé automatiquement.",
            false,
        );
        return;
    }

    const prix = await demanderPrixViaDialogue(nomProduit, item.prixVente);
    if (prix === null) {
        return;
    }

    item.prixVente = prix;
    afficherPanier();
}

/* =========================
 * Publication marche
 * ========================= */

async function supprimerAncienneOffreMarche(userId, productId) {
    try {
        const deleteResponse = await fetchApiWithCredentials(
            `/market/${userId}/${productId}`,
            "DELETE",
        );
        if (deleteResponse.ok || deleteResponse.status === 404) {
            console.log(
                `[Suppression] Ancienne offre supprimée (ID=${productId})`,
            );
            return true;
        }
        console.warn(`[Suppression] Échec: ${deleteResponse.status}`);
        return false;
    } catch (erreur) {
        console.warn(`[Suppression] Erreur: ${erreur.message}`);
        return false;
    }
}

async function publierProduitAuMarche(payload) {
    // Si userId est fourni, supprimer l'ancienne offre d'abord
    if (payload.userId) {
        await supprimerAncienneOffreMarche(payload.userId, payload.productId);
    }

    const responseAvecUserId = await fetchApiWithCredentials(
        "/market/ad",
        "POST",
        payload,
    );

    if (responseAvecUserId.ok) {
        return responseAvecUserId;
    }

    const { userId, ...payloadSansUserId } = payload;
    const responseSansUserId = await fetchApiWithCredentials(
        "/market/ad",
        "POST",
        payloadSansUserId,
    );

    return responseSansUserId;
}

async function publierProduitALaCooperative(payload) {
    return await fetchApiWithCredentials("/cooperative/sell", "POST", payload);
}

async function mettrePanierEnVente() {
    const lignesPanier = Object.entries(panier);
    if (lignesPanier.length === 0) {
        snackbarElement.showSnackbar(
            "Vous n'avez pas ajouté d'articles à mettre en vente.",
            false,
        );
        return;
    }

    if (!idUtilisateurCourant) {
        await chargerContextePublication();
    }

    // Fusionner les lignes du même produit (par productId)
    const panierParProductId = new Map();
    for (const [nomProduit, item] of lignesPanier) {
        const productId = obtenirIdProduitParNom(nomProduit);
        if (!Number.isFinite(productId)) {
            console.warn(
                `[Panier] Produit "${nomProduit}" introuvable dans l'API`,
            );
            continue;
        }

        const existant = panierParProductId.get(productId);
        if (existant) {
            // Produit déjà dans la map, fusionner les quantités
            console.log(
                `[Panier] Fusion: ${nomProduit} (ID=${productId}), ancien=${existant.quantite}, nouveau=+${item.quantite}`,
            );
            existant.quantite += item.quantite;
        } else {
            // Nouveau produit
            console.log(
                `[Panier] Ajout: ${nomProduit} (ID=${productId}), quantité=${item.quantite}, canal=${item.canal || obtenirCanalVente(nomProduit)}`,
            );
            panierParProductId.set(productId, {
                nomProduit,
                productId,
                quantite: item.quantite,
                prixVente: item.prixVente,
                canal: item.canal || obtenirCanalVente(nomProduit),
            });
        }
    }

    let succes = 0;
    const erreurs = [];

    for (const item of panierParProductId.values()) {
        const { nomProduit, productId, quantite, prixVente, canal } = item;

        try {
            console.log(
                `[Publication] "${nomProduit}" (ID=${productId}): quantité=${quantite}, canal=${canal}, userId=${idUtilisateurCourant}`,
            );

            const response =
                canal === "cooperative"
                    ? await publierProduitALaCooperative({
                          sellerId: idUtilisateurCourant,
                          productId: productId,
                          quantity: Number(quantite),
                      })
                    : await publierProduitAuMarche({
                          productId: productId,
                          userId: idUtilisateurCourant,
                          quantity: Number(quantite),
                          unitPrice: Number(prixVente),
                      });

            console.log(
                `[Publication] Réponse: ${response.status} ${response.statusText}`,
            );

            if (!response.ok) {
                const errorBody = await response.text();
                console.error(`[Publication] Erreur body: ${errorBody}`);
                erreurs.push(
                    `Echec publication ${nomProduit} (${response.status}): ${errorBody || "Erreur inconnue"}`,
                );
                continue;
            }

            console.log(`[Publication] ✓ "${nomProduit}" publié avec succès`);
            succes++;
            delete panier[nomProduit];
        } catch (erreur) {
            console.error(
                `[Publication] Exception pour ${nomProduit}:`,
                erreur,
            );
            erreurs.push(`Erreur publication ${nomProduit}: ${erreur.message}`);
        }
    }

    afficherPanier();

    if (succes > 0 && erreurs.length === 0) {
        snackbarElement.showSnackbar("Produits publiés au marché avec succès.");
        // Rafraîchir l'inventaire pour mettre à jour les stocks
        console.log("[Rafraîchissement] Actualisation de l'inventaire...");
        setTimeout(() => {
            initialiserBoutique();
        }, 500);
        return;
    }

    if (succes > 0 && erreurs.length > 0) {
        snackbarElement.showSnackbar(
            `${succes} publication(s) réussie(s), ${erreurs.length} en échec.`,
            false,
        );
        erreurs.forEach((e) => console.error(`[Erreur] ${e}`));
        // Rafraîchir même en cas d'erreurs partielles
        console.log(
            "[Rafraîchissement] Actualisation de l'inventaire après erreurs partielles...",
        );
        setTimeout(() => {
            initialiserBoutique();
        }, 500);
        return;
    }

    snackbarElement.showSnackbar("Aucune publication n'a abouti.", false);
    erreurs.forEach((e) => console.error(`[Erreur] ${e}`));
}

const boutonVente =
    document.getElementById("sellButton") ??
    document.querySelector(".btn-sell");
boutonVente?.addEventListener("click", () => {
    void mettrePanierEnVente();
});

/* =========================
 * Exposition globale
 * ========================= */

window.ajouterAuPanier = ajouterAuPanier;
window.retirerDuPanier = retirerDuPanier;
window.modifierPrixPanier = modifierPrixPanier;
