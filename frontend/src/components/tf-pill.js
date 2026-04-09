class TfPill extends HTMLElement {
    static get observedAttributes() {
        return ["variant", "icon"];
    }

    constructor() {
        super();
        this.attachShadow({ mode: "open" });

        this.shadowRoot.innerHTML = `
        <style>
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

            .tf-pill {
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

            .tf-pill.gold {
                background-color: var(--color-gold);
                color: var(--color-gold-dark);
            }
        </style>

        <div class="tf-pill">
            <slot></slot>
        </div>
        `;
        this.tfPill = this.shadowRoot.querySelector("div.tf-pill");
    }

    connectedCallback() {
        this.update();
    }

    attributeChangedCallback() {
        this.update();
    }

    get variant() {
        return this.getAttribute("variant") || "normal";
    }

    get icon() {
        return this.getAttribute("icon") || null;
    }

    update() {
        this.tfPill.className = `tf-pill ${this.variant}`;

        const currentIcon = this.tfPill.querySelector(
            ".material-symbols-rounded",
        );
        if (this.icon) {
            const icon = currentIcon ?? document.createElement("span");
            icon.classList.add("material-symbols-rounded");
            icon.textContent = this.icon;
            this.tfPill.prepend(icon);
        } else if (currentIcon) {
            this.tfPill.removeChild(currentIcon);
        }
    }
}

customElements.define("tf-pill", TfPill);
