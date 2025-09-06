/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A simple JDialog to display "About Us" information.
 */
public class AboutDialog extends JDialog {

    public AboutDialog() {
        super((Frame) null, "About Us", true); // true for modal, no owner frame

        // --- DIALOG SETUP ---
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        setLayout(new BorderLayout());

        // --- MAIN PANEL ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Vertical layout
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding
        mainPanel.setBackground(new Color(243, 243, 191));

        // --- TITLE ---
        JLabel titleLabel = new JLabel("About Us");
        // BOLD, larger font for the title
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        // Center the label horizontally
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- BODY TEXT ---
        // Use HTML for line breaks and centering
        String aboutText = "<html><div style='text-align: center;'>"
                         + "We help users manage their medicines<br>"
                         + "with daily reminders and expiry alerts,<br>"
                         + "making health management simple and<br>"
                         + "stress-free.</div></html>";
        JLabel bodyLabel = new JLabel(aboutText);
        // Plain, smaller font for the body
        bodyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        // Center the label horizontally
        bodyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        // --- ADD COMPONENTS TO PANEL ---
        mainPanel.add(titleLabel);
        // Add some space between title and body
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(bodyLabel);

        // Add the main panel to the dialog
        add(mainPanel, BorderLayout.CENTER);

        // Finalize
        setLocationRelativeTo(null); // Center relative to the screen
    }
}
