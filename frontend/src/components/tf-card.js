class TfCard extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.rendered = false;
    }

    connectedCallback() {
        this.render();
    }

    render() {
        if (this.rendered) {
            return;
        }

        const style = document.createElement("style");
        style.textContent = `
            .card {
                display: flex;
                flex-direction: column;
                background-color: var(--color-surface-dark);
                padding: 20px;
                border-radius: var(--radius);
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="card">
                <slot></slot>
            </div>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.rendered = true;
    }
}

customElements.define("tf-card", TfCard);
