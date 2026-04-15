class TfCard extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        this.render();
    }

    render() {
        const style = document.createElement("style");
        style.textContent = `
        .card {
            display: flex;
            flex-direction: column;
            height: 100%;
            background-color: var(--color-surface-dark);
            padding: 20px;
            border-radius: var(--radius);
        }
        `;
        this.shadowRoot.appendChild(style);

        const template = document.createElement("template");
        template.innerHTML = `
        <div class="card">
            <slot></slot>
        </div>
        `;
        this.shadowRoot.appendChild(template.content.cloneNode(true));
    }
}

customElements.define("tf-card", TfCard);
