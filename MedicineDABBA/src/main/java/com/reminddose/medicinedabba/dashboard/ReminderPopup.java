package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderPopup extends JDialog {
    private final String titleText;
    private final String medicineName;
    private final String dosage;
    private final LocalTime scheduledTime;

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public ReminderPopup(String titleText, String medicineName, String dosage, LocalTime scheduledTime) {
        this.titleText = titleText;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.scheduledTime = scheduledTime;

        setTitle("Medicine Reminder");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 240);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(243, 243, 191));
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Medicine Reminder", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(header, BorderLayout.NORTH);

        // Center details
        JPanel center = new JPanel(new GridLayout(3, 1, 5, 5));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        center.add(new JLabel("Medicine: " + medicineName));
        center.add(new JLabel("Dosage: " + dosage));
        center.add(new JLabel("Time: " + scheduledTime.toString()));
        add(center, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);

        JButton takeBtn = new JButton("Take It");
        takeBtn.setBackground(new Color(76, 175, 80));
        takeBtn.setForeground(Color.WHITE);
        takeBtn.addActionListener(e -> {
            logAction("taken");
            JOptionPane.showMessageDialog(this, "Marked as taken.");
            dispose();
        });

        JButton snoozeBtn = new JButton("Snooze");
        snoozeBtn.setBackground(new Color(255, 152, 0));
        snoozeBtn.setForeground(Color.WHITE);
        snoozeBtn.addActionListener(e -> {
            logAction("snoozed");

            // Ask custom snooze minutes (default 2)
            String input = JOptionPane.showInputDialog(this,
                    "Snooze for how many minutes?", "2");
            int minutes = 2;
            try {
                if (input != null && !input.trim().isEmpty()) {
                    minutes = Integer.parseInt(input.trim());
                }
            } catch (NumberFormatException ignored) {}

            int delay = minutes;

            // Schedule another popup
            scheduler.schedule(() -> SwingUtilities.invokeLater(() -> {
                ReminderPopup rp = new ReminderPopup(titleText, medicineName, dosage,
                        LocalTime.now().plusMinutes(delay));
                rp.setVisible(true);
            }), delay, TimeUnit.MINUTES);

            JOptionPane.showMessageDialog(this, "Snoozed for " + delay + " minutes.");
            dispose();
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(new Color(244, 67, 54));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> {
            logAction("missed");
            dispose();
        });

        buttons.add(takeBtn);
        buttons.add(snoozeBtn);
        buttons.add(cancelBtn);

        add(buttons, BorderLayout.SOUTH);
    }

    private void logAction(String action) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO reminder_logs (reminder_id, medicine_name, medicine_category, dosage, " +
                         "scheduled_time, repeat_type, taken_status, taken_time, user_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            // Since constructor doesn’t provide reminderId, category, repeatType → set them as NULL/empty
            ps.setNull(1, Types.BIGINT);
            ps.setString(2, medicineName);
            ps.setString(3, "Daily"); // fallback category
            ps.setString(4, dosage);

            LocalDateTime scheduledDateTime = LocalDateTime.of(LocalDate.now(), scheduledTime);
            ps.setTimestamp(5, Timestamp.valueOf(scheduledDateTime));

            ps.setString(6, "daily"); // valid repeat_type fallback
            ps.setString(7, action);

            if ("taken".equals(action)) {
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }

            ps.setLong(9, Session.getCurrentUserId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to log reminder: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
