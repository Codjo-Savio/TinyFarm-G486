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
        const template = document.createElement("template");
        template.innerHTML = `
      <link rel="stylesheet" href="/components/appbar/appbar.css">

      <div class="appbar">
        <div class="infos-left">
          <div id="rank">
            <span class="material-symbols-rounded">
              leaderboard
            </span>
            <span>${this.getAttribute("rank") || "10/100"}</span>
          </div>
          <div id="level">
            <span class="material-symbols-rounded">
              stars
            </span>
            <span>${this.getAttribute("level") || "Niveau 2"}</span>
          </div>
        </div>
        <div class="infos-right">
          <div id="money">
            <span class="material-symbols-rounded">
              paid
            </span>
            <span>${this.getAttribute("money") || "1000"}</span>
          </div>
          <div id="account">
            <span>${this.getAttribute("username") || "Pascal"}</span>
            <img src="${this.getAttribute("avatar") || "/assets/farmer-icon.png"}" alt="Avatar" />
          </div>
          <div id="account-menu">
            <a href="${this.getAttribute("rules-link") || "/doc/rules?from=/dashboard"}">
              <span class="material-symbols-rounded">
                book
              </span>
              <span>Règles du jeu</span>
            </a>
            <a href="${this.getAttribute("hibernate-link") || "#"}">
              <span class="material-symbols-rounded">
                pause_circle
              </span>
              <span>Hiberner</span>
            </a>
            <a href="${this.getAttribute("logout-link") || "/"}">
              <span class="material-symbols-rounded">
                logout
              </span>
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
