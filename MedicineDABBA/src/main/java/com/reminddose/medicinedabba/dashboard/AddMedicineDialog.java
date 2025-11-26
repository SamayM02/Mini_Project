/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.awt.geom.RoundRectangle2D;

import com.toedter.calendar.JDateChooser;

/**
 * A dialog to add a new medicine with a modern, rounded appearance.
 */
public class AddMedicineDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<String> categoryCombo;
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
        JPanel panel = new RoundedPanel(15, new Color(243, 243, 191));
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Medicine Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Medicine Name *:"), gbc);
        gbc.gridx = 1;
        nameField = new RoundedTextField(20);
        panel.add(nameField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        String[] categories = {"Morning", "Afternoon", "Evening", "Bedtime", "General"};
        categoryCombo = new JComboBox<>(categories);
        styleComboBox(categoryCombo);
        panel.add(categoryCombo, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Quantity *:"), gbc);
        gbc.gridx = 1;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(quantitySpinner, gbc);

        // Manufacture Date
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Manufacture Date *:"), gbc);
        gbc.gridx = 1;
        mfgDateChooser = new JDateChooser();
        mfgDateChooser.setDateFormatString("yyyy-MM-dd");
        mfgDateChooser.setBorder(BorderFactory.createEmptyBorder()); // remove border
        panel.add(mfgDateChooser, gbc);

        // Expiry Date
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Expiry Date *:"), gbc);
        gbc.gridx = 1;
        expiryDateChooser = new JDateChooser();
        expiryDateChooser.setDateFormatString("yyyy-MM-dd");
        expiryDateChooser.setBorder(BorderFactory.createEmptyBorder()); // remove border
        panel.add(expiryDateChooser, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new RoundedPanel(15, new Color(243, 243, 191));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton saveButton = new FilledRoundedButton("Save", new Color(81,189,101));
        saveButton.addActionListener(this::saveMedicine);
        saveButton.setPreferredSize(new Dimension(80, 30));

        JButton cancelButton = new FilledRoundedButton("Cancel", new Color(200, 80, 80));
        cancelButton.addActionListener(e -> dispose());
        cancelButton.setPreferredSize(new Dimension(80, 30));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setOpaque(false);
        combo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        combo.setUI(new BasicComboBoxUI());
    }

    private void saveMedicine(ActionEvent e) {
        String medicineName = nameField.getText().trim();
        
        if (medicineName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter medicine name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- EDITED CONSTRAINT VALIDATION ---
        // Check if the medicine name starts with a letter (A-Z or a-z). 
        // This is more restrictive than just checking for digits.
        if (!Character.isLetter(medicineName.charAt(0))) {
            JOptionPane.showMessageDialog(this, 
                    "Medicine name must start with a letter (A-Z).", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // --- END EDITED CONSTRAINT VALIDATION ---


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
            stmt.setString(2, medicineName);
            stmt.setString(3, ((String) categoryCombo.getSelectedItem()).toLowerCase());
            stmt.setDate(4, java.sql.Date.valueOf(mfgDate));
            stmt.setDate(5, java.sql.Date.valueOf(expDate));
            stmt.setNull(6, Types.VARCHAR); // Set dosage to null
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
    
    /**
     * A custom JTextField with a rounded border.
     */
    private static class RoundedTextField extends JTextField {
        private int cornerRadius;

        public RoundedTextField(int radius) {
            super();
            this.cornerRadius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            g2.dispose();
        }
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
}
