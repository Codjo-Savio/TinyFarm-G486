#!/usr/bin/env node

const { execSync } = require("node:child_process");
const fs = require("node:fs");
const path = require("node:path");

const repoRoot = path.resolve(__dirname, "..");
const outputDir = path.join(repoRoot, "github-stats");
const outputFile = path.join(outputDir, "index.html");

const criticalFileMatchers = [
    /^backend\//,
    /^frontend\//,
    /^database\//,
    /^docker-compose\.yml$/,
    /^README\.md$/,
];

const commitSep = "__COMMIT_END__";
const authorSep = "__AUTHOR_SEP__";

function run(command) {
    return execSync(command, {
        cwd: repoRoot,
        encoding: "utf8",
        stdio: ["ignore", "pipe", "pipe"],
    });
}

function toSafePercent(value, total) {
    if (!total) return 0;
    return Number(((value / total) * 100).toFixed(1));
}

function parseGitHistory() {
    const format = `%H%x1f%an%x1f%ad%x1f%s%x1f%b`;
    const raw = run(
        `git log --date=short --pretty=format:${format} --numstat`
    );
    const lines = raw.split("\n");
    const commits = [];
    let current = null;

    for (const line of lines) {
        if (!line.trim()) continue;

        if (line.includes("\x1f")) {
            if (current) commits.push(current);
            const [hash, author, date, subject, body] = line.split("\x1f");
            current = {
                hash,
                author: author || "Unknown",
                date,
                subject: (subject || "").trim(),
                body: (body || "").trim(),
                files: [],
                additions: 0,
                deletions: 0,
            };
            continue;
        }

        if (!current) continue;
        const parts = line.split("\t");
        if (parts.length !== 3) continue;
        const [addRaw, delRaw, file] = parts;

        const additions = Number.isNaN(Number(addRaw)) ? 0 : Number(addRaw);
        const deletions = Number.isNaN(Number(delRaw)) ? 0 : Number(delRaw);

        current.files.push(file);
        current.additions += additions;
        current.deletions += deletions;
    }

    if (current) commits.push(current);
    return commits;
}

function getQualityLevel(score) {
    if (score >= 75) return "Très bonne";
    if (score >= 55) return "Bonne";
    if (score >= 35) return "Moyenne";
    return "À renforcer";
}

function buildMetrics(commits) {
    const totalCommits = commits.length;
    let totalAdditions = 0;
    let totalDeletions = 0;
    let totalFilesChanged = 0;
    let pullRequests = 0;
    let bugFixes = 0;
    let featureCommits = 0;
    let docsCommits = 0;
    let refactorCommits = 0;

    const authorStats = new Map();
    const monthlyActivity = new Map();
    const criticalFiles = new Map();

    for (const commit of commits) {
        const changedLines = commit.additions + commit.deletions;
        totalAdditions += commit.additions;
        totalDeletions += commit.deletions;
        totalFilesChanged += commit.files.length;

        const text = `${commit.subject}\n${commit.body}`.toLowerCase();
        const isPR = /merge pull request #\d+|\(#\d+\)|\bpr\b/.test(text);
        const isBug =
            /\bfix(e[sd])?\b|\bbug(s)?\b|\bhotfix\b|\bissue(s)?\b|\bresolve(d|s)?\b|\bpatch\b/.test(
                text
            );
        const isFeature =
            /\bfeat(ure)?\b|\badd(ed|s|ing)?\b|\bimplement(ed|s|ing)?\b|\bnew\b|\bcreate(d|s|ing)?\b/.test(
                text
            );
        const isDocs = /\bdoc(s|umentation)?\b|\breadme\b/.test(text);
        const isRefactor = /\brefactor(ed|s|ing)?\b|\bcleanup\b/.test(text);

        if (isPR) pullRequests += 1;
        if (isBug) bugFixes += 1;
        if (isFeature) featureCommits += 1;
        if (isDocs) docsCommits += 1;
        if (isRefactor) refactorCommits += 1;

        const month = commit.date.slice(0, 7);
        monthlyActivity.set(month, (monthlyActivity.get(month) || 0) + 1);

        for (const file of commit.files) {
            if (criticalFileMatchers.some((matcher) => matcher.test(file))) {
                criticalFiles.set(file, (criticalFiles.get(file) || 0) + 1);
            }
        }

        const prevAuthor = authorStats.get(commit.author) || {
            commits: 0,
            changedLines: 0,
            additions: 0,
            deletions: 0,
            filesChanged: 0,
        };

        prevAuthor.commits += 1;
        prevAuthor.changedLines += changedLines;
        prevAuthor.additions += commit.additions;
        prevAuthor.deletions += commit.deletions;
        prevAuthor.filesChanged += commit.files.length;
        authorStats.set(commit.author, prevAuthor);
    }

    const avgLinesPerCommit = totalCommits
        ? Number(((totalAdditions + totalDeletions) / totalCommits).toFixed(1))
        : 0;
    const avgFilesPerCommit = totalCommits
        ? Number((totalFilesChanged / totalCommits).toFixed(1))
        : 0;

    const authors = Array.from(authorStats.entries())
        .map(([author, stats]) => ({
            author,
            ...stats,
            commitPercent: toSafePercent(stats.commits, totalCommits),
            avgCommitSize: stats.commits
                ? Number((stats.changedLines / stats.commits).toFixed(1))
                : 0,
        }))
        .sort((a, b) => b.commits - a.commits);

    const timeline = Array.from(monthlyActivity.entries())
        .map(([month, count]) => ({ month, count }))
        .sort((a, b) => a.month.localeCompare(b.month));

    const topCriticalFiles = Array.from(criticalFiles.entries())
        .map(([file, touches]) => ({ file, touches }))
        .sort((a, b) => b.touches - a.touches)
        .slice(0, 15);

    const prRatio = toSafePercent(pullRequests, totalCommits);
    const bugRatio = toSafePercent(bugFixes, totalCommits);
    const featureRatio = toSafePercent(featureCommits, totalCommits);
    const docRatio = toSafePercent(docsCommits, totalCommits);
    const refactorRatio = toSafePercent(refactorCommits, totalCommits);

    const qualityScore = Math.max(
        0,
        Math.min(
            100,
            Math.round(
                featureRatio * 0.35 +
                    bugRatio * 0.3 +
                    prRatio * 0.2 +
                    docRatio * 0.1 +
                    refactorRatio * 0.05
            )
        )
    );

    return {
        totalCommits,
        totalAdditions,
        totalDeletions,
        totalChangedLines: totalAdditions + totalDeletions,
        pullRequests,
        bugFixes,
        featureCommits,
        docsCommits,
        refactorCommits,
        prRatio,
        bugRatio,
        featureRatio,
        docRatio,
        refactorRatio,
        avgLinesPerCommit,
        avgFilesPerCommit,
        authors,
        timeline,
        topCriticalFiles,
        qualityScore,
        qualityLevel: getQualityLevel(qualityScore),
    };
}

