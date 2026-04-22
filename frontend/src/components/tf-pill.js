class TfPill extends HTMLElement {
    static get observedAttributes() {
        return ["variant", "icon"];
    }

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.rendered = false;
    }

    connectedCallback() {
        this.render();
        this.update();
    }

    attributeChangedCallback() {
        this.update();
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
                font-size: 22px;
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

            :host {
                display: inline-block;
            }

            .pill {
                border-radius: 100px;
                display: flex;
                gap: 6px;
                align-items: center;
                height: fit-content;
                padding: 8px 16px 8px 8px;
                background-color: var(--color-secondary);
                color: var(--color-primary);
                line-height: 1;
                font-weight: bold;
            }

            .pill.gold {
                background-color: var(--color-gold);
                color: var(--color-gold-dark);
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="pill">
                <slot></slot>
            </div>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.pillElement = this.shadowRoot.querySelector("div.pill");
        this.rendered = true;
    }

    get variant() {
        return this.getAttribute("variant") || "normal";
    }

    get icon() {
        return this.getAttribute("icon") || null;
    }

    update() {
        if (!this.pillElement) {
            return;
        }

        this.pillElement.className = `pill ${this.variant}`;

        const currentIcon = this.pillElement.querySelector(
            ".material-symbols-rounded",
        );
        if (this.icon) {
            const icon = currentIcon ?? document.createElement("span");
            icon.classList.add("material-symbols-rounded");
            icon.textContent = this.icon;
            this.pillElement.prepend(icon);
        } else if (currentIcon) {
            this.pillElement.removeChild(currentIcon);
        }
    }
}

customElements.define("tf-pill", TfPill);
