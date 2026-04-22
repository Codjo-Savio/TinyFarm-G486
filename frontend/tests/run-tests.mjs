import assert from "node:assert/strict";
import { startServer } from "../server.js";

async function withServer(env, fn) {
    const server = startServer(env);
    try {
        await fn();
    } finally {
        await new Promise((resolve, reject) => {
            server.close((err) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve();
            });
        });
    }
}

async function assertResponse(baseUrl, path, expectations = {}) {
    const res = await fetch(`${baseUrl}${path}`);
    const body = await res.text();

    const expectedStatus = expectations.status ?? 200;
    assert.equal(
        res.status,
        expectedStatus,
        `Status inattendu pour ${path}: ${res.status}`
    );

    if (expectations.contentTypeIncludes) {
        const contentType = res.headers.get("content-type") || "";
        assert.ok(
            contentType.includes(expectations.contentTypeIncludes),
            `Content-Type inattendu pour ${path}: ${contentType}`
        );
    }

    if (expectations.pattern) {
        assert.match(
            body,
            expectations.pattern,
            `Contenu inattendu pour ${path}`
        );
    }
}

async function testConfigRouteWithReleaseCommit() {
    const port = 3301;
    const baseUrl = `http://localhost:${port}`;
    await withServer(
        {
            ...process.env,
            PORT: String(port),
            API_URL: "http://localhost:8080/api/",
            RELEASE_NAME: "abc123",
            RELEASE_DATE: "2026-04-22T00:00:00.000Z",
        },
        async () => {
            await assertResponse(baseUrl, "/config.js", {
                contentTypeIncludes: "application/javascript",
                pattern:
                    /window\.apiUrl = "http:\/\/localhost:8080\/api";[\s\S]*name: "abc123"[\s\S]*date: "2026-04-22T00:00:00.000Z"[\s\S]*url: "https:\/\/github\.com\/Codjo-Savio\/TinyFarm-G486\/commit\/abc123"/,
            });
        }
    );
}

async function testConfigRouteWithDevRelease() {
    const port = 3302;
    const baseUrl = `http://localhost:${port}`;
    await withServer(
        {
            ...process.env,
            PORT: String(port),
            API_URL: "http://localhost:8080/api",
            RELEASE_NAME: "dev",
            RELEASE_DATE: "2026-04-22T00:00:00.000Z",
        },
        async () => {
            await assertResponse(baseUrl, "/config.js", {
                contentTypeIncludes: "application/javascript",
                pattern:
                    /window\.apiUrl = "http:\/\/localhost:8080\/api";[\s\S]*name: "dev"[\s\S]*url: "#"/,
            });
        }
    );
}

async function testStaticPagesAndAssets() {
    const port = 3303;
    const baseUrl = `http://localhost:${port}`;

    const routesToCheck = [
        { path: "/", pattern: /TinyFarm/i, contentTypeIncludes: "text/html" },
        { path: "/dashboard/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/dashboard/management/assets/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/dashboard/management/chicken-coop/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/dashboard/management/hutch/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/dashboard/management/meadow/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/dashboard/trade/cooperative/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/dashboard/trade/marketplace/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/doc/about/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/doc/rules/", pattern: /<html/i, contentTypeIncludes: "text/html" },
        { path: "/script.js", pattern: /window\.location\.href|api/i, contentTypeIncludes: "javascript" },
        { path: "/style.css", pattern: /:root|body|font-family/i, contentTypeIncludes: "text/css" },
        { path: "/styles/theme.css", pattern: /:root|--/i, contentTypeIncludes: "text/css" },
        { path: "/components/tf-layout.js", pattern: /class|customElements|HTMLElement/i, contentTypeIncludes: "javascript" },
        { path: "/components/tf-app-bar.js", pattern: /class|customElements|HTMLElement/i, contentTypeIncludes: "javascript" },
        { path: "/dashboard/management/hutch/script.js", pattern: /fetch|API_URL|document/i, contentTypeIncludes: "javascript" },
        { path: "/dashboard/management/chicken-coop/script.js", pattern: /fetch|API_URL|document/i, contentTypeIncludes: "javascript" },
        { path: "/dashboard/trade/marketplace/script.js", pattern: /fetch|document|panier/i, contentTypeIncludes: "javascript" },
    ];

    await withServer(
        {
            ...process.env,
            PORT: String(port),
        },
        async () => {
            for (const route of routesToCheck) {
                await assertResponse(baseUrl, route.path, {
                    status: 200,
                    pattern: route.pattern,
                    contentTypeIncludes: route.contentTypeIncludes,
                });
            }

            await assertResponse(baseUrl, "/this-route-does-not-exist", {
                status: 404,
            });
        }
    );
}

async function run() {
    const tests = [
        { name: "Config.js: release commit + trim API URL", fn: testConfigRouteWithReleaseCommit },
        { name: "Config.js: release dev + URL '#'", fn: testConfigRouteWithDevRelease },
        { name: "Routes et assets statiques essentiels (18 checks + 404)", fn: testStaticPagesAndAssets },
    ];

    let failed = 0;
    for (const testCase of tests) {
        try {
            await testCase.fn();
            console.log(`PASS - ${testCase.name}`);
        } catch (error) {
            failed += 1;
            console.error(`FAIL - ${testCase.name}`);
            console.error(error);
        }
    }

    if (failed > 0) {
        process.exit(1);
    }
}

run();