function esc(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;");
}

function buildHtml(metrics) {
    const maxActivity = Math.max(...metrics.timeline.map((i) => i.count), 1);
    const maxCritical = Math.max(
        ...metrics.topCriticalFiles.map((i) => i.touches),
        1
    );

    const authorRows = metrics.authors
        .map(
            (a) => `
        <tr>
          <td>${esc(a.author)}</td>
          <td>${a.commits}</td>
          <td>${a.commitPercent}%</td>
          <td>${a.avgCommitSize}</td>
          <td>+${a.additions} / -${a.deletions}</td>
          <td>${a.filesChanged}</td>
        </tr>`
        )
        .join("");

    const timelineRows = metrics.timeline
        .map((entry) => {
            const width = Math.max(
                6,
                Math.round((entry.count / maxActivity) * 100)
            );
            return `
        <div class="bar-row">
          <span class="label">${esc(entry.month)}</span>
          <div class="bar-wrap"><div class="bar" style="width:${width}%"></div></div>
          <span class="value">${entry.count}</span>
        </div>`;
        })
        .join("");

    const criticalRows = metrics.topCriticalFiles
        .map((entry) => {
            const width = Math.max(
                6,
                Math.round((entry.touches / maxCritical) * 100)
            );
            return `
        <div class="bar-row">
          <span class="label">${esc(entry.file)}</span>
          <div class="bar-wrap"><div class="bar critical" style="width:${width}%"></div></div>
          <span class="value">${entry.touches}</span>
        </div>`;
        })
        .join("");

    const generatedAt = new Date().toLocaleString("fr-FR", {
        dateStyle: "medium",
        timeStyle: "short",
    });

    return `<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>TinyFarm - GitHub Stats</title>
  <style>
    :root {
      --bg: #fff4e0;
      --card: #f2d49f;
      --text: #1f2937;
      --muted: #6b7280;
      --accent: #55361b;
      --accent-2: #db9519;
      --critical: #db3131;
      --border: #55361b;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
      color: var(--text);
      background: radial-gradient(circle at top right, #dbeafe 0, transparent 45%), var(--bg);
    }
    .container {
      max-width: 1100px;
      margin: 0 auto;
      padding: 24px 16px 40px;
    }
    .title {
      margin: 0 0 8px;
      font-size: 1.8rem;
    }
    .subtitle {
      color: var(--muted);
      margin: 0 0 20px;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 12px;
      margin-bottom: 16px;
    }
    .card {
      background: var(--card);
  
      border-radius: 12px;
      padding: 14px;
      box-shadow: 0 2px 6px rgba(17, 24, 39, 0.04);
    }
    .kpi-label {
      color: var(--muted);
      font-size: 0.88rem;
      margin-bottom: 4px;
    }
    .kpi-value {
      font-size: 1.4rem;
      font-weight: 700;
    }
    h2 {
      margin: 20px 0 10px;
      font-size: 1.15rem;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 10px;
      overflow: hidden;
      font-size: 0.95rem;
    }
    th, td {
      text-align: left;
      padding: 10px;
      border-bottom: 1px solid #55361b;
    }
    th {
      background: #f2d49f;
      font-weight: 600;
    }
    tr:last-child td {
      border-bottom: 0;
    }
    .panel {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 12px;
      margin-top: 10px;
    }
    .bar-row {
      display: grid;
      grid-template-columns: minmax(95px, 240px) 1fr auto;
      gap: 10px;
      align-items: center;
      margin: 8px 0;
    }
    .label {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      font-size: 0.9rem;
    }
    .bar-wrap {
      height: 12px;
      border-radius: 999px;
      background: #f2d49f;
      overflow: hidden;
    }
    .bar {
      height: 100%;
      background: linear-gradient(90deg, var(--accent), var(--accent-2));
    }
    .bar.critical {
      background: linear-gradient(90deg, #ff2222, var(--critical));
    }
    .value {
      min-width: 28px;
      text-align: right;
      color: var(--muted);
    }
    .qualitative {
      line-height: 1.5;
    }
    .quality-score {
      font-weight: 700;
    }
    @media (max-width: 680px) {
      .bar-row {
        grid-template-columns: 1fr;
      }
      .value {
        text-align: left;
      }
    }
  </style>
</head>
<body>
  <main class="container">
    <h1 class="title">TinyFarm - Dashboard GitHub</h1>
    <p class="subtitle">Généré automatiquement le ${esc(generatedAt)} depuis l'historique Git local.</p>

    <section class="grid">
      <article class="card"><div class="kpi-label">Commits</div><div class="kpi-value">${metrics.totalCommits}</div></article>
      <article class="card"><div class="kpi-label">Lignes modifiées</div><div class="kpi-value">${metrics.totalChangedLines}</div></article>
      <article class="card"><div class="kpi-label">Pull requests (estimées)</div><div class="kpi-value">${metrics.pullRequests}</div></article>
      <article class="card"><div class="kpi-label">Bugs corrigés (estimés)</div><div class="kpi-value">${metrics.bugFixes}</div></article>
      <article class="card"><div class="kpi-label">Features (estimées)</div><div class="kpi-value">${metrics.featureCommits}</div></article>
      <article class="card"><div class="kpi-label">Taille moyenne commit</div><div class="kpi-value">${metrics.avgLinesPerCommit} lignes</div></article>
    </section>

    <h2>Commits par auteur</h2>
    <table>
      <thead>
        <tr>
          <th>Auteur</th>
          <th>Commits</th>
          <th>% commits</th>
          <th>Taille moyenne</th>
          <th>Lignes</th>
          <th>Fichiers touchés</th>
        </tr>
      </thead>
      <tbody>${authorRows}</tbody>
    </table>

    <h2>Activité dans le temps</h2>
    <div class="panel">${timelineRows || "<p>Aucune activité détectée.</p>"}</div>

    <h2>Fichiers critiques touchés</h2>
    <div class="panel">${criticalRows || "<p>Aucun fichier critique détecté.</p>"}</div>

    <h2>Analyse qualitative</h2>
    <section class="panel qualitative">
      <p><span class="quality-score">Niveau global: ${metrics.qualityLevel}</span> (score ${metrics.qualityScore}/100).</p>
      <p>PR: ${metrics.pullRequests} (${metrics.prRatio}%), Features: ${metrics.featureCommits} (${metrics.featureRatio}%), Bugs corrigés: ${metrics.bugFixes} (${metrics.bugRatio}%).</p>
      <p>Documentation: ${metrics.docsCommits} (${metrics.docRatio}%), Refactor: ${metrics.refactorCommits} (${metrics.refactorRatio}%).</p>
      <p>Lecture rapide: une part élevée de features + corrections avec un flux PR régulier indique une base active et structurée. Si le ratio PR est bas, il faut renforcer la revue. Si la taille moyenne des commits est trop haute, découper davantage les PR pour améliorer la qualité.</p>
    </section>
  </main>
</body>
</html>`;
}

function main() {
    const commits = parseGitHistory();
    const metrics = buildMetrics(commits);
    const html = buildHtml(metrics);

    fs.mkdirSync(outputDir, { recursive: true });
    fs.writeFileSync(outputFile, html, "utf8");
    process.stdout.write(
        `GitHub stats dashboard generated: ${path.relative(
            repoRoot,
            outputFile
        )}\n`
    );
}

main();
