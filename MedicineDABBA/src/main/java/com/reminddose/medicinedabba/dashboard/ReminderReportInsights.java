package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReminderReportInsights extends JDialog {

    private JLabel takenLabel, missedLabel, canceledLabel, adherenceLabel;
    private JTable recentTable, medicineTable;

    // ✅ Modified constructor to accept parent frame and modal flag
    public ReminderReportInsights(JFrame parent, boolean modal) {
        super(parent, "Reminder Summary Report", modal);
        initializeUI();
        loadSummary();
        setSize(800, 650);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    // ✅ Added no-arg constructor for backward compatibility
    public ReminderReportInsights() {
        this(null, false);
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(243, 243, 191));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Totals Section ---
        JPanel totalsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        totalsPanel.setBackground(new Color(243, 243, 191));

        takenLabel = new JLabel("Taken: 0", SwingConstants.CENTER);
        missedLabel = new JLabel("Missed: 0", SwingConstants.CENTER);
        canceledLabel = new JLabel("Canceled: 0", SwingConstants.CENTER);
        adherenceLabel = new JLabel("Adherence: 0%", SwingConstants.CENTER);

        Font bigFont = new Font("Arial", Font.BOLD, 18);
        takenLabel.setFont(bigFont);
        missedLabel.setFont(bigFont);
        canceledLabel.setFont(bigFont);
        adherenceLabel.setFont(bigFont);

        totalsPanel.add(takenLabel);
        totalsPanel.add(missedLabel);
        totalsPanel.add(canceledLabel);
        totalsPanel.add(adherenceLabel);

        JLabel summaryTitle = new JLabel("Overall Summary");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 16));
        summaryTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(summaryTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(totalsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- Recent Activity Table ---
        JLabel recentTitle = new JLabel("Recent Activity");
        recentTitle.setFont(new Font("Arial", Font.BOLD, 16));
        recentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(recentTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        recentTable = new JTable();
        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        recentScroll.setPreferredSize(new Dimension(700, 150));

        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBackground(new Color(243, 243, 191));
        recentPanel.add(recentScroll, BorderLayout.CENTER);
        recentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        mainPanel.add(recentPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- Medicine-wise Table ---
        JLabel medicineTitle = new JLabel("Medicine-wise Summary");
        medicineTitle.setFont(new Font("Arial", Font.BOLD, 16));
        medicineTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(medicineTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        medicineTable = new JTable();
        JScrollPane medicineScroll = new JScrollPane(medicineTable);
        medicineScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        medicineScroll.setPreferredSize(new Dimension(700, 180));

        JPanel medicinePanel = new JPanel(new BorderLayout());
        medicinePanel.setBackground(new Color(243, 243, 191));
        medicinePanel.add(medicineScroll, BorderLayout.CENTER);
        medicinePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        mainPanel.add(medicinePanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void loadSummary() {
        int taken = 0, missed = 0, canceled = 0;

        try (Connection conn = DBConnection.getConnection()) {
            long userId = Session.getCurrentUserId();

            // --- Totals ---
            String sql = "SELECT taken_status, COUNT(*) as count " +
                    "FROM reminder_logs WHERE user_id = ? GROUP BY taken_status";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String status = rs.getString("taken_status");
                int count = rs.getInt("count");
                if ("taken".equalsIgnoreCase(status)) taken = count;
                else if ("missed".equalsIgnoreCase(status)) missed = count;
                else if ("canceled".equalsIgnoreCase(status) || "snoozed".equalsIgnoreCase(status)) canceled = count;
            }

            int total = taken + missed + canceled;
            double adherence = (total > 0) ? (taken * 100.0 / total) : 0;

            takenLabel.setText("Taken: " + taken);
            missedLabel.setText("Missed: " + missed);
            canceledLabel.setText("Canceled: " + canceled);
            adherenceLabel.setText("Adherence: " + String.format("%.1f", adherence) + "%");

            // --- Recent Activity ---
            DefaultTableModel recentModel = new DefaultTableModel(
                    new String[]{"Medicine", "Scheduled Time", "Status"}, 0);

            sql = "SELECT medicine_name, scheduled_time, taken_status " +
                    "FROM reminder_logs WHERE user_id = ? ORDER BY scheduled_time DESC LIMIT 5";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                recentModel.addRow(new Object[]{
                        rs.getString("medicine_name"),
                        rs.getTimestamp("scheduled_time"),
                        rs.getString("taken_status")
                });
            }
            recentTable.setModel(recentModel);

            // --- Medicine-wise Summary ---
            DefaultTableModel medModel = new DefaultTableModel(
                    new String[]{"Medicine", "Taken", "Missed", "Canceled"}, 0);

            sql = "SELECT medicine_name, " +
                    "SUM(CASE WHEN taken_status='taken' THEN 1 ELSE 0 END) as taken_count, " +
                    "SUM(CASE WHEN taken_status='missed' THEN 1 ELSE 0 END) as missed_count, " +
                    "SUM(CASE WHEN taken_status IN ('canceled','snoozed') THEN 1 ELSE 0 END) as canceled_count " +
                    "FROM reminder_logs WHERE user_id = ? GROUP BY medicine_name";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                medModel.addRow(new Object[]{
                        rs.getString("medicine_name"),
                        rs.getInt("taken_count"),
                        rs.getInt("missed_count"),
                        rs.getInt("canceled_count")
                });
            }
            medicineTable.setModel(medModel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading report: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
