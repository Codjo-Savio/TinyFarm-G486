class TfSnackbar extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.rendered = false;
        this.resizeHandler = this.updateTopOffset.bind(this);
    }

    connectedCallback() {
        this.render();
        this.updateTopOffset();
        window.addEventListener("resize", this.resizeHandler);
    }

    disconnectedCallback() {
        window.removeEventListener("resize", this.resizeHandler);
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
                top: var(--snackbar-top-offset, 96px);
                left: 50%;
                transform: translateX(-50%);
                z-index: 120;
                pointer-events: none;
                width: min(90vw, 400px);
            }

            .stack {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 10px;
            }

            .snackbar {
                width: 100%;
                max-height: 100px;
                box-sizing: border-box;
                border-radius: var(--radius);
                padding: 12px 16px;
                color: var(--color-surface);
                background-color: var(--color-primary);
                box-shadow: var(--shadow);
                display: flex;
                align-items: center;
                gap: 10px;
                font-weight: 700;
                pointer-events: auto;
                animation: enter .25s ease forwards;
                word-break: break-word;
                overflow: hidden;
            }

            .snackbar.success {
                background-color: #2c9150;
            }

            .snackbar.error {
                background-color: #be3a3a;
            }

            .snackbar.leaving {
                animation: exit .25s ease forwards;
            }

            .message {
                margin: 0;
            }

            @keyframes enter {
                from {
                    transform: translateY(24px);
                }

                to {
                    transform: translateY(0);
                }
            }

            @keyframes exit {
                to {
                    padding-top: 0;
                    padding-bottom: 0;
                    max-height: 0;
                }
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="stack"></div>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.stackElement = this.shadowRoot.querySelector(".stack");
        this.rendered = true;
    }

    updateTopOffset() {
        const appBar = document.querySelector("tf-app-bar");
        const defaultOffset = 24;
        const appBarBottom = appBar?.getBoundingClientRect().bottom ?? 0;
        const topOffset = Math.max(
            defaultOffset,
            Math.round(appBarBottom + 12),
        );
        this.style.setProperty("--snackbar-top-offset", `${topOffset}px`);
    }

    showSnackbar(message, isSuccess = true) {
        if (!this.stackElement) {
            return;
        }

        this.updateTopOffset();

        const snackbar = document.createElement("div");
        const isSuccessBoolean = Boolean(isSuccess);
        snackbar.className = `snackbar ${isSuccessBoolean ? "success" : "error"}`;

        const icon = document.createElement("span");
        icon.className = "material-symbols-rounded";
        icon.textContent = isSuccessBoolean ? "check_circle" : "cancel";

        const text = document.createElement("p");
        text.className = "message";
        text.textContent = message;

        snackbar.appendChild(icon);
        snackbar.appendChild(text);
        this.stackElement.appendChild(snackbar);

        window.setTimeout(() => {
            snackbar.classList.add("leaving");
            snackbar.addEventListener(
                "animationend",
                () => {
                    snackbar.remove();
                },
                { once: true },
            );
        }, 3000);
    }
}

customElements.define("tf-snackbar", TfSnackbar);
