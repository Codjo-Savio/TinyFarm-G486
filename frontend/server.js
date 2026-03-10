import express from "express";

const app = express();
const port = 3000;

app.use(express.static("src"));

app.listen(port, () => {
    console.log(`Listening on port ${port}!`);
    console.log("Visit http://localhost:3000 to view the frontend.");
});
