class TfMenuGroup extends HTMLElement {
    static get observedAttributes() {
        return ["legend"];
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

    get legend() {
        return this.getAttribute("legend") || "";
    }

    render() {
        if (this.rendered) {
            return;
        }

        const style = document.createElement("style");
        style.textContent = `
            :host {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }

            .legend {
                padding: 8px 12px 4px;
                color: var(--color-primary);
                font-size: 14px;
                font-weight: 700;
                text-transform: uppercase;
            }

            .legend.hidden {
                display: none;
            }

            .entries {
                display: flex;
                flex-direction: column;
                gap: 0;
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="legend hidden"></div>
            <div class="entries">
                <slot name="entry"></slot>
                <slot></slot>
            </div>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.legendElement = this.shadowRoot.querySelector(".legend");
        this.rendered = true;
    }

    update() {
        if (!this.legendElement) {
            return;
        }

        this.legendElement.textContent = this.legend;
        this.legendElement.className = `legend${this.legend ? "" : " hidden"}`;
    }
}

customElements.define("tf-menu-group", TfMenuGroup);
