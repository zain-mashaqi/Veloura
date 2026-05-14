package com.veloura.security;

public class HashTest {
    public static void main(String[] args) {
        System.out.println("admin123 -> " + PasswordUtil.hashPassword("admin123"));
        System.out.println("manager123 -> " + PasswordUtil.hashPassword("manager123"));
        System.out.println("cashier123 -> " + PasswordUtil.hashPassword("cashier123"));
    }
}