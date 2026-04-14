class TfProgressBar extends HTMLElement {
    static get observedAttributes() {
        return ["progress", "variant"];
    }

    constructor() {
        super();
        this.attachShadow({ mode: "open" });

        this.shadowRoot.innerHTML = `
        <style>
            :host {
                display: inline-block;
            }

            .tf-progress-bar {
                width: 100%;
                height: 8px;
                background-color: var(--color-secondary);
                border-radius: 1000px;
            }

            .tf-progress-bar.light {
                background-color: var(--color-surface-dark);
            }

            .line {
                transition: width .3s;
                height: 100%;
                background-color: var(--color-primary);
                border-radius: 1000px;
            }
        </style>

        <div class="tf-progress-bar">
            <div class="line"></div>
        </div>
        `;
        this.tfProgressBar = this.shadowRoot.querySelector(
            "div.tf-progress-bar",
        );
    }

    connectedCallback() {
        this.update();
    }

    attributeChangedCallback() {
        this.update();
    }

    get progress() {
        return this.getAttribute("progress") || 0;
    }

    get variant() {
        return this.getAttribute("variant") || "normal";
    }

    update() {
        this.tfProgressBar.className = `tf-progress-bar ${this.variant}`;

        const line = this.tfProgressBar.querySelector(".line");
        line.style.width = `${this.progress}%`;
    }
}

customElements.define("tf-progress-bar", TfProgressBar);
