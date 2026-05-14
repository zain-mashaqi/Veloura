package com.veloura.security;

public class UserSession {

    private static int userId;
    private static String username;
    private static String role;
    private static boolean loggedIn = false;

    private UserSession() {
    }

    public static void startSession(int id, String user, String userRole) {
        userId = id;
        username = user;
        role = userRole;
        loggedIn = true;
    }

    public static void clearSession() {
        userId = 0;
        username = null;
        role = null;
        loggedIn = false;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static boolean hasRole(String expectedRole) {
        return loggedIn && role != null && role.equalsIgnoreCase(expectedRole);
    }
}