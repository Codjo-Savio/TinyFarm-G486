class BottomActions extends HTMLElement {
    API_URL = "/fakeapi";

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        this.render();
    }

    async fetchTradeOverview() {
        const res = await fetch(this.API_URL + "/trade/overview.json");
        return await res.json();
    }

    async fetchTime() {
        const res = await fetch(this.API_URL + "/time.json");
        return await res.json();
    }

    async render() {
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
			font-variation-settings:var(--font-var-icon);
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

        .bottom-actions #coop .infos,
        .bottom-actions #marketplace .infos,
        .bottom-actions #time {
            opacity: 0;
            transition: opacity .3s;
        }

        .bottom-actions.ready #coop .infos,
        .bottom-actions.ready #marketplace .infos,
        .bottom-actions.ready #time {
            opacity: 1;
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
				<a id="coop" href="/dashboard/trade/cooperative?from=/dashboard">
					<span class="material-symbols-rounded large">
						storefront
					</span>
					<div>
						<span class="label">Coopérative</span>
						<span class="infos">En stock : -</span>
					</div>
				</a>
				<a id="marketplace" href="/dashboard/trade/marketplace?from=/dashboard">
					<span class="material-symbols-rounded large">
						groups
					</span>
					<div>
						<span class="label">Marché</span>
						<span class="infos">En stock : -</span>
					</div>
				</a>
			</div>
			<div id="time">
				<span class="material-symbols-rounded">
					schedule
				</span>
				<span><b class="current">- AoE</b> • Fin du jour : <b class="remaining">-</b></span>
			</div>
		</div>
		`;
        this.shadowRoot.appendChild(template.content.cloneNode(true));

        try {
            // Fetch cooperative and marketplace data
            const overview = await this.fetchTradeOverview();
            this.shadowRoot.querySelector("#coop .infos").textContent =
                this.shadowRoot
                    .querySelector("#coop .infos")
                    .textContent.replace("-", overview.cooperative.stock);
            this.shadowRoot.querySelector("#marketplace .infos").textContent =
                this.shadowRoot
                    .querySelector("#marketplace .infos")
                    .textContent.replace("-", overview.marketplace.stock);

            // Fetch time data
            const time = await this.fetchTime();
            this.shadowRoot.querySelector("#time .current").textContent =
                this.shadowRoot
                    .querySelector("#time .current")
                    .textContent.replace(
                        "-",
                        `${time.aoe.min}:${time.aoe.sec}`,
                    );
            this.shadowRoot.querySelector("#time .remaining").textContent =
                this.shadowRoot
                    .querySelector("#time .remaining")
                    .textContent.replace(
                        "-",
                        `${23 - time.aoe.min}h${60 - time.aoe.sec}min`,
                    );
        } finally {
            // Set as ready
            this.shadowRoot
                .querySelector(".bottom-actions")
                .classList.add("ready");
        }
    }
}

customElements.define("bottom-actions", BottomActions);
