package com.veloura.controller;

public class InvoiceItemModel {

    private String productName;
    private String barcode;
    private String size;
    private String color;

    private int qty;

    private double unitPrice;
    private double lineTotal;

    public InvoiceItemModel(String productName,
                            String barcode,
                            String size,
                            String color,
                            int qty,
                            double unitPrice,
                            double lineTotal) {

        this.productName = productName;
        this.barcode = barcode;
        this.size = size;
        this.color = color;

        this.qty = qty;

        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public String getProductName() {
        return productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public int getQty() {
        return qty;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }
}