class Layout extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        this.render();
    }

    render() {
        this.shadowRoot.innerHTML = `
        <link rel="stylesheet" href="/components/layout/layout.css">

        <div class="layout">
            <slot></slot>
        </div>
        `;
    }
}

customElements.define("app-layout", Layout);
