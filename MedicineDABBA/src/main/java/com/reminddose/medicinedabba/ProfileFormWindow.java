/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.sql.*;
import com.reminddose.medicinedabba.database.DBConnection;

/**
 * A window for users to enter their profile and medical information.
 */
public class ProfileFormWindow extends JFrame {

    // Define colors to match the main application theme
    private final Color COLOR_BACKGROUND = new Color(243, 243, 191); // pale green
    private final Color COLOR_PANEL_BG = new Color(255, 255, 255);   // White
    private final Color COLOR_BUTTON = new Color(111, 189, 82);     // Green
    private final Color COLOR_TEXT_PRIMARY = new Color(51, 51, 51); // Dark text
    
    // Form fields
    private JTextField fullNameField;
    private JTextField phoneField;
    private JTextField ageField;
    private JTextField heightField;
    private JComboBox<String> genderComboBox;
    private JTextField weightField;
    private JTextArea medicalIssuesArea;
    private long userId;

    public ProfileFormWindow(long userId) {
        this.userId = userId;
        
        // --- FRAME SETUP ---
        setTitle("Profile and Medical Information");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1050, 730));
        setMinimumSize(new Dimension(1000, 750));
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BACKGROUND);

        // Set application icon
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        }

        // --- MAIN SCROLLABLE PANEL ---
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
        add(scrollPane, BorderLayout.CENTER);

        // --- CONTAINER PANEL FOR ALL FORM ELEMENTS ---
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(COLOR_BACKGROUND);
        containerPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        scrollPane.setViewportView(containerPanel);

        // --- PROFILE INFORMATION PANEL ---
        JPanel profilePanel = createSectionPanel("Profile Information");
        addProfileFields(profilePanel);
        containerPanel.add(profilePanel);

        // Add spacing between the sections
        containerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- MEDICAL INFORMATION PANEL ---
        JPanel medicalPanel = createSectionPanel("Medical Information");
        addMedicalFields(medicalPanel);
        containerPanel.add(medicalPanel);

        // --- SUBMIT BUTTON PANEL ---
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        submitPanel.setOpaque(false);
        submitPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton submitButton = new RoundedButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBackground(COLOR_BUTTON);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(new EmptyBorder(10, 30, 10, 30));
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add action listener to submit button
        submitButton.addActionListener(e -> saveProfileData());
        
        submitPanel.add(submitButton);

        containerPanel.add(submitPanel);

        // Load existing profile data if available
        loadProfileData();
        
        // Finalize
        setLocationRelativeTo(null);
    }

    /**
     * Helper method to create a styled section panel with a title.
     * @param title The title for the section.
     * @return A styled JPanel.
     */
    private JPanel createSectionPanel(String title) {
        // Use the custom RoundedPanel for the main container
        RoundedPanel panel = new RoundedPanel(25, COLOR_PANEL_BG);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Adds the profile form fields to the specified panel.
     * @param panel The panel to add fields to.
     */
    private void addProfileFields(JPanel panel) {
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); // Increased spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 1
        gbc.gridy = 0;
        gbc.gridx = 0;
        fieldsPanel.add(createLabel("Full Name"), gbc);
        gbc.gridx = 1;
        fullNameField = new RoundedTextField();
        fieldsPanel.add(fullNameField, gbc);
        gbc.gridx = 2;
        fieldsPanel.add(createLabel("Phone Number"), gbc);
        gbc.gridx = 3;
        phoneField = new RoundedTextField();
        fieldsPanel.add(phoneField, gbc);

        // Row 2
        gbc.gridy = 1;
        gbc.gridx = 0;
        fieldsPanel.add(createLabel("Age"), gbc);
        gbc.gridx = 1;
        ageField = new RoundedTextField();
        fieldsPanel.add(ageField, gbc);
        gbc.gridx = 2;
        fieldsPanel.add(createLabel("Height (cm)"), gbc);
        gbc.gridx = 3;
        heightField = new RoundedTextField();
        fieldsPanel.add(heightField, gbc);

        // Row 3
        gbc.gridy = 2;
        gbc.gridx = 0;
        fieldsPanel.add(createLabel("Gender"), gbc);
        gbc.gridx = 1;
        genderComboBox = new JComboBox<>(new String[]{"Select Gender", "Male", "Female", "Other", "Prefer not to say"});
        fieldsPanel.add(genderComboBox, gbc);
        gbc.gridx = 2;
        fieldsPanel.add(createLabel("Weight (kg)"), gbc);
        gbc.gridx = 3;
        weightField = new RoundedTextField();
        fieldsPanel.add(weightField, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);
    }

    /**
     * Adds the medical form fields to the specified panel.
     * @param panel The panel to add fields to.
     */
    private void addMedicalFields(JPanel panel) {
        JPanel fieldsPanel = new JPanel(new BorderLayout(10, 5));
        fieldsPanel.setOpaque(false);

        fieldsPanel.add(createLabel("Medical Issue"), BorderLayout.NORTH);

        medicalIssuesArea = new JTextArea(4, 30);
        medicalIssuesArea.setFont(new Font("Arial", Font.PLAIN, 14));
        medicalIssuesArea.setLineWrap(true);
        medicalIssuesArea.setWrapStyleWord(true);
        medicalIssuesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true), // Rounded border
            new EmptyBorder(5, 8, 5, 8) // Inner padding
        ));

        JScrollPane issueScrollPane = new JScrollPane(medicalIssuesArea);
        issueScrollPane.setBorder(null); // Remove default scroll pane border

        panel.add(issueScrollPane, BorderLayout.CENTER);
    }

    /**
     * Helper method to create a styled JLabel for forms.
     * @param text The text for the label.
     * @return A styled JLabel.
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }
    
    /**
     * Saves profile data to the database
     */
        private void saveProfileData() {
        try {
            // Validate required fields
            if (fullNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full name is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse numeric values (same as before)
            int age = 0;
            if (!ageField.getText().trim().isEmpty()) {
                try {
                    age = Integer.parseInt(ageField.getText().trim());
                    if (age <= 0 || age > 150) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid age!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid age!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            double height = 0;
            if (!heightField.getText().trim().isEmpty()) {
                try {
                    height = Double.parseDouble(heightField.getText().trim());
                    if (height <= 0 || height > 300) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid height!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid height!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            double weight = 0;
            if (!weightField.getText().trim().isEmpty()) {
                try {
                    weight = Double.parseDouble(weightField.getText().trim());
                    if (weight <= 0 || weight > 500) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid weight!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid weight!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // Get gender value
            String gender = genderComboBox.getSelectedItem().toString();
            String genderValue = "unspecified";
            if ("Male".equals(gender)) genderValue = "male";
            else if ("Female".equals(gender)) genderValue = "female";
            else if ("Other".equals(gender)) genderValue = "other";
            
            // Save to database
            String sql = "INSERT INTO profiles (user_id, full_name, phone, age, height_cm, gender, weight_kg, medical_issue) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE " +
                         "full_name = VALUES(full_name), " +
                         "phone = VALUES(phone), " +
                         "age = VALUES(age), " +
                         "height_cm = VALUES(height_cm), " +
                         "gender = VALUES(gender), " +
                         "weight_kg = VALUES(weight_kg), " +
                         "medical_issue = VALUES(medical_issue)";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setLong(1, userId);
                stmt.setString(2, fullNameField.getText().trim());
                stmt.setString(3, phoneField.getText().trim());
                
                if (age > 0) {
                    stmt.setInt(4, age);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                
                if (height > 0) {
                    stmt.setInt(5, (int) height);
                } else {
                    stmt.setNull(5, Types.INTEGER);
                }
                
                stmt.setString(6, genderValue);
                
                if (weight > 0) {
                    stmt.setDouble(7, weight);
                } else {
                    stmt.setNull(7, Types.DECIMAL);
                }
                
                stmt.setString(8, medicalIssuesArea.getText().trim());
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Profile saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Close the window
                    
                    // Open the dashboard after saving profile
                    DashboardWindow dbw = new DashboardWindow();
                    dbw.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save profile!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads existing profile data from the database
     */
    private void loadProfileData() {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Populate form fields with existing data
                fullNameField.setText(rs.getString("full_name"));
                phoneField.setText(rs.getString("phone"));
                
                int age = rs.getInt("age");
                if (!rs.wasNull()) {
                    ageField.setText(String.valueOf(age));
                }
                
                int height = rs.getInt("height_cm");
                if (!rs.wasNull()) {
                    heightField.setText(String.valueOf(height));
                }
                
                double weight = rs.getDouble("weight_kg");
                if (!rs.wasNull()) {
                    weightField.setText(String.valueOf(weight));
                }
                
                String gender = rs.getString("gender");
                if (gender != null && !gender.isEmpty()) {
                    if ("male".equals(gender)) genderComboBox.setSelectedItem("Male");
                    else if ("female".equals(gender)) genderComboBox.setSelectedItem("Female");
                    else if ("other".equals(gender)) genderComboBox.setSelectedItem("Other");
                    else genderComboBox.setSelectedItem("Prefer not to say");
                }
                
                medicalIssuesArea.setText(rs.getString("medical_issue"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // It's okay if no profile exists yet - this is a new user
        }
    }

    // --- INNER CLASSES FOR CUSTOM ROUNDED COMPONENTS ---

    /**
     * A custom JPanel with rounded corners.
     */
    private static class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2d.dispose();
        }
    }

    /**
     * A custom JTextField with a rounded border and padding.
     */
    private static class RoundedTextField extends JTextField {
        public RoundedTextField() {
            super();
            setOpaque(false);
            setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding inside the text field
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.dispose();
        }
    }
    
    /**
     * A custom JButton with a rounded background.
     */
    private static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    /**
     * Main method for testing this window independently.
     */
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            // For testing, use a dummy user ID
//            ProfileFormWindow formWindow = new ProfileFormWindow(1);
//            formWindow.setVisible(true);
//        });
//    }
}