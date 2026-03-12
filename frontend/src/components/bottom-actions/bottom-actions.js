class BottomActions extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        this.render();
    }

    render() {
        const template = document.createElement("template");
        template.innerHTML = `
			<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
			<link rel="stylesheet" href="/components/bottom-actions/bottom-actions.css">

			<div class="bottom-actions">
				<div class="links">
					<a id="coop" href="${this.getAttribute("coop-link") || "/dashboard/trade/cooperative?from=/dashboard"}">
						<span class="material-symbols-rounded large">
							storefront
						</span>
						<div>
							<span class="label">${this.getAttribute("coop-label") || "Coopérative"}</span>
							<span class="infos">${this.getAttribute("coop-infos") || "En stock : 100"}</span>
						</div>
					</a>
					<a id="marketplace" href="${this.getAttribute("marketplace-link") || "/dashboard/trade/marketplace?from=/dashboard"}">
						<span class="material-symbols-rounded large">
							groups
						</span>
						<div>
							<span class="label">${this.getAttribute("marketplace-label") || "Marché"}</span>
							<span class="infos">${this.getAttribute("marketplace-infos") || "En stock : 2"}</span>
						</div>
					</a>
				</div>
				<div id="time">
					<span class="material-symbols-rounded">
						schedule
					</span>
					<span><b>${this.getAttribute("current-time") || "23:47 AoE"}</b> • Fin du jour : <b>${this.getAttribute("day-end") || "13 min"}</b></span>
				</div>
			</div>
		`;

        this.shadowRoot.appendChild(template.content.cloneNode(true));
    }
}

customElements.define("bottom-actions", BottomActions);
