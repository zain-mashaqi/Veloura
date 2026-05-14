package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.UserSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.Node;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.scene.paint.Color;

import javafx.util.Duration;

public class ReturnsController {

    @FXML private TextField txtSaleId;
    @FXML private ComboBox<String> cmbInvoiceItems;
    @FXML private TextField txtQty;
    @FXML private TextField txtReason;

    @FXML private Label lblMessage;

    @FXML private Button btnProcess;

    @FXML private TableView<ReturnModel> tblReturns;

    @FXML private TableColumn<ReturnModel, Number> colReturnId;
    @FXML private TableColumn<ReturnModel, Number> colSaleId;
    @FXML private TableColumn<ReturnModel, Number> colVariantId;
    @FXML private TableColumn<ReturnModel, String> colProduct;
    @FXML private TableColumn<ReturnModel, Number> colQty;
    @FXML private TableColumn<ReturnModel, String> colReason;
    @FXML private TableColumn<ReturnModel, String> colDate;

    private final ObservableList<ReturnModel> returnsList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        setupTable();

        tblReturns.setItems(returnsList);

        setupInvoiceItemsLoader();

        loadReturns();

        playReturnsAnimations();

        lblMessage.setText(
                "Enter Sale ID then press ENTER."
        );
    }

    private void setupTable() {

        colReturnId.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getReturnId()));

        colSaleId.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getSaleId()));

        colVariantId.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getVariantId()));

        colProduct.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getProductName()));

        colQty.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue().getQty()));

        colReason.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getReason()));

        colDate.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getReturnDate()));

        styleColumn(colReturnId);
        styleColumn(colSaleId);
        styleColumn(colVariantId);
        styleColumn(colProduct);
        styleColumn(colQty);
        styleColumn(colReason);
        styleColumn(colDate);
    }

    private <T> void styleColumn(TableColumn<ReturnModel, T> column) {

        column.setCellFactory(col -> new TableCell<ReturnModel, T>() {

            @Override
            protected void updateItem(T item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                    setText(null);

                    setStyle(
                            "-fx-background-color: transparent;"
                    );

                } else {

                    setText(String.valueOf(item));

                    setTextFill(Color.WHITE);

                    setStyle(
                            "-fx-alignment:CENTER;" +
                            "-fx-background-color: transparent;"
                    );
                }
            }
        });
    }

    private void setupInvoiceItemsLoader() {

        txtSaleId.setOnAction(e ->
                loadInvoiceItemsForSale());
    }

    private void loadInvoiceItemsForSale() {

        String saleIdText =
                txtSaleId.getText() == null
                        ? ""
                        : txtSaleId.getText().trim();

        if (saleIdText.isEmpty()) {
            return;
        }

        int saleId;

        try {

            saleId =
                    Integer.parseInt(saleIdText);

        } catch (NumberFormatException e) {

            showWarning(
                    "Sale ID must be a valid number."
            );

            return;
        }

        String sql = """
            SELECT 
                pv.variant_id,
                p.name AS product_name,
                pv.color,
                pv.size,
                si.qty

            FROM sale_items si

            JOIN product_variants pv
                 ON si.variant_id = pv.variant_id

            JOIN products p
                 ON pv.product_id = p.product_id

            WHERE si.sale_id = ?
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {

                cmbInvoiceItems.getItems().clear();

                while (rs.next()) {

                    int variantId =
                            rs.getInt("variant_id");

                    String itemText =
                            variantId + " - "
                            + rs.getString("product_name")
                            + " - "
                            + rs.getString("color")
                            + " - "
                            + rs.getString("size")
                            + " - Qty:"
                            + rs.getInt("qty");

                    cmbInvoiceItems.getItems().add(itemText);
                }

                if (cmbInvoiceItems.getItems().isEmpty()) {

                    showWarning(
                            "No items found for this invoice."
                    );

                } else {

                    lblMessage.setText(
                            "Invoice items loaded successfully."
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load invoice items."
            );
        }
    }

    @FXML
    private void processReturn() {

        String saleIdText =
                txtSaleId.getText() == null
                        ? ""
                        : txtSaleId.getText().trim();

        String qtyText =
                txtQty.getText() == null
                        ? ""
                        : txtQty.getText().trim();

        String reason =
                txtReason.getText() == null
                        ? ""
                        : txtReason.getText().trim();

        if (saleIdText.isEmpty()) {

            showWarning(
                    "Please enter Sale ID."
            );

            return;
        }

        String selectedItem =
                cmbInvoiceItems.getValue();

        if (selectedItem == null ||
                selectedItem.isBlank()) {

            showWarning(
                    "Please select invoice item."
            );

            return;
        }

        if (qtyText.isEmpty()) {

            showWarning(
                    "Please enter quantity."
            );

            return;
        }

        int saleId;
        int variantId;
        int qty;

        try {

            saleId =
                    Integer.parseInt(saleIdText);

            variantId =
                    Integer.parseInt(
                            selectedItem.split(" - ")[0]
                    );

            qty =
                    Integer.parseInt(qtyText);

        } catch (NumberFormatException e) {

            showWarning(
                    "Quantity and Sale ID must be valid numbers."
            );

            return;
        }

        if (qty <= 0) {

            showWarning(
                    "Quantity must be greater than zero."
            );

            return;
        }

        if (reason.isBlank()) {
            reason = "No reason provided";
        }

        int userId =
                UserSession.getUserId();

        if (userId <= 0) {

            showWarning(
                    "No active session found."
            );

            return;
        }

        try (Connection conn = DBConnection.connect()) {

            conn.setAutoCommit(false);

            try {

                int soldQty =
                        getSoldQty(
                                conn,
                                saleId,
                                variantId
                        );

                int returnedQty =
                        getAlreadyReturnedQty(
                                conn,
                                saleId,
                                variantId
                        );

                int availableToReturn =
                        soldQty - returnedQty;

                if (availableToReturn <= 0) {

                    conn.rollback();

                    showWarning(
                            "This item already fully returned."
                    );

                    return;
                }

                if (qty > availableToReturn) {

                    conn.rollback();

                    showWarning(
                            "Available to return: "
                                    + availableToReturn
                    );

                    return;
                }

                int returnId =
                        insertReturn(
                                conn,
                                saleId,
                                reason
                        );

                insertReturnItem(
                        conn,
                        returnId,
                        variantId,
                        qty
                );

                updateStock(
                        conn,
                        variantId,
                        qty
                );

                insertStockMovement(
                        conn,
                        variantId,
                        qty,
                        userId
                );

                updateSaleStatus(
                        conn,
                        saleId
                );

                conn.commit();

                clearFields();

                loadReturns();

                lblMessage.setText(
                        "Return processed successfully."
                );

                showInfo(
                        "Return processed successfully."
                );

            } catch (Exception e) {

                conn.rollback();

                e.printStackTrace();

                showError(
                        "Return failed: "
                                + e.getMessage()
                );

            } finally {

                conn.setAutoCommit(true);
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Database error."
            );
        }
    }

    private int getSoldQty(Connection conn,
                           int saleId,
                           int variantId) throws SQLException {

        String sql = """
            SELECT qty
            FROM sale_items
            WHERE sale_id = ?
              AND variant_id = ?
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            ps.setInt(2, variantId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("qty");
                }
            }
        }

        return 0;
    }

    private int getAlreadyReturnedQty(Connection conn,
                                      int saleId,
                                      int variantId) throws SQLException {

        String sql = """
            SELECT IFNULL(SUM(ri.qty),0) AS returned_qty

            FROM return_items ri

            JOIN returns r
                 ON ri.return_id = r.return_id

            WHERE r.sale_id = ?
              AND ri.variant_id = ?
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            ps.setInt(2, variantId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("returned_qty");
                }
            }
        }

        return 0;
    }

    private int insertReturn(Connection conn,
                             int saleId,
                             String reason) throws SQLException {

        String sql = """
            INSERT INTO returns
            (sale_id, return_date, reason)

            VALUES (?, CURDATE(), ?)
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            ps.setInt(1, saleId);
            ps.setString(2, reason);

            ps.executeUpdate();

            try (ResultSet rs =
                         ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Failed to create return."
        );
    }

    private void insertReturnItem(Connection conn,
                                  int returnId,
                                  int variantId,
                                  int qty) throws SQLException {

        String sql = """
            INSERT INTO return_items
            (return_id, variant_id, qty)

            VALUES (?, ?, ?)
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, returnId);
            ps.setInt(2, variantId);
            ps.setInt(3, qty);

            ps.executeUpdate();
        }
    }

    private void updateStock(Connection conn,
                             int variantId,
                             int qty) throws SQLException {

        String sql = """
            UPDATE product_variants

            SET current_qty =
                current_qty + ?

            WHERE variant_id = ?
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, qty);
            ps.setInt(2, variantId);

            ps.executeUpdate();
        }
    }

    private void insertStockMovement(Connection conn,
                                     int variantId,
                                     int qty,
                                     int userId) throws SQLException {

        String sql = """
            INSERT INTO stock_movements
            (variant_id, movement_type,
             qty, movement_date, user_id)

            VALUES (?, 'RETURN',
                    ?, NOW(), ?)
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            ps.setInt(2, qty);
            ps.setInt(3, userId);

            ps.executeUpdate();
        }
    }

    private void updateSaleStatus(Connection conn,
                                  int saleId) throws SQLException {

        String sql = """
            UPDATE sales
            SET status = 'RETURNED'
            WHERE sale_id = ?
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            ps.executeUpdate();
        }
    }

    private void loadReturns() {

        returnsList.clear();

        String sql = """
            SELECT
                r.return_id,
                r.sale_id,

                ri.variant_id,

                p.name AS product_name,

                ri.qty,

                COALESCE(r.reason,'-')
                    AS reason,

                r.return_date

            FROM returns r

            JOIN return_items ri
                 ON r.return_id = ri.return_id

            JOIN product_variants pv
                 ON ri.variant_id = pv.variant_id

            JOIN products p
                 ON pv.product_id = p.product_id

            ORDER BY r.return_id DESC
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                returnsList.add(

                        new ReturnModel(

                                rs.getInt("return_id"),

                                rs.getInt("sale_id"),

                                rs.getInt("variant_id"),

                                rs.getString("product_name"),

                                rs.getInt("qty"),

                                rs.getString("reason"),

                                String.valueOf(
                                        rs.getDate("return_date")
                                )
                        )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Could not load returns."
            );
        }
    }

    private void clearFields() {

        txtSaleId.clear();

        cmbInvoiceItems.getItems().clear();
        cmbInvoiceItems.setValue(null);

        txtQty.clear();
        txtReason.clear();
    }

    private void playReturnsAnimations() {

        animateNode(txtSaleId, 0);
        animateNode(cmbInvoiceItems, 120);
        animateNode(txtQty, 240);
        animateNode(txtReason, 360);
        animateNode(tblReturns, 480);

        animateButton(btnProcess);

        pulseNode(tblReturns);
    }

    private void animateNode(Node node,
                             int delay) {

        if (node == null) return;

        node.setOpacity(0);

        node.setTranslateY(20);

        FadeTransition fade =
                new FadeTransition(
                        Duration.millis(700),
                        node
                );

        fade.setFromValue(0);
        fade.setToValue(1);

        fade.setDelay(
                Duration.millis(delay)
        );

        TranslateTransition slide =
                new TranslateTransition(
                        Duration.millis(700),
                        node
                );

        slide.setFromY(20);
        slide.setToY(0);

        slide.setDelay(
                Duration.millis(delay)
        );

        fade.play();
        slide.play();
    }

    private void animateButton(Button btn) {

        if (btn == null) return;

        String originalStyle =
                btn.getStyle();

        btn.setOnMouseEntered(e -> {

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(170),
                            btn
                    );

            scale.setToX(1.045);
            scale.setToY(1.045);

            scale.play();

            btn.setStyle(
                    originalStyle +
                    "-fx-effect:dropshadow(gaussian,#D4A64A,22,0.45,0,0);"
            );
        });

        btn.setOnMouseExited(e -> {

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(170),
                            btn
                    );

            scale.setToX(1.0);
            scale.setToY(1.0);

            scale.play();

            btn.setStyle(originalStyle);
        });
    }

    private void pulseNode(Node node) {

        ScaleTransition pulse =
                new ScaleTransition(
                        Duration.seconds(2.4),
                        node
                );

        pulse.setFromX(1.0);
        pulse.setFromY(1.0);

        pulse.setToX(1.004);
        pulse.setToY(1.004);

        pulse.setCycleCount(
                Animation.INDEFINITE
        );

        pulse.setAutoReverse(true);

        pulse.play();
    }

    @FXML
    private void handleBack() {

        try {

            App.setRoot(
                    "Sales",
                    1006,
                    710
            );

        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Could not open Sales screen."
            );
        }
    }

    private void showInfo(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Success");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showWarning(String message) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Warning");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();

        lblMessage.setText(message);
    }

    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();

        lblMessage.setText(message);
    }
}