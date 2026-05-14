package com.veloura.controller;

import com.veloura.database.DBConnection;
import com.veloura.security.UserSession;
import com.veloura.util.ToastNotification;
import com.veloura.util.ToastNotification.ToastType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AddStockController {

    @FXML private TextField txtSupplierName;
    @FXML private ComboBox<String> cmbVariant;
    @FXML private TextField txtQty;
    @FXML private TextField txtCostPrice;
    @FXML private DatePicker dpPurchaseDate;
    @FXML private DatePicker dpExpiryDate;
    @FXML private Label lblMessage;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private boolean saved = false;

    @FXML
    public void initialize() {
        if (dpPurchaseDate != null) {
            dpPurchaseDate.setValue(LocalDate.now());
        }

        loadVariantsIntoCombo();
        playAddStockAnimations();
    }

    private void playAddStockAnimations() {
        animateNode(txtSupplierName, 0);
        animateNode(cmbVariant, 120);
        animateNode(txtQty, 240);
        animateNode(txtCostPrice, 360);
        animateNode(dpPurchaseDate, 480);
        animateNode(dpExpiryDate, 600);
        animateNode(lblMessage, 720);

        animateButton(btnSave);
        animateButton(btnCancel);
    }

    private void animateNode(Node node, int delay) {
        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(25);

        FadeTransition fade = new FadeTransition(Duration.millis(700), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide = new TranslateTransition(Duration.millis(700), node);
        slide.setFromY(25);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delay));

        fade.play();
        slide.play();
    }

    private void animateButton(Button btn) {
        if (btn == null) return;

        String originalStyle = btn.getStyle();
        btn.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(850), btn);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        btn.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(170), btn);
            scale.setToX(1.045);
            scale.setToY(1.045);
            scale.play();

            btn.setStyle(originalStyle + "-fx-effect:dropshadow(gaussian,#D4A64A,22,0.45,0,0);");
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(170), btn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            btn.setStyle(originalStyle);
        });
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String supplierName = txtSupplierName.getText() == null ? "" : txtSupplierName.getText().trim();
        String variantText = cmbVariant.getValue();
        String qtyText = txtQty.getText() == null ? "" : txtQty.getText().trim();
        String costText = txtCostPrice.getText() == null ? "" : txtCostPrice.getText().trim();

        LocalDate purchaseDate = dpPurchaseDate.getValue();
        LocalDate expiryDate = dpExpiryDate == null ? null : dpExpiryDate.getValue();

        if (supplierName.isEmpty()) {
            showMessage("Supplier name is required.");
            showToast("Supplier name is required.", ToastType.WARNING);
            return;
        }

        if (variantText == null || variantText.trim().isEmpty()) {
            showMessage("Please select a variant.");
            showToast("Please select a variant.", ToastType.WARNING);
            return;
        }

        if (qtyText.isEmpty()) {
            showMessage("Quantity is required.");
            showToast("Quantity is required.", ToastType.WARNING);
            return;
        }

        if (costText.isEmpty()) {
            showMessage("Cost price is required.");
            showToast("Cost price is required.", ToastType.WARNING);
            return;
        }

        if (purchaseDate == null) {
            showMessage("Purchase date is required.");
            showToast("Purchase date is required.", ToastType.WARNING);
            return;
        }

        if (expiryDate != null && expiryDate.isBefore(purchaseDate)) {
            showMessage("Expiry date cannot be before purchase date.");
            showToast("Expiry date cannot be before purchase date.", ToastType.WARNING);
            return;
        }

        int qty;
        double costPrice;

        try {
            qty = Integer.parseInt(qtyText);
            costPrice = Double.parseDouble(costText);

            if (qty <= 0) {
                showMessage("Quantity must be greater than 0.");
                showToast("Quantity must be greater than 0.", ToastType.WARNING);
                return;
            }

            if (costPrice < 0) {
                showMessage("Cost price cannot be negative.");
                showToast("Cost price cannot be negative.", ToastType.WARNING);
                return;
            }

        } catch (NumberFormatException e) {
            showMessage("Quantity and cost price must be valid numbers.");
            showToast("Quantity and cost price must be valid numbers.", ToastType.WARNING);
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showMessage("Database connection failed.");
                showToast("Database connection failed.", ToastType.ERROR);
                return;
            }

            conn.setAutoCommit(false);

            try {
                int supplierId = getOrCreateSupplierId(conn, supplierName);
                int variantId = extractVariantId(variantText);
                int userId = UserSession.getUserId();

                String purchaseSql = """
                    INSERT INTO purchases (supplier_id, purchase_date, user_id)
                    VALUES (?, ?, ?)
                """;

                int purchaseId;

                try (PreparedStatement pst = conn.prepareStatement(purchaseSql, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setInt(1, supplierId);
                    pst.setDate(2, java.sql.Date.valueOf(purchaseDate));
                    pst.setInt(3, userId);
                    pst.executeUpdate();

                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            purchaseId = rs.getInt(1);
                        } else {
                            throw new RuntimeException("Failed to create purchase.");
                        }
                    }
                }

                String itemSql = """
                    INSERT INTO purchase_items
                    (purchase_id, variant_id, qty, cost_price, expiry_date)
                    VALUES (?, ?, ?, ?, ?)
                """;

                try (PreparedStatement pst = conn.prepareStatement(itemSql)) {
                    pst.setInt(1, purchaseId);
                    pst.setInt(2, variantId);
                    pst.setInt(3, qty);
                    pst.setDouble(4, costPrice);

                    if (expiryDate == null) {
                        pst.setNull(5, Types.DATE);
                    } else {
                        pst.setDate(5, java.sql.Date.valueOf(expiryDate));
                    }

                    pst.executeUpdate();
                }

                String updateQtySql = """
                    UPDATE product_variants
                    SET current_qty = current_qty + ?
                    WHERE variant_id = ?
                """;

                try (PreparedStatement pst = conn.prepareStatement(updateQtySql)) {
                    pst.setInt(1, qty);
                    pst.setInt(2, variantId);
                    pst.executeUpdate();
                }

                String movementSql = """
                    INSERT INTO stock_movements (variant_id, movement_type, qty, user_id)
                    VALUES (?, ?, ?, ?)
                """;

                try (PreparedStatement pst = conn.prepareStatement(movementSql)) {
                    pst.setInt(1, variantId);
                    pst.setString(2, "PURCHASE");
                    pst.setInt(3, qty);
                    pst.setInt(4, userId);
                    pst.executeUpdate();
                }

                conn.commit();

                if (expiryDate == null) {
                    showToast("✔ Stock saved successfully", ToastType.SUCCESS);
                } else {
                    showToast("✔ Stock saved with expiry date: " + expiryDate, ToastType.SUCCESS);
                }

                saved = true;
                closeWindow();

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();

                showMessage("Failed to save stock: " + e.getMessage());
                showToast("Failed to save stock.", ToastType.ERROR);

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();

            showMessage("Error: " + e.getMessage());
            showToast("Error: " + e.getMessage(), ToastType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void loadVariantsIntoCombo() {
        String sql = """
            SELECT 
                pv.variant_id,
                pv.barcode,
                p.name AS product_name,
                pv.size,
                pv.color
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            ORDER BY pv.variant_id
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            cmbVariant.getItems().clear();

            while (rs.next()) {
                String item = rs.getInt("variant_id") + " - "
                        + rs.getString("product_name") + " - "
                        + rs.getString("barcode") + " - "
                        + rs.getString("size") + " - "
                        + rs.getString("color");

                cmbVariant.getItems().add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();

            showMessage("Failed to load variants.");
            showToast("Failed to load variants.", ToastType.ERROR);
        }
    }

    private int extractVariantId(String variantText) {
        return Integer.parseInt(variantText.split(" - ")[0].trim());
    }

    private int getOrCreateSupplierId(Connection conn, String supplierName) throws Exception {
        String selectSql = "SELECT supplier_id FROM suppliers WHERE supplier_name = ? LIMIT 1";

        try (PreparedStatement pst = conn.prepareStatement(selectSql)) {
            pst.setString(1, supplierName);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("supplier_id");
                }
            }
        }

        String insertSql = "INSERT INTO suppliers (supplier_name) VALUES (?)";

        try (PreparedStatement pst = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, supplierName);
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new RuntimeException("Failed to create supplier.");
    }

    private void showToast(String message, ToastType type) {
        try {
            Stage stage = null;

            if (btnSave != null && btnSave.getScene() != null) {
                stage = (Stage) btnSave.getScene().getWindow();
            }

            ToastNotification.show(stage, message, type);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    private void closeWindow() {
        if (txtSupplierName != null && txtSupplierName.getScene() != null) {
            Stage stage = (Stage) txtSupplierName.getScene().getWindow();
            stage.close();
        }
    }
}