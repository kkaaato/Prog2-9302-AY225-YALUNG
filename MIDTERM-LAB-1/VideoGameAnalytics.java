// Programmer: Yalung, Kurt Michael D.
// Program: BSIT-GD-1st yr
// Section: Prog2-9302-AY225

import java.io.*;
import java.util.*;

public class VideoGameAnalytics {

    public static void main(String[] args) {
        // ──────────────────────────────────────────
        // STEP 1: Prompt user for dataset file path
        // ──────────────────────────────────────────
        System.out.println("============================================");
        System.out.println("       VIDEO GAME ANALYTICS PROGRAM        ");
        System.out.println("============================================");
        System.out.println("Welcome! This program analyzes the VGChartz");
        System.out.println("2024 dataset and generates a summary report ");
        System.out.println("covering global sales, top games, genres,   ");
        System.out.println("regions, publishers, and consoles.          ");
        System.out.println("A summary_report.csv will also be exported. ");
        System.out.println("============================================\n");

        Scanner input = new Scanner(System.in);
        File file;

        while (true) {
            System.out.print("Enter dataset file path: ");
            String path = input.nextLine().trim();
            file = new File(path);

            if (!file.exists() || !file.isFile()) {
                System.out.println("File not found. Please try again.");
                continue;
            }

            // Validate CSV format by checking the header line
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String header = br.readLine();
                if (header == null || !header.contains(",")) {
                    System.out.println("File does not appear to be a valid CSV. Please try again.");
                    continue;
                }
            } catch (IOException e) {
                System.out.println("File is not readable: " + e.getMessage());
                continue;
            }

            System.out.println("File found. Processing...\n");
            break;
        }

        // ──────────────────────────────────────────
        // STEP 2: Load dataset into DataRecord list
        // ──────────────────────────────────────────
        List<DataRecord> records = new ArrayList<>();
        String[] headers = null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirst = true;

