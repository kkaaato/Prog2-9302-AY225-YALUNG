import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Attendance Tracker Application
 * A Java Swing application for tracking student attendance with e-signature functionality
 */
public class AttendanceTracker extends JFrame {
    
    // Components declaration
    private JTextField nameField;
    private JTextField courseField;
    private JTextField timeInField;
    private JTextField eSignatureField;
    private JButton submitButton;
    private JButton clearButton;
    private JButton checkListButton;
    
    // Date and time formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructor to initialize the attendance tracker UI
     */
    public AttendanceTracker() {
        // Set up the main frame
        setTitle("Attendance Tracker System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false);
        
        // Initialize components
        initializeComponents();
        
        // Make the frame visible
        setVisible(true);
    }
    
    /**
     * Initialize all UI components and layout
     */
    private void initializeComponents() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.CYAN.darker());
        JLabel titleLabel = new JLabel("ATTENDANCE TRACKER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // Form panel with GridBagLayout for better control
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.35;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.65;
        nameField = new JTextField(15);
        nameField.setFont(new Font("Arial", Font.PLAIN, 11));
        formPanel.add(nameField, gbc);
        
        // Course/Year field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.35;
        JLabel courseLabel = new JLabel("Course/Year:");
        courseLabel.setFont(new Font("Arial", Font.BOLD, 11));
        formPanel.add(courseLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.65;
        courseField = new JTextField(15);
        courseField.setFont(new Font("Arial", Font.PLAIN, 11));
        formPanel.add(courseField, gbc);
        
        // Time In field (auto-generated)
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.35;
        JLabel timeLabel = new JLabel("Time In:");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 11));
        formPanel.add(timeLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.65;
        timeInField = new JTextField(15);
        timeInField.setFont(new Font("Arial", Font.PLAIN, 10));
        timeInField.setEditable(false);
        timeInField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(timeInField, gbc);
        
        // E-Signature field (auto-generated)
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.35;
        JLabel sigLabel = new JLabel("E-Signature:");
        sigLabel.setFont(new Font("Arial", Font.BOLD, 11));
        formPanel.add(sigLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.65;
        eSignatureField = new JTextField(15);
        eSignatureField.setFont(new Font("Arial", Font.PLAIN, 10));
        eSignatureField.setEditable(false);
        eSignatureField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(eSignatureField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));

        submitButton = new JButton("Submit");
        submitButton.setBackground(Color.GREEN.darker());
        submitButton.setForeground(Color.GREEN);
        submitButton.setFocusPainted(false);
        submitButton.setFont(new Font("Arial", Font.BOLD, 10));
        submitButton.addActionListener(new SubmitButtonListener());
        
        clearButton = new JButton("Clear");
        clearButton.setBackground(Color.RED.darker());
        clearButton.setForeground(Color.RED);
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("Arial", Font.BOLD, 10));
        clearButton.addActionListener(new ClearButtonListener());
        
