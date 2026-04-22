class TfProgressBar extends HTMLElement {
    static get observedAttributes() {
        return ["progress", "variant"];
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
            :host {
                display: inline-block;
            }

            .progress-bar {
                width: 100%;
                height: 8px;
                background-color: var(--color-secondary);
                border-radius: 1000px;
            }

            .progress-bar.light {
                background-color: var(--color-surface-dark);
            }

            .line {
                transition: width .3s;
                height: 100%;
                background-color: var(--color-primary);
                border-radius: 1000px;
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="progress-bar">
                <div class="line"></div>
            </div>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.progressBarElement =
            this.shadowRoot.querySelector("div.progress-bar");
        this.lineElement = this.progressBarElement.querySelector(".line");
        this.rendered = true;
    }

    get progress() {
        return this.getAttribute("progress") || 0;
    }

    get variant() {
        return this.getAttribute("variant") || "normal";
    }

    update() {
        if (!this.progressBarElement || !this.lineElement) {
            return;
        }

        this.progressBarElement.className = `tf-progress-bar ${this.variant}`;
        this.lineElement.style.width = `${this.progress}%`;
    }
}

customElements.define("tf-progress-bar", TfProgressBar);
