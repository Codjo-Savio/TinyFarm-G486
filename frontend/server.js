import express from "express";
import liveReload from "livereload";
import connectLiveReload from "connect-livereload";
import { fileURLToPath } from "node:url";
import path from "node:path";

export function createApp(env = process.env) {
    const app = express();
    let apiUrl = env.API_URL || "http://localhost:8080/api";
    apiUrl = apiUrl.endsWith("/") ? apiUrl.slice(0, -1) : apiUrl;
    const releaseName = env.RELEASE_NAME || "dev";
    const releaseDate = env.RELEASE_DATE || new Date().toISOString();
    const releaseUrl =
        releaseName !== "dev"
            ? `https://github.com/Codjo-Savio/TinyFarm-G486/commit/${releaseName}`
            : "#";

    if (env.NODE_ENV === "development") {
        const liveReloadServer = liveReload.createServer({
            applyCSSLive: false,
        });
        liveReloadServer.watch("src");

        app.use(connectLiveReload());

        console.log("Development mode enabled, using hot reload");
    }

    app.get("/config.js", (req, res) => {
        res.type("application/javascript");
        res.send(`
        window.apiUrl = "${apiUrl}"; 
        window.release = {
            name: "${releaseName}",
            date: "${releaseDate}",
            url: "${releaseUrl}",
        }
    `);
    });

    app.use(express.static("src"));
    return { app, apiUrl, releaseName, releaseDate };
}

export function startServer(env = process.env) {
    const port = Number(env.PORT) || 3000;
    const { app, apiUrl, releaseName, releaseDate } = createApp(env);

    return app.listen(port, () => {
        console.log(`Server running on http://localhost:${port}`);
        console.log(`Using API URL ${apiUrl}`);
        console.log(`Using release '${releaseName}' (${releaseDate})`);
    });
}

const currentFile = fileURLToPath(import.meta.url);
if (process.argv[1] && path.resolve(process.argv[1]) === currentFile) {
    startServer(process.env);
}
