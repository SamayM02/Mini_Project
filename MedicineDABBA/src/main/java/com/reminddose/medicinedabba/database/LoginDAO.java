/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.database;

/**
 *
 * @author lenovo
 */

import java.sql.*;

// ... (package and imports remain the same)

public class LoginDAO {

    public static User validateUser(String username, String password) {
        String sql = "SELECT id, username, last_login FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Plain text password as requested

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long userId = rs.getLong("id");
                Timestamp lastLogin = rs.getTimestamp("last_login");

                // Update last_login to current timestamp
                updateLastLogin(userId);

                return new User(userId, username, lastLogin);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Invalid user
    }

    private static void updateLastLogin(long userId) {
        String updateSql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(String username, String email, String password) {
        String insertSql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password); // Plain text password as requested

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                System.err.println("Username already exists.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }
}