/*
  Student Record System - Java Swing (8 columns, Add + Search, integer 1–100 only)
  Programmer: Kurt Michael D. Yalung 21-1174-522

  CSV columns expected (header line in your MOCK_DATA.csv):
    0: StudentID
    1: first_name
    2: last_name
    3: LAB WORK 1
    4: LAB WORK 2
    5: LAB WORK 3
    6: PRELIM EXAM
    7: ATTENDANCE GRADE

    How To Run:
    1. Ensure MOCK_DATA.csv is in the same directory as this Java file (PrelimExam/Java).
    2. On terminal, ensure cd is C:\Prog2\Prog2-9302-AY225-YALUNG\PrelimExam\Java
    3. Compile: javac StudentRecordsApp.java
    4. Run:     java StudentRecordsApp
*/

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.regex.Pattern;

public class StudentRecordsApp extends JFrame {

    private static final String CSV_PATH = "MOCK_DATA.csv"; // run from PrelimExam/Java

    // --- Theme (echoes your web UI) ---
    private static final Color NEON_PURPLE = Color.decode("#8A2BE2");
    private static final Color NEON_PINK   = Color.decode("#FF1493");
    private static final Color NEON_CYAN   = Color.decode("#00B7EB");
    private static final Color NEON_GREEN  = Color.decode("#39FF14");
    private static final Color MAROON      = Color.decode("#800000");
    private static final Color LIGHT_BG    = Color.decode("#F5F5FA");
    private static final Color WHITE_BG    = Color.decode("#FFFFFF");
    private static final Color TEXT_MAIN   = Color.decode("#1A1A1A");

    // --- Table + sorter (for search) ---
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    // --- Add form fields (with numeric constraints) ---
    private final JTextField tfId      = styledField();
    private final JTextField tfFirst   = styledField();
    private final JTextField tfLast    = styledField();
    private final JTextField tfLab1    = styledNumeric();
    private final JTextField tfLab2    = styledNumeric();
    private final JTextField tfLab3    = styledNumeric();
    private final JTextField tfPrelim  = styledNumeric();
    private final JTextField tfAttend  = styledNumeric();

    // --- Search UI ---
    private final JTextField tfSearch = styledField();
    private final JComboBox<String> cbField =
            new JComboBox<>(new String[]{"StudentID", "First Name", "Last Name"});
    private final JComboBox<String> cbMode  =
            new JComboBox<>(new String[]{"Contains", "Starts with", "Exact"});

