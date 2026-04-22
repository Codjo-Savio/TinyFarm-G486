class TfMenuEntry extends HTMLElement {
    static get observedAttributes() {
        return ["icon", "href", "disabled"];
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
                display: block;
            }

            button {
                width: 100%;
                display: flex;
                align-items: center;
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
                text-align: left;
            }

            button:hover:not(:disabled) {
                background-color: var(--color-secondary);
            }

            button:disabled {
                cursor: not-allowed;
                color: var(--color-secondary);
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <button type="button">
                <span class="material-symbols-rounded"></span>
                <slot></slot>
            </button>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.buttonElement = this.shadowRoot.querySelector("button");
        this.buttonElement.addEventListener("click", () => {
            if (this.disabled) {
                return;
            }

            if (this.href) {
                window.location.href = this.href;
            }
        });
        this.rendered = true;
    }

    get icon() {
        return this.getAttribute("icon") || null;
    }

    get href() {
        return this.getAttribute("href") || null;
    }

    get disabled() {
        return this.hasAttribute("disabled");
    }

    update() {
        if (!this.buttonElement) {
            return;
        }

        this.buttonElement.disabled = this.disabled;
        const currentIcon = this.buttonElement.querySelector(
            ".material-symbols-rounded",
        );
        if (this.icon) {
            const icon = currentIcon ?? document.createElement("span");
            icon.classList.add("material-symbols-rounded");
            icon.textContent = this.icon;
            this.buttonElement.prepend(icon);
        } else if (currentIcon) {
            this.buttonElement.removeChild(currentIcon);
        }
    }
}

customElements.define("tf-menu-entry", TfMenuEntry);
