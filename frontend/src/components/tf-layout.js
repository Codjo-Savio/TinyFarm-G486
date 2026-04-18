class TfLayout extends HTMLElement {
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
        
        /* 
           Fix : Définit le slot comme un conteneur flex pour permettre aux enfants 
           (comme <main>) d'utiliser flex: 1 et overflow-y: auto pour le défilement.
        */
        slot {
            display: flex;
            flex-direction: column;
            flex: 1;
            min-height: 0;
        }
        `;
        this.shadowRoot.appendChild(style);

        const template = document.createElement("template");
        template.innerHTML = `
        <div class="layout">
            <slot></slot>
        </div>
        `;
        this.shadowRoot.appendChild(template.content.cloneNode(true));
    }
}

customElements.define("tf-layout", TfLayout);
