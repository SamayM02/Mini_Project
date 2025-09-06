/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.database;

/**
 *
 * @author lenovo
 */

import java.sql.Timestamp;

public class User {
    private long id;
    private String username;
    private Timestamp lastLogin;

    public User(long id, String username, Timestamp lastLogin) {
        this.id = id;
        this.username = username;
        this.lastLogin = lastLogin;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public Timestamp getLastLogin() { return lastLogin; }
}