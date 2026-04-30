const API_URL = window.apiUrl ?? "http://localhost:8080/api";

async function fetchApiWithCredentials(endpoint, method = "GET", body = null) {
    const normalized = endpoint.startsWith("/") ? endpoint : `/${endpoint}`;
    return await fetch(API_URL + normalized, {
        method: method,
        credentials: "include",
        ...(body && { body: body }),
    });
}

function loadScriptIfNeeded(scriptPath) {
    const normalizedScriptPath = scriptPath.trim().replaceAll("/", "");
    const headNode = document.querySelector("head");
    const headChildScripts = headNode.querySelectorAll("script");
    for (const node of headChildScripts) {
        if (node.src.trim().replaceAll("/", "") === normalizedScriptPath) {
            return;
        }
    }
    const script = document.createElement("script");
    script.src = scriptPath;
    script.type = "module";
    headNode.appendChild(script);
}

export { fetchApiWithCredentials, API_URL, loadScriptIfNeeded };
