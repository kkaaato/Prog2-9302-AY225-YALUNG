import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.DecimalFormat;

public class PrelimGradeCalculator extends JFrame {
    private JTextField attendanceField, labWork1Field, labWork2Field, labWork3Field;
    private JTextArea resultArea;
    private JButton submitButton, clearButton;
    private final DecimalFormat df = new DecimalFormat("#.##");

    // Color scheme
    private final Color neonPurple = new Color(138, 43, 226);
    private final Color neonPink = new Color(255, 20, 147);
    private final Color neonCyan = new Color(0, 255, 255);
    private final Color neonGreen = new Color(57, 255, 20);
    private final Color maroon = new Color(128, 0, 0);
    private final Color lightBg = new Color(245, 245, 250);
    private final Color whiteBg = Color.WHITE;

    public PrelimGradeCalculator() {
        setTitle("Prelim Grade Calculator");
        setSize(550, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(lightBg);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(lightBg);
        JLabel titleLabel = new JLabel("✨ PRELIM GRADE CALCULATOR ✨");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(neonPink);
        titlePanel.add(titleLabel);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 12));
        inputPanel.setBackground(whiteBg);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(maroon, 3),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel noteLabel = new JLabel("📝 Attendance (0-5), Lab Work (0-100)");
        noteLabel.setFont(new Font("Arial", Font.BOLD, 12));
        noteLabel.setForeground(neonPurple);

        inputPanel.add(noteLabel);
        inputPanel.add(new JLabel(""));

        inputPanel.add(createLabel("Attendance:"));
        attendanceField = createNumberField(true);
        inputPanel.add(attendanceField);

        inputPanel.add(createLabel("Lab Work 1:"));
        labWork1Field = createNumberField();
        inputPanel.add(labWork1Field);

        inputPanel.add(createLabel("Lab Work 2:"));
        labWork2Field = createNumberField();
        inputPanel.add(labWork2Field);

        inputPanel.add(createLabel("Lab Work 3:"));
        labWork3Field = createNumberField();
        inputPanel.add(labWork3Field);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(lightBg);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        submitButton = createRoundedButton("SUBMIT", neonGreen);
        submitButton.addActionListener(e -> calculateGrades());

