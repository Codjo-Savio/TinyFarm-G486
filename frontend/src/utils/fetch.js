const API_URL = window.apiUrl ?? "http://localhost:8080/api";

async function fetchApiWithCredentials(endpoint, method = "GET") {
    const normalized = endpoint.startsWith("/") ? endpoint : `/${endpoint}`;
    return await fetch(API_URL + normalized, {
        method: method,
        credentials: "include",
    });
}
