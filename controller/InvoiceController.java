package com.veloura.controller;

import com.veloura.database.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.print.PrinterJob;

import javafx.scene.Node;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.layout.AnchorPane;

import javafx.scene.paint.Color;

import javafx.stage.Stage;

import javafx.util.Duration;

public class InvoiceController {

    @FXML private AnchorPane rootPane;

    @FXML private Label lblInvoiceId;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCashier;
    @FXML private Label lblSaleDate;
    @FXML private Label lblPaymentMethod;
    @FXML private Label lblPaidAmount;

    @FXML private Label lblStatus;

    @FXML private Label lblSubtotal;
    @FXML private Label lblRegularDiscount;
    @FXML private Label lblBirthdayCoupon;
    @FXML private Label lblFinalTotal;

    @FXML private TableView<InvoiceItemModel> tblInvoiceItems;

    @FXML private TableColumn<InvoiceItemModel, String> colProductName;
    @FXML private TableColumn<InvoiceItemModel, String> colBarcode;
    @FXML private TableColumn<InvoiceItemModel, String> colSize;
    @FXML private TableColumn<InvoiceItemModel, String> colColor;
    @FXML private TableColumn<InvoiceItemModel, Integer> colQty;
    @FXML private TableColumn<InvoiceItemModel, Double> colUnitPrice;
    @FXML private TableColumn<InvoiceItemModel, Double> colLineTotal;

    @FXML private Button btnClose;

    private final ObservableList<InvoiceItemModel> invoiceItems =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        setupTable();
        tblInvoiceItems.setItems(invoiceItems);

