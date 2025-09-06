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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;   // ✅ Import Session

public class RemoveMedicineDialog extends JDialog {
    private JComboBox<String> medicineCombo;
    private JSpinner quantitySpinner;
    private JTextArea reasonArea;
    
    private List<Integer> medicineIds = new ArrayList<>();

    public RemoveMedicineDialog(JFrame parent, boolean modal) {
        super(parent, "Remove Medicine", modal);
        initializeUI();
        loadMedicines();
        setSize(420, 320);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(243,243,191)); // ✅ unified background
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Medicine Selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Medicine *:"), gbc);
        gbc.gridx = 1;
        medicineCombo = new JComboBox<>();
        medicineCombo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        medicineCombo.addActionListener(e -> updateMaxQuantity());
        panel.add(medicineCombo, gbc);

        // Quantity to remove
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Quantity to Remove *:"), gbc);
        gbc.gridx = 1;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        panel.add(quantitySpinner, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        panel.add(new JScrollPane(reasonArea), gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(243,243,191));

        JButton removeButton = new JButton("Remove");
        removeButton.setBackground(new Color(200, 80, 80)); // red for delete
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        removeButton.addActionListener(this::removeMedicine);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(81,189,101)); // ✅ green for cancel
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(removeButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
    }
    
    private void loadMedicines() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT id, name, quantity FROM medicines WHERE user_id = ? AND quantity > 0";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, getCurrentUserId());   // ✅ Use logged-in user's id
            
            ResultSet rs = stmt.executeQuery();
            medicineIds.clear();
            medicineCombo.removeAllItems();
            
            while (rs.next()) {
                medicineIds.add(rs.getInt("id"));
                String displayName = rs.getString("name") + " (" + rs.getInt("quantity") + " left)";
                medicineCombo.addItem(displayName);
            }

            if (medicineIds.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No medicines found for current user.",
                        "No Medicines",
                        JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + ex.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateMaxQuantity() {
        if (medicineCombo.getSelectedIndex() == -1) return;
        
        try (Connection conn = DBConnection.getConnection()) {
            int medicineId = medicineIds.get(medicineCombo.getSelectedIndex());
            String query = "SELECT quantity FROM medicines WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, medicineId);
            stmt.setLong(2, getCurrentUserId());   // ✅ Current user only
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int maxQuantity = rs.getInt("quantity");
                quantitySpinner.setModel(new SpinnerNumberModel(1, 1, maxQuantity, 1));
            }
        } catch (SQLException ex) {
            // Handle error gracefully
        }
    }
    
    private void removeMedicine(ActionEvent e) {
        if (medicineCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int quantityToRemove = (Integer) quantitySpinner.getValue();
        if (quantityToRemove <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            int medicineId = medicineIds.get(medicineCombo.getSelectedIndex());
            
            // Get current quantity
            String getQuery = "SELECT quantity FROM medicines WHERE id = ? AND user_id = ?";
            PreparedStatement getStmt = conn.prepareStatement(getQuery);
            getStmt.setInt(1, medicineId);
            getStmt.setLong(2, getCurrentUserId());
            
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                
                if (quantityToRemove > currentQuantity) {
                    JOptionPane.showMessageDialog(this, "Cannot remove more than available quantity", 
                                                 "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Update quantity
                String updateQuery = "UPDATE medicines SET quantity = quantity - ? " +
                                   "WHERE id = ? AND user_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, quantityToRemove);
                updateStmt.setInt(2, medicineId);
                updateStmt.setLong(3, getCurrentUserId());
                
                int rowsAffected = updateStmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // If quantity becomes zero, delete the medicine
                    if (currentQuantity - quantityToRemove <= 0) {
                        String deleteQuery = "DELETE FROM medicines WHERE id = ? AND user_id = ?";
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                        deleteStmt.setInt(1, medicineId);
                        deleteStmt.setLong(2, getCurrentUserId());
                        deleteStmt.executeUpdate();
                    }
                    
                    JOptionPane.showMessageDialog(this, "Medicine removed successfully!");
                    dispose();
                }
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error removing medicine: " + ex.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private long getCurrentUserId() {
        return Session.getCurrentUserId();   // ✅ Fetch user from global session
    }
}
