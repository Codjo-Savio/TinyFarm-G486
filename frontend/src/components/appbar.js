class AppBar extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        this.render();
        this.setupEventListeners();
    }

    render() {
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
                <div id="rank">
                    <span class="material-symbols-rounded">leaderboard</span>
                    <span>${this.getAttribute("rank") || "10/100"}</span>
                </div>
                <div id="level">
                    <span class="material-symbols-rounded">stars</span>
                    <span>${this.getAttribute("level") || "Niveau 2"}</span>
                </div>
            </div>
            <div class="infos-right">
                <div id="money">
                    <span class="material-symbols-rounded">paid</span>
                    <span>${this.getAttribute("money") || "1000"}</span>
                </div>
                <div id="account">
                    <span>${this.getAttribute("username") || "Pascal"}</span>
                    <img src="${this.getAttribute("avatar") || "/assets/farmer-icon.png"}" alt="Avatar" />
                </div>
                <div id="account-menu">
                    <a href="${this.getAttribute("rules-link") || "/doc/rules?from=/dashboard"}">
                        <span class="material-symbols-rounded">book</span>
                        <span>Règles du jeu</span>
                    </a>
                    <a href="${this.getAttribute("hibernate-link") || "#"}">
                        <span class="material-symbols-rounded">pause_circle</span>
                        <span>Hiberner</span>
                    </a>
                    <a href="${this.getAttribute("logout-link") || "/"}">
                        <span class="material-symbols-rounded">logout</span>
                        <span>Déconnexion</span>
                    </a>
                </div>
            </div>
        </div>
        `;
        this.shadowRoot.appendChild(template.content.cloneNode(true));
    }

    setupEventListeners() {
        const accountBtn = this.shadowRoot.getElementById("account");
        const accountMenu = this.shadowRoot.getElementById("account-menu");

        accountBtn.addEventListener("click", () => {
            accountMenu.classList.toggle("open");
        });

        // Close menu if outside click
        document.addEventListener("click", (e) => {
            if (!this.contains(e.target)) {
                accountMenu.classList.remove("open");
            }
        });
    }
}

customElements.define("app-bar", AppBar);