        checkListButton = new JButton("Check List");
        checkListButton.setBackground(Color.BLUE.darker());
        checkListButton.setForeground(Color.BLUE);
        checkListButton.setFocusPainted(false);
        checkListButton.setFont(new Font("Arial", Font.BOLD, 10));
        checkListButton.addActionListener(new CheckListButtonListener());
        
        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(checkListButton);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Auto-generate time and e-signature on startup
        generateTimeAndSignature();
    }
    
    /**
     * Validate course/year format
     * @param courseYear The course/year string to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidCourseYear(String courseYear) {
        // Remove spaces and convert to uppercase for validation
        courseYear = courseYear.trim().toUpperCase();
        
        // Valid patterns available: Course code (BSIT, BSCS, BSIS, BSCpE) followed by dash and year (1-4)
        // Examples: BSIT-1, BSCS-2, BSIS-3, BSCpE-4
        String pattern = "^(BSIT|BSCS|BSIS|BSCPE)-[1-4]$";
        
        return courseYear.matches(pattern);
    }
    /**
     * Generate current time and unique e-signature
     */
    private void generateTimeAndSignature() {
        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        String timeIn = now.format(dateFormatter);
        timeInField.setText(timeIn);
        
        // Generate unique e-signature using UUID
        String eSignature = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        eSignatureField.setText(eSignature);
    }
    
    /**
     * Activated ActionListener for Submit button
     */
    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Validate input fields
            String name = nameField.getText().trim();
            String course = courseField.getText().trim();
            
            // Check if name is empty
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(
                    AttendanceTracker.this,
                    "Please enter student name!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Check if course field is empty
            if (course.isEmpty()) {
                JOptionPane.showMessageDialog(
                    AttendanceTracker.this,
                    "Please enter course and year!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Validate course/year format
            if (!isValidCourseYear(course)) {
                JOptionPane.showMessageDialog(
                    AttendanceTracker.this,
                    "Invalid Course/Year format!\n\n" +
                    "Required format: [COURSE]-[YEAR]\n\n" +
                    "Valid Courses: BSIT, BSCS, BSIS, BSCpE\n" +
                    "Valid Years: 1, 2, 3, 4\n\n" +
                    "Examples:\n" +
                    "  • BSIT-1\n" +
                    "  • BSCS-2\n" +
                    "  • BSIS-3\n" +
                    "  • BSCpE-4",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Convert to uppercase for consistency
            course = course.toUpperCase();
            
            // Get values
            String timeIn = timeInField.getText();
            String signature = eSignatureField.getText();
            
            // Format attendance record
            String record = String.format(
                "Name: %s | Course: %s | Time: %s | Signature: %s\n",
                name, course, timeIn, signature
            );
            
            // Save to file
            saveToFile(record);
            
            // Show success message
            JOptionPane.showMessageDialog(
                AttendanceTracker.this,
                "Attendance recorded successfully!\n\n" +
                "Name: " + name + "\n" +
                "Course: " + course + "\n" +
                "Time: " + timeIn + "\n" +
                "Signature: " + signature,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Clear form and regenerate time/signature
            clearForm();
            generateTimeAndSignature();
        }
    }
    
    /**
     * ActionListener for Clear button
     */
    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearForm();
            generateTimeAndSignature();
        }
    }
    
    /**
     * ActionListener for Check List button
     */
    private class CheckListButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showAttendanceList();
        }
    }
    
    /**
     * Clear all input fields
     */
    private void clearForm() {
        nameField.setText("");
        courseField.setText("");
        timeInField.setText("");
        eSignatureField.setText("");
    }
    
    /**
     * Save attendance record to file
     * @param record The attendance record to save
     */
    private void saveToFile(String record) {
        try (FileWriter writer = new FileWriter("attendance_records.txt", true)) {
            writer.write(record);
            writer.write("─".repeat(70) + "\n");
        } catch (IOException ex) {
            System.err.println("Error saving to file: " + ex.getMessage());
        }
    }
    
    /**
     * Display the attendance list in a new window
     */
    private void showAttendanceList() {
        // Create a new frame for the attendance list
        JFrame listFrame = new JFrame("Attendance List");
        listFrame.setSize(400, 300);
        listFrame.setLocationRelativeTo(this);
        
        // Create list model and JList to display records
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> recordList = new JList<>(listModel);
        recordList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        recordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Read the attendance records from file
        java.util.List<String> records = new java.util.ArrayList<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader("attendance_records.txt"))) {
            String line;
            StringBuilder currentRecord = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("─")) {
                    if (currentRecord.length() > 0) {
                        records.add(currentRecord.toString().trim());
                        listModel.addElement(currentRecord.toString().trim());
                        currentRecord = new StringBuilder();
                    }
                } else if (!line.trim().isEmpty()) {
                    currentRecord.append(line);
                }
            }
            
            // Add last record if exists
            if (currentRecord.length() > 0) {
                records.add(currentRecord.toString().trim());
                listModel.addElement(currentRecord.toString().trim());
            }
            
            if (listModel.isEmpty()) {
                listModel.addElement("No attendance records found.");
            }
        } catch (java.io.FileNotFoundException ex) {
            listModel.addElement("No attendance records found.");
            listModel.addElement("The file will be created when the first attendance is submitted.");
        } catch (IOException ex) {
            listModel.addElement("Error reading attendance records: " + ex.getMessage());
        }
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(recordList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("All Attendance Records (Select to Delete)"));
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // Delete button
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setBackground(Color.RED.darker());
        deleteButton.setForeground(Color.RED);
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> {
            int selectedIndex = recordList.getSelectedIndex();
            if (selectedIndex >= 0 && !listModel.isEmpty() && 
                !listModel.get(0).startsWith("No attendance") &&
                !listModel.get(0).startsWith("Error")) {
                
                // Confirm deletion
                int confirm = JOptionPane.showConfirmDialog(
                    listFrame,
                    "Are you sure you want to delete this attendance record?\n\n" + 
                    listModel.get(selectedIndex),
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // Remove from list
                    records.remove(selectedIndex);
                    listModel.remove(selectedIndex);
                    
                    // Rewrite file without deleted record
                    rewriteAttendanceFile(records);
                    
                    // Show success message
                    JOptionPane.showMessageDialog(
                        listFrame,
                        "Attendance record deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Update display if empty
                    if (listModel.isEmpty()) {
                        listModel.addElement("No attendance records found.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                    listFrame,
                    "Please select an attendance record to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        });
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(Color.BLUE.darker());
        refreshButton.setForeground(Color.BLUE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> {
            listFrame.dispose();
            showAttendanceList();
        });
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(Color.LIGHT_GRAY);
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> listFrame.dispose());
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Layout
        listFrame.setLayout(new BorderLayout());
        listFrame.add(scrollPane, BorderLayout.CENTER);
        listFrame.add(buttonPanel, BorderLayout.SOUTH);
        
        // Make visible
        listFrame.setVisible(true);
    }
    
    /**
     * Rewrite the attendance file without deleted records
     * @param records List of remaining records
     */
    private void rewriteAttendanceFile(java.util.List<String> records) {
        try (FileWriter writer = new FileWriter("attendance_records.txt", false)) {
            for (String record : records) {
                writer.write(record + "\n");
                writer.write("─".repeat(70) + "\n");
            }
        } catch (IOException ex) {
            System.err.println("Error rewriting file: " + ex.getMessage());
        }
    }
    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Set look and feel to system default
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Create and display the attendance tracker
                new AttendanceTracker();
            }
        });
    }
}