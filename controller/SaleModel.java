package com.veloura.controller;

public class SaleModel {

    private int number;
    private int saleId;
    private String customerName;
    private String username;
    private String saleDate;
    private double totalAmount;
    private String actions;

    public SaleModel(int number, int saleId, String customerName, String username,
                     String saleDate, double totalAmount, String actions) {
        this.number = number;
        this.saleId = saleId;
        this.customerName = customerName;
        this.username = username;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.actions = actions;
    }

    public int getNumber() {
        return number;
    }

    public int getSaleId() {
        return saleId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getUsername() {
        return username;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getActions() {
        return actions;
    }
}