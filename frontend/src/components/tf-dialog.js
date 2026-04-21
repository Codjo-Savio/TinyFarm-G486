class TfDialog extends HTMLElement {
    static get observedAttributes() {
        return ["modal", "title", "title-icon", "show"];
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
                position: fixed;
            }

            .dialog {
                pointer-events: none;
            }

            .dialog.show {
                pointer-events: all;
            }

            .scrim {
                position: fixed;
                top: 0;
                left: 0;
                width: 100vw;
                height: 100vh;
                background-color: rgba(0, 0, 0, 0.5);
                opacity: 0;
                transition: opacity .3s;
            }

            .dialog.show > .scrim {
                opacity: 1;
            }

            .window {
                position: fixed;
                left: 50%;
                top: 50%;
                transform: translateX(-50%) translateY(calc(-50% + 40px));
                min-width: 500px;
                min-height: 200px;
                background-color: var(--color-surface-dark);
                border-radius: var(--radius);
                padding: 24px;
                display: flex;
                flex-direction: column;
                gap: 24px;
                opacity: 0;
                transition: opacity .1s, transform .3s;
            }

            .dialog.show .window {
                opacity: 1;
                transform: translateX(-50%) translateY(-50%);
            }

            .window > .header {
                display: flex;
                align-items: center;
                gap: 12px;
            }

            .header > .material-symbols-rounded {
                font-size: var(--font-size-icon-large);
                font-variation-settings: var(--font-var-icon-large);
            }

            .window > .body {
                flex-grow: 1;
            }

            .window > .footer {
                display: flex;
                align-items: center;
                justify-content: end;
                gap: 12px;
            }

            .window > .footer.hidden {
                display: none;
            }

            .title {
                font-size: var(--font-size-subtitle);
                font-weight: var(--font-weight-subtitle);
                margin-block: 0;
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="dialog">
                <div class="scrim"></div>
                <div class="window">
                    <div class="header">
                        <h2 class="title"></h2>
                    </div>
                    <div class="body">
                        <slot></slot>
                    </div>
                    <div class="footer">
                        <slot name="cancel-button">
                            <tf-button variant="secondary">Annuler</tf-button>
                        </slot>
                        <slot name="confirm-button">
                            <tf-button>Ok</tf-button>
                        </slot>
                    </div>
                </div>
            </div>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.dialogElement = this.shadowRoot.querySelector(".dialog");
        this.headerElement = this.dialogElement.querySelector(".header");
        this.footerElement = this.dialogElement.querySelector(".footer");
        this.titleElement = this.headerElement.querySelector(".title");
        this.rendered = true;
    }

    get modal() {
        return this.hasAttribute("modal");
    }

    get show() {
        return this.hasAttribute("show");
    }

    get title() {
        return this.getAttribute("title") || "";
    }

    get titleIcon() {
        return this.getAttribute("title-icon") || null;
    }

    update() {
        if (!this.headerElement || !this.footerElement) {
            return;
        }

        this.footerElement.className = `footer${this.modal ? "" : " hidden"}`;
        this.dialogElement.className = `dialog${this.show ? " show" : ""}`;
        this.titleElement.textContent = this.title;

        const currentModalIcon = this.headerElement.querySelector(
            ".material-symbols-rounded",
        );
        if (this.titleIcon) {
            const icon = currentModalIcon ?? document.createElement("span");
            icon.classList.add("material-symbols-rounded");
            icon.textContent = this.titleIcon;
            this.headerElement.prepend(icon);
        } else if (currentModalIcon) {
            this.buttonElement.removeChild(currentModalIcon);
        }
    }
}

customElements.define("tf-dialog", TfDialog);
