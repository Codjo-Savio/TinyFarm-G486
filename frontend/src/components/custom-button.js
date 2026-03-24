class CustomButton extends HTMLElement {
    static get observedAttributes() {
        return ["variant", "disabled", "icon", "size"];
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
                font-size: 20px;
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

            button {
                font-family: inherit;
                font-weight: 600;
                font-size: 18px;
                height: 45px;
                padding: 0 16px;
                border-radius: 10px;
                border: none;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
            }

            button.large {
                height: 55px;
                padding: 0 32px;
                border-radius: 12px;
            }

            button:has(.material-symbols-rounded) {
                padding-left: 10px
            }

            button.primary {
                background-color: var(--color-primary);
                color: var(--color-secondary);
            }

            button.primary:hover:not(:disabled) {
                background-color: var(--color-primary-dark);
            }

            button.secondary {
                background-color: var(--color-secondary);
                color: var(--color-primary);
            }

            button.secondary:hover:not(:disabled) {
                background-color: var(--color-primary);
                color: var(--color-secondary);
            }

            button:disabled {
                background-color: transparent;
                color: var(--color-secondary);
                cursor: not-allowed;
            }
        </style>

        <button part="button">
            <slot></slot>
        </button>
        `;
        this.button = this.shadowRoot.querySelector("button");
    }

    connectedCallback() {
        this.update();
    }

    attributeChangedCallback() {
        this.update();
    }

    get variant() {
        return this.getAttribute("variant") || "primary";
    }

    get disabled() {
        return this.hasAttribute("disabled");
    }

    get icon() {
        return this.getAttribute("icon") || null;
    }

    get size() {
        return this.getAttribute("size") || "normal";
    }

    update() {
        this.button.disabled = this.disabled;
        this.button.className = `${this.variant} ${this.size}`;

        const currentIcon = this.button.querySelector(
            ".material-symbols-rounded",
        );
        if (this.icon) {
            const icon = currentIcon ?? document.createElement("span");
            icon.classList.add("material-symbols-rounded");
            icon.textContent = this.icon;
            this.button.prepend(icon);
        } else if (currentIcon) {
            this.button.removeChild(currentIcon);
        }
    }
}

customElements.define("custom-button", CustomButton);
