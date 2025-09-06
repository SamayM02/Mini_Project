/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.reminddose.medicinedabba.database;

/**
 *
 * @author lenovo
 */

public class Session {
    private static long currentUserId;
    private static String username;
    //private static String email;

    public static void setUser(long userId, String uname/**, String uname**/) {
        currentUserId = userId;
        username = uname;
        //email = mail;
    }

    public static long getCurrentUserId() {
        return currentUserId;
    }

    public static String getUsername() {
        return username;
    }
    public static void logout() {
        currentUserId = 0;
        username = null;
    }
    public static boolean isLoggedIn() {
        return currentUserId > 0;
    }

//    public static String getEmail() {
//        return email;
//    }

    public static void clear() {
        currentUserId = 0;
        username = null;
        //email = null;
    }
}
