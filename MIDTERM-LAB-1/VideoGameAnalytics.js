// Programmer: Yalung, Kurt Michael D.
// Program: BSIT-GD-1st yr
// Section: Prog2-9302-AY225

const fs = require('fs');
const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

// ──────────────────────────────────────────
// HELPER: Parse a CSV line (handles quoted fields)
// ──────────────────────────────────────────
function parseCSVLine(line) {
    const fields = [];
    let current = '';
    let inQuotes = false;

    for (let i = 0; i < line.length; i++) {
        const c = line[i];
        if (c === '"') {
            if (inQuotes && line[i + 1] === '"') {
                current += '"';
                i++;
            } else {
                inQuotes = !inQuotes;
            }
        } else if (c === ',' && !inQuotes) {
            fields.push(current.trim());
            current = '';
        } else {
            current += c;
        }
    }
    fields.push(current.trim());
    return fields;
}

// ──────────────────────────────────────────
// HELPER: Parse a float safely
// ──────────────────────────────────────────
function parseFloat2(value) {
    const n = parseFloat(value);
    return isNaN(n) ? 0 : n;
}

// ──────────────────────────────────────────
// HELPER: Escape a CSV field
// ──────────────────────────────────────────
function escapeCSV(value) {
    if (value.includes(',') || value.includes('"') || value.includes('\n')) {
        return '"' + value.replace(/"/g, '""') + '"';
    }
    return value;
}

// ──────────────────────────────────────────
// HELPER: Truncate string
// ──────────────────────────────────────────
function truncate(s, maxLen) {
    return s.length <= maxLen ? s : s.substring(0, maxLen - 1) + '…';
}

// ──────────────────────────────────────────
// MAIN: Ask for file path (loop until valid)
// ──────────────────────────────────────────
function askFilePath() {
    console.log("============================================");
    console.log("       VIDEO GAME ANALYTICS PROGRAM        ");
    console.log("============================================");
    console.log("Welcome! This program analyzes the VGChartz");
    console.log("2024 dataset and generates a summary report ");
    console.log("covering global sales, top games, genres,   ");
    console.log("regions, publishers, and consoles.          ");
    console.log("A summary_report.csv will also be exported. ");
    console.log("============================================\n");

    askQuestion();
}

function askQuestion() {
    rl.question("Enter dataset file path: ", function (path) {
        if (fs.existsSync(path)) {
            console.log("File found. Processing...\n");
            rl.close();
            processFile(path);
        } else {
            console.log("Invalid file path. Try again.");
            askQuestion();
        }
    });
}

// ──────────────────────────────────────────
// PROCESS: Load, analyze, display, export
// ──────────────────────────────────────────
function processFile(filePath) {
    const lines = fs.readFileSync(filePath, 'utf8').split('\n');

    // Column indices
    // img,title,console,genre,publisher,developer,
    // critic_score,total_sales,na_sales,jp_sales,pal_sales,other_sales,
    // release_date,last_update
    const IDX_TITLE       = 1;
    const IDX_CONSOLE     = 2;
    const IDX_GENRE       = 3;
    const IDX_PUBLISHER   = 4;
    const IDX_CRITIC      = 6;
    const IDX_TOTAL_SALES = 7;
    const IDX_NA_SALES    = 8;
    const IDX_JP_SALES    = 9;
    const IDX_PAL_SALES   = 10;
    const IDX_OTHER_SALES = 11;

    // Load records
    const headers = parseCSVLine(lines[0]);
    const records = [];
    for (let i = 1; i < lines.length; i++) {
        if (lines[i].trim() === '') continue;
        const row = parseCSVLine(lines[i]);
        if (row.length === headers.length) records.push(row);
    }

    console.log(`Total records loaded: ${records.length}`);

    // --- Total global sales ---
    let totalGlobalSales = 0;
    for (const row of records) totalGlobalSales += parseFloat2(row[IDX_TOTAL_SALES]);

    // --- Top 5 best-selling games ---
    const sorted = [...records].sort((a, b) =>
        parseFloat2(b[IDX_TOTAL_SALES]) - parseFloat2(a[IDX_TOTAL_SALES]));
    const top5 = sorted.slice(0, 5);

    // --- Sales by genre ---
    const salesByGenre = {};
    for (const row of records) {
        const genre = row[IDX_GENRE] || 'Unknown';
        salesByGenre[genre] = (salesByGenre[genre] || 0) + parseFloat2(row[IDX_TOTAL_SALES]);
    }
    const genreList = Object.entries(salesByGenre).sort((a, b) => b[1] - a[1]);

    // --- Top publisher ---
    const salesByPublisher = {};
    for (const row of records) {
        const pub = row[IDX_PUBLISHER] || 'Unknown';
        salesByPublisher[pub] = (salesByPublisher[pub] || 0) + parseFloat2(row[IDX_TOTAL_SALES]);
    }
    const topPublisher = Object.entries(salesByPublisher).sort((a, b) => b[1] - a[1])[0];

    // --- Average critic score ---
    let totalScore = 0, scoreCount = 0;
    for (const row of records) {
        const score = parseFloat2(row[IDX_CRITIC]);
        if (score > 0) { totalScore += score; scoreCount++; }
    }
    const avgCriticScore = scoreCount > 0 ? totalScore / scoreCount : 0;

    // --- Regional sales ---
    let totalNA = 0, totalJP = 0, totalPAL = 0, totalOther = 0;
    for (const row of records) {
        totalNA    += parseFloat2(row[IDX_NA_SALES]);
        totalJP    += parseFloat2(row[IDX_JP_SALES]);
        totalPAL   += parseFloat2(row[IDX_PAL_SALES]);
        totalOther += parseFloat2(row[IDX_OTHER_SALES]);
    }

    // --- Top console ---
    const salesByConsole = {};
    for (const row of records) {
        const console_ = row[IDX_CONSOLE] || 'Unknown';
        salesByConsole[console_] = (salesByConsole[console_] || 0) + parseFloat2(row[IDX_TOTAL_SALES]);
    }
    const topConsole = Object.entries(salesByConsole).sort((a, b) => b[1] - a[1])[0];

    // ──────────────────────────────────────────
    // Display Results
    // ──────────────────────────────────────────
    console.log("============================================");
    console.log("       VIDEO GAME ANALYTICS REPORT         ");
    console.log("============================================");
    console.log(`Total Records Analyzed : ${records.length.toLocaleString()}`);
    console.log(`Total Global Sales     : ${totalGlobalSales.toFixed(2)} million units`);
    console.log(`Average Critic Score   : ${avgCriticScore.toFixed(2)} / 10`);

    console.log("\n--- Top 5 Best-Selling Games ---");
    console.log("Rank  Title                                         Console    Sales (M)");
    console.log("-".repeat(70));
    top5.forEach((row, i) => {
        console.log(
            `${String(i + 1).padEnd(6)}${truncate(row[IDX_TITLE], 46).padEnd(46)}${row[IDX_CONSOLE].padEnd(11)}${parseFloat2(row[IDX_TOTAL_SALES]).toFixed(2)}`
        );
    });

    console.log("\n--- Sales by Genre (Top 5) ---");
    console.log("Genre                Sales (M)");
    console.log("-".repeat(35));
    genreList.slice(0, 5).forEach(([genre, sales]) => {
        console.log(`${genre.padEnd(21)}${sales.toFixed(2)}`);
    });

    console.log("\n--- Regional Sales Breakdown ---");
    console.log(`  North America : ${totalNA.toFixed(2)} million`);
    console.log(`  Japan         : ${totalJP.toFixed(2)} million`);
    console.log(`  PAL Region    : ${totalPAL.toFixed(2)} million`);
    console.log(`  Other Regions : ${totalOther.toFixed(2)} million`);

    console.log("\n--- Top Publisher ---");
    console.log(`  ${topPublisher[0]} (${topPublisher[1].toFixed(2)} million units)`);

    console.log("\n--- Top Console ---");
    console.log(`  ${topConsole[0]} (${topConsole[1].toFixed(2)} million units)`);
    console.log("============================================");

    // ──────────────────────────────────────────
    // Export summary_report.csv
    // ──────────────────────────────────────────
    let csv = '';

    // Section 1: Overview
    csv += 'Section,Metric,Value\n';
    csv += `Overview,Total Records,${records.length}\n`;
    csv += `Overview,Total Global Sales (millions),${totalGlobalSales.toFixed(2)}\n`;
    csv += `Overview,Average Critic Score,${avgCriticScore.toFixed(2)}\n`;
    csv += `Overview,Top Publisher,${topPublisher[0]}\n`;
    csv += `Overview,Top Publisher Sales (millions),${topPublisher[1].toFixed(2)}\n`;
    csv += `Overview,Top Console,${topConsole[0]}\n`;
    csv += `Overview,Top Console Sales (millions),${topConsole[1].toFixed(2)}\n\n`;

    // Section 2: Top 5 Games
    csv += 'Top 5 Games,Rank,Title,Console,Total Sales (millions)\n';
    top5.forEach((row, i) => {
        csv += `Top 5 Games,${i + 1},${escapeCSV(row[IDX_TITLE])},${escapeCSV(row[IDX_CONSOLE])},${parseFloat2(row[IDX_TOTAL_SALES]).toFixed(2)}\n`;
    });
    csv += '\n';

    // Section 3: Genre Sales
    csv += 'Genre Sales,Genre,Total Sales (millions)\n';
    genreList.forEach(([genre, sales]) => {
        csv += `Genre Sales,${escapeCSV(genre)},${sales.toFixed(2)}\n`;
    });
    csv += '\n';

    // Section 4: Regional Sales
    csv += 'Regional Sales,Region,Total Sales (millions)\n';
    csv += `Regional Sales,North America,${totalNA.toFixed(2)}\n`;
    csv += `Regional Sales,Japan,${totalJP.toFixed(2)}\n`;
    csv += `Regional Sales,PAL Region,${totalPAL.toFixed(2)}\n`;
    csv += `Regional Sales,Other,${totalOther.toFixed(2)}\n`;

    fs.writeFile('summary_report.csv', csv, (err) => {
        if (err) {
            console.log("Error writing CSV: " + err.message);
        } else {
            console.log("\nSummary report exported to: summary_report.csv");
        }
    });
}

askFilePath();