/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;

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
        JPanel panel = new RoundedPanel(15, new Color(243,243,191));
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Reminder Selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Reminder *:"), gbc);
        gbc.gridx = 1;
        reminderCombo = new JComboBox<>();
        styleComboBox(reminderCombo);
        panel.add(reminderCombo, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        panel.add(new JScrollPane(reasonArea), gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new RoundedPanel(15, new Color(243,243,191));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton removeButton = new FilledRoundedButton("Remove", new Color(200, 80, 80));
        removeButton.addActionListener(this::removeReminder);
        removeButton.setPreferredSize(new Dimension(90, 30));

        JButton cancelButton = new FilledRoundedButton("Cancel", new Color(81,189,101));
        cancelButton.addActionListener(e -> dispose());
        cancelButton.setPreferredSize(new Dimension(90, 30));

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
            stmt.setLong(1, getCurrentUserId());
            
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
            timesStmt.setLong(3, getCurrentUserId());
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
        return Session.getCurrentUserId();
    }
    
    /**
     * A custom JPanel with a rounded background.
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
     * A custom JButton with a filled, rounded background and hover effect.
     */
    private static class FilledRoundedButton extends JButton {
        private final Color baseColor;
        private static final int CORNER_RADIUS = 15;

        public FilledRoundedButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setFont(new Font("Arial", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Add hover effect
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(baseColor.brighter());
                    repaint(); // repaint to show brighter color
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(baseColor);
                    repaint(); // repaint to show base color
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Set background color based on hover state
            if (getModel().isRollover()) {
                g2.setColor(baseColor.brighter());
            } else {
                g2.setColor(baseColor);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No border painting
        }
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setOpaque(false);
        combo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        combo.setUI(new BasicComboBoxUI());
    }
}
