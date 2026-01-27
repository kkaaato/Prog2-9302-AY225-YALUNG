import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class PrelimGradeCalculator extends JFrame {
    private JTextField attendanceField, labWork1Field, labWork2Field, labWork3Field;
    private JTextArea resultArea;  // Keep as JTextArea (centering not supported but that's ok)
    private JButton submitButton, clearButton;
    private DecimalFormat df = new DecimalFormat("#.##");
    
    // Color scheme
    private Color neonPurple = new Color(138, 43, 226);
    private Color neonPink = new Color(255, 20, 147);
    private Color neonCyan = new Color(0, 255, 255);
    private Color neonGreen = new Color(57, 255, 20);
    private Color maroon = new Color(128, 0, 0);
    private Color lightBg = new Color(245, 245, 250);
    private Color whiteBg = Color.WHITE;
    
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
        
        JLabel noteLabel = new JLabel("📝 Enter grades (0-100 only)");
        noteLabel.setFont(new Font("Arial", Font.BOLD, 12));
        noteLabel.setForeground(neonPurple);
        
        inputPanel.add(noteLabel);
        inputPanel.add(new JLabel(""));
        
        inputPanel.add(createLabel("Attendance:"));
        attendanceField = createNumberField();
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
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(Color.WHITE);
        textField.setForeground(neonPurple);
        textField.setCaretColor(neonPink);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(maroon, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Restrict input to numbers only (0-100)
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
                // Only allow digits and decimal point
                if (!string.matches("[0-9.]*")) {
                    return false;
                }
                
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + string + 
                               currentText.substring(offset + length);
                
                if (newText.isEmpty()) return true;
                
                // Prevent multiple leading zeros (like "000")
                if (newText.matches("^0+[0-9]")) {
                    return false;
                }
                
                // Allow single zero or "0." but not multiple zeros
                if (newText.equals("0") || newText.equals("0.")) {
                    return true;
                }
                
                // Prevent leading zeros before other numbers
                if (newText.matches("^0[0-9]+")) {
                    return false;
                }
                
                // Check for valid number format and range
                try {
                    // Don't allow more than one decimal point
                    if (newText.chars().filter(ch -> ch == '.').count() > 1) {
                        return false;
                    }
                    
                    // Limit to 2 decimal places
                    if (newText.contains(".")) {
                        String[] parts = newText.split("\\.");
                        if (parts.length > 1 && parts[1].length() > 2) {
                            return false; // More than 2 decimal places
                        }
                    }
                    
                    // If it ends with a decimal point, it's valid (user is still typing)
                    if (newText.endsWith(".")) {
                        if (newText.length() == 1) return true; // Just "."
                        double baseValue = Double.parseDouble(newText.substring(0, newText.length() - 1));
                        return baseValue >= 0 && baseValue <= 100;
                    }
                    
                    double value = Double.parseDouble(newText);
                    return value >= 0 && value <= 100;
                } catch (NumberFormatException e) {
                    // Allow incomplete numbers but with restrictions
                    return newText.matches("^[0-9]{0,3}\\.?[0-9]{0,2}$");
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
            // Get and validate inputs
            double attendance = validateInput(attendanceField.getText(), "Attendance");
            double labWork1 = validateInput(labWork1Field.getText(), "Lab Work 1");
            double labWork2 = validateInput(labWork2Field.getText(), "Lab Work 2");
            double labWork3 = validateInput(labWork3Field.getText(), "Lab Work 3");
            
            // Calculations
            double labWorkAverage = (labWork1 + labWork2 + labWork3) / 3;
            double classStanding = (attendance * 0.40) + (labWorkAverage * 0.60);
            double requiredExamToPass = (75 - (classStanding * 0.70)) / 0.30;
            double requiredExamForExcellent = (100 - (classStanding * 0.70)) / 0.30;
            
            // Display simplified results
            StringBuilder result = new StringBuilder();
            result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append("  📊 YOUR GRADES\n");
            result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append(String.format("  Attendance:        %s\n", df.format(attendance)));
            result.append(String.format("  Lab Work Avg:      %s\n", df.format(labWorkAverage)));
            result.append(String.format("  Class Standing:    %s\n", df.format(classStanding)));
            result.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append("  🎯 REQUIRED PRELIM EXAM SCORES\n");
            result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            
            // For Passing
            result.append("  To PASS (75):\n");
            if (requiredExamToPass <= 0) {
                result.append("    ✅ Already Passing!\n");
            } else if (requiredExamToPass > 100) {
                result.append(String.format("    ❌ Need: %s (Impossible)\n", df.format(requiredExamToPass)));
            } else {
                result.append(String.format("    📝 Need: %s\n", df.format(requiredExamToPass)));
            }
            
            result.append("\n");
            
            // For Excellent
            result.append("  For EXCELLENT (100):\n");
            if (requiredExamForExcellent <= 0) {
                result.append("    ⭐ Already Excellent!\n");
            } else if (requiredExamForExcellent > 100) {
                result.append(String.format("    ❌ Need: %s (Impossible)\n", df.format(requiredExamForExcellent)));
            } else {
                result.append(String.format("    📝 Need: %s\n", df.format(requiredExamForExcellent)));
            }
            
            result.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append("🍀 Goodluck With The Exam!🍀 \n");
            
            resultArea.setText(result.toString());
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "❌ Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private double validateInput(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty!");
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            if (value < 0 || value > 100) {
                throw new IllegalArgumentException(fieldName + " must be between 0 and 100!");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number!");
        }
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