        playInvoiceAnimations();
    }

    private void setupTable() {

        colProductName.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getProductName()));

        colBarcode.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getBarcode()));

        colSize.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getSize()));

        colColor.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getColor()));

        colQty.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getQty()));

        colUnitPrice.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getUnitPrice()));

        colLineTotal.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getLineTotal()));

        styleColumn(colProductName);
        styleColumn(colBarcode);
        styleColumn(colSize);
        styleColumn(colColor);
        styleColumn(colQty);
        styleColumn(colUnitPrice);
        styleColumn(colLineTotal);
    }

    private <T> void styleColumn(TableColumn<InvoiceItemModel, T> column) {

        column.setCellFactory(col -> new TableCell<InvoiceItemModel, T>() {

            @Override
            protected void updateItem(T item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                    setText(null);

                    setStyle(
                            "-fx-background-color: transparent;"
                    );

                } else {

                    if (item instanceof Double) {

                        setText(String.format("%.2f", item));

                    } else {

                        setText(String.valueOf(item));
                    }

                    setTextFill(Color.WHITE);

                    setStyle(
                            "-fx-alignment:CENTER;" +
                            "-fx-background-color: transparent;"
                    );
                }
            }
        });
    }

    public void loadInvoiceData(int saleId) {

        loadInvoiceHeader(saleId);
        loadInvoiceItems(saleId);
    }

    private void loadInvoiceHeader(int saleId) {

        String sql = """
            SELECT 
                s.sale_id,
                c.name AS customer_name,
                COALESCE(u.username, 'Unknown') AS username,
                s.sale_date,

                COALESCE(s.subtotal,0) AS subtotal,
                COALESCE(s.discount_percent,0) AS discount_percent,
                COALESCE(s.discount_amount,0) AS discount_amount,
                COALESCE(s.total_amount,0) AS total_amount,

                COALESCE(p.payment_method,'N/A') AS payment_method,
                COALESCE(p.amount,s.total_amount) AS paid_amount,

                COALESCE(s.status,'COMPLETED') AS status

            FROM sales s

            LEFT JOIN customers c
                   ON s.customer_id = c.customer_id

            LEFT JOIN users u
                   ON s.user_id = u.user_id

            LEFT JOIN payments p
                   ON s.sale_id = p.sale_id

            WHERE s.sale_id = ?

            LIMIT 1
        """;

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    double subtotal =
                            rs.getDouble("subtotal");

                    double discountPercent =
                            rs.getDouble("discount_percent");

                    double totalDiscount =
                            rs.getDouble("discount_amount");

                    double regularDiscount =
                            subtotal * (discountPercent / 100.0);

                    double birthdayCoupon =
                            totalDiscount - regularDiscount;

                    if (birthdayCoupon < 0) {
                        birthdayCoupon = 0;
                    }

                    lblInvoiceId.setText(
                            String.valueOf(
                                    rs.getInt("sale_id"))
                    );

                    lblCustomerName.setText(
                            safeText(
                                    rs.getString("customer_name"))
                    );

                    lblCashier.setText(
                            safeText(
                                    rs.getString("username"))
                    );

                    lblSaleDate.setText(
                            String.valueOf(
                                    rs.getDate("sale_date"))
                    );

                    lblPaymentMethod.setText(
                            safeText(
                                    rs.getString("payment_method"))
                    );

                    lblPaidAmount.setText(
                            formatMoney(
                                    rs.getDouble("paid_amount"))
                    );

                    lblSubtotal.setText(
                            formatMoney(subtotal)
                    );

                    lblRegularDiscount.setText(
                            formatMoney(regularDiscount)
                    );

                    lblBirthdayCoupon.setText(
                            formatMoney(birthdayCoupon)
                    );

                    lblFinalTotal.setText(
                            formatMoney(
                                    rs.getDouble("total_amount"))
                    );

                    String status =
                            rs.getString("status");

                    lblStatus.setText(status);

                    switch (status.toUpperCase()) {

                        case "COMPLETED" ->

                                lblStatus.setStyle(
                                        "-fx-background-color:#1E8E3E;" +
                                        "-fx-text-fill:white;" +
                                        "-fx-padding:4 14 4 14;" +
                                        "-fx-background-radius:14;" +
                                        "-fx-font-size:12px;" +
                                        "-fx-font-weight:bold;"
                                );

                        case "CANCELLED" ->

                                lblStatus.setStyle(
                                        "-fx-background-color:#C62828;" +
                                        "-fx-text-fill:white;" +
                                        "-fx-padding:4 14 4 14;" +
                                        "-fx-background-radius:14;" +
                                        "-fx-font-size:12px;" +
                                        "-fx-font-weight:bold;"
                                );

                        case "RETURNED" ->

                                lblStatus.setStyle(
                                        "-fx-background-color:#B98224;" +
                                        "-fx-text-fill:white;" +
                                        "-fx-padding:4 14 4 14;" +
                                        "-fx-background-radius:14;" +
                                        "-fx-font-size:12px;" +
                                        "-fx-font-weight:bold;"
                                );

                        default ->

                                lblStatus.setStyle(
                                        "-fx-background-color:#444;" +
                                        "-fx-text-fill:white;" +
                                        "-fx-padding:4 14 4 14;" +
                                        "-fx-background-radius:14;"
                                );
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Invoice Error",
                    e.getMessage()
            );
        }
    }

    private void loadInvoiceItems(int saleId) {

        invoiceItems.clear();

        String sql = """
            SELECT
                p.name AS product_name,
                pv.barcode,
                pv.size,
                pv.color,

                si.qty,
                si.unit_price,

                (si.qty * si.unit_price) AS line_total

            FROM sale_items si

            JOIN product_variants pv
                 ON si.variant_id = pv.variant_id

            JOIN products p
                 ON pv.product_id = p.product_id

            WHERE si.sale_id = ?
        """;

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    invoiceItems.add(
                            new InvoiceItemModel(

                                    rs.getString("product_name"),

                                    rs.getString("barcode"),

                                    rs.getString("size"),

                                    rs.getString("color"),

                                    rs.getInt("qty"),

                                    rs.getDouble("unit_price"),

                                    rs.getDouble("line_total")
                            )
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Invoice Items Error",
                    e.getMessage()
            );
        }
    }

    private void playInvoiceAnimations() {

        animateNode(tblInvoiceItems, 0);

        animateNode(lblInvoiceId, 120);
        animateNode(lblCustomerName, 180);
        animateNode(lblCashier, 240);
        animateNode(lblSaleDate, 300);
        animateNode(lblPaymentMethod, 360);

        animateNode(lblSubtotal, 420);
        animateNode(lblRegularDiscount, 480);
        animateNode(lblBirthdayCoupon, 540);
        animateNode(lblFinalTotal, 600);

        animateButton(btnClose);
    }

    private void animateNode(Node node, int delay) {

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
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide =
                new TranslateTransition(
                        Duration.millis(700),
                        node
                );

        slide.setFromY(20);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delay));

        fade.play();
        slide.play();
    }

    private void animateButton(Button btn) {

        if (btn == null) return;

        String originalStyle = btn.getStyle();

        btn.setOnMouseEntered(e -> {

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(160),
                            btn
                    );

            scale.setToX(1.04);
            scale.setToY(1.04);

            scale.play();

            btn.setStyle(
                    originalStyle +
                    "-fx-effect:dropshadow(gaussian,#D4A64A,20,0.45,0,0);"
            );
        });

        btn.setOnMouseExited(e -> {

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(160),
                            btn
                    );

            scale.setToX(1.0);
            scale.setToY(1.0);

            scale.play();

            btn.setStyle(originalStyle);
        });
    }

    @FXML
    private void handlePrintInvoice() {

        PrinterJob job =
                PrinterJob.createPrinterJob();

        if (job != null &&
                job.showPrintDialog(rootPane.getScene().getWindow())) {

            boolean success =
                    job.printPage(rootPane);

            if (success) {
                job.endJob();
            }
        }
    }

    @FXML
    private void handleClose() {

        Stage stage =
                (Stage) btnClose
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void showError(String title, String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    private String safeText(String value) {

        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }

    private String formatMoney(double amount) {

        return String.format("$ %.2f", amount);
    }
}