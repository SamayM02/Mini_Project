/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.reminddose.medicinedabba;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.net.URL;

public class RemindDoseGUI extends JFrame {

    // Declare panels as class members to be accessible by action listeners
    private JPanel featuresPanel;
    private JPanel howItWorksPanel;
    private JPanel reminderGuidePanel;
    private JScrollPane scrollPane;
    private Timer scrollTimer; // Timer for smooth scrolling animation

    // Define colors for easy access and modification
    private final Color COLOR_BACKGROUND = new Color(243, 243, 191);//pale green
    private final Color COLOR_NAV = new Color(81, 189, 101);//dark green
    private final Color COLOR_BUTTON_PRIMARY_BG = new Color(222, 244, 180);//pale green
    private final Color COLOR_LINK_ABOUT = new Color(94, 169, 244);//link blue
    private final Color COLOR_FOOTER_BG = new Color(17, 24, 39);//pale dark
    private final Color COLOR_CARD_BG = new Color(249, 249, 249);//white
    private final Color COLOR_TEXT = new Color(51, 51, 51);//pale dark
    private final Color COLOR_HEADER_BG = new Color(222, 244, 180);//pale green

    public RemindDoseGUI() {
        
        //icon setting
        Image icon = new ImageIcon(getClass().getResource("ico.png")).getImage();
        setIconImage(icon);
        
        // --- FRAME SETUP ---
        setTitle("RemindDose");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1050, 730));
        setMinimumSize(new Dimension(1000, 750));
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BACKGROUND);

        // Set application icon
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        }

        // --- HEADER / NAVIGATION PANEL ---
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN CONTENT PANEL (SCROLLABLE) ---
        JPanel mainPanel = createMainContentPanel();
        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // --- FOOTER PANEL ---
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        // Finalize and display the frame
        pack();
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    /**
     * Creates the header panel containing the logo and navigation buttons.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 20));
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel logoLabel = new JLabel("<html><h1>RemindDose<sub style='font-size:0.6em; margin-left:5px;'>(A Medicine Management System)</sub></h1></html>");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        navPanel.setOpaque(false);

        JButton featuresButton = createNavLink("Features");
        featuresButton.addActionListener(e -> smoothScrollTo(featuresPanel));
        JButton howItWorksButton = createNavLink("How It Works");
        howItWorksButton.addActionListener(e -> smoothScrollTo(howItWorksPanel));
        JButton reminderGuideButton = createNavLink("Reminder Guide");
        reminderGuideButton.addActionListener(e -> smoothScrollTo(reminderGuidePanel));
        JButton signInButton = new RoundedButton("Login");
        stylePrimaryButton(signInButton);
        signInButton.setBackground(COLOR_NAV);
        signInButton.setForeground(Color.WHITE);

        navPanel.add(featuresButton);
        navPanel.add(howItWorksButton);
        navPanel.add(reminderGuideButton);
        navPanel.add(signInButton);

        signInButton.addActionListener(e -> {
            // 1. Create and apply the blur effect to the main window.
            //    This uses the BlurGlassPane inner class from your RemindDoseGUI.
            BlurGlassPane glassPane = new BlurGlassPane(this);
            this.setGlassPane(glassPane);
            glassPane.setVisible(true);

            // 2. Create and show the AboutDialog.
            //    Since the dialog is modal, the code will pause here until it's closed.
            login dialog = new login();
            dialog.setVisible(true);

            // 3. Once the dialog is closed, remove the blur effect.
            glassPane.setVisible(false);
        });

        headerPanel.add(navPanel, BorderLayout.EAST);
        return headerPanel;
    }

    /**
     * Creates the main scrollable content area with all the sections.
     */
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        mainPanel.add(createHeroSection());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 70)));
        featuresPanel = createFeaturesSection();
        mainPanel.add(featuresPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 70)));
        howItWorksPanel = createHowItWorksSection();
        mainPanel.add(howItWorksPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 70)));
        reminderGuidePanel = createReminderGuideSection();
        mainPanel.add(reminderGuidePanel);

        return mainPanel;
    }

    /**
     * Creates the top "Hero" section with text and an image loaded from local
     * resources.
     */
    private JPanel createHeroSection() {
        JPanel heroPanel = new JPanel(new GridBagLayout());
        heroPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Your Smart Virtual Medicine Box");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Get daily medicine reminders, expiry alerts, and stay on top of your health.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton createProfileButton = new RoundedButton("Create Your Profile");
        stylePrimaryButton(createProfileButton);
        createProfileButton.setBackground(COLOR_NAV);
        createProfileButton.setForeground(Color.WHITE);
        createProfileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        createProfileButton.setMaximumSize(new Dimension(200, 40));
        createProfileButton.addActionListener(e -> {
            // 1. Create and apply the blur effect to the main window.
            //    This uses the BlurGlassPane inner class from your RemindDoseGUI.
            BlurGlassPane glassPane = new BlurGlassPane(this);
            this.setGlassPane(glassPane);
            glassPane.setVisible(true);

            // 2. Create and show the AboutDialog.
            //    Since the dialog is modal, the code will pause here until it's closed.
            SignUpWindow dialog = new SignUpWindow();
            dialog.setVisible(true);

            // 3. Once the dialog is closed, remove the blur effect.
            glassPane.setVisible(false);
        });

        textPanel.add(title);
        textPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        textPanel.add(subtitle);
        textPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        textPanel.add(createProfileButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        heroPanel.add(textPanel, gbc);

        // --- IMAGE LOADING FROM LOCAL PROJECT ---
        try {
            // Load the image from the resources folder
            URL imageUrl = getClass().getResource("/Medicine-Dabba-Ashish-Tone-Images.png");
            if (imageUrl == null) {
                throw new IOException("Image not found in resources: Medicine-Dabba-Ashish-Tone-Images.png");
            }
            Image image = ImageIO.read(imageUrl);
            Image scaledImage = image.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.WHITE);

            gbc.gridx = 1;
            gbc.weightx = 0.4;
            heroPanel.add(imageLabel, gbc);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            // Add a placeholder if image fails to load
            JPanel imagePlaceholder = new JPanel();
            imagePlaceholder.setPreferredSize(new Dimension(300, 300));
            imagePlaceholder.setBackground(Color.GRAY);
            JLabel errorLabel = new JLabel("Image not found");
            errorLabel.setForeground(Color.WHITE);
            imagePlaceholder.add(errorLabel);
            gbc.gridx = 1;
            gbc.weightx = 0.4;
            heroPanel.add(imagePlaceholder, gbc);
        }

        return heroPanel;
    }

    /**
     * Creates the "Key Features" section with multiple cards.
     */
    private JPanel createFeaturesSection() {
        JPanel sectionPanel = createSectionPanel("Key Features");
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[] titles = {"Daily Reminders", "Medicine Inventory", "Expiry Alerts", "Dosage Scheduling", "User Profile"};
        String[] texts = {
            "Never miss a dose with our smart notification system.",
            "Track what's in your virtual medicine box effortlessly.",
            "Get notified before your medicine expires.",
            "Set precise dosage and scheduling for each medicine.",
            "Store your medical information securely."
        };

        for (int i = 0; i < titles.length; i++) {
            gbc.gridx = i % 3;
            gbc.gridy = i / 3;
            gridPanel.add(createCard(titles[i], texts[i]), gbc);
        }

        sectionPanel.add(gridPanel);
        return sectionPanel;
    }

    /**
     * Creates the "How It Works" section with three cards.
     */
    private JPanel createHowItWorksSection() {
        JPanel sectionPanel = createSectionPanel("How It Works");
        JPanel gridPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        gridPanel.setOpaque(false);

        gridPanel.add(createCard("Create Profile", "Set up your personal health profile in minutes."));
        gridPanel.add(createCard("Add Medicines", "Input your medications and prescriptions."));
        gridPanel.add(createCard("Set Reminders", "Configure your personalized reminder schedule."));

        sectionPanel.add(gridPanel);
        return sectionPanel;
    }

    /**
     * Creates the "How to Set Reminder" section with three cards.
     */
    private JPanel createReminderGuideSection() {
        JPanel sectionPanel = createSectionPanel("How to Set Reminder");
        JPanel gridPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        gridPanel.setOpaque(false);

        gridPanel.add(createCard("Step 1", "Go to the 'Add Medicine' section in your profile dashboard."));
        gridPanel.add(createCard("Step 2", "Enter medicine name, dosage, and frequency."));
        gridPanel.add(createCard("Step 3", "Set the reminder time and save. You're done!"));

        sectionPanel.add(gridPanel);
        return sectionPanel;
    }

    /**
     * Creates the footer panel with links.
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout(20, 10));
        footerPanel.setBackground(COLOR_FOOTER_BG);
        footerPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel brandLabel = new JLabel("RemindDose");
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setFont(new Font("Arial", Font.BOLD, 18));
        footerPanel.add(brandLabel, BorderLayout.WEST);

        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        linksPanel.setOpaque(false);

        JLabel quickLinksLabel = new JLabel("Quick Links");
        quickLinksLabel.setForeground(Color.WHITE);
        quickLinksLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton aboutButton = new JButton("About");
        aboutButton.setForeground(COLOR_LINK_ABOUT);
        aboutButton.setFont(new Font("Arial", Font.PLAIN, 16));
        aboutButton.setOpaque(false);
        aboutButton.setContentAreaFilled(false);
        aboutButton.setBorderPainted(false);
        aboutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Add an ActionListener to the button
        aboutButton.addActionListener(e -> {
            // 1. Create and apply the blur effect to the main window.
            //    This uses the BlurGlassPane inner class from your RemindDoseGUI.
            BlurGlassPane glassPane = new BlurGlassPane(this);
            this.setGlassPane(glassPane);
            glassPane.setVisible(true);

            // 2. Create and show the AboutDialog.
            //    Since the dialog is modal, the code will pause here until it's closed.
            AboutDialog dialog = new AboutDialog();
            dialog.setVisible(true);

            // 3. Once the dialog is closed, remove the blur effect.
            glassPane.setVisible(false);
        });

        linksPanel.add(quickLinksLabel);
        linksPanel.add(aboutButton);

        footerPanel.add(linksPanel, BorderLayout.EAST);
        return footerPanel;
    }

    // --- HELPER AND UTILITY METHODS ---
    private JPanel createSectionPanel(String titleText) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionPanel.add(title);
        sectionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        return sectionPanel;
    }

    private JPanel createCard(String titleText, String bodyText) {
        JPanel card = new RoundedPanel(25, COLOR_CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel body = new JLabel("<html><p style='text-align:center; width: 180px;'>" + bodyText + "</p></html>");
        body.setFont(new Font("Arial", Font.PLAIN, 14));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(body);
        return card;
    }

    private JButton createNavLink(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setForeground(COLOR_TEXT);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(COLOR_BUTTON_PRIMARY_BG);
        button.setForeground(COLOR_TEXT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void smoothScrollTo(JPanel panel) {
        if (scrollTimer != null && scrollTimer.isRunning()) {
            scrollTimer.stop();
        }
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        int startValue = verticalScrollBar.getValue();
        int endValue = panel.getY();
        int distance = endValue - startValue;
        if (distance == 0) {
            return;
        }
        int duration = 400;
        int steps = 40;
        int delay = duration / steps;
        long startTime = System.currentTimeMillis();
        scrollTimer = new Timer(delay, e -> {
            long now = System.currentTimeMillis();
            long elapsedTime = now - startTime;
            if (elapsedTime >= duration) {
                verticalScrollBar.setValue(endValue);
                ((Timer) e.getSource()).stop();
                return;
            }
            float t = (float) elapsedTime / duration;
            t = (float) (1 - Math.pow(1 - t, 3));
            int currentValue = startValue + (int) (distance * t);
            verticalScrollBar.setValue(currentValue);
        });
        scrollTimer.start();
    }

    /**
     * Displays a custom, themed dialog with a custom title bar and blurred
     * background.
     */
    private void showThemedAboutDialog() {
        // 1. Create the blur effect component
        BlurGlassPane glassPane = new BlurGlassPane(this);
        this.setGlassPane(glassPane);
        glassPane.setVisible(true);

        // 2. Create the custom dialog
        CustomDialog aboutDialog = new CustomDialog(this, "About RemindDose", glassPane);
        aboutDialog.setSize(380, 220);
        aboutDialog.setLocationRelativeTo(this);

        // 3. Create the main content for the dialog
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JLabel title = new JLabel("About Us");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(COLOR_NAV);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        String aboutText = "<html><p style='text-align:center;'>We help users manage their medicines with daily reminders and expiry alerts, making health management simple and stress-free.</p></html>";
        JLabel body = new JLabel(aboutText);
        body.setFont(new Font("Arial", Font.PLAIN, 14));
        body.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(body);

        // Add the content to the dialog's center
        aboutDialog.add(contentPanel, BorderLayout.CENTER);
        aboutDialog.setVisible(true); // This must be called last
    }

    // --- INNER CLASSES FOR CUSTOM COMPONENTS ---
    private static class BlurGlassPane extends JComponent {

        private BufferedImage blurredImage;
        private final JFrame parentFrame;

        public BlurGlassPane(JFrame frame) {
            this.parentFrame = frame;
        }

        @Override
        public void setVisible(boolean aFlag) {
            if (aFlag) {
                updateBlurredImage();
            }
            super.setVisible(aFlag);
        }

        private void updateBlurredImage() {
            try {
                Robot robot = new Robot();
                Rectangle rect = parentFrame.getBounds();
                BufferedImage screenCapture = robot.createScreenCapture(rect);
                float[] blurKernel = new float[49];
                for (int i = 0; i < blurKernel.length; i++) {
                    blurKernel[i] = 1.0f / 49.0f;
                }
                Kernel kernel = new Kernel(7, 7, blurKernel);
                ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
                blurredImage = op.filter(screenCapture, null);
            } catch (AWTException e) {
                e.printStackTrace();
                blurredImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (blurredImage != null) {
                g.drawImage(blurredImage, 0, 0, this);
            }
            g.setColor(new Color(0, 0, 0, 80));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * A custom undecorated JDialog with a draggable title bar and window
     * controls.
     */
    private static class CustomDialog extends JDialog {

        private Point initialClick;
        private final JFrame parentFrame;

        public CustomDialog(JFrame parent, String title, BlurGlassPane glassPane) {
            super(parent, title, true);
            this.parentFrame = parent;
            setUndecorated(true);
            setLayout(new BorderLayout());
            getRootPane().setBorder(BorderFactory.createLineBorder(new Color(81, 189, 101), 2));

            // --- Custom Title Bar ---
            JPanel titleBar = new JPanel(new BorderLayout());
            titleBar.setBackground(new Color(222, 244, 180));
            titleBar.setBorder(new EmptyBorder(2, 8, 2, 2));

            JPanel titleContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            titleContent.setOpaque(false);

            // Add Icon
            if (parent.getIconImage() != null) {
                ImageIcon icon = new ImageIcon(parent.getIconImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                titleContent.add(new JLabel(icon));
            }

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(new Color(51, 51, 51));
            titleContent.add(titleLabel);

            // --- Window Controls ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            buttonPanel.setOpaque(false);

            JButton minimizeButton = createControlButton("\u2014"); // Underscore
            minimizeButton.addActionListener(e -> parentFrame.setState(Frame.ICONIFIED));

            JButton maximizeButton = createControlButton("\u25A1"); // Square
            maximizeButton.setEnabled(false); // Disabled as requested

            JButton closeButton = createControlButton("\u2715"); // X symbol
            closeButton.addActionListener(e -> {
                dispose();
                glassPane.setVisible(false);
            });
            // Special styling for close button on hover
            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    closeButton.setBackground(Color.RED);
                    closeButton.setOpaque(true);
                    closeButton.setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeButton.setBackground(titleBar.getBackground());
                    closeButton.setOpaque(false);
                    closeButton.setForeground(new Color(51, 51, 51));
                }
            });

            buttonPanel.add(minimizeButton);
            buttonPanel.add(maximizeButton);
            buttonPanel.add(closeButton);

            titleBar.add(titleContent, BorderLayout.WEST);
            titleBar.add(buttonPanel, BorderLayout.EAST);

            // --- Drag Listener ---
            MouseAdapter dragAdapter = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint();
                }
            };
            titleBar.addMouseListener(dragAdapter);

            MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;
                    // Move window by the distance mouse has moved
                    int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
                    int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);
                    int newX = thisX + xMoved;
                    int newY = thisY + yMoved;

                    // Constrain to parent bounds
                    Rectangle parentBounds = parentFrame.getBounds();
                    newX = Math.max(parentBounds.x, Math.min(newX, parentBounds.x + parentBounds.width - getWidth()));
                    newY = Math.max(parentBounds.y, Math.min(newY, parentBounds.y + parentBounds.height - getHeight()));

                    setLocation(newX, newY);
                }
            };
            titleBar.addMouseMotionListener(motionAdapter);

            add(titleBar, BorderLayout.NORTH);
        }

        private JButton createControlButton(String text) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.setForeground(new Color(51, 51, 51));
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(5, 12, 5, 12));
            button.setContentAreaFilled(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Hover effect for non-close buttons
            if (!text.equals("\u2715")) {
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        button.setBackground(new Color(0, 0, 0, 30)); // Semi-transparent gray
                        button.setOpaque(true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setOpaque(false);
                    }
                });
            }
            return button;
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

    private static class RoundedButton extends JButton {

        public RoundedButton(String text) {
            super(text);
        }
    }

    /**
     * Main method to run the application.
     */
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(RemindDoseGUI::new);
//    }
}
