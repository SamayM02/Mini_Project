/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;  // ✅ Use Session for logged-in user

public class ProfileDialog extends JDialog {
    private JTextField fullNameField;
    private JTextField phoneField;
    private JTextField ageField;
    private JTextField heightField;
    private JComboBox<String> genderComboBox;
    private JTextField weightField;
    private JTextArea medicalIssuesArea;

    public ProfileDialog(JFrame parent, boolean modal) {
        super(parent, "User Profile", modal);
        initializeUI();
        loadProfileData();
        setSize(500, 500);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(243,243,191));  
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Label styling
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Color labelColor = new Color(0, 51, 102);

        // Full Name (disabled)
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name *:");
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(labelColor);
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        fullNameField = new JTextField(20);
        fullNameField.setEnabled(false);
        panel.add(fullNameField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(labelFont);
        phoneLabel.setForeground(labelColor);
        panel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        panel.add(phoneField, gbc);

        // Age
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(labelFont);
        ageLabel.setForeground(labelColor);
        panel.add(ageLabel, gbc);
        gbc.gridx = 1;
        ageField = new JTextField(20);
        panel.add(ageField, gbc);

        // Height
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel heightLabel = new JLabel("Height (cm):");
        heightLabel.setFont(labelFont);
        heightLabel.setForeground(labelColor);
        panel.add(heightLabel, gbc);
        gbc.gridx = 1;
        heightField = new JTextField(20);
        panel.add(heightField, gbc);

        // Gender (disabled)
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(labelFont);
        genderLabel.setForeground(labelColor);
        panel.add(genderLabel, gbc);
        gbc.gridx = 1;
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other", "Prefer not to say"});
        genderComboBox.setEnabled(false);
        panel.add(genderComboBox, gbc);

        // Weight
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel weightLabel = new JLabel("Weight (kg):");
        weightLabel.setFont(labelFont);
        weightLabel.setForeground(labelColor);
        panel.add(weightLabel, gbc);
        gbc.gridx = 1;
        weightField = new JTextField(20);
        panel.add(weightField, gbc);

        // Medical Issues
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel medicalLabel = new JLabel("Medical Issues:");
        medicalLabel.setFont(labelFont);
        medicalLabel.setForeground(labelColor);
        panel.add(medicalLabel, gbc);
        gbc.gridx = 1;
        medicalIssuesArea = new JTextArea(3, 20);
        medicalIssuesArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(medicalIssuesArea);
        scrollPane.setBackground(Color.WHITE);
        panel.add(scrollPane, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(243,243,191)); // Match background

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(new Color(81,189,101));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(200, 80, 80));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        saveButton.addActionListener(e -> saveProfile());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    // ----------------- Load Profile Data -----------------
    private void loadProfileData() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM profiles WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, getCurrentUserId());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fullNameField.setText(rs.getString("full_name"));
                phoneField.setText(rs.getString("phone"));

                int age = rs.getInt("age");
                if (!rs.wasNull()) ageField.setText(String.valueOf(age));

                int height = rs.getInt("height_cm");
                if (!rs.wasNull()) heightField.setText(String.valueOf(height));

                double weight = rs.getDouble("weight_kg");
                if (!rs.wasNull()) weightField.setText(String.valueOf(weight));

                String gender = rs.getString("gender");
                if (gender != null && !gender.isEmpty()) {
                    switch (gender.toLowerCase()) {
                        case "male" -> genderComboBox.setSelectedItem("Male");
                        case "female" -> genderComboBox.setSelectedItem("Female");
                        case "other" -> genderComboBox.setSelectedItem("Other");
                        default -> genderComboBox.setSelectedItem("Prefer not to say");
                    }
                }
                medicalIssuesArea.setText(rs.getString("medical_issue"));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading profile data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------- Save Profile -----------------
    private void saveProfile() {
        String phone = phoneField.getText().trim();
        String ageText = ageField.getText().trim();
        String heightText = heightField.getText().trim();
        String weightText = weightField.getText().trim();
        String medicalIssues = medicalIssuesArea.getText().trim();

        int age = parseIntOrZero(ageText, "Age", 1, 150);
        if (age == -1) return;

        int height = parseIntOrZero(heightText, "Height", 1, 300);
        if (height == -1) return;

        double weight = parseDoubleOrZero(weightText, "Weight", 1, 500);
        if (weight == -1) return;

        String fullName = fullNameField.getText().trim();
        String genderValue = genderComboBox.getSelectedItem().toString().toLowerCase();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO profiles (user_id, full_name, phone, age, height_cm, gender, weight_kg, medical_issue) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "phone = VALUES(phone), " +
                    "age = VALUES(age), " +
                    "height_cm = VALUES(height_cm), " +
                    "weight_kg = VALUES(weight_kg), " +
                    "medical_issue = VALUES(medical_issue)";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, getCurrentUserId());
            stmt.setString(2, fullName);
            stmt.setString(3, phone);

            if (age > 0) stmt.setInt(4, age); else stmt.setNull(4, java.sql.Types.INTEGER);
            if (height > 0) stmt.setInt(5, height); else stmt.setNull(5, java.sql.Types.INTEGER);
            stmt.setString(6, genderValue);
            if (weight > 0) stmt.setDouble(7, weight); else stmt.setNull(7, java.sql.Types.DECIMAL);
            stmt.setString(8, medicalIssues);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseIntOrZero(String value, String field, int min, int max) {
        if (value.isEmpty()) return 0;
        try {
            int num = Integer.parseInt(value);
            if (num < min || num > max) throw new NumberFormatException();
            return num;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid " + field, "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private double parseDoubleOrZero(String value, String field, int min, int max) {
        if (value.isEmpty()) return 0;
        try {
            double num = Double.parseDouble(value);
            if (num < min || num > max) throw new NumberFormatException();
            return num;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid " + field, "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private long getCurrentUserId() {
        return Session.getCurrentUserId();   // ✅ Now it uses Session
    }
}