class AppBar extends HTMLElement {
    API_URL = window.apiUrl || "http://localhost:8080/api";

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        this.render();
        this.setupEventListeners();
    }

    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(";").shift();
    }

    async fetchUser() {
        try {
            const jwt = this.getCookie("jwt");

            if (!jwt) throw new Error();

            const user = await (
                await fetch(`${this.API_URL}/auth/me`, {
                    headers: new Headers({
                        Authorization: "Bearer " + jwt,
                    }),
                })
            ).json();

            const res = await fetch(`${this.API_URL}/users/id/${user.id}`, {
                headers: new Headers({
                    Authorization: "Bearer " + jwt,
                }),
            });
            return await res.json();
        } catch {
            window.location.href = "/";
        }
    }

    async logout() {
        const jwt = this.getCookie("jwt");

        let res = await fetch(`${this.API_URL}/auth/logout`, {
            method: "POST",
            credentials: "include",
            redirect: "manual",
            headers: new Headers({
                Authorization: "Bearer " + jwt,
            }),
        });
        console.log("Logout response:", res);
        if (res.ok || res.status === 0) {
            // Status= 0 when spring redirects to /login?logout
            window.location.href = "/";
        }
    }

    async render() {
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
        this.shadowRoot.appendChild(style);

        const template = document.createElement("template");
        template.innerHTML = `
        <div class="appbar">
            <div class="infos-left">
                <div>
                    <span class="material-symbols-rounded">leaderboard</span>
                    <span id="rank">-/-</span>
                </div>
                <div>
                    <span class="material-symbols-rounded">stars</span>
                    <span id="level">Niveau -</span>
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
                    <a href="#">
                        <span class="material-symbols-rounded">pause_circle</span>
                        <span>Hiberner</span>
                    </a>
                    <button id="logout">
                        <span class="material-symbols-rounded">logout</span>
                        <span>Déconnexion</span>
                    </button>
                </div>
            </div>
        </div>
        `;
        this.shadowRoot.appendChild(template.content.cloneNode(true));

        // Fetch user data
        const user = await this.fetchUser();
        // this.shadowRoot.querySelector("#rank").textContent =
        //     `${user.rank.current}/${user.rank.max}`;
        this.shadowRoot.querySelector("#level").textContent = this.shadowRoot
            .querySelector("#level")
            .textContent.replace("-", user.level);
        this.shadowRoot.querySelector("#username").textContent = user.name;
        this.shadowRoot.querySelector("#money-level").textContent = user.ecus;
        // Set as ready
        this.shadowRoot.querySelector(".appbar").classList.add("ready");
    }

    setupEventListeners() {
        const accountBtn = this.shadowRoot.getElementById("account");
        const accountMenu = this.shadowRoot.getElementById("account-menu");
        const logoutBtn = this.shadowRoot.getElementById("logout");

        accountBtn.addEventListener("click", () => {
            accountMenu.classList.toggle("open");
        });

        logoutBtn.addEventListener("click", () => {
            this.logout();
        });

        // Close menu if outside click
        document.addEventListener("click", (e) => {
            const path = e.composedPath();
            const clickedAccount = path.includes(accountBtn);
            const clickedMenu = path.includes(accountMenu);

            if (!clickedAccount && !clickedMenu) {
                accountMenu.classList.remove("open");
            }
        });
    }
}

customElements.define("app-bar", AppBar);
