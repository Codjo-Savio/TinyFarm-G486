import { loadScriptIfNeeded } from "/utils/fetch.js";

class TfMenu extends HTMLElement {
    static get observedAttributes() {
        return ["offset-x", "offset-y", "open"];
    }

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.rendered = false;
        this.targetListeners = new Map();
        this.targetElements = [];
        this.documentClickHandler = this.handleDocumentClick.bind(this);
        this.resizeHandler = this.updateMenuPosition.bind(this);
    }

    connectedCallback() {
        this.render();
        this.bindTargetElements();
        document.addEventListener("click", this.documentClickHandler);
        window.addEventListener("resize", this.resizeHandler);
        window.addEventListener("scroll", this.resizeHandler, true);
    }

    disconnectedCallback() {
        this.unbindTargetElements();
        document.removeEventListener("click", this.documentClickHandler);
        window.removeEventListener("resize", this.resizeHandler);
        window.removeEventListener("scroll", this.resizeHandler, true);
    }

    attributeChangedCallback(name) {
        if (name === "open") {
            this.updateOpenState();
            return;
        }

        this.updateMenuPosition();
    }

    get offsetX() {
        return Number(this.getAttribute("offset-x") ?? "0");
    }

    get offsetY() {
        return Number(this.getAttribute("offset-y") ?? "8");
    }

    get open() {
        return this.hasAttribute("open");
    }

    openMenu() {
        this.setAttribute("open", "");
    }

    closeMenu() {
        this.removeAttribute("open");
    }

    toggleMenu() {
        if (this.open) {
            this.closeMenu();
            return;
        }

        this.openMenu();
    }

    updateOpenState() {
        if (!this.menuElement) {
            return;
        }

        this.classList.toggle("open", this.open);
        if (this.open) {
            this.updateMenuHeight();
            this.updateMenuPosition();
        }
    }

    updateMenuHeight() {
        if (!this.menuElement || !this.groupsElement) {
            return;
        }

        const contentHeight = this.groupsElement.scrollHeight;
        const finalHeight =
            contentHeight + Number(this.menuElement.style.paddingTop) * 2;
        this.style.setProperty("--menu-open-max-height", `${finalHeight}px`);
    }

    getPrimaryTargetElement() {
        if (this.targetElements.length > 0) {
            return this.targetElements[0];
        }

        return this;
    }

    updateMenuPosition() {
        if (!this.menuElement) {
            return;
        }

        const targetElement = this.getPrimaryTargetElement();
        if (!targetElement) {
            return;
        }

        const targetRect = targetElement.getBoundingClientRect();
        this.menuElement.style.left = `${targetRect.left + this.offsetX}px`;
        this.menuElement.style.top = `${targetRect.bottom + this.offsetY}px`;
    }

    bindTargetElements() {
        this.unbindTargetElements();

        if (!this.targetSlotElement) {
            return;
        }

        this.targetElements = this.targetSlotElement
            .assignedElements({ flatten: true })
            .filter((element) => element !== this);

        for (const element of this.targetElements) {
            const listener = (event) => {
                event.stopPropagation();
                this.toggleMenu();
            };
            element.addEventListener("click", listener);
            this.targetListeners.set(element, listener);
        }
    }

    unbindTargetElements() {
        for (const [element, listener] of this.targetListeners.entries()) {
            element.removeEventListener("click", listener);
        }

        this.targetListeners.clear();
        this.targetElements = [];
    }

    handleDocumentClick(event) {
        if (!this.open) {
            return;
        }

        const path = event.composedPath();
        if (!path.includes(this)) {
            this.closeMenu();
        }
    }

    render() {
        if (this.rendered) {
            return;
        }

        const style = document.createElement("style");
        style.textContent = `
            :host {
                display: inline-block;
                position: relative;
                --menu-open-max-height: 500px;
            }

            .menu {
                background-color: var(--color-surface-dark);
                border-radius: var(--radius);
                box-shadow: var(--shadow);
                min-width: 264px;
                display: flex;
                flex-direction: column;
                gap: 4px;
                padding: 0px 6px;
                overflow: hidden;
                max-height: 0px;
                position: fixed;
                z-index: 10;
                transform: translateY(-8px);
                transition:
                    max-height .3s,
                    opacity .3s,
                    transform .3s,
                    padding .3s;
                font-weight: normal;
            }

            :host(.open) .menu {
                max-height: var(--menu-open-max-height);
                opacity: 1;
                transform: translateY(0px);
                padding: 6px;
            }

            .groups {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }
        `;

        const template = document.createElement("template");
        template.innerHTML = `
            <slot name="menu-target"></slot>
            <div class="menu" part="menu">
                <div class="groups">
                    <slot name="group"></slot>
                    <slot></slot>
                </div>
            </div>
        `;

        loadScriptIfNeeded("/components/tf-menu-entry.js");
        loadScriptIfNeeded("/components/tf-menu-group.js");
        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.menuElement = this.shadowRoot.querySelector(".menu");
        this.groupsElement = this.shadowRoot.querySelector(".groups");
        this.targetSlotElement = this.shadowRoot.querySelector(
            'slot[name="menu-target"]',
        );
        this.groupSlotElement =
            this.shadowRoot.querySelector('slot[name="group"]');
        this.defaultSlotElement =
            this.shadowRoot.querySelector("slot:not([name])");
        this.targetSlotElement.addEventListener("slotchange", () => {
            this.bindTargetElements();
            this.updateMenuPosition();
        });
        this.groupSlotElement.addEventListener("slotchange", () => {
            this.updateMenuHeight();
        });
        this.defaultSlotElement.addEventListener("slotchange", () => {
            this.updateMenuHeight();
        });
        this.menuElement.addEventListener("click", (event) => {
            const path = event.composedPath();
            const clickedEntry = path.some(
                (node) => node?.tagName === "TF-MENU-ENTRY",
            );

            if (clickedEntry) {
                this.closeMenu();
            }
        });
        this.updateMenuHeight();
        this.updateOpenState();
        this.rendered = true;
    }
}

customElements.define("tf-menu", TfMenu);
