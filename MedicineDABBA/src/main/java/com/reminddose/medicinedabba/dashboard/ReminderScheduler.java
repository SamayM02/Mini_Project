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

import javax.swing.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReminderScheduler {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ReminderScheduler");
        t.setDaemon(true);
        return t;
    });
    private final JFrame parentFrame;

    public ReminderScheduler(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    // avoid showing same minute multiple times
    private volatile int lastProcessedMinute = -1;

    private static final Logger logger = Logger.getLogger(ReminderScheduler.class.getName());

    public void start() {
        // initial immediate run then every 15 seconds, but only process when minute changes
        executor.scheduleAtFixedRate(this::pollAndShow, 0, 15, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdownNow();
    }

    private void pollAndShow() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int minuteOfHour = now.getMinute();
            if (minuteOfHour == lastProcessedMinute) {
                return; // already processed this minute
            }
            lastProcessedMinute = minuteOfHour;

            LocalDate today = now.toLocalDate();
            LocalTime timeToMatch = now.truncatedTo(ChronoUnit.MINUTES).toLocalTime(); // hh:mm

            // build SQL: find reminders valid today whose reminder_time == current minute and weekday matches (if present)
            String sql
                    = "SELECT r.id AS reminder_id, r.repeat_type, r.start_date, r.end_date, r.user_id, r.medicine_id, r.dosage, r.notes, "
                    + "rt.time AS rtime, rt.weekday AS weekday, m.name AS medicine_name "
                    + "FROM reminders r "
                    + "JOIN reminder_times rt ON rt.reminder_id = r.id "
                    + "JOIN medicines m ON m.id = r.medicine_id "
                    + "WHERE r.user_id = ? "
                    + "AND r.start_date <= ? "
                    + "AND (r.end_date IS NULL OR r.end_date >= ?) "
                    + "AND rt.time = ?"; // time exact match to minute

            try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setLong(1, Session.getCurrentUserId());
                ps.setDate(2, java.sql.Date.valueOf(today));
                ps.setDate(3, java.sql.Date.valueOf(today));
                ps.setTime(4, Time.valueOf(timeToMatch));

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    long reminderId = rs.getLong("reminder_id");
                    String repeatType = rs.getString("repeat_type"); // e.g. "daily", "weekly", "alternate", "custom"
                    Date startDateSql = rs.getDate("start_date");
                    LocalDate startDate = startDateSql != null ? startDateSql.toLocalDate() : today;
                    Integer weekday = rs.getObject("weekday") != null ? rs.getInt("weekday") : null; // likely 1..7 or 0..6 depending on app
                    String medicineName = rs.getString("medicine_name");
                    String dosage = rs.getString("dosage");
                    Time rtime = rs.getTime("rtime");

                    // Decide whether this specific reminder_time should fire today
                    boolean shouldFire = false;

                    if ("alternate".equalsIgnoreCase(repeatType) || "alternate_days".equalsIgnoreCase(repeatType)) {
                        long daysDiff = ChronoUnit.DAYS.between(startDate, today);
                        shouldFire = (daysDiff % 2) == 0;
                    } else if ("weekly".equalsIgnoreCase(repeatType) || "custom".equalsIgnoreCase(repeatType)) {
                        if (weekday != null) {
                            // assume weekday in DB is 1=Mon..7=Sun (based on earlier SetReminderDialog code using i+1)
                            int todayWeekday = today.getDayOfWeek().getValue(); // 1 (Mon) .. 7 (Sun)
                            shouldFire = (weekday == todayWeekday);
                        } else {
                            shouldFire = true; // fallback
                        }
                    } else {
                        // daily or other -> fire every day in the date range
                        shouldFire = true;
                    }

                    if (!shouldFire) {
                        continue;
                    }

                    // Avoid duplicates across restarts by writing/reading reminder_history
                    if (wasAlreadyShown(conn, reminderId, rtime.toLocalTime(), today)) {
                        continue;
                    }

                    // mark as shown BEFORE showing (helps avoid duplicates)
                    writeHistory(conn, reminderId, rtime.toLocalTime(), today);

                    // show UI on Swing EDT
                    final String title = "Reminder: " + medicineName;
                    final String details = "Take: " + dosage + " at " + rtime.toLocalTime().truncatedTo(ChronoUnit.MINUTES);

                    // In ReminderScheduler.java -> pollAndShow() method:
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // Create and apply the blur effect to the parent frame.
                            BlurGlassPane glassPane = new BlurGlassPane(parentFrame);
                            parentFrame.setGlassPane(glassPane);
                            glassPane.setVisible(true);

                            // Create the ReminderPopup, passing the parent frame to it if needed
                            // ReminderPopup already has a constructor for this, you don't need to change its constructor
                            ReminderPopup popup = new ReminderPopup(title, medicineName, dosage, rtime.toLocalTime());
                            popup.setVisible(true);

                            // Once the popup is closed, remove the blur effect.
                            glassPane.setVisible(false);

                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Failed to show reminder UI", ex);
                        }
                    });
                }
            } // connection auto-closed

        } catch (Throwable t) {
            logger.log(Level.WARNING, "ReminderScheduler poll error", t);
        }
    }

    private boolean wasAlreadyShown(Connection conn, long reminderId, LocalTime time, LocalDate date) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM reminder_history WHERE reminder_id = ? AND reminder_time = ? AND shown_date = ?")) {
            ps.setLong(1, reminderId);
            ps.setTime(2, Time.valueOf(time));
            ps.setDate(3, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to check reminder_history", e);
            // If history table not present (older schema), return false so it'll show - this is safe
        }
        return false;
    }

    private void writeHistory(Connection conn, long reminderId, LocalTime time, LocalDate date) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reminder_history (reminder_id, reminder_time, shown_at, shown_date) VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, reminderId);
            ps.setTime(2, Time.valueOf(time));
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDate(4, java.sql.Date.valueOf(date));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to write reminder_history (maybe table missing) - continuing", e);
        }
    }
}
