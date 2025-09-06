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
import com.reminddose.medicinedabba.database.Session;   // ✅ Import Session for current user

public class RemoveReminderDialog extends JDialog {
    private JComboBox<String> reminderCombo;
    private JTextArea reasonArea;
    
    private List<Integer> reminderIds = new ArrayList<>();

    public RemoveReminderDialog(JFrame parent, boolean modal) {
        super(parent, "Remove Reminder", modal);
        initializeUI();
        loadReminders();
        setSize(420, 280);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(243,243,191)); // ✅ Unified background
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Reminder Selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Reminder *:"), gbc);
        gbc.gridx = 1;
        reminderCombo = new JComboBox<>();
        reminderCombo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        panel.add(reminderCombo, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 1;
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
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(243,243,191));

        JButton removeButton = new JButton("Remove");
        removeButton.setBackground(new Color(200, 80, 80)); // ❌ Red for delete
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        removeButton.addActionListener(this::removeReminder);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(81,189,101)); // ✅ Green for cancel
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(removeButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
    }
    
    private void loadReminders() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT r.id, m.name, rt.time, r.repeat_type " +
                         "FROM reminders r " +
                         "JOIN medicines m ON r.medicine_id = m.id " +
                         "JOIN reminder_times rt ON r.id = rt.reminder_id " +
                         "WHERE r.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, getCurrentUserId());   // ✅ Logged-in user's reminders only
            
            ResultSet rs = stmt.executeQuery();
            reminderIds.clear();
            reminderCombo.removeAllItems();
            
            while (rs.next()) {
                reminderIds.add(rs.getInt("id"));
                String displayText = rs.getString("name") + " - " + 
                                   rs.getTime("time") + " (" + 
                                   rs.getString("repeat_type") + ")";
                reminderCombo.addItem(displayText);
            }

            if (reminderIds.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No reminders found for current user.",
                        "No Reminders",
                        JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading reminders: " + ex.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeReminder(ActionEvent e) {
        if (reminderCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reminder", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove this reminder?", 
            "Confirm Removal", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            int reminderId = reminderIds.get(reminderCombo.getSelectedIndex());
            
            // First delete reminder times
            String deleteTimesQuery = "DELETE FROM reminder_times WHERE reminder_id = ? " +
                                      "AND reminder_id IN (SELECT id FROM reminders WHERE id = ? AND user_id = ?)";
            PreparedStatement timesStmt = conn.prepareStatement(deleteTimesQuery);
            timesStmt.setInt(1, reminderId);
            timesStmt.setInt(2, reminderId);
            timesStmt.setLong(3, getCurrentUserId());   // ✅ Ensure only user's reminder
            timesStmt.executeUpdate();
            
            // Then delete the reminder itself
            String deleteQuery = "DELETE FROM reminders WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteQuery);
            stmt.setInt(1, reminderId);
            stmt.setLong(2, getCurrentUserId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Reminder removed successfully!");
                dispose();
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error removing reminder: " + ex.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private long getCurrentUserId() {
        return Session.getCurrentUserId();   // ✅ Use Session instead of hardcoded id
    }
}