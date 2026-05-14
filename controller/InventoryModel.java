package com.veloura.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javafx.beans.property.*;

public class InventoryModel {

    private final IntegerProperty variantId;
    private final StringProperty barcode;
    private final StringProperty productName;
    private final StringProperty brandName;
    private final StringProperty categoryName;
    private final StringProperty audience;
    private final StringProperty size;
    private final StringProperty color;
    private final DoubleProperty price;
    private final IntegerProperty currentQty;
    private final StringProperty stockStatus;
    private final StringProperty stockPriority;

    private final StringProperty expiryDateText;
    private final StringProperty expiryStatus;

    public InventoryModel(int variantId,
                          String barcode,
                          String productName,
                          String brandName,
                          String categoryName,
                          String audience,
                          String size,
                          String color,
                          double price,
                          int currentQty,
                          String stockStatus) {

        this(variantId, barcode, productName, brandName, categoryName, audience,
                size, color, price, currentQty, stockStatus, null);
    }

    public InventoryModel(int variantId,
                          String barcode,
                          String productName,
                          String brandName,
                          String categoryName,
                          String audience,
                          String size,
                          String color,
                          double price,
                          int currentQty,
                          String stockStatus,
                          LocalDate nearestExpiryDate) {

        this.variantId = new SimpleIntegerProperty(variantId);
        this.barcode = new SimpleStringProperty(barcode);
        this.productName = new SimpleStringProperty(productName);
        this.brandName = new SimpleStringProperty(brandName);
        this.categoryName = new SimpleStringProperty(categoryName);
        this.audience = new SimpleStringProperty(audience);
        this.size = new SimpleStringProperty(size);
        this.color = new SimpleStringProperty(color);
        this.price = new SimpleDoubleProperty(price);
        this.currentQty = new SimpleIntegerProperty(currentQty);
        this.stockStatus = new SimpleStringProperty(stockStatus);
        this.stockPriority = new SimpleStringProperty(calculatePriority(currentQty));

        if (nearestExpiryDate == null) {
            this.expiryDateText = new SimpleStringProperty("-");
            this.expiryStatus = new SimpleStringProperty("— NOT REQUIRED");
        } else {
            this.expiryDateText = new SimpleStringProperty(nearestExpiryDate.toString());
            this.expiryStatus = new SimpleStringProperty(calculateExpiryStatus(nearestExpiryDate));
        }
    }

    private String calculatePriority(int qty) {
        if (qty == 0) return "URGENT";
        if (qty <= 5) return "HIGH";
        if (qty <= 10) return "MEDIUM";
        return "NORMAL";
    }

    private String calculateExpiryStatus(LocalDate expiryDate) {
        LocalDate today = LocalDate.now();
        long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);

        if (daysLeft < 0) {
            return "⛔ EXPIRED";
        }

        if (daysLeft <= 60) {
            return "⚠ EXPIRING SOON";
        }

        return "✔ VALID";
    }

    public boolean isOutOfStock() {
        return getCurrentQty() == 0;
    }

    public boolean isLowStock() {
        return getCurrentQty() > 0 && getCurrentQty() <= 5;
    }

    public boolean needsAttention() {
        return getCurrentQty() <= 5;
    }

    public boolean isExpired() {
        return getExpiryStatus().contains("EXPIRED");
    }

    public boolean isExpiringSoon() {
        return getExpiryStatus().contains("EXPIRING SOON");
    }

    public String getSmartStatusText() {
        if (getCurrentQty() == 0) return "⛔ OUT OF STOCK";
        if (getCurrentQty() <= 5) return "⚠ LOW STOCK";
        if (getCurrentQty() <= 10) return "● WATCH";
        return "✔ IN STOCK";
    }

    public int getRecommendedRestockQty() {
        if (getCurrentQty() == 0) return 50;
        if (getCurrentQty() <= 3) return 40;
        if (getCurrentQty() <= 5) return 30;
        if (getCurrentQty() <= 10) return 20;
        return 0;
    }

    public int getVariantId() {
        return variantId.get();
    }

    public IntegerProperty variantIdProperty() {
        return variantId;
    }

    public String getBarcode() {
        return barcode.get();
    }

    public StringProperty barcodeProperty() {
        return barcode;
    }

    public String getProductName() {
        return productName.get();
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public String getBrandName() {
        return brandName.get();
    }

    public StringProperty brandNameProperty() {
        return brandName;
    }

    public String getCategoryName() {
        return categoryName.get();
    }

    public StringProperty categoryNameProperty() {
        return categoryName;
    }

    public String getAudience() {
        return audience.get();
    }

    public StringProperty audienceProperty() {
        return audience;
    }

    public String getSize() {
        return size.get();
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public String getColor() {
        return color.get();
    }

    public StringProperty colorProperty() {
        return color;
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public int getCurrentQty() {
        return currentQty.get();
    }

    public IntegerProperty currentQtyProperty() {
        return currentQty;
    }

    public String getStockStatus() {
        return stockStatus.get();
    }

    public StringProperty stockStatusProperty() {
        return stockStatus;
    }

    public String getStockPriority() {
        return stockPriority.get();
    }

    public StringProperty stockPriorityProperty() {
        return stockPriority;
    }

    public String getExpiryDateText() {
        return expiryDateText.get();
    }

    public StringProperty expiryDateTextProperty() {
        return expiryDateText;
    }

    public String getExpiryStatus() {
        return expiryStatus.get();
    }

    public StringProperty expiryStatusProperty() {
        return expiryStatus;
    }
}