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
import com.reminddose.medicinedabba.database.SignUpDAO;

public class SignUpWindow extends JDialog {

    // Colors
    private final Color COLOR_PANEL_BG = new Color(255, 255, 255);
    private final Color COLOR_BUTTON = new Color(111, 189, 82);
    private final Color COLOR_LINK = new Color(94, 169, 244);
    private final Color COLOR_TEXT_PRIMARY = new Color(51, 51, 51);
    private final Color COLOR_TEXT_SECONDARY = new Color(150, 150, 150);

    public SignUpWindow() {
        super((Frame) null, "Sign Up", true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 450);
        setResizable(false);
        setLayout(new BorderLayout());

        // Set icon
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_PANEL_BG);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Sign Up");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(COLOR_TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField usernameField = new PlaceholderTextField("Username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameField.getPreferredSize().height + 10));

        JTextField emailField = new PlaceholderTextField("Email");
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, emailField.getPreferredSize().height + 10));

        JPasswordField passwordField = new PlaceholderPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height + 10));

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setFont(new Font("Arial", Font.BOLD, 16));
        signUpButton.setBackground(COLOR_BUTTON);
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFocusPainted(false);
        signUpButton.setBorder(new EmptyBorder(12, 0, 12, 0));
        signUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, signUpButton.getPreferredSize().height));

        // ✅ Action for Sign Up Button
        signUpButton.addActionListener(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = SignUpDAO.registerUser(username, email, password);
            if (success) {
                JOptionPane.showMessageDialog(this, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close sign-up window
            } else {
                JOptionPane.showMessageDialog(this, "Error registering user. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel loginLabel = new JLabel("<html><p>Already have an account?</p><html>");
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginLabel.setForeground(COLOR_TEXT_PRIMARY);

        JLabel loginLink = new JLabel("<html><u>Login</u></html>");
        loginLink.setFont(new Font("Arial", Font.PLAIN, 14));
        loginLink.setForeground(COLOR_LINK);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new login().setVisible(true);
                SignUpWindow.this.dispose();
            }
        });

        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        loginPanel.setOpaque(false);
        loginPanel.add(loginLabel);
        loginPanel.add(loginLink);
        loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(emailField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(signUpButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(loginPanel);

        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    // Placeholder classes
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