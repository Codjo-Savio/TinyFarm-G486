class TfBottomActions extends HTMLElement {
    FAKE_API_URL = "/fakeapi";
    API_URL = window.apiUrl || "http://localhost:8080/api";
    timeIntervalId;

    static get observedAttributes() {
        return ["size", "variant"];
    }

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
    }

    connectedCallback() {
        this.render();
    }

    get size() {
        return this.getAttribute("size") || "normal";
    }

    get variant() {
        return this.getAttribute("variant") || "normal";
    }

    async fetchTradeOverview() {
        const res = await fetch(this.FAKE_API_URL + "/trade/overview.json");
        return await res.json();
    }

    getAoeTime() {
        // AoE time = UTC-12
        const currentTime = new Date();
        return new Date(currentTime.getTime() - 12 * 60 * 60 * 1000);
    }

    setTime() {
        const time = this.getAoeTime();
        const aoeHours = time.getUTCHours();
        const aoeMinutes = time.getUTCMinutes();
        const remainingHours = 23 - aoeHours;
        const remainingMinutes = 59 - aoeMinutes;
        const prettyRemaining =
            remainingHours === 0
                ? `${remainingMinutes}min`
                : `${remainingHours}h`;
        this.shadowRoot.querySelector("#time .current").textContent =
            `${aoeHours.toString().padStart(2, "0")}:${aoeMinutes.toString().padStart(2, "0")} AoE`;
        this.shadowRoot.querySelector("#time .remaining").textContent =
            prettyRemaining;
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
            transition: opacity .3s;
            opacity: 0;
        }

        .bottom-actions.ready {
            opacity: 1;
        }

		.links {
			display: flex;
			gap: 24px;
            position: fixed;
            left: 24px;
            bottom: 24px;
		}

        .bottom-actions.small .links {
            flex-direction: column;
            left: -50px;
            gap: 16px;
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
				transform .3s,
				background-color .3s;
		}

		.links > a:hover {
			background-color: var(--color-secondary);
			transform: translateY(-4px);
		}

        .bottom-actions.small .links > a {
            padding: 12px;
            padding-left: 60px;
            height: 48px;
        }

        .bottom-actions.small .links > a > .small-legend {
            display: block;
            position: absolute;
            text-wrap-mode: nowrap;
            left: 110px;
            background-color: var(--color-secondary);
            box-shadow: var(--shadow);
            padding: 4px 8px;
            border-radius: 8px;
            opacity: 0;
            transition: opacity .3s;
            pointer-events: none;
        }

        .bottom-actions .links > a > .small-legend {
            display: none;
        }

        .bottom-actions.small .links > a:hover {
            transform: translateX(10px);
        }

        .bottom-actions.small .links > a:hover > .small-legend {
            opacity: 1;
        }

		.links > a > div {
			display: flex;
			flex-direction: column;
		}

        .bottom-actions.small .links > a > div {
            display: none;
        }

		.label {
			font-size: var(--font-size-body-large);
			font-weight: bold;
			margin-bottom: 4px;
		}

        .bottom-actions.small .links > a > div > .infos {
            display: none;
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
            position: fixed;
            right: 24px;
            bottom: 24px;
		}

		.large {
			font-size: 40px;
		}

        .bottom-actions.small .large {
			font-size: 30px;
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
		<div class="bottom-actions ${this.size}">
			<div class="links">
				<a id="coop" ${this.variant === "marketplace-only" ? 'style="display: none;"' : ""} href="/dashboard/trade/cooperative?from=/dashboard">
					<span class="material-symbols-rounded large">
						storefront
					</span>
					<div>
						<span class="label">Coopérative</span>
						<span class="infos">En stock : -</span>
					</div>
                    <div class="small-legend">Coopérative</div>
				</a>
				<a id="marketplace" ${this.variant === "coop-only" ? 'style="display: none;"' : ""} href="/dashboard/trade/marketplace?from=/dashboard">
					<span class="material-symbols-rounded large">
						groups
					</span>
					<div>
						<span class="label">Marché</span>
						<span class="infos">En stock : -</span>
					</div>
                    <div class="small-legend">Marché</div>
				</a>
			</div>
			<div id="time">
				<span class="material-symbols-rounded">
					schedule
				</span>
				<span><b class="current"></b> • Fin du jour dans <b class="remaining"></b></span>
			</div>
		</div>
		`;
        this.shadowRoot.appendChild(template.content.cloneNode(true));

        if (this.timeIntervalId) {
            clearInterval(this.timeIntervalId);
        } else {
            this.setTime();
        }
        this.timeIntervalId = setInterval(() => this.setTime(), 1000);

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
        } finally {
            // Set as ready
            this.shadowRoot
                .querySelector(".bottom-actions")
                .classList.add("ready");
        }
    }
}

customElements.define("tf-bottom-actions", TfBottomActions);
