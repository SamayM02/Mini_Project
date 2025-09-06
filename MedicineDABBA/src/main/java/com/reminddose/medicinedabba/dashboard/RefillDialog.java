/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

/**
 *
 * @author lenovo
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.reminddose.medicinedabba.database.DBConnection;

public class RefillDialog extends JDialog {
    private int medicineId;
    private JLabel medicineNameLabel;
    private JLabel currentQuantityLabel;
    private JSpinner quantitySpinner;

    public RefillDialog(JFrame parent, boolean modal, int medicineId) {
        super(parent, "Refill Medicine", modal);
        this.medicineId = medicineId;
        initializeUI();
        loadMedicineDetails();
        setSize(400, 250);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(243, 243, 191));  // Set background color
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Medicine Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Medicine:"), gbc);
        gbc.gridx = 1;
        medicineNameLabel = new JLabel();
        panel.add(medicineNameLabel, gbc);

        // Current Quantity
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Current Quantity:"), gbc);
        gbc.gridx = 1;
        currentQuantityLabel = new JLabel();
        panel.add(currentQuantityLabel, gbc);

        // Quantity to Add
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Quantity to Add *:"), gbc);
        gbc.gridx = 1;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(quantitySpinner, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(243, 243, 191));

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(76, 175, 80)); // Green
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refillMedicine();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(244, 67, 54)); // Red
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setFont(cancelButton.getFont().deriveFont(Font.BOLD));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void loadMedicineDetails() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT name, quantity FROM medicines WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, medicineId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                medicineNameLabel.setText(rs.getString("name"));
                currentQuantityLabel.setText(String.valueOf(rs.getInt("quantity")));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading medicine details: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refillMedicine() {
        int quantityToAdd = (Integer) quantitySpinner.getValue();
        if (quantityToAdd <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Update medicine quantity
            String updateQuery = "UPDATE medicines SET quantity = quantity + ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, quantityToAdd);
            updateStmt.setInt(2, medicineId);

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Medicine refilled successfully!");
                dispose();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error refilling medicine: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
//---------test in main meth
//    public static void main(String[] args) {
//        RefillDialog rf = new RefillDialog(null, true, 500);
//        rf.setVisible(true);
//    }
}
