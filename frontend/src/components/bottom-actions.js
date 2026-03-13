class BottomActions extends HTMLElement {
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
		.material-symbols-rounded {
            font-family: "Material Symbols Rounded";
            font-weight: normal;
            font-style: normal;
            line-height: 1;
            letter-spacing: normal;
            text-transform: none;
            display: inline-block;
            white-space: nowrap;
            word-wrap: normal;
            direction: ltr;
            -webkit-font-feature-settings: "liga";
            -webkit-font-smoothing: antialiased;
			font-variation-settings:
				"FILL" 0,
				"wght" 400,
				"GRAD" 0,
				"opsz" 24;
        }

		.bottom-actions {
			padding: 24px;
			display: flex;
			position: fixed;
			bottom: 0;
			left: 0;
			width: 100%;
			gap: 24px;
			justify-content: space-between;
			align-items: flex-end;
			box-sizing: border-box;
		}

		.links {
			display: flex;
			gap: 24px;
		}

		.links > a {
			text-decoration: none;
			color: var(--color-primary);
			display: flex;
			align-items: center;
			gap: 24px;
			background-color: var(--color-surface-dark);
			border-radius: var(--radius);
			padding: 16px 24px;
			box-shadow: var(--shadow);
			transition:
				transform 0.3s,
				background-color 0.3s;
		}

		.links > a:hover {
			background-color: var(--color-secondary);
			transform: translateY(-4px);
		}

		.links > a > div {
			display: flex;
			flex-direction: column;
		}

		.label {
			font-size: var(--font-size-body-large);
			font-weight: bold;
			margin-bottom: 4px;
		}

		#time {
			box-shadow: var(--shadow);
			background-color: var(--color-surface-dark);
			border-radius: 100px;
			display: flex;
			gap: 6px;
			align-items: center;
			height: fit-content;
			padding: 6px 16px 6px 6px;
			line-height: 1;
			color: var(--color-primary);
		}

		.large {
			font-size: 40px;
		}

		@media (max-width: 900px) {
			.bottom-actions {
				flex-direction: column;
				align-items: stretch;
			}

			.links {
				flex-direction: column;
			}

			#time {
				align-self: flex-start;
			}
		}
		`;
        this.shadowRoot.appendChild(style);

        const template = document.createElement("template");
        template.innerHTML = `
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
