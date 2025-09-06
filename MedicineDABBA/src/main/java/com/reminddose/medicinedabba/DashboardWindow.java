/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba;

/**
 * The main dashboard window for the RemindDose application.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.reminddose.medicinedabba.database.DBConnection;
import com.reminddose.medicinedabba.database.Session;
import com.reminddose.medicinedabba.dashboard.*;

/**
 * The main dashboard window for the RemindDose application.
 */
public class DashboardWindow extends JFrame {

    // Define colors to match the main application theme
    private final Color COLOR_BACKGROUND = new Color(243, 243, 191); // pale green
    private final Color COLOR_PANEL_BG = new Color(255, 255, 255);   // White
    private final Color COLOR_BUTTON_GREEN = new Color(111, 189, 82); // Green
    private final Color COLOR_BUTTON_RED = new Color(220, 53, 69);    // Red
    private final Color COLOR_TEXT_PRIMARY = new Color(51, 51, 51); // Dark text
    private final Color COLOR_TEXT_SECONDARY = new Color(108, 117, 125); // Gray text
    private final Color COLOR_BLUE_TEXT = new Color(94, 169, 244);
    private final Color COLOR_ORANGE_TEXT = new Color(253, 126, 20);

    private JTextField searchInput;
    private JPanel mainPanel;
    private JPanel dashboardTilesPanel;
    private JPanel categoryTilesPanel;

    private ReminderScheduler reminderScheduler;

    public DashboardWindow() {

        //icon setting
        Image icon = new ImageIcon(getClass().getResource("ico.png")).getImage();
        setIconImage(icon);

        // --- FRAME SETUP ---
        setTitle("RemindDose Dashboard - " + Session.getUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(COLOR_BACKGROUND);

        // Set application icon
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        }

        // --- HEADER / NAVIGATION PANEL ---
        add(createHeaderPanel(), BorderLayout.NORTH);

        // --- MAIN SCROLLABLE CONTENT ---
        mainPanel = createMainContentPanel();
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Load data from database
        loadDashboardData();

        // in constructor or after login:
        reminderScheduler = new ReminderScheduler();
        reminderScheduler.start();

        // Finalize
        setLocationRelativeTo(null);
    }

    // on window close (or logout)
    @Override
    public void dispose() {
        if (reminderScheduler != null) {
            reminderScheduler.stop();
        }
        super.dispose();
    }

