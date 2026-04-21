class TfAppBar extends HTMLElement {
    API_URL = window.apiUrl || "http://localhost:8080/api";

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.rendered = false;
        this.userLoadPromise = null;
        this.userLoaded = false;
        this.listenersAttached = false;
    }

    connectedCallback() {
        this.render();
        this.setupEventListeners();
        this.update();
    }

    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(";").shift();
    }

    async fetchUser() {
        try {
            const currentUserRes = await fetchApiWithCredentials("/auth/me");
            if (currentUserRes.status !== 200) throw new Error();
            return currentUserRes.json();
        } catch (e) {
            // window.location.href = "/";
            console.error(e);
            return null;
        }
    }

    async logout() {
        const jwt = this.getCookie("jwt");

        let res = await fetchApiWithCredentials("/auth/logout", "POST");
        // if (res.ok || res.status === 0) {
        //     // Status = 0 when Spring redirects to /login?logout
        //     window.location.href = "/";
        // }
    }

    showHibernateDialog() {
        this.accountMenu.classList.remove("open");
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

            .appbar > .infos-left > div {
                display: flex;
                align-items: center;
                gap: 12px;
            }

            .appbar > .infos-right {
                display: flex;
                gap: 12px;
                align-items: center;
                position: relative;
            }

            .appbar > .infos-right > #account {
                display: flex;
                gap: 12px;
                align-items: center;
                cursor: pointer;
            }

            .appbar > .infos-right > #account > img {
                height: 40px;
                width: auto;
                border-radius: 100px;
            }

            .appbar > .infos-right > #account-menu {
                background-color: var(--color-surface-dark);
                position: absolute;
                top: 64px;
                right: -16px;
                margin-top: 8px;
                border-radius: var(--radius);
                box-shadow: var(--shadow);
                display: flex;
                flex-direction: column;
                font-weight: normal;
                min-width: 264px;
                max-height: 0px;
                padding: 0px 6px;
                overflow: hidden;
                transition-duration: 0.3s;
                transform: translateY(-8px);
            }

            .appbar > .infos-right > #account-menu.open {
                max-height: 156px;
                padding: 6px;
                transform: translateY(0px);
            }

            .appbar > .infos-right > #account-menu > * {
                display: flex;
                gap: 16px;
                padding: 12px;
                border-radius: calc(var(--radius) - 6px);
                color: var(--color-primary);
                text-decoration: none;
                border: none;
                cursor: pointer;
                background-color: transparent;
                font: inherit;
                font-size: inherit;
            }

            .appbar > .infos-right > #account-menu > *:hover {
                background-color: var(--color-secondary);
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
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="appbar">
                <tf-dialog title="Mettre la ferme en hibernation" title-icon="person" modal>
                    Vous retrouverez votre ferme et vos animaux dans le même état qu'à votre départ lors de votre prochaine connexion.
                    Si votre compte hiberne pendant plus de 50 jours, il sera automatiquement supprimé.
                    <tf-button class="cancel" slot="cancel-button" variant="secondary">Annuler</tf-button>
                    <tf-button class="confirm" slot="confirm-button">Confirmer et me déconnecter</tf-button>
                </tf-dialog>
                <div class="infos-left">
                    <div>
                        <span class="material-symbols-rounded">leaderboard</span>
                        <span id="rank">-/-</span>
                    </div>
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
                    <div id="account">
                        <span id="username">-</span>
                        <img src="/assets/farmer-icon.png" alt="Avatar" />
                    </div>
                    <div id="account-menu">
                        <a href="/doc/rules?from=/dashboard">
                            <span class="material-symbols-rounded">book</span>
                            <span>Règles du jeu</span>
                        </a>
                        <button id="hibernate">
                            <span class="material-symbols-rounded">pause_circle</span>
                            <span>Hiberner</span>
                        </button>
                        <button id="logout">
                            <span class="material-symbols-rounded">logout</span>
                            <span>Déconnexion</span>
                        </button>
                    </div>
                </div>
            </div>
        `;

        let script = document.createElement("script");
        script.src = "/components/tf-dialog.js";
        document
            .getElementsByTagName("head")[0]
            .appendChild(script.cloneNode());
        script.src = "/utils/fetch.js";
        document
            .getElementsByTagName("head")[0]
            .appendChild(script.cloneNode());

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.appbarElement = this.shadowRoot.querySelector(".appbar");
        this.rankElement = this.appbarElement.querySelector("#rank");
        this.levelElement = this.appbarElement.querySelector("#level");
        this.usernameElement = this.appbarElement.querySelector("#username");
        this.moneyLevelElement =
            this.appbarElement.querySelector("#money-level");
        this.accountButton = this.appbarElement.querySelector("#account");
        this.accountMenu = this.appbarElement.querySelector("#account-menu");
        this.logoutButton = this.accountMenu.querySelector("#logout");
        this.hibernateButton = this.accountMenu.querySelector("#hibernate");
        this.dialogElement = this.shadowRoot.querySelector("tf-dialog");
        this.dialogCancelButton = this.dialogElement.querySelector(".cancel");
        this.dialogConfirmButton = this.dialogElement.querySelector(".confirm");
        this.rendered = true;
    }

    async update() {
        if (!this.levelElement || this.userLoaded) {
            return;
        }

        this.user = await this.fetchUser();
        if (!this.user) {
            return;
        }

        this.levelElement.textContent = `Niveau ${this.user.level}`;
        this.usernameElement.textContent = this.user.name;
        this.moneyLevelElement.textContent = this.user.ecus;
        this.appbarElement.classList.add("ready");
        this.userLoaded = true;
    }

    setupEventListeners() {
        if (
            this.listenersAttached ||
            !this.accountButton ||
            !this.accountMenu ||
            !this.logoutButton ||
            !this.hibernateButton ||
            !this.dialogCancelButton ||
            !this.dialogConfirmButton
        ) {
            return;
        }

        this.accountButton.addEventListener("click", () => {
            this.accountMenu.classList.toggle("open");
        });

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

        // Close menu if outside click
        document.addEventListener("click", (e) => {
            const path = e.composedPath();
            const clickedAccount = path.includes(this.accountButton);
            const clickedMenu = path.includes(this.accountMenu);

            if (!clickedAccount && !clickedMenu) {
                this.accountMenu.classList.remove("open");
            }
        });

        this.listenersAttached = true;
    }
}

customElements.define("tf-app-bar", TfAppBar);
