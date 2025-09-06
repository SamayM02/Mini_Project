/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;  // ✅ Import Session

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import com.toedter.calendar.JDateChooser;   // ✅ Calendar date chooser

/**
 *
 * @author lenovo
 */
public class AddMedicineDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<String> categoryCombo;
    private JTextField dosageField;
    private JSpinner quantitySpinner;
    private JDateChooser mfgDateChooser;
    private JDateChooser expiryDateChooser;

    public AddMedicineDialog(JFrame parent, boolean modal) {
        super(parent, "Add New Medicine", modal);
        initializeUI();
        setSize(500, 450);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(243,243,191)); // ✅ Background color
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Medicine Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Medicine Name *:"), gbc);
        gbc.gridx = 1;
        nameField = createRoundedTextField();
        panel.add(nameField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        String[] categories = {"Morning", "Afternoon", "Evening", "Bedtime", "General"};
        categoryCombo = new JComboBox<>(categories);
        styleComboBox(categoryCombo);
        panel.add(categoryCombo, gbc);

        // Dosage
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Dosage:"), gbc);
        gbc.gridx = 1;
        dosageField = createRoundedTextField();
        panel.add(dosageField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Quantity *:"), gbc);
        gbc.gridx = 1;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(quantitySpinner, gbc);

        // Manufacture Date
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Manufacture Date *:"), gbc);
        gbc.gridx = 1;
        mfgDateChooser = new JDateChooser();
        mfgDateChooser.setDateFormatString("yyyy-MM-dd"); // ✅ display format
        panel.add(mfgDateChooser, gbc);

        // Expiry Date
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Expiry Date *:"), gbc);
        gbc.gridx = 1;
        expiryDateChooser = new JDateChooser();
        expiryDateChooser.setDateFormatString("yyyy-MM-dd");
        panel.add(expiryDateChooser, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(243,243,191)); // match bg

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(81,189,101)); // ✅ button color
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveButton.addActionListener(this::saveMedicine);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(200, 80, 80));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private JTextField createRoundedTextField() {
        JTextField field = new JTextField(20);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        combo.setUI(new BasicComboBoxUI());
    }

    private void saveMedicine(ActionEvent e) {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter medicine name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.util.Date mfgDateUtil = mfgDateChooser.getDate();
        java.util.Date expDateUtil = expiryDateChooser.getDate();

        if (mfgDateUtil == null || expDateUtil == null) {
            JOptionPane.showMessageDialog(this, "Please select both manufacture and expiry dates", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate mfgDate = new java.sql.Date(mfgDateUtil.getTime()).toLocalDate();
        LocalDate expDate = new java.sql.Date(expDateUtil.getTime()).toLocalDate();

        if (expDate.isBefore(mfgDate)) {
            JOptionPane.showMessageDialog(this, "Expiry date cannot be before manufacture date", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO medicines (user_id, name, category, manufacture_date, expiry_date, dosage, quantity) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, Session.getCurrentUserId());
            stmt.setString(2, nameField.getText().trim());
            stmt.setString(3, ((String) categoryCombo.getSelectedItem()).toLowerCase());
            stmt.setDate(4, java.sql.Date.valueOf(mfgDate));
            stmt.setDate(5, java.sql.Date.valueOf(expDate));
            stmt.setString(6, dosageField.getText().trim());
            stmt.setInt(7, (Integer) quantitySpinner.getValue());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Medicine added successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add medicine", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving medicine: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}