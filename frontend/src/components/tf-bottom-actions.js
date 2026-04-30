import { fetchApiWithCredentials } from "/utils/fetch.js";

class TfBottomActions extends HTMLElement {
    timeIntervalId;

    static get observedAttributes() {
        return ["size", "variant"];
    }

    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.rendered = false;
        this.overviewLoaded = false;
    }

    connectedCallback() {
        this.render();
        this.update();
    }

    get size() {
        return this.getAttribute("size") || "normal";
    }

    get variant() {
        return this.getAttribute("variant") || "normal";
    }

    async fetchUserId() {
        const currentUserRes = await fetchApiWithCredentials("/auth/me");
        return (await currentUserRes.json()).id;
    }

    async fetchTradeOverview() {
        const coopRes = await fetchApiWithCredentials("/cooperative");
        const marketRes = await fetchApiWithCredentials(
            `/market/not?uid=${await this.fetchUserId()}`,
        );
        return {
            cooperativeStock: Object.keys(await coopRes.json()).length,
            marketStock: (await marketRes.json()).length,
        };
    }

    async isCooperativeOpen() {
        const coopRes = await fetchApiWithCredentials("/cooperative/isOpen");
        return (await coopRes.text()) === "true";
    }

    getAoeTime() {
        // AoE time = UTC-12
        const currentTime = new Date();
        return new Date(currentTime.getTime() - 12 * 60 * 60 * 1000);
    }

    setTime() {
        if (!this.currentTimeElement || !this.remainingTimeElement) {
            return;
        }

        const time = this.getAoeTime();
        const aoeHours = time.getUTCHours();
        const aoeMinutes = time.getUTCMinutes();
        const remainingHours = 23 - aoeHours;
        const remainingMinutes = 59 - aoeMinutes;
        const prettyRemaining =
            remainingHours === 0
                ? `${remainingMinutes}min`
                : `${remainingHours}h`;
        this.currentTimeElement.textContent = `${aoeHours.toString().padStart(2, "0")}:${aoeMinutes.toString().padStart(2, "0")} AoE`;
        this.remainingTimeElement.textContent = prettyRemaining;
    }

    render() {
        if (this.rendered) {
            return;
        }

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

            .links > #coop.closed,
            .links > #coop.closed:hover {
                color: var(--color-secondary);
                background-color: var(--color-surface-dark);
                cursor: not-allowed;
            }

            .bottom-actions:not(.small) .links > #coop.closed:hover {
                transform: none;
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

            .bottom-actions.small .links > #coop.closed > .small-legend {
                background-color: var(--color-surface-dark);
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

        const template = document.createElement("template");
        template.innerHTML = `
            <div class="bottom-actions">
                <div class="links">
                    <a id="coop" href="/dashboard/trade/cooperative">
                        <span class="material-symbols-rounded large">
                            storefront
                        </span>
                        <div>
                            <span class="label">Coopérative</span>
                            <span class="infos"></span>
                        </div>
                        <div class="small-legend">Coopérative</div>
                    </a>
                    <a id="marketplace" href="/dashboard/trade/marketplace">
                        <span class="material-symbols-rounded large">
                            groups
                        </span>
                        <div>
                            <span class="label">Marché</span>
                            <span class="infos"></span>
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

        this.shadowRoot.appendChild(style);
        this.shadowRoot.appendChild(template.content.cloneNode(true));
        this.bottomActionsElement =
            this.shadowRoot.querySelector(".bottom-actions");
        this.coopLink = this.bottomActionsElement.querySelector("#coop");
        this.marketplaceLink =
            this.bottomActionsElement.querySelector("#marketplace");
        this.coopInfosElement = this.coopLink.querySelector(".infos");
        this.marketplaceInfosElement =
            this.marketplaceLink.querySelector(".infos");
        this.currentTimeElement =
            this.bottomActionsElement.querySelector("#time .current");
        this.remainingTimeElement =
            this.bottomActionsElement.querySelector("#time .remaining");
        this.rendered = true;
    }

    async update() {
        if (!this.bottomActionsElement) {
            return;
        }

        this.bottomActionsElement.className = `bottom-actions ${this.size}`;
        this.coopLink.style.display =
            this.variant === "marketplace-only" ? "none" : "";
        this.marketplaceLink.style.display =
            this.variant === "coop-only" ? "none" : "";

        if (!this.timeIntervalId) {
            this.setTime();
            this.timeIntervalId = setInterval(() => this.setTime(), 1000);
        }

        if (!this.overviewLoaded) {
            try {
                const overview = await this.fetchTradeOverview();
                this.coopInfosElement.textContent = `En stock : ${overview.cooperativeStock}`;
                this.marketplaceInfosElement.textContent = `En stock : ${overview.marketStock}`;
                if (!(await this.isCooperativeOpen())) {
                    this.coopLink.classList.add("closed");
                    this.coopLink.removeAttribute("href");
                    this.coopLink.querySelector(".small-legend").textContent =
                        "Coopérative fermée";
                    this.coopInfosElement.textContent = "Fermée";
                }
                this.overviewLoaded = true;
            } finally {
                this.bottomActionsElement.classList.add("ready");
            }
        }
    }
}

customElements.define("tf-bottom-actions", TfBottomActions);
