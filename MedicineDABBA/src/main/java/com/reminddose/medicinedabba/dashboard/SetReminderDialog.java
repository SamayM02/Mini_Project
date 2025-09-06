/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

/**
 *
 * @author lenovo
 */


import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SetReminderDialog extends JDialog {
    private JComboBox<String> medicineCombo;
    private JComboBox<String> frequencyCombo;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JSpinner timeSpinner;
    private JComboBox<String> amPmCombo;                // <-- AM/PM toggle
    private JTextArea notesArea;
    private JCheckBox[] dayCheckboxes;
    private JTextField dosageField;

    private List<Integer> medicineIds = new ArrayList<>();

    public SetReminderDialog(JFrame parent, boolean modal) {
        super(parent, "Set Reminder", modal);
        initializeUI();
        loadMedicines();
        setSize(520, 620);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(243,243,191));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Medicine Selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Medicine *:"), gbc);
        gbc.gridx = 1;
        medicineCombo = new JComboBox<>();
        medicineCombo.addActionListener(e -> updateDosageField());
        medicineCombo.setBorder(BorderFactory.createLineBorder(Color.GRAY,1,true));
        panel.add(medicineCombo, gbc);

        // Dosage
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Dosage *:"), gbc);
        gbc.gridx = 1;
        dosageField = new JTextField(20);
        dosageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        panel.add(dosageField, gbc);

        // Frequency
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Frequency *:"), gbc);
        gbc.gridx = 1;
        String[] frequencies = {"Daily", "Weekly", "Alternate Days", "Custom"};
        frequencyCombo = new JComboBox<>(frequencies);
        frequencyCombo.addActionListener(e -> toggleDaySelection());
        frequencyCombo.setBorder(BorderFactory.createLineBorder(Color.GRAY,1,true));
        panel.add(frequencyCombo, gbc);

        // Days of week (hidden unless Weekly/Custom)
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Days:"), gbc);
        gbc.gridx = 1;
        JPanel daysPanel = new JPanel(new GridLayout(1, 7, 4, 4));
        daysPanel.setOpaque(false);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        dayCheckboxes = new JCheckBox[7];
        for (int i = 0; i < 7; i++) {
            dayCheckboxes[i] = new JCheckBox(days[i]);
            dayCheckboxes[i].setOpaque(false);
            dayCheckboxes[i].setEnabled(false); // default disabled until weekly/custom
            daysPanel.add(dayCheckboxes[i]);
        }
        panel.add(daysPanel, gbc);

        // Start and End Date
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Start Date *:"), gbc);
        gbc.gridx = 1;
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        panel.add(startDateChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1;
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        panel.add(endDateChooser, gbc);

        // Time spinner + AM/PM toggle
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Time *:"), gbc);
        gbc.gridx = 1;
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        timePanel.setOpaque(false);

        // Spinner configured to show hour:minute in 12-hour mode for user's convenience
        SpinnerDateModel spinnerModel = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        timeSpinner = new JSpinner(spinnerModel);
        // Use a 12-hour editor (hh:mm)
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setPreferredSize(new Dimension(90, timeSpinner.getPreferredSize().height));
        timePanel.add(timeSpinner);

        // AM/PM combo (explicit toggle)
        amPmCombo = new JComboBox<>(new String[]{"AM","PM"});
        amPmCombo.setPreferredSize(new Dimension(65, amPmCombo.getPreferredSize().height));
        timePanel.add(amPmCombo);

        panel.add(timePanel, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        panel.add(new JScrollPane(notesArea), gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(243,243,191));

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(111, 189, 82));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveButton.addActionListener(this::saveReminder);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(81,189,101));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void toggleDaySelection() {
        String freq = (String) frequencyCombo.getSelectedItem();
        boolean enable = "Weekly".equals(freq) || "Custom".equals(freq);
        for (JCheckBox cb : dayCheckboxes) {
            cb.setEnabled(enable);
        }
    }

    private void loadMedicines() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT id, name FROM medicines WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, Session.getCurrentUserId());
            ResultSet rs = stmt.executeQuery();
            medicineIds.clear();
            medicineCombo.removeAllItems();
            while (rs.next()) {
                medicineIds.add(rs.getInt("id"));
                medicineCombo.addItem(rs.getString("name"));
            }
            if (medicineIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No medicines found. Please add medicines first.",
                        "No Medicines", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateDosageField() {
        if (medicineCombo.getSelectedIndex() == -1) return;

        try (Connection conn = DBConnection.getConnection()) {
            int medicineId = medicineIds.get(medicineCombo.getSelectedIndex());
            String query = "SELECT dosage FROM medicines WHERE id = ? AND user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, medicineId);
            stmt.setLong(2, Session.getCurrentUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dosage = rs.getString("dosage");
                dosageField.setText((dosage != null && !dosage.isEmpty()) ? dosage : "1 tablet");
            } else {
                dosageField.setText("1 tablet");
            }
        } catch (SQLException ex) {
            dosageField.setText("1 tablet");
        }
    }

    /**
     * Save reminder and insert time(s) into reminder_times.
     * Important: converts the selected 12-hour spinner + AM/PM into a proper 24-hour java.sql.Time.
     */
    private void saveReminder(ActionEvent e) {
        if (medicineCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dosageField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a dosage", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Dates from JDateChooser
        Date startUtil = startDateChooser.getDate();
        Date endUtil = endDateChooser.getDate();
        if (startUtil == null) {
            JOptionPane.showMessageDialog(this, "Please select a start date", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate startDate = startUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = null;
        if (endUtil != null) {
            endDate = endUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this, "End date cannot be before start date",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Convert spinner time (hh:mm) + AM/PM into LocalTime (24h)
        Date spinnerDate = (Date) timeSpinner.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(spinnerDate);
        int hour = cal.get(Calendar.HOUR);       // HOUR returns 0..11 for 12-hour clock
        int minute = cal.get(Calendar.MINUTE);

        // HOUR returns 0..11 — map correctly: if 12 was selected in editor, HOUR may be 0; we handle with AM/PM
        String ampm = (String) amPmCombo.getSelectedItem();
        if ("PM".equals(ampm)) {
            // if hour == 0 -> 12 PM -> 12
            if (hour < 12) hour += 12;
        } else { // AM
            if (hour == 12) hour = 0; // 12 AM is 0 hours
        }

        LocalTime localTime = LocalTime.of(hour, minute);
        Time sqlTime = Time.valueOf(localTime);

        // Insert into DB
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO reminders (user_id, medicine_id, dosage, repeat_type, start_date, end_date, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, Session.getCurrentUserId());

            int medicineId = medicineIds.get(medicineCombo.getSelectedIndex());
            stmt.setInt(2, medicineId);
            stmt.setString(3, dosageField.getText().trim());

            String frequency = ((String) frequencyCombo.getSelectedItem()).toLowerCase().replace(" ", "_");
            if ("alternate_days".equals(frequency)) frequency = "alternate";
            stmt.setString(4, frequency);

            stmt.setDate(5, java.sql.Date.valueOf(startDate));
            if (endDate != null) {
                stmt.setDate(6, java.sql.Date.valueOf(endDate));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }
            stmt.setString(7, notesArea.getText().trim());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    long reminderId = generatedKeys.getLong(1);

                    String frequencyType = (String) frequencyCombo.getSelectedItem();
                    if ("Weekly".equals(frequencyType) || "Custom".equals(frequencyType)) {
                        // Insert one reminder_time per selected weekday
                        for (int i = 0; i < dayCheckboxes.length; i++) {
                            if (dayCheckboxes[i].isSelected()) {
                                String timeQuery = "INSERT INTO reminder_times (reminder_id, time, weekday) VALUES (?, ?, ?)";
                                PreparedStatement timeStmt = conn.prepareStatement(timeQuery);
                                timeStmt.setLong(1, reminderId);
                                timeStmt.setTime(2, sqlTime);
                                // Keep same weekday encoding as your project uses elsewhere (i+1)
                                timeStmt.setInt(3, i + 1);
                                timeStmt.executeUpdate();
                                timeStmt.close();
                            }
                        }
                    } else {
                        String timeQuery = "INSERT INTO reminder_times (reminder_id, time) VALUES (?, ?)";
                        PreparedStatement timeStmt = conn.prepareStatement(timeQuery);
                        timeStmt.setLong(1, reminderId);
                        timeStmt.setTime(2, sqlTime);
                        timeStmt.executeUpdate();
                        timeStmt.close();
                    }
                }

                JOptionPane.showMessageDialog(this, "Reminder set successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save reminder", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving reminder: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