    public StudentRecordsApp() {
        setTitle("📚 STUDENT RECORDS - Kurt Michael D. Yalung 21-1174-522");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(LIGHT_BG);
        setContentPane(root);

        // ===== Title =====
        JPanel titlePanel = roundedPanel();
        titlePanel.setLayout(new GridBagLayout());
        JLabel title = new JLabel("📚 STUDENT RECORDS SYSTEM - KURT MICHAEL D. YALUNG 21-1174-522");
        title.setForeground(NEON_PINK);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        titlePanel.add(title);
        root.add(titlePanel, BorderLayout.NORTH);

        // ===== Center stack (Search + Table + Footer) =====
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        root.add(center, BorderLayout.CENTER);

        // --- Search panel (matches web behavior) ---
        JPanel searchPanel = roundedPanel();
        searchPanel.setLayout(new GridBagLayout());
        GridBagConstraints sgc = new GridBagConstraints();
        sgc.insets = new Insets(8, 10, 8, 10);
        sgc.fill = GridBagConstraints.HORIZONTAL;
        sgc.gridy = 0;

        addSearchItem(searchPanel, sgc, 0, "Search Text", tfSearch);
        addSearchItem(searchPanel, sgc, 1, "Search By", cbField);
        addSearchItem(searchPanel, sgc, 2, "Match", cbMode);

        JPanel srBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        srBtns.setOpaque(false);
        JButton btnSearch = neonButton("SEARCH", NEON_CYAN);
        JButton btnReset  = neonButton("RESET", NEON_PURPLE);
        btnSearch.addActionListener(e -> applySearch());
        btnReset.addActionListener(e -> resetSearch());
        tfSearch.addActionListener(e -> applySearch()); // Enter triggers search
        srBtns.add(btnSearch); srBtns.add(btnReset);
        sgc.gridx = 3; sgc.weightx = 1; searchPanel.add(srBtns, sgc);

        center.add(searchPanel, BorderLayout.NORTH);

        // --- Table ---
        String[] cols = {"StudentID", "First Name", "Last Name", "Lab 1", "Lab 2", "Lab 3", "Prelim", "Attendance"};
        model  = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table  = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        table.setRowHeight(24);
        table.setGridColor(new Color(0,0,0,20));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(NEON_CYAN);
        table.setSelectionBackground(new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 40));
        table.setSelectionForeground(TEXT_MAIN);

        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 30));
        hdr.setForeground(NEON_PURPLE.darker());
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 12f));
        ((DefaultTableCellRenderer) hdr.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(styledBorder());
        JPanel tableContainer = roundedPanel();
        tableContainer.setLayout(new BorderLayout());
        JLabel tblHeader = new JLabel("📊 Student Records");
        tblHeader.setBorder(new EmptyBorder(12, 12, 12, 12));
        tblHeader.setForeground(NEON_PURPLE);
        tblHeader.setFont(tblHeader.getFont().deriveFont(Font.BOLD, 14f));
        tableContainer.add(tblHeader, BorderLayout.NORTH);
        tableContainer.add(scroll, BorderLayout.CENTER);
        center.add(tableContainer, BorderLayout.CENTER);

        // --- Footer (under the table) ---
        JLabel footer = new JLabel("Student Records © 2026 — Kurt Michael D. Yalung", SwingConstants.CENTER);
        footer.setForeground(new Color(85,85,85));
        footer.setBorder(new EmptyBorder(6, 0, 0, 0));
        center.add(footer, BorderLayout.SOUTH);

        // ===== Bottom: Add / Delete form =====
        JPanel formPanel = roundedPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int y = 0;
        addLabeled(formPanel, gc, y, 0, "Student ID", tfId);
        addLabeled(formPanel, gc, y, 1, "First Name", tfFirst);
        addLabeled(formPanel, gc, y, 2, "Last Name", tfLast);
        y++;
        addLabeled(formPanel, gc, y, 0, "Lab Work 1", tfLab1);
        addLabeled(formPanel, gc, y, 1, "Lab Work 2", tfLab2);
        addLabeled(formPanel, gc, y, 2, "Lab Work 3", tfLab3);
        y++;
        addLabeled(formPanel, gc, y, 0, "Prelim Exam", tfPrelim);
        addLabeled(formPanel, gc, y, 1, "Attendance", tfAttend);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton btnAdd    = neonButton("ADD STUDENT", NEON_GREEN);
        JButton btnDelete = neonButton("DELETE", NEON_PINK);
        JButton btnClear  = neonButton("CLEAR", NEON_PURPLE);
        btnAdd.addActionListener(this::onAdd);
        btnDelete.addActionListener(this::onDelete);
        btnClear.addActionListener(e -> clearForm());
        btnRow.add(btnAdd); btnRow.add(btnDelete); btnRow.add(btnClear);

        gc.gridx = 0; gc.gridy = ++y; gc.gridwidth = 3;
        formPanel.add(btnRow, gc);

        root.add(formPanel, BorderLayout.SOUTH);

        // Enforce/repair pasted values on blur (optional UX)
        attachNumericBlurSanitizer(tfLab1, tfLab2, tfLab3, tfPrelim, tfAttend);

        // Load CSV
        loadCsvIntoModel();

        setSize(1200, 720);
        setLocationRelativeTo(null);
    }

    // ===== CSV load (normalize to 1..100) =====
    private void loadCsvIntoModel() {
        try {
            Path p = Path.of(CSV_PATH);
            if (!Files.exists(p)) {
                JOptionPane.showMessageDialog(this,
                    "MOCK_DATA.csv not found at: " + p.toAbsolutePath() + "\nOpening with an empty table.",
                    "CSV Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(p.toFile()))) {
                String line; boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] t = line.split(",", -1);
                    if (first) { first = false; continue; } // skip header

                    String id   = get(t, 0);
                    String fn   = get(t, 1);
                    String ln   = get(t, 2);
                    int lab1    = toInt1to100(get(t, 3));
                    int lab2    = toInt1to100(get(t, 4));
                    int lab3    = toInt1to100(get(t, 5));
                    int prelim  = toInt1to100(get(t, 6));
                    int attend  = toInt1to100(get(t, 7));

                    model.addRow(new Object[]{id, fn, ln, lab1, lab2, lab3, prelim, attend});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading CSV:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Search (Contains / Starts / Exact) =====
    private void applySearch() {
        String q = Objects.toString(tfSearch.getText(), "").trim();
        if (q.isEmpty()) { sorter.setRowFilter(null); return; }

        int col = switch (Objects.toString(cbField.getSelectedItem(), "StudentID")) {
            case "First Name" -> 1;
            case "Last Name"  -> 2;
            default           -> 0;
        };

        String mode = Objects.toString(cbMode.getSelectedItem(), "Contains");
        String quoted = Pattern.quote(q);
        String regex = switch (mode) {
            case "Exact"       -> "(?i)^" + quoted + "$";
            case "Starts with" -> "(?i)^" + quoted + ".*";
            default            -> "(?i).*" + quoted + ".*";
        };

        sorter.setRowFilter(RowFilter.regexFilter(regex, col));
    }

    private void resetSearch() {
        tfSearch.setText("");
        cbField.setSelectedIndex(0);
        cbMode.setSelectedIndex(0);
        sorter.setRowFilter(null);
    }

    // ===== CRUD (strict 1–100 integers only; no decimals) =====
    private void onAdd(ActionEvent e) {
        String id = tfId.getText().trim();
        String fn = tfFirst.getText().trim();
        String ln = tfLast.getText().trim();
        if (id.isEmpty() || fn.isEmpty() || ln.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill Student ID, First Name, and Last Name.",
                    "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer lab1   = parseInt1to100(tfLab1.getText());
        Integer lab2   = parseInt1to100(tfLab2.getText());
        Integer lab3   = parseInt1to100(tfLab3.getText());
        Integer prelim = parseInt1to100(tfPrelim.getText());
        Integer attend = parseInt1to100(tfAttend.getText());

        if (lab1==null || lab2==null || lab3==null || prelim==null || attend==null) {
            JOptionPane.showMessageDialog(this,
                "Lab 1, Lab 2, Lab 3, Prelim, and Attendance must be whole numbers from 1 to 100 (no decimals).",
                "Invalid Score", JOptionPane.WARNING_MESSAGE);
            return;
        }

        model.addRow(new Object[]{id, fn, ln, lab1, lab2, lab3, prelim, attend});
        clearForm();
    }

    private void onDelete(ActionEvent e) {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int yes = JOptionPane.showConfirmDialog(this, "Delete selected row?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (yes == JOptionPane.YES_OPTION) {
            model.removeRow(table.convertRowIndexToModel(r));
        }
    }

    private void clearForm() {
        for (JTextField f : new JTextField[]{tfId, tfFirst, tfLast, tfLab1, tfLab2, tfLab3, tfPrelim, tfAttend}) {
            f.setText("");
        }
        tfId.requestFocus();
    }

    // ===== UI helpers =====
    private static JTextField styledField() {
        JTextField t = new JTextField();
        t.setBorder(new CompoundBorder(new LineBorder(MAROON, 2, true), new EmptyBorder(8, 10, 8, 10)));
        t.setForeground(NEON_PURPLE);
        t.setBackground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return t;
    }

    // Digits only while typing; max 3 chars; decimals blocked
    private static JTextField styledNumeric() {
        JTextField t = styledField();
        t.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                    return;
                }
                // limit to 3 digits unless replacing selection
                if (Character.isDigit(c) && t.getText().length() >= 3 && t.getSelectedText() == null) {
                    e.consume();
                }
            }
        });
        return t;
    }

    // On blur, sanitize to 1..100 or empty if invalid
    private void attachNumericBlurSanitizer(JTextField... fields) {
        for (JTextField f : fields) {
            f.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent e) {
                    Integer n = parseInt1to100(f.getText());
                    f.setText(n == null ? "" : String.valueOf(n));
                }
            });
        }
    }

    private static JPanel roundedPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(WHITE_BG);
                g2.fillRoundRect(0, 0, w, h, 16, 16);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 25),
                    w, 0, new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 25)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, Math.min(44, h), 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(styledBorder());
        p.setLayout(new BorderLayout());
        return p;
    }

    private static Border styledBorder() {
        return new CompoundBorder(new LineBorder(MAROON, 3, true), new EmptyBorder(12,12,12,12));
    }

    private static void addLabeled(JPanel panel, GridBagConstraints gc, int y, int col, String label, JComponent field) {
        JLabel lab = new JLabel(label);
        lab.setForeground(NEON_CYAN.darker());
        lab.setFont(lab.getFont().deriveFont(Font.BOLD, 12f));
        gc.gridy = y; gc.gridx = col*2;
        gc.weightx = 0; panel.add(lab, gc);
        gc.gridx = col*2 + 1; gc.weightx = 1; panel.add(field, gc);
    }

    private static void addSearchItem(JPanel panel, GridBagConstraints gc, int col, String label, JComponent field) {
        JLabel lab = new JLabel(label);
        lab.setForeground(NEON_CYAN.darker());
        lab.setFont(lab.getFont().deriveFont(Font.BOLD, 12f));
        gc.gridx = col; gc.weightx = (col == 0 ? 0.6 : 0.2);
        JPanel cell = new JPanel(new BorderLayout(6, 6));
        cell.setOpaque(false);
        cell.add(lab, BorderLayout.NORTH);
        cell.add(field, BorderLayout.CENTER);
        panel.add(cell, gc);
    }

    // Neon button with hover brighten
    private static JButton neonButton(String text, Color background) {
        final Color base = background;
        JButton b = new JButton(text);
        b.setForeground(Color.WHITE);
        b.setBackground(base);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(brighten(base, 1.10f)); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(base); }
        });
        return b;
    }

    private static Color brighten(Color c, float factor) {
        int r = Math.min(255, Math.round(c.getRed()   * factor));
        int g = Math.min(255, Math.round(c.getGreen() * factor));
        int b = Math.min(255, Math.round(c.getBlue()  * factor));
        return new Color(r, g, b);
    }

    // ===== Utilities & Validation =====
    private static String get(String[] a, int i) { return (i>=0 && i<a.length) ? a[i].trim() : ""; }

    // Strict parse for user input: returns null unless 1..100
    private static Integer parseInt1to100(String s) {
        try {
            int n = Integer.parseInt(s.trim());
            return (n < 1 || n > 100) ? null : n;
        } catch (Exception e) { return null; }
    }

    // Normalize CSV numbers to 1..100 (coerce out-of-range)
    private static int toInt1to100(String s) {
        try {
            int n = Integer.parseInt(s.trim());
            if (n < 1) n = 1;
            if (n > 100) n = 100;
            return n;
        } catch (Exception e) { return 1; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentRecordsApp().setVisible(true));
    }
}