            while ((line = br.readLine()) != null) {
                if (isFirst) {
                    headers = parseCSVLine(line);
                    isFirst = false;
                    continue;
                }
                String[] row = parseCSVLine(line);
                if (row.length == headers.length) {
                    records.add(new DataRecord(row));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            input.close();
            return;
        }

        System.out.println("Total records loaded: " + records.size());

        // ──────────────────────────────────────────
        // STEP 3: Perform Analytics
        // ──────────────────────────────────────────

        // --- 3a. Total global sales ---
        double totalGlobalSales = 0;
        for (DataRecord r : records) {
            totalGlobalSales += r.totalSales;
        }

        // --- 3b. Top 5 best-selling games ---
        List<DataRecord> sorted = new ArrayList<>(records);
        sorted.sort((a, b) -> Double.compare(b.totalSales, a.totalSales));
        List<DataRecord> top5Games = sorted.subList(0, Math.min(5, sorted.size()));

        // --- 3c. Sales by genre ---
        Map<String, Double> salesByGenre = new LinkedHashMap<>();
        for (DataRecord r : records) {
            salesByGenre.merge(r.genre, r.totalSales, Double::sum);
        }
        List<Map.Entry<String, Double>> genreList = new ArrayList<>(salesByGenre.entrySet());
        genreList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // --- 3d. Top publisher by total sales ---
        Map<String, Double> salesByPublisher = new HashMap<>();
        for (DataRecord r : records) {
            salesByPublisher.merge(r.publisher, r.totalSales, Double::sum);
        }
        String topPublisher = Collections.max(salesByPublisher.entrySet(),
                Map.Entry.comparingByValue()).getKey();
        double topPublisherSales = salesByPublisher.get(topPublisher);

        // --- 3e. Average critic score ---
        double totalScore = 0;
        int scoreCount = 0;
        for (DataRecord r : records) {
            if (r.criticScore > 0) {
                totalScore += r.criticScore;
                scoreCount++;
            }
        }
        double avgCriticScore = scoreCount > 0 ? totalScore / scoreCount : 0;

        // --- 3f. Regional sales breakdown ---
        double totalNA = 0, totalJP = 0, totalPAL = 0, totalOther = 0;
        for (DataRecord r : records) {
            totalNA    += r.naSales;
            totalJP    += r.jpSales;
            totalPAL   += r.palSales;
            totalOther += r.otherSales;
        }

        // --- 3g. Top console by total sales ---
        Map<String, Double> salesByConsole = new HashMap<>();
        for (DataRecord r : records) {
            salesByConsole.merge(r.console, r.totalSales, Double::sum);
        }
        String topConsole = Collections.max(salesByConsole.entrySet(),
                Map.Entry.comparingByValue()).getKey();
        double topConsoleSales = salesByConsole.get(topConsole);

        // ──────────────────────────────────────────
        // STEP 4: Display Formatted Results
        // ──────────────────────────────────────────
        System.out.println("============================================");
        System.out.println("       VIDEO GAME ANALYTICS REPORT         ");
        System.out.println("============================================");
        System.out.printf("Total Records Analyzed : %,d%n", records.size());
        System.out.printf("Total Global Sales     : %.2f million units%n", totalGlobalSales);
        System.out.printf("Average Critic Score   : %.2f / 10%n", avgCriticScore);

        System.out.println("\n--- Top 5 Best-Selling Games ---");
        System.out.printf("%-5s %-45s %-10s %s%n", "Rank", "Title", "Console", "Sales (M)");
        System.out.println("-".repeat(70));
        for (int i = 0; i < top5Games.size(); i++) {
            DataRecord r = top5Games.get(i);
            System.out.printf("%-5d %-45s %-10s %.2f%n",
                    i + 1,
                    truncate(r.title, 44),
                    r.console,
                    r.totalSales);
        }

        System.out.println("\n--- Sales by Genre (Top 5) ---");
        System.out.printf("%-20s %s%n", "Genre", "Sales (M)");
        System.out.println("-".repeat(35));
        for (int i = 0; i < Math.min(5, genreList.size()); i++) {
            System.out.printf("%-20s %.2f%n",
                    genreList.get(i).getKey(),
                    genreList.get(i).getValue());
        }

        System.out.println("\n--- Regional Sales Breakdown ---");
        System.out.printf("  North America : %.2f million%n", totalNA);
        System.out.printf("  Japan         : %.2f million%n", totalJP);
        System.out.printf("  PAL Region    : %.2f million%n", totalPAL);
        System.out.printf("  Other Regions : %.2f million%n", totalOther);

        System.out.println("\n--- Top Publisher ---");
        System.out.printf("  %s (%.2f million units)%n", topPublisher, topPublisherSales);

        System.out.println("\n--- Top Console ---");
        System.out.printf("  %s (%.2f million units)%n", topConsole, topConsoleSales);
        System.out.println("============================================");

        // ──────────────────────────────────────────
        // STEP 5: Export summary_report.csv
        // ──────────────────────────────────────────
        String outputPath = "summary_report.csv";

        try (FileWriter fw = new FileWriter(outputPath)) {
            fw.write("Section,Metric,Value\n");
            fw.write("Overview,Total Records," + records.size() + "\n");
            fw.write("Overview,Total Global Sales (millions)," + String.format("%.2f", totalGlobalSales) + "\n");
            fw.write("Overview,Average Critic Score," + String.format("%.2f", avgCriticScore) + "\n");
            fw.write("Overview,Top Publisher," + topPublisher + "\n");
            fw.write("Overview,Top Publisher Sales (millions)," + String.format("%.2f", topPublisherSales) + "\n");
            fw.write("Overview,Top Console," + topConsole + "\n");
            fw.write("Overview,Top Console Sales (millions)," + String.format("%.2f", topConsoleSales) + "\n");
            fw.write("\n");

            fw.write("Top 5 Games,Rank,Title,Console,Total Sales (millions)\n");
            for (int i = 0; i < top5Games.size(); i++) {
                DataRecord r = top5Games.get(i);
                fw.write("Top 5 Games," + (i + 1) + ","
                        + escapeCSV(r.title) + ","
                        + escapeCSV(r.console) + ","
                        + String.format("%.2f", r.totalSales) + "\n");
            }
            fw.write("\n");

            fw.write("Genre Sales,Genre,Total Sales (millions)\n");
            for (Map.Entry<String, Double> entry : genreList) {
                fw.write("Genre Sales," + escapeCSV(entry.getKey()) + ","
                        + String.format("%.2f", entry.getValue()) + "\n");
            }
            fw.write("\n");

            fw.write("Regional Sales,Region,Total Sales (millions)\n");
            fw.write("Regional Sales,North America," + String.format("%.2f", totalNA) + "\n");
            fw.write("Regional Sales,Japan," + String.format("%.2f", totalJP) + "\n");
            fw.write("Regional Sales,PAL Region," + String.format("%.2f", totalPAL) + "\n");
            fw.write("Regional Sales,Other," + String.format("%.2f", totalOther) + "\n");

            System.out.println("\nSummary report exported to: " + outputPath);

        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }

        input.close();
    }

    // ──────────────────────────────────────────
    // HELPER METHODS
    // ──────────────────────────────────────────

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    private static String escapeCSV(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }
}

// ──────────────────────────────────────────
// DATA CLASS
// ──────────────────────────────────────────
class DataRecord {
    String title, console, genre, publisher;
    double totalSales, naSales, jpSales, palSales, otherSales, criticScore;

    public DataRecord(String[] row) {
        this.title       = row[1];
        this.console     = row[2].isEmpty() ? "Unknown" : row[2];
        this.genre       = row[3].isEmpty() ? "Unknown" : row[3];
        this.publisher   = row[4].isEmpty() ? "Unknown" : row[4];
        this.criticScore = parseDouble(row[6]);
        this.totalSales  = parseDouble(row[7]);
        this.naSales     = parseDouble(row[8]);
        this.jpSales     = parseDouble(row[9]);
        this.palSales    = parseDouble(row[10]);
        this.otherSales  = parseDouble(row[11]);
    }

    private static double parseDouble(String value) {
        try {
            return value == null || value.isEmpty() ? 0 : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
