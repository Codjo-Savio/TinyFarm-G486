import express from "express";
import liveReload from "livereload";
import connectLiveReload from "connect-livereload";

const app = express();
const PORT = 3000;

if (process.env.NODE_ENV === "development") {
    const liveReloadServer = liveReload.createServer({
        applyCSSLive: false,
    });
    liveReloadServer.watch("src");

    app.use(connectLiveReload());

    console.log("Development mode enabled, using hot reload");
}

app.use(express.static("src"));

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
