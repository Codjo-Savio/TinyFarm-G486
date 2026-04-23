class TfLayout extends HTMLElement {
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
            :host {
                display: block;
                height: 100%;
                min-height: 0;
            }

            .layout {
                display: flex;
                flex-direction: column;
                height: 100%;
                min-height: 0;
            }

            slot {
                display: flex;
                flex-direction: column;
                flex: 1;
                min-height: 0;
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="layout">
                <slot></slot>
            </div>
        `;

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.rendered = true;
    }
}

customElements.define("tf-layout", TfLayout);
