package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.sound.sampled.*;
import java.net.URL;

public class ReminderPopup extends JDialog {
    private final String titleText;
    private final String medicineName;
    private final String dosage;
    private final LocalTime scheduledTime;
    private Clip audioClip;

    public ReminderPopup(String titleText, String medicineName, String dosage, LocalTime scheduledTime) {
        // Use the classic JDialog constructor
        super((Frame) null, "About Us", true);
        this.titleText = titleText;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.scheduledTime = scheduledTime;

        // setUndecorated(true); // Commented out to allow classic JDialog border
        setModal(true);
        setSize(400, 280);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create and add the new custom panel for the content and animation
        PulsingPanel mainPanel = new PulsingPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(243, 243, 191));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Title Label
        JLabel titleLabel = new JLabel("TIME TO TAKE YOUR MEDICINE!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(51, 51, 51));
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        // Medicine Details Panel
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        detailsPanel.setOpaque(false);

        JLabel medLabel = new JLabel("<html><center><b>" + medicineName + "</b></center></html>", SwingConstants.CENTER);
        medLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        medLabel.setForeground(new Color(51, 51, 51));
        
        // Format the time to AM/PM and no seconds
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        JLabel dosageLabel = new JLabel("<html><center>" + dosage + " at " + scheduledTime.format(formatter) + "</center></html>", SwingConstants.CENTER);
        dosageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dosageLabel.setForeground(new Color(108, 117, 125));

        detailsPanel.add(medLabel);
        detailsPanel.add(dosageLabel);
        gbc.gridy = 1;
        mainPanel.add(detailsPanel, gbc);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);

        JButton takeBtn = createButton("TAKE", new Color(111, 189, 82), Color.BLACK);
        takeBtn.addActionListener(e -> {
            logAction("taken");
            stopAudio();
            dispose();
        });

        JButton snoozeBtn = createButton("SNOOZE", new Color(253, 126, 20), Color.BLACK);
        snoozeBtn.addActionListener(e -> {
            logAction("snoozed");
            stopAudio();
            // Ask custom snooze minutes (default 5)
            String input = JOptionPane.showInputDialog(this, "Snooze for how many minutes?", "5");
            int minutes = 5;
            try {
                if (input != null && !input.trim().isEmpty()) {
                    minutes = Integer.parseInt(input.trim());
                }
            } catch (NumberFormatException ignored) {
            }
            JOptionPane.showMessageDialog(this, "Snoozed for " + minutes + " minutes.");
            dispose();
        });

        JButton cancelBtn = createButton("CANCEL", new Color(220, 53, 69), Color.BLACK);
        cancelBtn.addActionListener(e -> {
            logAction("missed");
            stopAudio();
            dispose();
        });

        buttonsPanel.add(takeBtn);
        buttonsPanel.add(snoozeBtn);
        buttonsPanel.add(cancelBtn);
        gbc.gridy = 2;
        mainPanel.add(buttonsPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // --- Window listeners to manage audio playback ---
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                playAudio("OG_Alarm.wav");
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopAudio();
            }
        });
    }

    private JButton createButton(String text, Color bgColor, Color borderColor) {
        return new RoundedButton(text, bgColor, borderColor);
    }

    private void logAction(String action) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO reminder_logs (medicine_name, dosage, scheduled_time, taken_status, taken_time, user_id, medicine_category) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, medicineName);
            ps.setString(2, dosage);
            LocalDateTime scheduledDateTime = LocalDateTime.of(LocalDate.now(), scheduledTime);
            ps.setTimestamp(3, Timestamp.valueOf(scheduledDateTime));
            ps.setString(4, action);
            if ("taken".equals(action)) {
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            ps.setLong(6, Session.getCurrentUserId());
            ps.setString(7, "Not Specified"); // Set a default value for the missing field
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to log reminder: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void playAudio(String audioFilePath) {
        try {
            // Get the URL from the resources folder
            URL audioURL = getClass().getResource("/" + audioFilePath);
            if (audioURL == null) {
                System.err.println("Audio file not found in resources: " + audioFilePath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioURL);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            audioClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the audio
            audioClip.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    private void stopAudio() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
            audioClip.close();
        }
    }

    // New Inner Class for the pulsing animation
    private class PulsingPanel extends JPanel {
        private final Timer pulseTimer;
        private float pulseAlpha = 0.5f;
        private boolean increasing = true;

        public PulsingPanel() {
            setOpaque(false);
            pulseTimer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (increasing) {
                        pulseAlpha += 0.05f;
                        if (pulseAlpha >= 1.0f) {
                            pulseAlpha = 1.0f;
                            increasing = false;
                        }
                    } else {
                        pulseAlpha -= 0.05f;
                        if (pulseAlpha <= 0.5f) {
                            pulseAlpha = 0.5f;
                            increasing = true;
                        }
                    }
                    repaint();
                }
            });
            pulseTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Pulsing background color
            g2d.setColor(new Color(111, 189, 82, (int) (255 * pulseAlpha))); // Pulsing green
            g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

            // Add the custom border
            g2d.setColor(new Color(81, 189, 101));
            g2d.setStroke(new BasicStroke(4));
            g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));

            g2d.dispose();
        }
    }
    
    // New custom button class with rounded corners and border
    private static class RoundedButton extends JButton {
        private final Color backgroundColor;
        private final Color borderColor;
        private static final int CORNER_RADIUS = 15;

        public RoundedButton(String text, Color bgColor, Color borderColor) {
            super(text);
            this.backgroundColor = bgColor;
            this.borderColor = borderColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paint the rounded background
            g2.setColor(backgroundColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));

            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paint the rounded border
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(2)); // Border thickness
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, CORNER_RADIUS, CORNER_RADIUS));

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        // Use a dummy user session for testing database logging
        Session.setUser(1L, "testuser");
        
        // Ensure the Swing UI is created on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Create a dummy reminder popup for testing
            ReminderPopup popup = new ReminderPopup(
                "Medicine Reminder",  // Title (unused by the new UI, but kept for constructor)
                "Paracetamol",        // Medicine Name
                "1 Tablet",           // Dosage
                LocalTime.now()       // Scheduled time
            );
            popup.setVisible(true);
        });
    }
}
