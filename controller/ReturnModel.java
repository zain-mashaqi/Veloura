package com.veloura.controller;

import javafx.beans.property.*;

public class ReturnModel {

    private final IntegerProperty returnId;
    private final IntegerProperty saleId;
    private final IntegerProperty variantId;
    private final StringProperty productName;
    private final IntegerProperty qty;
    private final StringProperty reason;
    private final StringProperty returnDate;

    public ReturnModel(int returnId, int saleId, int variantId,
                       String productName, int qty, String reason, String returnDate) {

        this.returnId = new SimpleIntegerProperty(returnId);
        this.saleId = new SimpleIntegerProperty(saleId);
        this.variantId = new SimpleIntegerProperty(variantId);
        this.productName = new SimpleStringProperty(productName);
        this.qty = new SimpleIntegerProperty(qty);
        this.reason = new SimpleStringProperty(reason);
        this.returnDate = new SimpleStringProperty(returnDate);
    }

    public int getReturnId() { return returnId.get(); }
    public IntegerProperty returnIdProperty() { return returnId; }

    public int getSaleId() { return saleId.get(); }
    public IntegerProperty saleIdProperty() { return saleId; }

    public int getVariantId() { return variantId.get(); }
    public IntegerProperty variantIdProperty() { return variantId; }

    public String getProductName() { return productName.get(); }
    public StringProperty productNameProperty() { return productName; }

    public int getQty() { return qty.get(); }
    public IntegerProperty qtyProperty() { return qty; }

    public String getReason() { return reason.get(); }
    public StringProperty reasonProperty() { return reason; }

    public String getReturnDate() { return returnDate.get(); }
    public StringProperty returnDateProperty() { return returnDate; }
}