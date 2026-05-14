package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.UserSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AddSaleController {

    @FXML private ComboBox<String> cmbCustomer;
    @FXML private ComboBox<String> cmbVariant;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtQty;
    @FXML private ComboBox<String> cmbPaymentMethod;
    @FXML private TextField txtDiscount;
    @FXML private Label lblMessage;

    @FXML private Label lblSubtotal;
    @FXML private Label lblEstimatedTotal;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnAddItem;
    @FXML private Button btnRemoveItem;

    @FXML private TableView<CartItemModel> tblCart;
    @FXML private TableColumn<CartItemModel, String> colProductName;
    @FXML private TableColumn<CartItemModel, String> colBarcode;
    @FXML private TableColumn<CartItemModel, String> colSize;
    @FXML private TableColumn<CartItemModel, String> colColor;
    @FXML private TableColumn<CartItemModel, Integer> colQty;
    @FXML private TableColumn<CartItemModel, Double> colUnitPrice;
    @FXML private TableColumn<CartItemModel, Double> colLineTotal;

    private final ObservableList<CartItemModel> cartItems = FXCollections.observableArrayList();

    private boolean saved = false;
    private static final int LOW_STOCK_LIMIT = 3;

    @FXML
    public void initialize() {
        setupCartTable();
        loadCustomersIntoCombo();
        loadVariantsIntoCombo();

        cmbPaymentMethod.getItems().addAll("Cash", "Card", "Cash + Card");
        cmbPaymentMethod.setValue("Cash");

        if (txtBarcode != null) {
            txtBarcode.setOnAction(e -> handleBarcodeSearch());
        }

        if (txtDiscount != null) {
            txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> updateTotalsLabels());
        }

        playAddSaleAnimations();
        updateTotalsLabels();
    }

    private void setupCartTable() {
        if (colProductName != null) {
            colProductName.setCellValueFactory(data ->
                    new ReadOnlyStringWrapper(data.getValue().getProductName()));
            styleColumn(colProductName);
        }

        if (colBarcode != null) {
            colBarcode.setCellValueFactory(data ->
                    new ReadOnlyStringWrapper(data.getValue().getBarcode()));
            styleColumn(colBarcode);
        }

        if (colSize != null) {
            colSize.setCellValueFactory(data ->
                    new ReadOnlyStringWrapper(data.getValue().getSize()));
            styleColumn(colSize);
        }

        if (colColor != null) {
            colColor.setCellValueFactory(data ->
                    new ReadOnlyStringWrapper(data.getValue().getColor()));
            styleColumn(colColor);
        }

        if (colQty != null) {
            colQty.setCellValueFactory(data ->
                    new ReadOnlyObjectWrapper<>(data.getValue().getQty()));
            styleColumn(colQty);
        }

        if (colUnitPrice != null) {
            colUnitPrice.setCellValueFactory(data ->
                    new ReadOnlyObjectWrapper<>(data.getValue().getUnitPrice()));
            styleColumn(colUnitPrice);
        }

        if (colLineTotal != null) {
            colLineTotal.setCellValueFactory(data ->
                    new ReadOnlyObjectWrapper<>(data.getValue().getLineTotal()));
            styleColumn(colLineTotal);
        }

        if (tblCart != null) {
            tblCart.setItems(cartItems);
        }
    }

    private <T> void styleColumn(TableColumn<CartItemModel, T> column) {
        column.setCellFactory(col -> new TableCell<CartItemModel, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    if (item instanceof Double) {
                        setText(String.format("%.2f", (Double) item));
                    } else {
                        setText(String.valueOf(item));
                    }

                    setTextFill(Color.WHITE);
                    setStyle("-fx-alignment: CENTER; -fx-background-color: transparent;");
                }
            }
        });
    }

    private void playAddSaleAnimations() {
        animateNode(txtBarcode, 0);
        animateNode(cmbCustomer, 120);
        animateNode(cmbVariant, 240);
        animateNode(txtQty, 360);
        animateNode(tblCart, 480);
        animateNode(cmbPaymentMethod, 600);
        animateNode(txtDiscount, 720);
        animateNode(lblMessage, 840);
        animateNode(lblSubtotal, 900);
        animateNode(lblEstimatedTotal, 960);

        animateButton(btnAddItem);
        animateButton(btnRemoveItem);
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

    private void handleBarcodeSearch() {
        String barcode = txtBarcode.getText() == null ? "" : txtBarcode.getText().trim();

        if (barcode.isEmpty()) {
            showMessage("Please enter barcode.");
            return;
        }

        String sql = """
            SELECT 
                pv.variant_id,
                pv.barcode,
                p.name AS product_name,
                pv.size,
                pv.color,
                pv.price,
                pv.current_qty
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            WHERE pv.barcode = ?
            LIMIT 1
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            if (conn == null) {
                showMessage("Database connection failed.");
                return;
            }

            pst.setString(1, barcode);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String item = buildVariantComboText(
                            rs.getInt("variant_id"),
                            rs.getString("product_name"),
                            rs.getString("barcode"),
                            rs.getString("size"),
                            rs.getString("color"),
                            rs.getInt("current_qty")
                    );

                    if (!cmbVariant.getItems().contains(item)) {
                        cmbVariant.getItems().add(item);
                    }

                    cmbVariant.setValue(item);
                    txtQty.requestFocus();

                    showMessage("Barcode found: " + rs.getString("product_name")
                            + " | Available Qty: " + rs.getInt("current_qty"));
                } else {
                    cmbVariant.setValue(null);
                    showMessage("No product found for this barcode.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Barcode search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddItem() {
        String variantText = cmbVariant.getValue();
        String qtyText = txtQty.getText() == null ? "" : txtQty.getText().trim();

        if (variantText == null || variantText.trim().isEmpty()) {
            showMessage("Please select a variant.");
            return;
        }

        if (qtyText.isEmpty()) {
            showMessage("Quantity is required.");
            return;
        }

        int qty;

        try {
            qty = Integer.parseInt(qtyText);

            if (qty <= 0) {
                showMessage("Quantity must be greater than 0.");
                return;
            }

        } catch (NumberFormatException e) {
            showMessage("Quantity must be a valid number.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showMessage("Database connection failed.");
                return;
            }

            int variantId = extractVariantId(variantText);
            int currentQty = getVariantCurrentQty(conn, variantId);

            int alreadyInCart = getCartQtyForVariant(variantId);

            if (alreadyInCart + qty > currentQty) {
                showMessage("Not enough stock. Available: " + currentQty
                        + " | Already in cart: " + alreadyInCart);
                return;
            }

            CartItemModel item = getVariantDetailsForCart(conn, variantId, qty);

            CartItemModel existing = findCartItemByVariantId(variantId);

            if (existing != null) {
                existing.addQty(qty);
                tblCart.refresh();
            } else {
                cartItems.add(item);
            }

            txtQty.clear();
            txtBarcode.clear();
            cmbVariant.setValue(null);

            updateTotalsLabels();
            showMessage("Item added to invoice.");

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Failed to add item: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveItem() {
        CartItemModel selected = tblCart == null ? null : tblCart.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Please select an item from cart to remove.");
            return;
        }

        cartItems.remove(selected);
        updateTotalsLabels();
        showMessage("Item removed from invoice.");
    }

    @FXML
    private void handleSave() {
        String customerName = cmbCustomer.getValue();
        String paymentMethod = cmbPaymentMethod.getValue();
        String discountText = txtDiscount.getText() == null ? "" : txtDiscount.getText().trim();

        if (customerName == null || customerName.trim().isEmpty()) {
            showMessage("Please select a customer.");
            return;
        }

        if (cartItems.isEmpty()) {
            showMessage("Please add at least one item to the invoice.");
            return;
        }

        double manualDiscountPercent = 0;

        try {
            if (!discountText.isEmpty()) {
                manualDiscountPercent = Double.parseDouble(discountText);

                if (manualDiscountPercent < 0 || manualDiscountPercent > 100) {
                    showMessage("Discount must be between 0 and 100.");
                    return;
                }
            }

        } catch (NumberFormatException e) {
            showMessage("Discount must be a valid number.");
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                showMessage("Database connection failed.");
                return;
            }

            conn.setAutoCommit(false);

            try {
                int customerId = getCustomerIdByName(conn, customerName);
                int userId = UserSession.getUserId();

                if (userId <= 0) {
                    showMessage("No active user session found. Please login again.");
                    conn.rollback();
                    return;
                }

                for (CartItemModel item : cartItems) {
                    int currentQty = getVariantCurrentQty(conn, item.getVariantId());

                    if (currentQty < item.getQty()) {
                        showMessage("Not enough quantity for: " + item.getProductName()
                                + ". Available: " + currentQty);
                        conn.rollback();
                        return;
                    }
                }

                double subtotal = calculateSubtotal();
                double autoDiscountPercent = subtotal >= 100 ? 10 : 0;
                double finalDiscountPercent = Math.max(autoDiscountPercent, manualDiscountPercent);
                double regularDiscountAmount = subtotal * (finalDiscountPercent / 100.0);
                double birthdayCouponAmount = getCustomerBirthdayCouponAmount(conn, customerId);

                double totalDiscount = regularDiscountAmount + birthdayCouponAmount;
                double totalAmount = subtotal - totalDiscount;

                if (totalAmount < 0) {
                    totalAmount = 0;
                }

                String saleSql = """
                    INSERT INTO sales
                    (customer_id, user_id, sale_date, subtotal, discount_percent, discount_amount, total_amount)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

                int saleId;

                try (PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                    saleStmt.setInt(1, customerId);
                    saleStmt.setInt(2, userId);
                    saleStmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                    saleStmt.setDouble(4, subtotal);
                    saleStmt.setDouble(5, finalDiscountPercent);
                    saleStmt.setDouble(6, totalDiscount);
                    saleStmt.setDouble(7, totalAmount);
                    saleStmt.executeUpdate();

                    try (ResultSet rs = saleStmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            saleId = rs.getInt(1);
                        } else {
                            throw new RuntimeException("Failed to create sale.");
                        }
                    }
                }

                String saleItemSql = """
                    INSERT INTO sale_items (sale_id, variant_id, qty, unit_price)
                    VALUES (?, ?, ?, ?)
                """;

                try (PreparedStatement itemStmt = conn.prepareStatement(saleItemSql)) {
                    for (CartItemModel item : cartItems) {
                        itemStmt.setInt(1, saleId);
                        itemStmt.setInt(2, item.getVariantId());
                        itemStmt.setInt(3, item.getQty());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.addBatch();
                    }

                    itemStmt.executeBatch();
                }

                String paymentSql = """
                    INSERT INTO payments (payment_method, amount, sale_id)
                    VALUES (?, ?, ?)
                """;

                try (PreparedStatement payStmt = conn.prepareStatement(paymentSql)) {
                    payStmt.setString(1, paymentMethod == null ? "Cash" : paymentMethod);
                    payStmt.setDouble(2, totalAmount);
                    payStmt.setInt(3, saleId);
                    payStmt.executeUpdate();
                }

                String updateQtySql = """
                    UPDATE product_variants
                    SET current_qty = current_qty - ?
                    WHERE variant_id = ?
                """;

                try (PreparedStatement updateStmt = conn.prepareStatement(updateQtySql)) {
                    for (CartItemModel item : cartItems) {
                        updateStmt.setInt(1, item.getQty());
                        updateStmt.setInt(2, item.getVariantId());
                        updateStmt.addBatch();
                    }

                    updateStmt.executeBatch();
                }

                String movementSql = """
                    INSERT INTO stock_movements
                    (variant_id, movement_type, qty, user_id)
                    VALUES (?, ?, ?, ?)
                """;

                try (PreparedStatement movementStmt = conn.prepareStatement(movementSql)) {
                    for (CartItemModel item : cartItems) {
                        movementStmt.setInt(1, item.getVariantId());
                        movementStmt.setString(2, "SALE");
                        movementStmt.setInt(3, item.getQty());
                        movementStmt.setInt(4, userId);
                        movementStmt.addBatch();
                    }

                    movementStmt.executeBatch();
                }

                int earnedPoints = calculateTotalQty();

                String loyaltySql = """
                    INSERT INTO loyalty_transactions
                    (customer_id, sale_id, points, transaction_type)
                    VALUES (?, ?, ?, ?)
                """;

                try (PreparedStatement loyaltyStmt = conn.prepareStatement(loyaltySql)) {
                    loyaltyStmt.setInt(1, customerId);
                    loyaltyStmt.setInt(2, saleId);
                    loyaltyStmt.setInt(3, earnedPoints);
                    loyaltyStmt.setString(4, "EARN");
                    loyaltyStmt.executeUpdate();
                }

                String updatePointsSql = """
                    UPDATE customers
                    SET loyalty_points = loyalty_points + ?
                    WHERE customer_id = ?
                """;

                try (PreparedStatement pointsStmt = conn.prepareStatement(updatePointsSql)) {
                    pointsStmt.setInt(1, earnedPoints);
                    pointsStmt.setInt(2, customerId);
                    pointsStmt.executeUpdate();
                }

                if (birthdayCouponAmount > 0) {
                    resetCustomerBirthdayCoupon(conn, customerId);
                }

                String lowStockMessage = buildLowStockMessage(conn);

                conn.commit();
                saved = true;

                closeWindow();

                final int finalSaleId = saleId;
                final String finalLowStockMessage = lowStockMessage;

                Platform.runLater(() -> {
                    openInvoiceWindow(finalSaleId);

                    if (!finalLowStockMessage.isBlank()) {
                        showLowStockAlert(finalLowStockMessage);
                    }
                });

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                showMessage("Failed to save sale: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void loadCustomersIntoCombo() {
        String sql = "SELECT name FROM customers ORDER BY name";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (conn == null) {
                showMessage("Database connection failed.");
                return;
            }

            cmbCustomer.getItems().clear();

            while (rs.next()) {
                cmbCustomer.getItems().add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Failed to load customers.");
        }
    }

    private void loadVariantsIntoCombo() {
        String sql = """
            SELECT 
                pv.variant_id,
                pv.barcode,
                p.name AS product_name,
                pv.size,
                pv.color,
                pv.current_qty
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            ORDER BY pv.variant_id
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (conn == null) {
                showMessage("Database connection failed.");
                return;
            }

            cmbVariant.getItems().clear();

            while (rs.next()) {
                cmbVariant.getItems().add(
                        buildVariantComboText(
                                rs.getInt("variant_id"),
                                rs.getString("product_name"),
                                rs.getString("barcode"),
                                rs.getString("size"),
                                rs.getString("color"),
                                rs.getInt("current_qty")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Failed to load variants.");
        }
    }

    private String buildVariantComboText(int variantId, String productName, String barcode,
                                         String size, String color, int currentQty) {
        return variantId + " - "
                + safe(productName) + " - "
                + safe(barcode) + " - "
                + safe(size) + " - "
                + safe(color) + " - Qty: "
                + currentQty;
    }

    private int extractVariantId(String variantText) {
        return Integer.parseInt(variantText.split(" - ")[0].trim());
    }

    private int getCustomerIdByName(Connection conn, String customerName) throws Exception {
        String sql = "SELECT customer_id FROM customers WHERE name = ? LIMIT 1";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, customerName);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("customer_id");
            }
        }

        throw new RuntimeException("Customer not found.");
    }

    private double getVariantPrice(Connection conn, int variantId) throws Exception {
        String sql = "SELECT price FROM product_variants WHERE variant_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, variantId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble("price");
            }
        }

        throw new RuntimeException("Variant not found.");
    }

    private int getVariantCurrentQty(Connection conn, int variantId) throws Exception {
        String sql = "SELECT current_qty FROM product_variants WHERE variant_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, variantId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("current_qty");
            }
        }

        throw new RuntimeException("Variant not found.");
    }

    private CartItemModel getVariantDetailsForCart(Connection conn, int variantId, int qty) throws Exception {
        String sql = """
            SELECT p.name AS product_name,
                   pv.barcode,
                   pv.size,
                   pv.color,
                   pv.price
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            WHERE pv.variant_id = ?
            LIMIT 1
        """;

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, variantId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double unitPrice = rs.getDouble("price");

                    return new CartItemModel(
                            variantId,
                            rs.getString("product_name"),
                            rs.getString("barcode"),
                            rs.getString("size"),
                            rs.getString("color"),
                            qty,
                            unitPrice
                    );
                }
            }
        }

        throw new RuntimeException("Variant not found.");
    }

    private CartItemModel findCartItemByVariantId(int variantId) {
        for (CartItemModel item : cartItems) {
            if (item.getVariantId() == variantId) {
                return item;
            }
        }

        return null;
    }

    private int getCartQtyForVariant(int variantId) {
        CartItemModel item = findCartItemByVariantId(variantId);
        return item == null ? 0 : item.getQty();
    }

    private double calculateSubtotal() {
        double subtotal = 0;

        for (CartItemModel item : cartItems) {
            subtotal += item.getLineTotal();
        }

        return subtotal;
    }

    private int calculateTotalQty() {
        int totalQty = 0;

        for (CartItemModel item : cartItems) {
            totalQty += item.getQty();
        }

        return totalQty;
    }

    private void updateTotalsLabels() {
        double subtotal = calculateSubtotal();
        double manualDiscountPercent = 0;

        try {
            String discountText = txtDiscount == null || txtDiscount.getText() == null
                    ? ""
                    : txtDiscount.getText().trim();

            if (!discountText.isEmpty()) {
                manualDiscountPercent = Double.parseDouble(discountText);
            }
        } catch (Exception ignored) {
            manualDiscountPercent = 0;
        }

        if (manualDiscountPercent < 0 || manualDiscountPercent > 100) {
            manualDiscountPercent = 0;
        }

        double autoDiscountPercent = subtotal >= 100 ? 10 : 0;
        double finalDiscountPercent = Math.max(autoDiscountPercent, manualDiscountPercent);
        double estimatedTotal = subtotal - (subtotal * finalDiscountPercent / 100.0);

        if (estimatedTotal < 0) {
            estimatedTotal = 0;
        }

        if (lblSubtotal != null) {
            lblSubtotal.setText(formatMoney(subtotal));
        }

        if (lblEstimatedTotal != null) {
            lblEstimatedTotal.setText(formatMoney(estimatedTotal));
        }
    }

    private double getCustomerBirthdayCouponAmount(Connection conn, int customerId) throws Exception {
        String sql = "SELECT birthday_coupon_amount FROM customers WHERE customer_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, customerId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble("birthday_coupon_amount");
            }
        }

        return 0.0;
    }

    private void resetCustomerBirthdayCoupon(Connection conn, int customerId) throws Exception {
        String sql = "UPDATE customers SET birthday_coupon_amount = 0.00 WHERE customer_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, customerId);
            pst.executeUpdate();
        }
    }

    private String buildLowStockMessage(Connection conn) throws Exception {
        StringBuilder message = new StringBuilder();

        for (CartItemModel item : cartItems) {
            int remainingQty = getVariantCurrentQty(conn, item.getVariantId()) - item.getQty();

            if (remainingQty <= LOW_STOCK_LIMIT) {
                message.append(item.getProductName())
                        .append(" | Barcode: ")
                        .append(item.getBarcode())
                        .append(" | Size: ")
                        .append(item.getSize())
                        .append(" | Color: ")
                        .append(item.getColor())
                        .append("\nOnly ")
                        .append(remainingQty)
                        .append(" item(s) left in stock.\n\n");
            }
        }

        return message.toString();
    }

    private void openInvoiceWindow(int saleId) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/Invoice.fxml"));
            Parent root = loader.load();

            InvoiceController controller = loader.getController();
            controller.loadInvoiceData(saleId);

            Stage stage = new Stage();
            stage.setTitle("Veloura Invoice");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Invoice Error", "Invoice failed to open: " + e.getMessage());
        }
    }

    private void showLowStockAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Low Stock Alert");
        alert.setHeaderText("Smart Inventory Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Unknown error" : message);
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatMoney(double value) {
        return String.format("%.2f", value);
    }

    private void showMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    private void closeWindow() {
        if (btnCancel != null && btnCancel.getScene() != null) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        }
    }

    public static class CartItemModel {

        private final int variantId;
        private final String productName;
        private final String barcode;
        private final String size;
        private final String color;
        private int qty;
        private final double unitPrice;

        public CartItemModel(int variantId,
                             String productName,
                             String barcode,
                             String size,
                             String color,
                             int qty,
                             double unitPrice) {
            this.variantId = variantId;
            this.productName = productName;
            this.barcode = barcode;
            this.size = size;
            this.color = color;
            this.qty = qty;
            this.unitPrice = unitPrice;
        }

        public int getVariantId() {
            return variantId;
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
            return qty * unitPrice;
        }

        public void addQty(int extraQty) {
            this.qty += extraQty;
        }
    }
}