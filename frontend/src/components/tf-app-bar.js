import { fetchApiWithCredentials, loadScriptIfNeeded } from "/utils/fetch.js";

class TfAppBar extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.rendered = false;
        this.userLoadPromise = null;
        this.listenersAttached = false;
    }

    connectedCallback() {
        this.render();
        this.setupEventListeners();
        this.update();
    }

    async fetchUser() {
        try {
            const currentUserRes = await fetchApiWithCredentials("/auth/me");
            if (currentUserRes.status !== 200) throw new Error();
            const authUser = await currentUserRes.json();

            const fullUserRes = await fetchApiWithCredentials(
                `/users/id/${authUser.id}`,
            );
            if (fullUserRes.status !== 200) throw new Error();
            return fullUserRes.json();
        } catch (e) {
            window.location.href = "/";
            return null;
        }
    }

    async fetchUserRank() {
        if (!this.user) return;

        const rankRes = await fetchApiWithCredentials("/ranking");
        const ranking = await rankRes.json();

        return {
            user: ranking.find((user) => user.uid === this.user.id).rank,
            total: ranking.length,
        };
    }

    async logout() {
        const res = await fetchApiWithCredentials("/auth/logout", "POST");
        if (res.ok) {
            window.location.href = "/";
        } else {
            console.error("Cannot logout the user");
            console.erreur(await res.text());
        }
    }

    showHibernateDialog() {
        this.accountMenu.closeMenu();
        this.dialogElement.setAttribute("show", "");
    }

    async hibernate() {
        await fetchApiWithCredentials(
            `/users/hibernate/id/${this.user.id}`,
            "PATCH",
        );
        this.logout();
    }

    render() {
        if (this.rendered) {
            return;
        }

        const style = document.createElement("style");
        style.textContent = `
            .material-symbols-rounded {
                font-family: "Material Symbols Rounded";
                font-weight: normal;
                font-style: normal;
                font-size: 24px;
                line-height: 1;
                letter-spacing: normal;
                text-transform: none;
                display: inline-block;
                white-space: nowrap;
                word-wrap: normal;
                direction: ltr;
                -webkit-font-feature-settings: "liga";
                -webkit-font-smoothing: antialiased;
                font-variation-settings: var(--font-var-icon);
            }

            .appbar {
                background-color: var(--color-primary);
                box-shadow: var(--shadow);
                position: relative;
                z-index: 1;
                display: flex;
                gap: 24px;
                padding: 24px;
                color: var(--color-surface);
                font-weight: bold;
                justify-content: space-between;
                align-items: center;
            }

            .appbar>* {
                opacity: 0;
                transition: opacity .3s;
            }

            .appbar.ready>* {
                opacity: 1;
            }

            .appbar > .infos-left {
                display: flex;
                gap: 24px;
            }

            .appbar > .infos-left > * {
                display: flex;
                align-items: center;
                gap: 12px;
                text-decoration: none;
                color: var(--color-surface);
            }

            .appbar > .infos-right {
                display: flex;
                gap: 12px;
                align-items: center;
                position: relative;
            }

            .appbar > .infos-right > tf-menu#account-menu > #account {
                display: flex;
                gap: 12px;
                align-items: center;
                cursor: pointer;
            }

            .appbar > .infos-right > tf-menu#account-menu > #account > img {
                height: 40px;
                width: auto;
                border-radius: 100px;
            }

            .appbar > .infos-right > #money {
                border-radius: 100px;
                display: flex;
                gap: 6px;
                align-items: center;
                height: fit-content;
                padding: 6px 16px 6px 6px;
                background-color: var(--color-gold);
                color: var(--color-gold-dark);
                line-height: 1;
            }

            .appbar {
                z-index: 99;
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="appbar">
                <tf-dialog title="Mettre la ferme en hibernation" title-icon="pause_circle" modal>
                    Vous retrouverez votre ferme et vos animaux dans le même état qu'à votre départ lors de votre prochaine connexion.
                    Si votre compte hiberne pendant plus de 50 jours, il sera automatiquement supprimé.
                    <tf-button class="cancel" slot="cancel-button" variant="secondary">Annuler</tf-button>
                    <tf-button class="confirm" slot="confirm-button">Confirmer et me déconnecter</tf-button>
                </tf-dialog>
                <div class="infos-left">
                    <a href="/">
                        <span class="material-symbols-rounded">leaderboard</span>
                        <span id="rank">-/-</span>
                    </a>
                    <div>
                        <span class="material-symbols-rounded">stars</span>
                        <span id="level"></span>
                    </div>
                </div>
                <div class="infos-right">
                    <div id="money">
                        <span class="material-symbols-rounded">paid</span>
                        <span id="money-level">-</span>
                    </div>
                    <tf-menu id="account-menu" offset-x="-151" offset-y="32">
                        <div id="account" slot="menu-target">
                            <span id="username">-</span>
                            <img src="/assets/farmer-icon.png" alt="Avatar" />
                        </div>
                        <tf-menu-group slot="group">
                            <tf-menu-entry
                                id="rules"
                                slot="entry"
                                icon="book"
                                href="/doc/rules"
                            >
                                Règles du jeu
                            </tf-menu-entry>
                        </tf-menu-group>
                        <tf-menu-group slot="group" legend="Compte">
                            <tf-menu-entry
                                id="hibernate"
                                slot="entry"
                                icon="pause_circle"
                            >
                                Hiberner
                            </tf-menu-entry>
                            <tf-menu-entry
                                id="logout"
                                slot="entry"
                                icon="logout"
                            >
                                Déconnexion
                            </tf-menu-entry>
                        </tf-menu-group>
                    </tf-menu>
                </div>
            </div>
        `;

        loadScriptIfNeeded("/components/tf-menu.js");
        loadScriptIfNeeded("/components/tf-dialog.js");
        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.appbarElement = this.shadowRoot.querySelector(".appbar");
        this.rankElement = this.appbarElement.querySelector("#rank");
        this.levelElement = this.appbarElement.querySelector("#level");
        this.usernameElement = this.appbarElement.querySelector("#username");
        this.moneyLevelElement =
            this.appbarElement.querySelector("#money-level");
        this.accountMenu = this.appbarElement.querySelector("#account-menu");
        this.logoutButton = this.accountMenu.querySelector("#logout");
        this.hibernateButton = this.accountMenu.querySelector("#hibernate");
        this.dialogElement = this.shadowRoot.querySelector("tf-dialog");
        this.dialogCancelButton = this.dialogElement.querySelector(".cancel");
        this.dialogConfirmButton = this.dialogElement.querySelector(".confirm");
        this.rendered = true;
    }

    async update(force = false) {
        if (!this.levelElement || (this.userLoaded && !force)) {
            return;
        }

        this.user = await this.fetchUser();
        if (!this.user) {
            return;
        }

        this.levelElement.textContent = `Niveau ${this.user.level}`;
        this.usernameElement.textContent = this.user.name;
        this.moneyLevelElement.textContent = this.user.ecus;
        try {
            const rankInfos = await this.fetchUserRank();
            this.rankElement.textContent = `${rankInfos.user}/${rankInfos.total}`;
        } finally {
            this.appbarElement.classList.add("ready");
        }
    }

    setupEventListeners() {
        if (
            this.listenersAttached ||
            !this.accountMenu ||
            !this.logoutButton ||
            !this.hibernateButton ||
            !this.dialogCancelButton ||
            !this.dialogConfirmButton
        ) {
            return;
        }

        this.logoutButton.addEventListener("click", () => {
            this.logout();
        });

        this.hibernateButton.addEventListener("click", () => {
            this.showHibernateDialog();
        });

        this.dialogCancelButton.addEventListener("click", () => {
            this.dialogElement.removeAttribute("show");
        });

        this.dialogConfirmButton.addEventListener("click", () => {
            this.hibernate();
        });

        window.addEventListener("refresh-user-data", () => {
            this.update(true);
        });

        this.listenersAttached = true;
    }
}

customElements.define("tf-app-bar", TfAppBar);
