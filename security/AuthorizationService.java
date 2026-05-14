package com.veloura.security;

import java.util.Set;

public class AuthorizationService {

    private AuthorizationService() {
    }

    public static boolean canManageUsers() {
        return hasAnyRole("ADMIN");
    }

    public static boolean canManageProducts() {
        return hasAnyRole("ADMIN", "MANAGER");
    }

    public static boolean canDeleteProducts() {
        return hasAnyRole("ADMIN", "MANAGER");
    }

    public static boolean canViewCustomers() {
        return hasAnyRole("ADMIN", "MANAGER", "CASHIER");
    }

    public static boolean canManageCustomers() {
        return hasAnyRole("ADMIN", "MANAGER", "CASHIER");
    }

    public static boolean canViewSales() {
        return hasAnyRole("ADMIN", "MANAGER", "CASHIER");
    }

    public static boolean canCreateSales() {
        return hasAnyRole("ADMIN", "MANAGER", "CASHIER");
    }

    public static boolean canDeleteSales() {
        return hasAnyRole("ADMIN", "MANAGER");
    }

    public static boolean canViewInventory() {
        return hasAnyRole("ADMIN", "MANAGER", "CASHIER");
    }

    public static boolean canManageInventory() {
        return hasAnyRole("ADMIN", "MANAGER");
    }

    public static boolean canViewDashboard() {
        return hasAnyRole("ADMIN", "MANAGER", "CASHIER");
    }

    public static boolean canViewStatistics() {
        return hasAnyRole("ADMIN", "MANAGER");
    }

    private static boolean hasAnyRole(String... roles) {
        if (!UserSession.isLoggedIn() || UserSession.getRole() == null) {
            return false;
        }

        String currentRole = UserSession.getRole().toUpperCase();
        return Set.of(roles).contains(currentRole);
    }

    public static boolean hasRole(String role) {
        return UserSession.isLoggedIn()
                && UserSession.getRole() != null
                && UserSession.getRole().equalsIgnoreCase(role);
    }
}