    /**
     * Creates the header panel with navigation.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(new Color(222, 244, 180));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel logoLabel = new JLabel("<html><strong>RemindDose</strong><sub style='font-size:0.6em; margin-left:5px;'>(A Medicine Management System)</sub></html>");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        JButton newMedicineBtn = createNavLink("New Medicine");
        newMedicineBtn.addActionListener(e -> openAddMedicineModal());
        navPanel.add(newMedicineBtn);

        JButton dashboardBtn = createNavLink("Dashboard");
        dashboardBtn.addActionListener(e -> refreshDashboard());
        navPanel.add(dashboardBtn);

        JButton setReminderBtn = createNavLink("Set Reminder");
        setReminderBtn.addActionListener(e -> openReminderModal());
        navPanel.add(setReminderBtn);

//        JButton importBtn = createNavLink("Import");
//        navPanel.add(importBtn);
//        
//        JButton saveDataBtn = createNavLink("Save Data");
//        navPanel.add(saveDataBtn);
        JButton profileBtn = createNavLink("Profile");
        profileBtn.addActionListener(e -> openProfileModal());
        navPanel.add(profileBtn);

        // Add the new Report link
        JButton reportBtn = createNavLink("Report");
        reportBtn.addActionListener(e -> openReportWindow());
        navPanel.add(reportBtn);

        JButton signoutButton = new JButton("Signout/Logout");
        signoutButton.setBackground(new Color(111, 189, 82));
        signoutButton.addActionListener(e -> {
            Session.logout();
            this.dispose();
            new login().setVisible(true);
        });
        stylePrimaryButton(signoutButton);
        signoutButton.setForeground(new Color(255, 255, 255));
        navPanel.add(signoutButton);
        headerPanel.add(navPanel, BorderLayout.CENTER);

        return headerPanel;
    }

    /**
     * Creates the main content panel containing all sections.
     */
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(10, 40, 20, 40));

        // Search Bar - Modern rounded design
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setOpaque(false);
        searchPanel.setMaximumSize(new Dimension(500, 50));

        searchInput = new RoundedTextField("Search medicines...", 20);
        searchInput.setPreferredSize(new Dimension(400, 40));
        searchInput.setFont(new Font("Arial", Font.PLAIN, 14));
        searchInput.setForeground(COLOR_TEXT_SECONDARY);
        searchInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchInput.getText().equals("Search medicines...")) {
                    searchInput.setText("");
                    searchInput.setForeground(COLOR_TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchInput.getText().isEmpty()) {
                    searchInput.setText("Search medicines...");
                    searchInput.setForeground(COLOR_TEXT_SECONDARY);
                }
            }
        });
        searchInput.addActionListener(e -> searchMedicines());

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.setBackground(COLOR_BUTTON_GREEN);
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> searchMedicines());

        searchPanel.add(searchInput);
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Dashboard Tiles
        dashboardTilesPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        dashboardTilesPanel.setOpaque(false);
        mainPanel.add(dashboardTilesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Medicine Categories
        mainPanel.add(createSectionTitle("Medicine Categories"));
        categoryTilesPanel = createCategoryTiles();
        mainPanel.add(categoryTilesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action Buttons
        mainPanel.add(createActionButtons());

        return mainPanel;
    }

    /**
     * Creates the top row of dashboard info tiles.
     */
    private void createDashboardTiles() {
        dashboardTilesPanel.removeAll();
        dashboardTilesPanel.add(new RoundedPanel(15, COLOR_PANEL_BG)); // Today's Medicines
        dashboardTilesPanel.add(new RoundedPanel(15, COLOR_PANEL_BG)); // Expiring Soon
        dashboardTilesPanel.add(new RoundedPanel(15, COLOR_PANEL_BG)); // Upcoming Refills

        // Medicine A-Z Tile
        RoundedPanel azPanel = new RoundedPanel(15, Color.WHITE);
        azPanel.setLayout(new BorderLayout());
        JLabel azLabel = new JLabel("Medicine A-Z", SwingConstants.CENTER);
        azLabel.setFont(new Font("Arial", Font.BOLD, 18));
        azLabel.setForeground(COLOR_TEXT_PRIMARY);
        azPanel.add(azLabel, BorderLayout.CENTER);
        azPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add mouse click listener
        azPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Modified to call the new method that handles blurring
                openMedicineStorageUI();
            }
        });

        dashboardTilesPanel.add(azPanel);
        dashboardTilesPanel.revalidate();
        dashboardTilesPanel.repaint();
    }

    /**
     * Creates the row of medicine category tiles.
     */
    private JPanel createCategoryTiles() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 15));
        panel.setOpaque(false);

        panel.add(createCategoryTile("Morning", "0 medicines", "8:00 AM - 10:00 AM", new Color(251, 191, 36)));
        panel.add(createCategoryTile("Afternoon", "0 medicines", "2:00 PM - 4:00 PM", new Color(251, 146, 60)));
        panel.add(createCategoryTile("Evening", "0 medicines", "6:00 PM - 8:00 PM", new Color(79, 70, 229)));
        panel.add(createCategoryTile("Bedtime", "0 medicines", "9:00 PM - 10:00 PM", new Color(124, 58, 237)));
        panel.add(createCategoryTile("General Medicine", "0 medicines", "All Medicines", new Color(22, 163, 74)));

        return panel;
    }

    /**
     * Creates the two rows of action buttons.
     */
    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setOpaque(false);

        JButton addMedicineBtn = createActionButton("Add New Medicine", "Track a new medication", "Add Medicine", COLOR_BUTTON_GREEN);
        addMedicineBtn.addActionListener(e -> openAddMedicineModal());
        panel.add(addMedicineBtn);

        JButton setReminderBtn = createActionButton("Set Reminder", "Create a new reminder schedule", "Set Reminder", COLOR_BUTTON_GREEN);
        setReminderBtn.addActionListener(e -> openReminderModal());
        panel.add(setReminderBtn);

        JButton removeMedicineBtn = createActionButton("Remove Medicine", "Delete an existing medication", "Remove Medicine", COLOR_BUTTON_RED);
        removeMedicineBtn.addActionListener(e -> openRemoveMedicineModal());
        panel.add(removeMedicineBtn);

        JButton removeReminderBtn = createActionButton("Remove Reminder", "Delete an existing reminder", "Remove Reminder", COLOR_BUTTON_RED);
        removeReminderBtn.addActionListener(e -> openRemoveReminderModal());
        panel.add(removeReminderBtn);

        return panel;
    }

    // --- HELPER METHODS FOR CREATING COMPONENTS ---
    private JPanel createInfoTile(String title, String count, String details, Color countColor) {
        RoundedPanel panel = new RoundedPanel(15, COLOR_PANEL_BG);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JLabel countLabel = new JLabel(count, SwingConstants.CENTER);
        countLabel.setFont(new Font("Arial", Font.BOLD, 18));
        countLabel.setForeground(countColor);

        JLabel detailsLabel = new JLabel(details, SwingConstants.CENTER);
        detailsLabel.setFont(new Font("Arial", Font.BOLD, 12)); // Made medicine names bold
        detailsLabel.setForeground(COLOR_TEXT_SECONDARY);

        centerPanel.add(countLabel, BorderLayout.NORTH);
        centerPanel.add(detailsLabel, BorderLayout.CENTER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a list-style tile without buttons for Today's Medicines and
     * Expiring Soon.
     */
    private JPanel createListTile(String title, List<MedicineInfo> medicines, Color countColor) {
        RoundedPanel panel = new RoundedPanel(15, COLOR_PANEL_BG);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel countLabel = new JLabel(medicines.size() + " medicines");
        countLabel.setFont(new Font("Arial", Font.BOLD, 14));
        countLabel.setForeground(countColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // List Panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        for (MedicineInfo medicine : medicines) {
            JPanel medicineItem = new RoundedPanel(10, new Color(248, 248, 255)); // Lighter background
            medicineItem.setLayout(new BorderLayout(10, 0));
            medicineItem.setBorder(new EmptyBorder(8, 10, 8, 10));
            medicineItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(medicine.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setForeground(COLOR_BLUE_TEXT);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel detailsLabel = new JLabel();
            if (title.equals("Expiring Soon")) {
                detailsLabel.setText("Expires in " + medicine.getQuantity() + " days");
                detailsLabel.setForeground(COLOR_ORANGE_TEXT);
            } else if (title.equals("Today's Medicines")) {
                detailsLabel.setText("1 tablet(s) - " + medicine.getTime()); // Assuming 1 tablet dose for now, updated to show time
                detailsLabel.setForeground(COLOR_TEXT_SECONDARY);
            } else {
                detailsLabel.setText(medicine.getQuantity() + " remaining");
                detailsLabel.setForeground(COLOR_TEXT_SECONDARY);
            }
            detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(nameLabel);
            textPanel.add(detailsLabel);

            medicineItem.add(textPanel, BorderLayout.CENTER);

            listPanel.add(medicineItem);
            listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        panel.add(listPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the new custom "Upcoming Refills" tile with refill buttons.
     */
    private JPanel createRefillTile(List<MedicineInfo> medicines) {
        RoundedPanel panel = new RoundedPanel(15, COLOR_PANEL_BG);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Upcoming Refills");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel countLabel = new JLabel(medicines.size() + " medicines");
        countLabel.setFont(new Font("Arial", Font.BOLD, 14));
        countLabel.setForeground(COLOR_BLUE_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // List Panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        for (MedicineInfo medicine : medicines) {
            JPanel medicineItem = new RoundedPanel(10, new Color(248, 248, 255)); // Lighter blue/gray background
            medicineItem.setLayout(new BorderLayout(10, 0));
            medicineItem.setBorder(new EmptyBorder(8, 10, 8, 10));
            medicineItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(medicine.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setForeground(COLOR_BLUE_TEXT);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel quantityLabel = new JLabel(medicine.getQuantity() + " remaining");
            quantityLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            quantityLabel.setForeground(COLOR_TEXT_SECONDARY);
            quantityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(nameLabel);
            textPanel.add(quantityLabel);

            JButton refillButton = new RoundedButton("Refill");
            refillButton.setPreferredSize(new Dimension(80, 30));
            refillButton.setBackground(new Color(81, 189, 101));
            refillButton.setForeground(Color.WHITE);
            refillButton.setFont(new Font("Arial", Font.BOLD, 12));
            refillButton.setFocusPainted(false);
            refillButton.setBorder(new EmptyBorder(5, 10, 5, 10));

            refillButton.addActionListener(e -> openRefillModal(medicine.getId()));

            medicineItem.add(textPanel, BorderLayout.CENTER);
            medicineItem.add(refillButton, BorderLayout.EAST);

            listPanel.add(medicineItem);
            listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        panel.add(listPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCategoryTile(String title, String count, String time, Color titleColor) {
        RoundedPanel panel = new RoundedPanel(15, COLOR_PANEL_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(titleColor);

        JLabel countLabel = new JLabel(count, SwingConstants.CENTER);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        countLabel.setForeground(COLOR_TEXT_SECONDARY);

        JLabel timeLabel = new JLabel(time, SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(COLOR_TEXT_SECONDARY);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(countLabel);
        panel.add(timeLabel);

        return panel;
    }

    private JButton createActionButton(String title, String subtitle, String buttonText, Color buttonColor) {
        RoundedButton button = new RoundedButton(buttonText, 10);
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));

        return button;
    }

    private JButton createNavLink(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(111, 189, 82));
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createSectionTitle(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(label);
        return panel;
    }

    // --- DATABASE OPERATIONS ---
    private void loadDashboardData() {
        createDashboardTiles(); // Recreate tiles to clear previous data

        try (Connection conn = DBConnection.getConnection()) {
            long userId = Session.getCurrentUserId();

            // 1. Today's Medicines - Count of medicines with reminders scheduled for today
            String todayQuery = "SELECT m.id, m.name, m.quantity, TIME_FORMAT(rt.time, '%h:%i %p') AS formatted_time "
                    + "FROM medicines m "
                    + "JOIN reminders r ON m.id = r.medicine_id "
                    + "JOIN reminder_times rt ON r.id = rt.reminder_id "
                    + "WHERE m.user_id = ? AND r.user_id = ? "
                    + "AND CURDATE() BETWEEN r.start_date AND r.end_date "
                    + "AND (r.repeat_type = 'daily' OR "
                    + "(r.repeat_type = 'weekly' AND WEEKDAY(CURDATE()) + 1 = rt.weekday) OR "
                    + "(r.repeat_type = 'alternate' AND DATEDIFF(CURDATE(), r.start_date) % 2 = 0))";
            PreparedStatement stmt = conn.prepareStatement(todayQuery);
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            List<MedicineInfo> todayMedicines = new ArrayList<>();
            while (rs.next()) {
                todayMedicines.add(new MedicineInfo(rs.getInt("id"), rs.getString("name"), rs.getInt("quantity"), rs.getString("formatted_time")));
            }

            JPanel todayTile = createListTile("Today's Medicines", todayMedicines, COLOR_BLUE_TEXT);
            Component existingTodayTile = dashboardTilesPanel.getComponent(0);
            if (existingTodayTile instanceof RoundedPanel) {
                RoundedPanel panel = (RoundedPanel) existingTodayTile;
                panel.removeAll();
                panel.setLayout(new BorderLayout());
                panel.add(todayTile, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }

            // 2. Expiring Soon - Medicines expiring in the next 24 days
            String expiringQuery = "SELECT id, name, DATEDIFF(expiry_date, CURDATE()) as days_left "
                    + "FROM medicines "
                    + "WHERE user_id = ? AND expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 24 DAY)";
            stmt = conn.prepareStatement(expiringQuery);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();

            List<MedicineInfo> expiringMedicines = new ArrayList<>();
            while (rs.next()) {
                expiringMedicines.add(new MedicineInfo(rs.getInt("id"), rs.getString("name"), rs.getInt("days_left")));
            }

            JPanel expiringTile = createListTile("Expiring Soon", expiringMedicines, COLOR_ORANGE_TEXT);
            Component existingExpiringTile = dashboardTilesPanel.getComponent(1);
            if (existingExpiringTile instanceof RoundedPanel) {
                RoundedPanel panel = (RoundedPanel) existingExpiringTile;
                panel.removeAll();
                panel.setLayout(new BorderLayout());
                panel.add(expiringTile, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }

            // 3. Upcoming Refills - Medicines with quantity <= 4
            String refillQuery = "SELECT id, name, quantity FROM medicines WHERE user_id = ? AND quantity <= 4 ORDER BY name";
            stmt = conn.prepareStatement(refillQuery);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();

            List<MedicineInfo> refillMedicines = new ArrayList<>();
            while (rs.next()) {
                refillMedicines.add(new MedicineInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity")
                ));
            }

            // This is the key change: update the refills tile with the new custom method
            JPanel refillTile = createRefillTile(refillMedicines);
            Component existingRefillTile = dashboardTilesPanel.getComponent(2);
            if (existingRefillTile instanceof RoundedPanel) {
                RoundedPanel panel = (RoundedPanel) existingRefillTile;
                panel.removeAll();
                panel.setLayout(new BorderLayout());
                panel.add(refillTile, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }

            // 4. Load category counts for Morning, Afternoon, Evening, Bedtime, General
            String[] categories = {"morning", "afternoon", "evening", "bedtime", "general"};
            for (int i = 0; i < categories.length; i++) {
                String categoryQuery = "SELECT COUNT(*) as count, "
                        + "GROUP_CONCAT(name SEPARATOR ', ') as medicines "
                        + "FROM medicines "
                        + "WHERE user_id = ? AND category = ?";
                stmt = conn.prepareStatement(categoryQuery);
                stmt.setLong(1, userId);
                stmt.setString(2, categories[i]);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String count = rs.getInt("count") + " medicines";
                    String medicines = formatMedicineList(rs.getString("medicines"));
                    updateCategoryTile(i, count, medicines);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading dashboard data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String formatMedicineList(String medicines) {
        if (medicines == null || medicines.trim().isEmpty()) {
            return "No medicines";
        }

        // Split the comma-separated list and format with HTML for better display
        String[] medicineArray = medicines.split(", ");
        if (medicineArray.length > 3) {
            // Show only first 3 medicines with "and X more"
            StringBuilder result = new StringBuilder("<html><center>");
            for (int i = 0; i < Math.min(3, medicineArray.length); i++) {
                result.append(medicineArray[i]).append("<br>");
            }
            if (medicineArray.length > 3) {
                result.append("and ").append(medicineArray.length - 3).append(" more");
            }
            result.append("</center></html>");
            return result.toString();
        } else {
            // Show all medicines with line breaks
            StringBuilder result = new StringBuilder("<html><center>");
            for (String medicine : medicineArray) {
                result.append(medicine).append("<br>");
            }
            result.append("</center></html>");
            return result.toString();
        }
    }

    private void updateInfoTile(int index, String count, String details, Color countColor) {
        Component tile = dashboardTilesPanel.getComponent(index);
        if (tile instanceof RoundedPanel) {
            RoundedPanel panel = (RoundedPanel) tile;

            // Remove existing components
            panel.removeAll();

            // Recreate the tile with centered content
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel titleLabel = new JLabel(getTileTitle(index), SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);

            JLabel countLabel = new JLabel(count, SwingConstants.CENTER);
            countLabel.setFont(new Font("Arial", Font.BOLD, 18));
            countLabel.setForeground(countColor);

            JLabel detailsLabel = new JLabel(details, SwingConstants.CENTER);
            detailsLabel.setFont(new Font("Arial", Font.BOLD, 12)); // Made medicine names bold
            detailsLabel.setForeground(COLOR_TEXT_SECONDARY);

            centerPanel.add(countLabel, BorderLayout.NORTH);
            centerPanel.add(detailsLabel, BorderLayout.CENTER);

            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(centerPanel, BorderLayout.CENTER);

            panel.revalidate();
            panel.repaint();
        }
    }

    private String getTileTitle(int index) {
        switch (index) {
            case 0:
                return "Today's Medicines";
            case 1:
                return "Expiring Soon";
            case 2:
                return "Upcoming Refills";
            default:
                return "";
        }
    }

    private void updateCategoryTile(int index, String count, String medicines) {
        Component tile = categoryTilesPanel.getComponent(index);
        if (tile instanceof RoundedPanel) {
            RoundedPanel panel = (RoundedPanel) tile;

            // Remove existing components
            panel.removeAll();

            // Recreate the tile with centered content
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            String[] titles = {"Morning", "Afternoon", "Evening", "Bedtime", "General Medicine"};
            Color[] colors = {
                new Color(251, 191, 36),
                new Color(251, 146, 60),
                new Color(79, 70, 229),
                new Color(124, 58, 237),
                new Color(22, 163, 74)
            };
            String[] times = {
                "8:00 AM - 10:00 AM",
                "2:00 PM - 4:00 PM",
                "6:00 PM - 8:00 PM",
                "9:00 PM - 10:00 PM",
                "All Medicines"
            };

            JLabel titleLabel = new JLabel(titles[index], SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setForeground(colors[index]);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel countLabel = new JLabel(count, SwingConstants.CENTER);
            countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            countLabel.setForeground(COLOR_TEXT_SECONDARY);
            countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel medicinesLabel = new JLabel(medicines, SwingConstants.CENTER);
            medicinesLabel.setFont(new Font("Arial", Font.BOLD, 11));
            medicinesLabel.setForeground(COLOR_TEXT_SECONDARY);
            medicinesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel timeLabel = new JLabel(times[index], SwingConstants.CENTER);
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            timeLabel.setForeground(COLOR_TEXT_SECONDARY);
            timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(titleLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(countLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(medicinesLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            panel.add(timeLabel);

            panel.revalidate();
            panel.repaint();
        }
    }

    private void searchMedicines() {
        String searchTerm = searchInput.getText().trim();
        if (searchTerm.isEmpty() || searchTerm.equals("Search medicines...")) {
            refreshDashboard();
            return;
        }

        // Search medicines in database for current user only
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT m.name, m.category, m.quantity, m.expiry_date "
                    + "FROM medicines m "
                    + "WHERE m.user_id = ? AND (m.name LIKE ? OR m.category LIKE ?) "
                    + "ORDER BY m.name";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, Session.getCurrentUserId());
            stmt.setString(2, "%" + searchTerm + "%");
            stmt.setString(3, "%" + searchTerm + "%");

            ResultSet rs = stmt.executeQuery();

            // Create a dialog to display search results
            JDialog resultsDialog = new JDialog(this, "Search Results - '" + searchTerm + "'", true);
            resultsDialog.setSize(700, 500);
            resultsDialog.setBackground(COLOR_BACKGROUND);
            resultsDialog.setLocationRelativeTo(this);
            resultsDialog.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JTextArea resultsArea = new JTextArea();
            resultsArea.setEditable(false);
            resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            StringBuilder results = new StringBuilder();
            results.append(String.format("%-25s %-15s %-10s %-15s\n", "Name", "Category", "Quantity", "Expiry Date"));
            results.append("-----------------------------------------------------------------\n");

            int resultCount = 0;
            while (rs.next()) {
                results.append(String.format("%-25s %-15s %-10d %-15s\n",
                        truncateString(rs.getString("name"), 22),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDate("expiry_date")));
                resultCount++;
            }

            if (resultCount == 0) {
                results.append("No medicines found matching your search.\n");
            } else {
                results.append("\n" + resultCount + " medicine(s) found.\n");
            }

            resultsArea.setText(results.toString());
            panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

            JButton closeButton = new JButton("Close");
            closeButton.setBackground(COLOR_BUTTON_RED);
            closeButton.addActionListener(e -> resultsDialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            resultsDialog.add(panel);
            BlurGlassPane bgp = new BlurGlassPane(this);
            this.setGlassPane(bgp);
            bgp.setVisible(true);
            resultsDialog.setVisible(true);
            bgp.setVisible(false);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching medicines: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String truncateString(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private void refreshDashboard() {
        // Refresh the dashboard with current data
        loadDashboardData();
    }

    // --- MODAL WINDOW METHODS ---
    private void openAddMedicineModal() {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        AddMedicineDialog dialog = new AddMedicineDialog(this, true);
        dialog.setVisible(true);
        bgp.setVisible(false);
        refreshDashboard();
    }

    /**
     * Opens the MedicineStorageUI as a modal dialog with a blurred parent
     * window.
     */
    private void openMedicineStorageUI() {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        MedicineStorageUI dialog = new MedicineStorageUI(this, true);
        dialog.setVisible(true);
        bgp.setVisible(false);
    }

    private void openReminderModal() {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        SetReminderDialog dialog = new SetReminderDialog(this, true);
        dialog.setVisible(true);
        bgp.setVisible(false);
        refreshDashboard();
    }

    private void openRemoveMedicineModal() {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        RemoveMedicineDialog dialog = new RemoveMedicineDialog(this, true);
        dialog.setVisible(true);
        bgp.setVisible(false);
        refreshDashboard();
    }

    private void openRemoveReminderModal() {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        RemoveReminderDialog dialog = new RemoveReminderDialog(this, true);
        dialog.setVisible(true);
        bgp.setVisible(false);
        refreshDashboard();
    }

    private void openProfileModal() {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        ProfileDialog dialog = new ProfileDialog(this, true);
        dialog.setVisible(true);
        bgp.setVisible(false);
    }

    /**
     * Opens the Reminder Report Insights window.
     */
    private void openReportWindow() {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        ReminderReportInsights reportWindow = new ReminderReportInsights(this, true);
        reportWindow.setVisible(true);
        bgp.setVisible(false);
    }

    private void openRefillModal(int medicineId) {
        BlurGlassPane bgp = new BlurGlassPane(this);
        this.setGlassPane(bgp);
        bgp.setVisible(true);
        RefillDialog dialog = new RefillDialog(this, true, medicineId);
        dialog.setVisible(true);
        bgp.setVisible(false);
        refreshDashboard();
    }

    /**
     * Inner class to hold medicine data for the refill list.
     */
    private static class MedicineInfo {

        private final int id;
        private final String name;
        private final int quantity;
        private String time; // Now includes time for "Today's Medicines"

        public MedicineInfo(int id, String name, int quantity) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
        }

        public MedicineInfo(int id, String name, int quantity, String time) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.time = time;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getTime() {
            return time;
        }
    }

    // --- INNER CLASS FOR ROUNDED TEXT FIELD ---
    private static class RoundedTextField extends JTextField {

        private int cornerRadius;

        public RoundedTextField(String text, int radius) {
            super(text);
            this.cornerRadius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paint the background with rounded corners
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Paint the border
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            super.paintComponent(g);
            g2.dispose();
        }
    }

    // --- INNER CLASS FOR ROUNDED PANELS ---
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

    // --- INNER CLASS FOR ROUNDED BUTTONS ---
    private static class RoundedButton extends JButton {

        private int cornerRadius;

        public RoundedButton(String text) {
            this(text, 10);
        }

        public RoundedButton(String text, int radius) {
            super(text);
            this.cornerRadius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();

            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No border painting for rounded button
        }
    }
}
