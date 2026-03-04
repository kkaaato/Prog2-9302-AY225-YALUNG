**Name:** Yalung, Kurt Michael D.
**Program:** BSIT-GD-1st yr
**Section:** Prog2-9302-AY225

This program reads the `vgchartz-2024.csv` dataset and generates an analytics summary covering global sales, top games, genres, regional breakdown, publishers, and consoles. Results are exported to `summary_report.csv`.

To ensure the right path of the vgchartz, In VS Code, **right-click** on the `vgchartz-2024.csv`, find and click on the **"Copy Path"**. Paste it when the program asks for the file path.


Running the Java Version

**Requirements:** Java JDK installed

```bash
# 1. Compile
javac VideoGameAnalytics.java

# 2. Run
java VideoGameAnalytics

# 3. Enter path when prompted
Enter dataset file path: C:\path\to\vgchartz-2024.csv
```

Running the JavaScript Version

**Requirements:** Node.js installed (https://nodejs.org)

```bash
# 1. Run
node VideoGameAnalytics.js

# 2. Enter path when prompted
Enter dataset file path: C:\path\to\vgchartz-2024.csv
```

Output
Both versions display the report in the terminal and export `summary_report.csv` in the same folder.