const API_URL = window.apiUrl || "http://localhost:8080/api";
let currentUser = null;
let chickens = [];

function showInfo(title, message, icon = "info") {
    const dialog = document.getElementById("info-dialog");
    const messageEl = document.getElementById("info-dialog-message");
    const okBtn = document.getElementById("info-dialog-ok");

    if (!dialog || !messageEl || !okBtn) return;

    dialog.setAttribute("title", title);
    dialog.setAttribute("title-icon", icon);
    messageEl.textContent = message;
    dialog.setAttribute("show", "");

    okBtn.onclick = () => {
        console.log("Ok button clicked");
        dialog.removeAttribute("show");
    };
}

const ACTION_COSTS = {
    feed: 3,
    water: 1,
    heal: 6,
    clean: 3
};

// Helper to get cookies (needed for JWT)
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

async function apiFetch(endpoint, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...options.headers,
    };

    const res = await fetch(`${API_URL}${endpoint}`, { 
        ...options, 
        headers,
        credentials: "include"
    });
    if (!res.ok) {
        if (res.status === 401 || res.status === 403) {
            window.location.href = "/"; // Redirect to login if unauthorized
        }
        throw new Error(`API Error: ${res.status}`);
    }
    return res.status !== 204 ? res.json() : null;
}

async function fetchInitialData() {
    try {
        // 1. Get current auth user
        const authUser = await apiFetch("/auth/me");
        // 2. Get full user details (ecus, etc.)
        currentUser = await apiFetch(`/users/id/${authUser.id}`);
        // 3. Get all chickens and filter
        const allChickens = await apiFetch("/chickens");
        chickens = allChickens
            .filter(c => c.userId === currentUser.id)
            .sort((a, b) => a.id - b.id);

        renderUI();
    } catch (error) {
        console.error("Failed to load data:", error);
    }
}

function renderUI() {
    renderStats();
    renderChickens();
    setupDropdown();
}

function renderStats() {
    const total = chickens.length;
    document.getElementById("chicken-count").textContent = total;
    
    if (total > 0) {
        const sickCount = chickens.filter(c => !c.healthy).length;
        const dirtyCount = chickens.filter(c => !c.clean).length;
        
        document.getElementById("sick-percent").textContent = Math.round((sickCount / total) * 100) + "%";
        document.getElementById("dirty-percent").textContent = Math.round((dirtyCount / total) * 100) + "%";
    } else {
        document.getElementById("sick-percent").textContent = "0%";
        document.getElementById("dirty-percent").textContent = "0%";
    }

    // Note: For now, egg count is not directly reachable via Chicken model in a simple way 
    // without more backend logic, so we'll leave it as is or default to 0.
    document.getElementById("egg-count").textContent = "0"; 
}

function renderChickens() {
    const grid = document.getElementById("chicken-grid");
    grid.innerHTML = "";

    if (chickens.length === 0) {
        grid.innerHTML = "<p>Vous n'avez pas de poules.</p>";
        return;
    }

    chickens.forEach(chicken => {
        const item = document.createElement("div");
        item.className = "grid-item";
        
        // Map types (French names as in mockup)
        const types = {
            'C': 'Poussin',
            'H': 'Poule',
            'R': 'Coq',
            'L': 'Pondeuse',
            'B': 'Coq reproducteur'
        };

        const stateBar = [];
        if (!chicken.healthy) stateBar.push('<div class="animal-state"><span class="material-symbols-rounded">heart_broken</span><p>Malade</p></div>');
        if (!chicken.clean) stateBar.push('<div class="animal-state"><span class="material-symbols-rounded">format_paint</span><p>Sale</p></div>');

        item.innerHTML = `
            <div class="animal-title">
                <h2>${chicken.name || 'Poule sans nom'}</h2>
                <div class="animal-state-bar">
                    ${stateBar.join('')}
                </div>
            </div>
            <div class="animal-content">
                <div class="food-state">
                    <span class="material-symbols-rounded">nutrition</span>
                    <div class="food-state-line-place-holder">
                        <div class="food-state-line" style="width: ${chicken.fedToday ? 100 : 20}%"></div>
                    </div>
                </div>
                <div class="animal-type">
                    <span class="material-symbols-rounded">info</span>
                    <div class="animal-type-text">
                        <p>${types[chicken.chickenType] || chicken.chickenType}</p>
                    </div>
                </div>
                <div class="animal-weight">
                    <span class="material-symbols-rounded">weight</span>
                    <div class="animal-weight-text">
                        <p>${chicken.weight.toFixed(2)}</p>
                        <p>kg</p>
                    </div>
                </div>
            </div>
            <div class="animal-actions">
                <button class="action-button" onclick="performAction(${chicken.id}, 'feed')" ${chicken.fedToday ? 'disabled' : ''}>
                    Nourrir ${chicken.fedToday ? '' : `(${ACTION_COSTS.feed}$)`}
                </button>
                <button class="action-button" onclick="performAction(${chicken.id}, 'water')" ${chicken.wateredToday ? 'disabled' : ''}>
                    Abreuver ${chicken.wateredToday ? '' : `(${ACTION_COSTS.water}$)`}
                </button>
                <button class="action-button" onclick="performAction(${chicken.id}, 'heal')" ${chicken.healthy ? 'disabled' : ''}>
                    Soigner ${chicken.healthy ? '' : `(${ACTION_COSTS.heal}$)`}
                </button>
                <button class="action-button" onclick="performAction(${chicken.id}, 'clean')" ${chicken.clean ? 'disabled' : ''}>
                    Nettoyer ${chicken.clean ? '' : `(${ACTION_COSTS.clean}$)`}
                </button>
            </div>
        `;
        grid.appendChild(item);
    });
}

