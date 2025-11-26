/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MedicineStorageUI extends JDialog {

    private JPanel gridPanel;
    private Map<Character, Integer> letterCounts = new HashMap<>();
    private Map<Character, String> letterMedicines = new HashMap<>();
    private final JFrame parentFrame;

    public MedicineStorageUI(JFrame parent, boolean modal) {
        super(parent, "Medicine Storage", modal);
        this.parentFrame = parent;
        initializeUI();
    }

    public MedicineStorageUI() {
        this(null, false);
    }

    private void initializeUI() {
        setTitle("Medicine Storage");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(243, 243, 191));

        JLabel title = new JLabel("Medicine Storage", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        gridPanel = new JPanel(new GridLayout(7, 4, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loadMedicineData();

        for (char c = 'A'; c <= 'Z'; c++) {
            int count = letterCounts.getOrDefault(c, 0);
            FilledRoundedButton letterBtn = new FilledRoundedButton(
                    "<html><center>" + c + "<br>" + count + "</center></html>",
                    count > 0 ? new Color(0, 170, 120) : new Color(120, 220, 180)
            );
            letterBtn.setFont(new Font("Arial", Font.BOLD, 16));
            letterBtn.setForeground(Color.WHITE);

            // Tooltip on hover
            String meds = letterMedicines.getOrDefault(c, "No medicines");
            letterBtn.setToolTipText("<html>" + meds.replaceAll("\n", "<br>") + "</html>");
            
            final char selectedLetter = c;
            letterBtn.addActionListener(e -> showMedicineDetails(selectedLetter));
            
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
    
    private void showMedicineDetails(char letter) {
        String detailsSql = "SELECT name, category, quantity, expiry_date, dosage FROM medicines WHERE user_id = ? AND name LIKE ? ORDER BY name";
        
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(detailsSql)) {
            ps.setLong(1, Session.getCurrentUserId());
            ps.setString(2, letter + "%");
            
            ResultSet rs = ps.executeQuery();
            
            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setBackground(new Color(243, 243, 191));
            detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("Medicines starting with '" + letter + "'");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailsPanel.add(titleLabel);
            detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

            if (!rs.isBeforeFirst()) {
                JLabel noMedLabel = new JLabel("No medicines found.", SwingConstants.CENTER);
                noMedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                detailsPanel.add(noMedLabel);
            } else {
                while (rs.next()) {
                    JPanel medCard = new RoundedPanel(10, Color.WHITE);
                    medCard.setLayout(new BoxLayout(medCard, BoxLayout.Y_AXIS));
                    medCard.setBorder(new EmptyBorder(10, 15, 10, 15));
                    
                    JLabel nameLabel = new JLabel("<html><b>" + rs.getString("name") + "</b></html>");
                    nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    JLabel categoryLabel = new JLabel("Category: " + rs.getString("category"));
                    categoryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    JLabel dosageLabel = new JLabel("Dosage: " + rs.getString("dosage"));
                    dosageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    dosageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    JLabel quantityLabel = new JLabel("Quantity: " + rs.getInt("quantity"));
                    quantityLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    quantityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    JLabel expiryLabel = new JLabel("Expiry Date: " + rs.getDate("expiry_date"));
                    expiryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    expiryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    medCard.add(nameLabel);
                    medCard.add(categoryLabel);
                    medCard.add(dosageLabel);
                    medCard.add(quantityLabel);
                    medCard.add(expiryLabel);
                    
                    // Wrap the card in a new panel with FlowLayout for centering
                    JPanel cardWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    cardWrapper.setOpaque(false);
                    cardWrapper.add(medCard);
                    
                    detailsPanel.add(cardWrapper);
                    detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            
            JScrollPane scrollPane = new JScrollPane(detailsPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            // Create and show the new dialog
            JDialog detailsDialog = new JDialog(parentFrame, "Medicine Details", true);
            detailsDialog.add(scrollPane);
            detailsDialog.setSize(450, 600);
            detailsDialog.setLocationRelativeTo(parentFrame);
            
            BlurGlassPane bgp = new BlurGlassPane(parentFrame);
            parentFrame.setGlassPane(bgp);
            bgp.setVisible(true);
            detailsDialog.setVisible(true);
            bgp.setVisible(false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching medicine details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Rounded button class
    static class FilledRoundedButton extends JButton {
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

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(baseColor.brighter());
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(baseColor);
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
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
        }
    }
    
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
}
