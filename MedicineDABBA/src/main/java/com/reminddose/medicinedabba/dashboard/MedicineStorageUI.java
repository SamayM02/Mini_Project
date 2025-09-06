package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class MedicineStorageUI extends JDialog {

    private JPanel gridPanel;
    private Map<Character, Integer> letterCounts = new HashMap<>();
    private Map<Character, String> letterMedicines = new HashMap<>();

    // ✅ Modified constructor to accept parent frame and modal flag
    public MedicineStorageUI(JFrame parent, boolean modal) {
        super(parent, "Medicine Storage", modal);
        initializeUI();
    }

    // ✅ Added no-arg constructor for backward compatibility
    public MedicineStorageUI() {
        this(null, false);
    }

    private void initializeUI() {
        setTitle("Medicine Storage");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Background color
        getContentPane().setBackground(new Color(243, 243, 191));

        // Title centered
        JLabel title = new JLabel("Medicine Storage", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        gridPanel = new JPanel(new GridLayout(7, 4, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loadMedicineData();

        for (char c = 'A'; c <= 'Z'; c++) {
            int count = letterCounts.getOrDefault(c, 0);
            RoundedButton letterBtn = new RoundedButton(
                    "<html><center>" + c + "<br>" + count + "</center></html>",
                    count > 0 ? new Color(0, 170, 120) : new Color(120, 220, 180)
            );
            letterBtn.setFont(new Font("Arial", Font.BOLD, 16));
            letterBtn.setForeground(Color.WHITE);

            // Tooltip on hover
            String meds = letterMedicines.getOrDefault(c, "No medicines");
            letterBtn.setToolTipText("<html>" + meds.replaceAll("\n", "<br>") + "</html>");

            gridPanel.add(letterBtn);
        }

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(title, BorderLayout.NORTH);
        container.add(gridPanel, BorderLayout.CENTER);

        add(container);
    }

    private void loadMedicineData() {
        long userId = Session.getCurrentUserId();

        String sql = "SELECT name FROM medicines WHERE user_id = ? ORDER BY name ASC";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    if (name != null && !name.isEmpty()) {
                        char firstLetter = Character.toUpperCase(name.charAt(0));
                        if (firstLetter < 'A' || firstLetter > 'Z') {
                            continue;
                        }

                        // Count increment
                        letterCounts.put(firstLetter, letterCounts.getOrDefault(firstLetter, 0) + 1);

                        // Medicines list for tooltip
                        String existing = letterMedicines.getOrDefault(firstLetter, "");
                        if (!existing.isEmpty()) {
                            existing += "\n";
                        }
                        existing += name;
                        letterMedicines.put(firstLetter, existing);
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Rounded button class
    static class RoundedButton extends JButton {

        private final Color bgColor;

        public RoundedButton(String text, Color bgColor) {
            super(text);
            this.bgColor = bgColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(bgColor);
            g2.fill(round);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setForeground(Color.WHITE);
        }
    }
}