        clearButton = createRoundedButton("CLEAR", neonPink);
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);

        // Result Panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(whiteBg);
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(maroon, 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        resultArea.setBackground(whiteBg);
        resultArea.setForeground(neonPurple);
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // Add all panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(lightBg);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(resultPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(neonCyan);
        return label;
    }

    private JTextField createNumberField() {
        return createNumberField(false);
    }

    private JTextField createNumberField(boolean isAttendance) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(Color.WHITE);
        textField.setForeground(neonPurple);
        textField.setCaretColor(neonPink);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(maroon, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;
                if (isValid(fb, offset, string, 0)) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;
                if (isValid(fb, offset, text, length)) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            private boolean isValid(FilterBypass fb, int offset, String string, int length)
                    throws BadLocationException {
                if (isAttendance) {
                    // Attendance: only allow digits 0-5
                    if (!string.matches("[0-9]*")) return false;

                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + string +
                            currentText.substring(offset + length);

                    if (newText.isEmpty()) return true;

                    // Prevent leading zeros like "00", "01"
                    if (newText.matches("^0[0-9]")) return false;

                    try {
                        int value = Integer.parseInt(newText);
                        return value >= 0 && value <= 5;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                } else {
                    // Lab work: allow digits and decimal point (0-100)
                    if (!string.matches("[0-9.]*")) return false;

                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + string +
                            currentText.substring(offset + length);

                    if (newText.isEmpty()) return true;

                    // Prevent multiple leading zeros (like "000")
                    if (newText.matches("^0+[0-9]")) return false;

                    // Allow single zero or "0." but not multiple zeros
                    if (newText.equals("0") || newText.equals("0.")) return true;

                    // Prevent leading zeros before other numbers
                    if (newText.matches("^0[0-9]+")) return false;

                    try {
                        if (newText.chars().filter(ch -> ch == '.').count() > 1) return false;

                        if (newText.contains(".")) {
                            String[] parts = newText.split("\\.");
                            if (parts.length > 1 && parts[1].length() > 2) return false;
                        }

                        if (newText.endsWith(".")) {
                            if (newText.length() == 1) return true;
                            double baseValue = Double.parseDouble(newText.substring(0, newText.length() - 1));
                            return baseValue >= 0 && baseValue <= 100;
                        }

                        double value = Double.parseDouble(newText);
                        return value >= 0 && value <= 100;
                    } catch (NumberFormatException e) {
                        return newText.matches("^[0-9]{0,3}\\.?[0-9]{0,2}$");
                    }
                }
            }
        });

        return textField;
    }

    private JButton createRoundedButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + textHeight) / 2 - 2;

                g2.drawString(getText(), x, y);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color.brighter());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                g2.dispose();
            }
        };

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(140, 40));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void calculateGrades() {
        try {
            final int totalSessions = 5;

            // 1) Get attendance (PRESENT sessions)
            int attendanceCount = (int) validateInput(attendanceField.getText(), "Attendance");

            // 2) Ask for EXCUSED absences only if attendance is incomplete
            int excusedAbsences = 0;
            int missingSessions = totalSessions - attendanceCount;

            if (attendanceCount < totalSessions) {
                int resp = JOptionPane.showConfirmDialog(
                        this,
                        "You have " + missingSessions + " absence(s).\nAny excused absences?",
                        "Excused Absences",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (resp == JOptionPane.YES_OPTION) {
                    Integer chosen = promptExcusedAbsences(missingSessions);
                    excusedAbsences = (chosen != null) ? chosen : 0; // cancel -> 0
                }
            }

            // 3) Compute unexcused and check auto-fail rule (3+ unexcused absences)
            int unexcusedAbsences = Math.max(0, totalSessions - attendanceCount - excusedAbsences);
            if (unexcusedAbsences >= 3) {
                StringBuilder result = new StringBuilder();
                result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                result.append("  ❌ AUTOMATIC FAILURE\n");
                result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                result.append(String.format("  Present:            %d/%d\n", attendanceCount, totalSessions));
                result.append(String.format("  Excused Absences:   %d\n", excusedAbsences));
                result.append(String.format("  Unexcused Absences: %d\n\n", unexcusedAbsences));
                result.append("  You have 3 or more UNEXCUSED absences.\n");
                result.append("  You are automatically FAILED.\n\n");
                result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                resultArea.setText(result.toString());
                return;
            }

            // 4) Effective attendance (present + excused, capped)
            int effectiveAttendance = Math.min(totalSessions, attendanceCount + excusedAbsences);
            double attendancePercentage = (effectiveAttendance / (double) totalSessions) * 100.0;

            // 5) Lab inputs
            double labWork1 = validateInput(labWork1Field.getText(), "Lab Work 1");
            double labWork2 = validateInput(labWork2Field.getText(), "Lab Work 2");
            double labWork3 = validateInput(labWork3Field.getText(), "Lab Work 3");

            // Calculations
            double labWorkAverage = (labWork1 + labWork2 + labWork3) / 3.0;
            double classStanding = (attendancePercentage * 0.40) + (labWorkAverage * 0.60);
            double requiredExamToPass = (75 - (classStanding * 0.70)) / 0.30;
            double requiredExamForExcellent = (100 - (classStanding * 0.70)) / 0.30;

            // Display results
            StringBuilder result = new StringBuilder();
            result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append("  📊 YOUR GRADES\n");
            result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append(String.format("  Present:            %d/%d\n", attendanceCount, totalSessions));
            result.append(String.format("  Excused Absences:   %d\n", excusedAbsences));
            result.append(String.format("  Unexcused Absences: %d\n", unexcusedAbsences));
            result.append(String.format("  Effective Attend.:  %d/%d\n", effectiveAttendance, totalSessions));
            result.append(String.format("  Attendance %%:      %s\n", df.format(attendancePercentage)));
            result.append(String.format("  Lab Work 1:        %s\n", df.format(labWork1)));
            result.append(String.format("  Lab Work 2:        %s\n", df.format(labWork2)));
            result.append(String.format("  Lab Work 3:        %s\n", df.format(labWork3)));
            result.append(String.format("  Lab Work Avg:      %s\n", df.format(labWorkAverage)));
            result.append(String.format("  Class Standing:    %s\n", df.format(classStanding)));
            result.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append("  🎯 REQUIRED PRELIM EXAM SCORES\n");
            result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

            if (requiredExamToPass > 100 && requiredExamForExcellent > 100) {
                result.append("  ❌ GOAL NOT ACHIEVABLE\n\n");
                result.append("  Your current grades are too low to achieve\n");
                result.append("  a passing or excellent score, even with a\n");
                result.append("  perfect exam.\n\n");
                result.append("  💪 Don't give up! Try again next year\n");
                result.append("  with better preparation.\n");
            } else {
                result.append("  To PASS (75):\n");
                if (requiredExamToPass <= 0) {
                    result.append("    ✅ Already Passing!\n");
                } else if (requiredExamToPass > 100) {
                    result.append("    ❌ Score not achievable\n");
                } else {
                    result.append(String.format("    📝 Need: %s %s\n",
                            df.format(requiredExamToPass), getDifficultyAssessment(requiredExamToPass)));
                }

                result.append("\n");

                result.append("  For EXCELLENT (100):\n");
                if (requiredExamForExcellent <= 0) {
                    result.append("    ⭐ Already Excellent!\n");
                } else if (requiredExamForExcellent > 100) {
                    result.append("    ❌ Score not achievable\n");
                } else {
                    result.append(String.format("    📝 Need: %s %s\n",
                            df.format(requiredExamForExcellent), getDifficultyAssessment(requiredExamForExcellent)));
                }
            }

            result.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            resultArea.setText(result.toString());

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "❌ Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getDifficultyAssessment(double requiredScore) {
        if (requiredScore <= 30) {
            return "✅ (Very Achievable!)";
        } else if (requiredScore <= 50) {
            return "✅ (Achievable)";
        } else if (requiredScore <= 75) {
            return "⚠️ (Moderate Difficulty)";
        } else {
            return "❌ (Very Challenging)";
        }
    }

    private double validateInput(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty!");
        }
        try {
            double value = Double.parseDouble(input.trim());
            if ("Attendance".equals(fieldName)) {
                if (value < 0 || value > 5) {
                    throw new IllegalArgumentException(fieldName + " must be between 0 and 5!");
                }
            } else {
                if (value < 0 || value > 100) {
                    throw new IllegalArgumentException(fieldName + " must be between 0 and 100!");
                }
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number!");
        }
    }

    // Spinner-based dialog to choose excused absences within allowed range
    private Integer promptExcusedAbsences(int missingSessions) {
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, missingSessions, 1);
        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
        spinner.setEditor(editor);
        
        // Disable text field editing - scroll only
        JFormattedTextField textField = editor.getTextField();
        textField.setEditable(false);
        
        // Add document filter to prevent any text input
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                // Block all text insertion
                Toolkit.getDefaultToolkit().beep();
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                // Block all text replacement
                Toolkit.getDefaultToolkit().beep();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Select number of excused absences:"), BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Excused Absences (0-" + missingSessions + ")",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            return (Integer) spinner.getValue();
        }
        return null; // user cancelled
    }

    private void clearFields() {
        attendanceField.setText("");
        labWork1Field.setText("");
        labWork2Field.setText("");
        labWork3Field.setText("");
        resultArea.setText("");
        attendanceField.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PrelimGradeCalculator calculator = new PrelimGradeCalculator();
            calculator.setVisible(true);
        });
    }
}