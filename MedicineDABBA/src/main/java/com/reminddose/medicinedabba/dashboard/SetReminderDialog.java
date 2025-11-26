/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.awt.geom.RoundRectangle2D;

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
        JPanel panel = new RoundedPanel(15, new Color(243, 243, 191));
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Medicine Selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Medicine *:"), gbc);
        gbc.gridx = 1;
        medicineCombo = new JComboBox<>();
        medicineCombo.addActionListener(e -> updateDosageField());
        styleComboBox(medicineCombo);
        panel.add(medicineCombo, gbc);

        // Dosage
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Dosage *:"), gbc);
        gbc.gridx = 1;
        dosageField = new RoundedTextField(20);
        panel.add(dosageField, gbc);

        // Frequency
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Frequency *:"), gbc);
        gbc.gridx = 1;
        String[] frequencies = {"Daily", "Weekly", "Alternate Days"};
        frequencyCombo = new JComboBox<>(frequencies);
        frequencyCombo.addActionListener(e -> toggleDaySelection());
        styleComboBox(frequencyCombo);
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
            final int index = i;
            dayCheckboxes[i] = new JCheckBox(days[i]);
            dayCheckboxes[i].setOpaque(false);
            dayCheckboxes[i].setEnabled(false); // default disabled until weekly/custom
            dayCheckboxes[i].addActionListener(e -> {
                if (dayCheckboxes[index].isSelected()) {
                    for (int j = 0; j < dayCheckboxes.length; j++) {
                        if (j != index) {
                            dayCheckboxes[j].setSelected(false);
                        }
                    }
                }
            });
            daysPanel.add(dayCheckboxes[i]);
        }
        panel.add(daysPanel, gbc);

        // Start and End Date
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Start Date *:"), gbc);
        gbc.gridx = 1;
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        styleDateChooser(startDateChooser);
        panel.add(startDateChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1;
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        styleDateChooser(endDateChooser);
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
        styleSpinner(timeSpinner);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setPreferredSize(new Dimension(90, timeSpinner.getPreferredSize().height));
        timePanel.add(timeSpinner);

        // AM/PM combo (explicit toggle)
        amPmCombo = new JComboBox<>(new String[]{"AM","PM"});
        amPmCombo.setPreferredSize(new Dimension(65, amPmCombo.getPreferredSize().height));
        styleComboBox(amPmCombo);
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
        JPanel buttonPanel = new RoundedPanel(15, new Color(243, 243, 191));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton saveButton = new FilledRoundedButton("Save", new Color(81,189,101));
        saveButton.addActionListener(this::saveReminder);
        saveButton.setPreferredSize(new Dimension(80, 30));

        JButton cancelButton = new FilledRoundedButton("Cancel", new Color(200, 80, 80));
        cancelButton.addActionListener(e -> dispose());
        cancelButton.setPreferredSize(new Dimension(80, 30));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, gbc);

        add(panel);
    }

    private void toggleDaySelection() {
        String freq = (String) frequencyCombo.getSelectedItem();
        boolean enable = "Weekly".equals(freq);
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
                    if ("Weekly".equals(frequencyType)) {
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

    private void styleComboBox(JComboBox<String> combo) {
        combo.setOpaque(false);
        combo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        combo.setUI(new BasicComboBoxUI());
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setOpaque(false);
        spinner.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected JComponent createEditor() {
                JComponent editor = super.createEditor();
                if (editor instanceof JSpinner.DefaultEditor) {
                    JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
                    textField.setUI(new BasicTextFieldUI() {
                        @Override
                        protected void paintBackground(Graphics g) {
                            // Do nothing to avoid painting a separate background
                        }
                    });
                    textField.setOpaque(false);
                }
                return editor;
            }
        });
    }

    private void styleDateChooser(JDateChooser dateChooser) {
        dateChooser.setOpaque(false);
        dateChooser.getCalendarButton().setContentAreaFilled(false);
        dateChooser.getCalendarButton().setBorderPainted(false);
        dateChooser.getCalendarButton().setBorder(BorderFactory.createEmptyBorder());
        dateChooser.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        
        // This is a direct fix, styling the internal text field
        Component[] components = dateChooser.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                ((JTextField) comp).setBorder(BorderFactory.createEmptyBorder());
                ((JTextField) comp).setOpaque(false);
                break;
            }
        }
    }
}
