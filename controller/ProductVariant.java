package com.veloura.controller;

public class ProductVariant {

    private int number;
    private String productName;
    private String size;
    private String color;
    private String barcode;
    private double price;
    private int currentQty;
    private String actions;

    public ProductVariant(int number, String productName, String size, String color,
                          String barcode, double price, int currentQty, String actions) {
        this.number = number;
        this.productName = productName;
        this.size = size;
        this.color = color;
        this.barcode = barcode;
        this.price = price;
        this.currentQty = currentQty;
        this.actions = actions;
    }

    public int getNumber() {
        return number;
    }

    public String getProductName() {
        return productName;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public String getBarcode() {
        return barcode;
    }

    public double getPrice() {
        return price;
    }

    public int getCurrentQty() {
        return currentQty;
    }

    public String getActions() {
        return actions;
    }
}