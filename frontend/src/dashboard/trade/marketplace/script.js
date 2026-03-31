async function initialiserBoutique() {
    const container = document.getElementById('shop-container');

    try {
        const response = await fetch('../../../fakeapi/trade/marketplace.json');

        if (!response.ok) {
            throw new Error(`Erreur HTTP : ${response.status}`);
        }

        const inventaire = await response.json();
        container.innerHTML = "";

        for (const [nom, details] of Object.entries(inventaire)) {
            const productHTML = `
                <div class="product-row">
                    <div class="prod-info">
                        <span class="stock-badge">
                            <svg viewBox="0 0 32 32">
                                <path
                                    d="M19.9996 27.9998V21.3332C19.9996 20.9795 19.8591 20.6404 19.6091 20.3904C19.359 20.1403 19.0199 19.9998 18.6663 19.9998H13.3329C12.9793 19.9998 12.6402 20.1403 12.3901 20.3904C12.1401 20.6404 11.9996 20.9795 11.9996 21.3332V27.9998M23.6983 13.7465C23.4203 13.4804 23.0504 13.3319 22.6656 13.3319C22.2808 13.3319 21.9109 13.4804 21.6329 13.7465C21.013 14.3379 20.1891 14.6678 19.3323 14.6678C18.4755 14.6678 17.6516 14.3379 17.0316 13.7465C16.7537 13.4808 16.3841 13.3325 15.9996 13.3325C15.6151 13.3325 15.2455 13.4808 14.9676 13.7465C14.3476 14.3383 13.5234 14.6684 12.6663 14.6684C11.8092 14.6684 10.985 14.3383 10.3649 13.7465C10.087 13.4804 9.71706 13.3319 9.33228 13.3319C8.94749 13.3319 8.57756 13.4804 8.29961 13.7465C7.70074 14.318 6.91064 14.6462 6.08311 14.6672C5.25558 14.6883 4.44983 14.4007 3.82268 13.8603C3.19554 13.32 2.79187 12.5657 2.69026 11.7442C2.58864 10.9226 2.79635 10.0927 3.27294 9.41584L7.12494 3.83717C7.36935 3.47652 7.6984 3.18124 8.08331 2.97716C8.46823 2.77309 8.89728 2.66642 9.33294 2.6665H22.6663C23.1007 2.66634 23.5285 2.77229 23.9126 2.97515C24.2968 3.17802 24.6255 3.47165 24.8703 3.8305L28.7303 9.41984C29.207 10.0972 29.4144 10.9278 29.3122 11.7498C29.2099 12.5717 28.8053 13.3262 28.1772 13.8661C27.549 14.406 26.7424 14.6927 25.9144 14.6704C25.0864 14.648 24.2964 14.3182 23.6983 13.7452M5.33294 14.5998V25.3332C5.33294 26.0404 5.6139 26.7187 6.11399 27.2188C6.61409 27.7189 7.29237 27.9998 7.99961 27.9998H23.9996C24.7069 27.9998 25.3851 27.7189 25.8852 27.2188C26.3853 26.7187 26.6663 26.0404 26.6663 25.3332V14.5998"
                                    stroke-linecap="round" stroke-linejoin="round" />
                            </svg>
                            ${details.stock}
                        </span>
                        <span class="prod-name">
                            ${nom} 
                            <span class="seller" style="font-size: 0.8em; opacity: 0.7;">
                                • Vendu par ${details.seller}
                            </span>
                        </span>
                    </div>
                    <div class="prod-action">
                        <span class="price">$${details.price}</span>
                        <button class="btn-add" onclick="ajouterAuPanier('${nom.replace(/'/g, "\\'")}')">Ajouter</button>
                    </div>
                </div>
            `;

            container.insertAdjacentHTML('beforeend', productHTML);
        }

    } catch (erreur) {
        console.error("Erreur :", erreur);
        container.innerHTML = "<p>Inventaire indisponible.</p>";
    }
}

document.addEventListener('DOMContentLoaded', initialiserBoutique);

function ajouterAuPanier(nomProduit) {
    alert(`Vous avez ajouté : ${nomProduit}`);
}