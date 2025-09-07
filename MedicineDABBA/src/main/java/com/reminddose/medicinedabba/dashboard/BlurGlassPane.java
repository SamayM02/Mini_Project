/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.dashboard;

import com.reminddose.medicinedabba.*;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author lenovo
 */
class BlurGlassPane extends JComponent {

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
