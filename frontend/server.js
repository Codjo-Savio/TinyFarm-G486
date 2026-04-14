import express from "express";
import liveReload from "livereload";
import connectLiveReload from "connect-livereload";

const app = express();
const PORT = 3000;
let API_URL = process.env.API_URL || "http://localhost:8080/api";
API_URL = API_URL.endsWith("/") ? API_URL.slice(0, -1) : API_URL;

if (process.env.NODE_ENV === "development") {
    const liveReloadServer = liveReload.createServer({
        applyCSSLive: false,
    });
    liveReloadServer.watch("src");

    //app.use(connectLiveReload());

    console.log("Development mode enabled, using hot reload");
}

app.get("/config.js", (req, res) => {
    res.type("application/javascript");
    res.send(`window.apiUrl = "${API_URL}";`);
});

app.use(express.static("src"));

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
    console.log(`Using API URL ${API_URL}`);
});
