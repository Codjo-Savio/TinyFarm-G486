class TfButton extends HTMLElement {
    static get observedAttributes() {
        return ["variant", "disabled", "icon", "size", "loading"];
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
                padding-left: 12px
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
                background-color: var(--color-surface-dark);
                color: var(--color-secondary);
                cursor: not-allowed;
            }

            button.loading > * {
                visibility: hidden;
            }

            button.loading::after {
                /* Adapted from https://css-loaders.com/dots/ */
                content: "";
                position: absolute;
                width: 40px;
                aspect-ratio: 1;
                --pattern: no-repeat radial-gradient(farthest-side, var(--color-secondary) 90%, #0000);
                background: var(--pattern), var(--pattern), var(--pattern);
                background-size: 25% 25%;
                animation: loading 1.6s infinite;
            }

            @keyframes loading {
                0% {
                    background-position:
                        0% -100%,
                        50% -100%,
                        100% -100%;
                }

                16.67% {
                    background-position:
                        0% 50%,
                        50% -100%,
                        100% -100%;
                }

                33.33% {
                    background-position:
                        0% 50%,
                        50% 50%,
                        100% -100%;
                }

                45%,
                55% {
                    background-position:
                        0% 50%,
                        50% 50%,
                        100% 50%;
                }

                66.67% {
                    background-position:
                        0% 200%,
                        50% 50%,
                        100% 50%;
                }

                83.33% {
                    background-position:
                        0% 200%,
                        50% 200%,
                        100% 50%;
                }

                100% {
                    background-position:
                        0% 200%,
                        50% 200%,
                        100% 200%;
                }
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <button part="button">
                <slot></slot>
            </button>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.buttonElement = this.shadowRoot.querySelector("button");
        this.rendered = true;
    }

    get variant() {
        return this.getAttribute("variant") || "primary";
    }

    get disabled() {
        return this.hasAttribute("disabled");
    }

    get loading() {
        return this.hasAttribute("loading");
    }

    get icon() {
        return this.getAttribute("icon") || null;
    }

    get size() {
        return this.getAttribute("size") || "normal";
    }

    update() {
        if (!this.buttonElement) {
            return;
        }

        this.buttonElement.disabled = this.disabled || this.loading;
        this.buttonElement.className =
            `${this.variant} ${this.size} ${this.loading ? "loading" : ""}`.trim();

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

customElements.define("tf-button", TfButton);