async function performAction(chickenId, action) {
    try {
        await apiFetch(`/chickens/${chickenId}/${action}?userId=${currentUser.id}`, {
            method: "POST"
        });
        // Refresh data after action
        await fetchInitialData();
        window.dispatchEvent(new CustomEvent("refresh-user-data"));
    } catch (error) {
        showInfo("Erreur", "Erreur lors de l'action : " + error.message, "error");
    }
}

// Group actions
async function performAll(action) {
    const eligibleChickens = chickens.filter(c => {
        if (action === 'feed') return !c.fedToday;
        if (action === 'water') return !c.wateredToday;
        if (action === 'heal') return !c.healthy;
        if (action === 'clean') return !c.clean;
        return false;
    });

    if (eligibleChickens.length === 0) {
        showInfo("Action inutile", `Toutes vos volailles sont déjà ${action === 'feed' ? 'nourries' : action === 'water' ? 'abreuvées' : action === 'clean' ? 'propres' : 'en bonne santé'} !`, "info");
        return;
    }

    const btn = document.getElementById(`${action}-all-btn`);
    const originalText = btn.textContent;
    btn.textContent = "Chargement...";
    btn.disabled = true;

    try {
        for (const chicken of eligibleChickens) {
            await apiFetch(`/chickens/${chicken.id}/${action}?userId=${currentUser.id}`, {
                method: "POST"
            });
        }
        await fetchInitialData();
        window.dispatchEvent(new CustomEvent("refresh-user-data"));
    } catch (error) {
        showInfo("Erreur", "Une erreur est survenue lors des actions groupées : " + error.message, "error");
    } finally {
        btn.disabled = false;
        // setupDropdown() will be called by fetchInitialData() -> renderUI()
        // but we call it here again to be sure the label is restored even on error
        setupDropdown(); 
        document.getElementById("more-actions-content").classList.remove("grid");
    }
}

function setupDropdown() {
    const button = document.getElementById('more-actions-btn');
    const menu = document.getElementById('more-actions-content');

    if (!button) return;

    // Update bulk action labels with costs
    const bulkButtons = {
        feed: { id: "feed-all-btn", label: "Nourrir tout", filter: c => !c.fedToday },
        water: { id: "water-all-btn", label: "Abreuver tout", filter: c => !c.wateredToday },
        heal: { id: "heal-all-btn", label: "Soigner tout", filter: c => !c.healthy },
        clean: { id: "clean-all-btn", label: "Nettoyer tout", filter: c => !c.clean }
    };

    Object.entries(bulkButtons).forEach(([key, config]) => {
        const btn = document.getElementById(config.id);
        if (btn) {
            const count = chickens.filter(config.filter).length;
            const totalCost = count * ACTION_COSTS[key];
            btn.textContent = `${config.label} ${totalCost > 0 ? `(${totalCost}$)` : ''}`;
            btn.onclick = () => performAll(key);
        }
    });


    if (button._hasListener) return;

    button.addEventListener('click', (event) => {
        event.stopPropagation();
        menu.classList.toggle('grid');
    });

    menu.addEventListener('click', (event) => event.stopPropagation());

    document.addEventListener('click', () => {
        menu.classList.remove('grid');
    });

    button._hasListener = true;
}

// Initial load
document.addEventListener("DOMContentLoaded", fetchInitialData);

// Expose performAction to global scope for inline onclick
window.performAction = performAction;

