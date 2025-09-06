/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import com.reminddose.medicinedabba.database.*;
import com.reminddose.medicinedabba.database.User;
import com.reminddose.medicinedabba.ProfileFormWindow;


public class login extends JDialog {

    // Theme Colors
    private final Color COLOR_PANEL_BG = new Color(255, 255, 255);
    private final Color COLOR_BUTTON = new Color(111, 189, 82);
    private final Color COLOR_LINK = new Color(94, 169, 244);
    private final Color COLOR_TEXT_PRIMARY = new Color(51, 51, 51);
    private final Color COLOR_TEXT_SECONDARY = new Color(150, 150, 150);

    public login() {
        super((Frame) null, "Login", true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setResizable(false);
        setLayout(new BorderLayout());

        // Set app icon
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        }

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_PANEL_BG);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(COLOR_TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField usernameField = new PlaceholderTextField("Username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameField.getPreferredSize().height + 10));

        JPasswordField passwordField = new PlaceholderPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height + 10));

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(COLOR_BUTTON);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(new EmptyBorder(12, 0, 12, 0));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, loginButton.getPreferredSize().height));

        // ✅ Action for Login Button
        loginButton.addActionListener(e -> {
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Username and password are required!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    User user = LoginDAO.validateUser(username, password);
    if (user != null) {
        //session setting globally
         Session.setUser(user.getId(), user.getUsername());
        dispose(); // Close login dialog
        if (user.getLastLogin() == null) {
            // First time login - pass user ID to profile form
            ProfileFormWindow pw = new ProfileFormWindow(user.getId());
            pw.setVisible(true);
        } else {
            // Returning user
            DashboardWindow dbw = new DashboardWindow();
            dbw.setVisible(true);
        }
        
    } else {
        JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
    }
});


        JLabel signUpLabel = new JLabel("<html><p>Don't have an account?</p><html>");
        signUpLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        signUpLabel.setForeground(COLOR_TEXT_PRIMARY);

        JLabel signUpLink = new JLabel("<html><u>Sign Up</u></html>");
        signUpLink.setFont(new Font("Arial", Font.PLAIN, 14));
        signUpLink.setForeground(COLOR_LINK);
        signUpLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SignUpWindow().setVisible(true);
                login.this.dispose();
            }
        });

        JPanel signUpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        signUpPanel.setOpaque(false);
        signUpPanel.add(signUpLabel);
        signUpPanel.add(signUpLink);
        signUpPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(loginButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(signUpPanel);

        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    // Placeholder text field
    private class PlaceholderTextField extends JTextField implements FocusListener {
        private String placeholder;
        private boolean showingPlaceholder;

        public PlaceholderTextField(String placeholder) {
            super(placeholder);
            this.placeholder = placeholder;
            this.showingPlaceholder = true;
            addFocusListener(this);
            setFont(new Font("Arial", Font.PLAIN, 14));
            setForeground(COLOR_TEXT_SECONDARY);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 10, 5, 10)
            ));
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (showingPlaceholder) {
                setText("");
                setForeground(COLOR_TEXT_PRIMARY);
                showingPlaceholder = false;
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (getText().isEmpty()) {
                setText(placeholder);
                setForeground(COLOR_TEXT_SECONDARY);
                showingPlaceholder = true;
            }
        }

        @Override
        public String getText() {
            return showingPlaceholder ? "" : super.getText();
        }
    }

    // Placeholder password field
    private class PlaceholderPasswordField extends JPasswordField implements FocusListener {
        private String placeholder;
        private boolean showingPlaceholder;

        public PlaceholderPasswordField(String placeholder) {
            super(placeholder);
            this.placeholder = placeholder;
            this.showingPlaceholder = true;
            addFocusListener(this);
            setEchoChar((char) 0);
            setFont(new Font("Arial", Font.PLAIN, 14));
            setForeground(COLOR_TEXT_SECONDARY);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 10, 5, 10)
            ));
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (showingPlaceholder) {
                setText("");
                setEchoChar('\u2022');
                setForeground(COLOR_TEXT_PRIMARY);
                showingPlaceholder = false;
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (getPassword().length == 0) {
                setText(placeholder);
                setEchoChar((char) 0);
                setForeground(COLOR_TEXT_SECONDARY);
                showingPlaceholder = true;
            }
        }

        @Override
        public char[] getPassword() {
            return showingPlaceholder ? new char[0] : super.getPassword();
        }
    }
}