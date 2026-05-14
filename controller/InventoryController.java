package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.AuthorizationService;
import com.veloura.security.UserSession;
import com.veloura.util.ToastNotification;
import com.veloura.util.ToastNotification.ToastType;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class InventoryController implements Initializable {

    @FXML private Label lblTotalVariants;
    @FXML private Label lblTotalUnits;
    @FXML private Label lblLowStock;
    @FXML private Label lblOutOfStock;

    @FXML private Label lblAlertTitle;
    @FXML private Label lblAlertMessage;
    @FXML private Label lblAlertDetails;
    @FXML private HBox lowStockAlertBox;
    @FXML private Button btnViewLowStock;
    @FXML private Button btnDismissAlert;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbCategory;
    @FXML private ComboBox<String> cbBrand;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<InventoryModel> inventoryTable;
    @FXML private TableColumn<InventoryModel, Integer> colVariantId;
    @FXML private TableColumn<InventoryModel, String> colBarcode;
    @FXML private TableColumn<InventoryModel, String> colProductName;
    @FXML private TableColumn<InventoryModel, String> colBrand;
    @FXML private TableColumn<InventoryModel, String> colCategory;
    @FXML private TableColumn<InventoryModel, String> colAudience;
    @FXML private TableColumn<InventoryModel, String> colSize;
    @FXML private TableColumn<InventoryModel, String> colColor;
    @FXML private TableColumn<InventoryModel, Double> colPrice;
    @FXML private TableColumn<InventoryModel, Integer> colCurrentQty;
    @FXML private TableColumn<InventoryModel, String> colStatus;
    @FXML private TableColumn<InventoryModel, String> colExpiryDate;
    @FXML private TableColumn<InventoryModel, String> colExpiryStatus;

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCustomers;
    @FXML private Button btnSales;
    @FXML private Button btnInventory;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;
    @FXML private Button btnRefresh;
    @FXML private Button btnAddStock;
    @FXML private Button btnProductStats;

    private final ObservableList<InventoryModel> inventoryList =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("InventoryController loaded");
        System.out.println("Logged in user: " + UserSession.getUsername());
        System.out.println("Role: " + UserSession.getRole());

        if (!AuthorizationService.canViewInventory()) {
            try {
                App.setRoot("Dashboard", 980, 640);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        applyPermissions();
        setupTableColumns();

        if (inventoryTable != null) {
            inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            inventoryTable.setFixedCellSize(35);
           inventoryTable.setStyle("""
    -fx-background-color: rgba(2,9,16,0.88);
    -fx-control-inner-background: #06101F;
    -fx-table-cell-border-color: transparent;
    -fx-selection-bar: #B98224;
    -fx-selection-bar-non-focused: #8B6526;
    -fx-table-header-border-color: #8B6526;
    -fx-background-radius: 12;
    -fx-border-color: #8B6526;
    -fx-border-radius: 12;
    -fx-text-background-color: white;
""");
        }

        loadInventoryCards();
        loadCategoryFilter();
        loadBrandFilter();
        loadStatusFilter();
        loadInventoryTable();
        updateLowStockAlert();
        playInventoryAnimations();
        showOpeningStockWarningIfNeeded();
        showOpeningExpiryWarningIfNeeded();
    }

    private void playInventoryAnimations() {
        animateNode(lblTotalVariants, 0);
        animateNode(lblTotalUnits, 150);
        animateNode(lblLowStock, 300);
        animateNode(lblOutOfStock, 450);
        animateNode(txtSearch, 250);
        animateNode(cbCategory, 350);
        animateNode(cbBrand, 450);
        animateNode(cbStatus, 550);
        animateNode(lowStockAlertBox, 650);
        animateNode(inventoryTable, 750);

        animateMainButton(btnRefresh);
        animateMainButton(btnAddStock);
        animateMainButton(btnViewLowStock);
        animateMainButton(btnDismissAlert);

        animateSidebarButton(btnDashboard);
        animateSidebarButton(btnProducts);
        animateSidebarButton(btnCustomers);
        animateSidebarButton(btnSales);
        animateSidebarButton(btnInventory);
        animateSidebarButton(btnUsers);
        animateSidebarButton(btnLogout);

        pulseNode(lblTotalVariants);
        pulseNode(lblTotalUnits);
        pulseNode(lblLowStock);
        pulseNode(lblOutOfStock);
    }

    private void animateNode(Node node, int delay) {
        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(750), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide = new TranslateTransition(Duration.millis(750), node);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delay));

        fade.play();
        slide.play();
    }

    private void animateSidebarButton(Button btn) {
        if (btn == null) return;

        String originalStyle = btn.getStyle();

        btn.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(170), btn);
            scale.setToX(1.035);
            scale.setToY(1.035);
            scale.play();
            btn.setStyle(originalStyle + "-fx-effect:dropshadow(gaussian,#D4A64A,18,0.35,0,0);");
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(170), btn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
            btn.setStyle(originalStyle);
        });
    }

    private void animateMainButton(Button btn) {
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

    private void pulseNode(Node node) {
        if (node == null) return;

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.4), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.018);
        pulse.setToY(1.018);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void applyPermissions() {
        setButtonVisible(btnDashboard, AuthorizationService.canViewDashboard());
        setButtonVisible(btnProducts, AuthorizationService.canManageProducts());
        setButtonVisible(btnCustomers, AuthorizationService.canViewCustomers());
        setButtonVisible(btnSales, AuthorizationService.canViewSales());
        setButtonVisible(btnInventory, AuthorizationService.canViewInventory());
        setButtonVisible(btnUsers, AuthorizationService.canManageUsers());
        setButtonVisible(btnProductStats, AuthorizationService.canViewStatistics());

        setButtonVisible(btnAddStock, AuthorizationService.canManageInventory());
    }

    private void setButtonVisible(Button button, boolean allowed) {
        if (button == null) return;

        button.setVisible(allowed);
        button.setManaged(allowed);
    }

    private void setupTableColumns() {
        colVariantId.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getVariantId()));

        colBarcode.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getBarcode()));

        colProductName.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getProductName()));

        colBrand.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getBrandName()));

        colCategory.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getCategoryName()));

        colAudience.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getAudience()));

        colSize.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getSize()));

        colColor.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getColor()));

        colPrice.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice()));

        colCurrentQty.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getCurrentQty()));

        colStatus.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getStockStatus()));

        if (colExpiryDate != null) {
            colExpiryDate.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getExpiryDateText()));
        }

        if (colExpiryStatus != null) {
            colExpiryStatus.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getExpiryStatus()));
        }

        styleColumn(colVariantId);
        styleColumn(colBarcode);
        styleColumn(colProductName);
        styleColumn(colBrand);
        styleColumn(colCategory);
        styleColumn(colAudience);
        styleColumn(colSize);
        styleColumn(colColor);
        styleColumn(colPrice);
        styleQtyColumn();
        styleStatusColumn();

        if (colExpiryDate != null) {
            styleColumn(colExpiryDate);
        }

        if (colExpiryStatus != null) {
            styleExpiryColumn();
        }
    }

    private <T> void styleColumn(TableColumn<InventoryModel, T> column) {
        if (column == null) return;

        column.setCellFactory(col -> new TableCell<InventoryModel, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(String.valueOf(item));
                    setTextFill(Color.WHITE);
                    setStyle("-fx-alignment: CENTER; -fx-background-color: transparent;");
                }
            }
        });
    }

    private void styleQtyColumn() {
        if (colCurrentQty == null) return;

        colCurrentQty.setCellFactory(col -> new TableCell<InventoryModel, Integer>() {
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);

                if (empty || qty == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                setText(String.valueOf(qty));
                setTextFill(Color.WHITE);

                if (qty == 0) {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: rgba(120,20,30,0.55); -fx-font-weight: bold;");
                } else if (qty <= 5) {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: rgba(190,70,30,0.45); -fx-font-weight: bold;");
                } else if (qty <= 10) {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: rgba(212,166,74,0.28); -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: transparent;");
                }
            }
        });
    }

    private void styleStatusColumn() {
        if (colStatus == null) return;

        colStatus.setCellFactory(col -> new TableCell<InventoryModel, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                setText(status);
                setTextFill(Color.WHITE);

                if (status.contains("OUT")) {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: rgba(120,20,30,0.78); -fx-font-weight: bold; -fx-background-radius: 8;");
                } else if (status.contains("LOW")) {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: rgba(190,70,30,0.60); -fx-font-weight: bold; -fx-background-radius: 8;");
                } else if (status.contains("WATCH")) {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: rgba(212,166,74,0.35); -fx-font-weight: bold; -fx-background-radius: 8;");
                } else {
                    setStyle("-fx-alignment: CENTER; -fx-background-color: rgba(30,120,70,0.35); -fx-font-weight: bold; -fx-background-radius: 8;");
                }
            }
        });
    }

    private void styleExpiryColumn() {
        if (colExpiryStatus == null) return;

        colExpiryStatus.setCellFactory(col -> new TableCell<InventoryModel, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                setText(status);
                setTextFill(Color.WHITE);

                if (status.contains("EXPIRED")) {
                    setStyle("-fx-alignment:CENTER; -fx-background-color:rgba(145,20,35,0.82); -fx-font-weight:bold; -fx-background-radius:8;");
                } else if (status.contains("EXPIRING")) {
                    setStyle("-fx-alignment:CENTER; -fx-background-color:rgba(212,120,20,0.65); -fx-font-weight:bold; -fx-background-radius:8;");
                } else if (status.contains("VALID")) {
                    setStyle("-fx-alignment:CENTER; -fx-background-color:rgba(30,120,70,0.40); -fx-font-weight:bold; -fx-background-radius:8;");
                } else {
                    setStyle("-fx-alignment:CENTER; -fx-background-color:rgba(80,80,80,0.30); -fx-font-weight:bold; -fx-background-radius:8;");
                }
            }
        });
    }

    private String getStockStatus(int qty) {
        if (qty == 0) return "⛔ OUT OF STOCK";
        if (qty <= 5) return "⚠ LOW STOCK";
        if (qty <= 10) return "● WATCH";
        return "✔ IN STOCK";
    }

    private void loadInventoryCards() {
        String totalVariantsQuery = "SELECT COUNT(*) AS total_variants FROM product_variants";
        String totalUnitsQuery = "SELECT COALESCE(SUM(current_qty), 0) AS total_units FROM product_variants";
        String lowStockQuery = "SELECT COUNT(*) AS low_stock FROM product_variants WHERE current_qty > 0 AND current_qty <= 5";
        String outOfStockQuery = "SELECT COUNT(*) AS out_of_stock FROM product_variants WHERE current_qty = 0";

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) return;

            try (PreparedStatement pst = conn.prepareStatement(totalVariantsQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblTotalVariants.setText(rs.getString("total_variants"));
            }

            try (PreparedStatement pst = conn.prepareStatement(totalUnitsQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblTotalUnits.setText(rs.getString("total_units"));
            }

            try (PreparedStatement pst = conn.prepareStatement(lowStockQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblLowStock.setText(rs.getString("low_stock"));
            }

            try (PreparedStatement pst = conn.prepareStatement(outOfStockQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblOutOfStock.setText(rs.getString("out_of_stock"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadInventoryTable() {
        inventoryList.clear();

        String query = """
            SELECT 
                pv.variant_id,
                pv.barcode,
                p.name AS product_name,
                COALESCE(b.brand_name, p.brand) AS brand_name,
                COALESCE(c.category_name, p.category) AS category_name,
                p.audience,
                pv.size,
                pv.color,
                pv.price,
                pv.current_qty,
                (
                    SELECT MIN(pi.expiry_date)
                    FROM purchase_items pi
                    WHERE pi.variant_id = pv.variant_id
                      AND pi.expiry_date IS NOT NULL
                ) AS nearest_expiry
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
            LEFT JOIN brands b ON p.brand_id = b.brand_id
            LEFT JOIN categories c ON p.category_id = c.category_id
            ORDER BY
                CASE
                    WHEN pv.current_qty = 0 THEN 1
                    WHEN pv.current_qty <= 5 THEN 2
                    WHEN pv.current_qty <= 10 THEN 3
                    ELSE 4
                END,
                pv.current_qty ASC,
                pv.variant_id ASC
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                int qty = rs.getInt("current_qty");

                LocalDate nearestExpiry = null;
                java.sql.Date expirySqlDate = rs.getDate("nearest_expiry");

                if (expirySqlDate != null) {
                    nearestExpiry = expirySqlDate.toLocalDate();
                }

                inventoryList.add(new InventoryModel(
                        rs.getInt("variant_id"),
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        rs.getString("brand_name"),
                        rs.getString("category_name"),
                        rs.getString("audience"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getDouble("price"),
                        qty,
                        getStockStatus(qty),
                        nearestExpiry
                ));
            }

            inventoryTable.setItems(FXCollections.observableArrayList(inventoryList));
            System.out.println("Inventory loaded: " + inventoryList.size());

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to load inventory.", ToastType.ERROR);
        }
    }

    private void loadCategoryFilter() {
        if (cbCategory == null) return;

        cbCategory.getItems().clear();
        cbCategory.getItems().add("All");

        String query = "SELECT category_name FROM categories ORDER BY category_name";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                cbCategory.getItems().add(rs.getString("category_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cbCategory.setValue("All");
    }

    private void loadBrandFilter() {
        if (cbBrand == null) return;

        cbBrand.getItems().clear();
        cbBrand.getItems().add("All");

        String query = "SELECT brand_name FROM brands ORDER BY brand_name";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                cbBrand.getItems().add(rs.getString("brand_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cbBrand.setValue("All");
    }

    private void loadStatusFilter() {
        if (cbStatus == null) return;

        cbStatus.getItems().clear();
        cbStatus.getItems().addAll(
                "All",
                "✔ IN STOCK",
                "⚠ LOW STOCK",
                "⛔ OUT OF STOCK",
                "● WATCH"
        );
        cbStatus.setValue("All");
    }

    private void updateLowStockAlert() {
        if (lowStockAlertBox == null) return;

        int lowStockCount = 0;
        int outOfStockCount = 0;

        String lowStockCountQuery = "SELECT COUNT(*) AS low_stock FROM product_variants WHERE current_qty > 0 AND current_qty <= 5";
        String outOfStockCountQuery = "SELECT COUNT(*) AS out_of_stock FROM product_variants WHERE current_qty = 0";

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) return;

            try (PreparedStatement pst = conn.prepareStatement(lowStockCountQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lowStockCount = rs.getInt("low_stock");
            }

            try (PreparedStatement pst = conn.prepareStatement(outOfStockCountQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) outOfStockCount = rs.getInt("out_of_stock");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lowStockCount == 0 && outOfStockCount == 0) {
            lowStockAlertBox.setVisible(false);
            lowStockAlertBox.setManaged(false);
            return;
        }

        lowStockAlertBox.setVisible(true);
        lowStockAlertBox.setManaged(true);

        if (lblAlertTitle != null) lblAlertTitle.setText("Smart Inventory Alert");

        if (lblAlertMessage != null) {
            if (outOfStockCount > 0) {
                lblAlertMessage.setText("Some items are out of stock and need urgent restocking.");
            } else {
                lblAlertMessage.setText("Some variants are running low and need attention.");
            }
        }

        if (lblAlertDetails != null) {
            lblAlertDetails.setText("Low Stock: " + lowStockCount + "   |   Out of Stock: " + outOfStockCount);
        }
    }

    private void showOpeningStockWarningIfNeeded() {
        int low = 0;
        int out = 0;

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) return;

            try (PreparedStatement pst = conn.prepareStatement(
                    "SELECT COUNT(*) FROM product_variants WHERE current_qty > 0 AND current_qty <= 5");
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) low = rs.getInt(1);
            }

            try (PreparedStatement pst = conn.prepareStatement(
                    "SELECT COUNT(*) FROM product_variants WHERE current_qty = 0");
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) out = rs.getInt(1);
            }

            if (low > 0 || out > 0) {
                showToast("⚠ Inventory needs attention: Low Stock " + low + " | Out of Stock " + out,
                        ToastType.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOpeningExpiryWarningIfNeeded() {
        int expired = 0;
        int expiringSoon = 0;

        String expiredSql = """
            SELECT COUNT(*)
            FROM purchase_items
            WHERE expiry_date IS NOT NULL
              AND expiry_date < CURDATE()
        """;

        String soonSql = """
            SELECT COUNT(*)
            FROM purchase_items
            WHERE expiry_date IS NOT NULL
              AND expiry_date >= CURDATE()
              AND expiry_date <= DATE_ADD(CURDATE(), INTERVAL 60 DAY)
        """;

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) return;

            try (PreparedStatement pst = conn.prepareStatement(expiredSql);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) expired = rs.getInt(1);
            }

            try (PreparedStatement pst = conn.prepareStatement(soonSql);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) expiringSoon = rs.getInt(1);
            }

            if (expired > 0 || expiringSoon > 0) {
                showToast("⏳ Expiry alert: Expired " + expired + " | Expiring soon " + expiringSoon,
                        ToastType.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddStock() {
        if (!AuthorizationService.canManageProducts()) {
            showToast("Access denied. You are not allowed to add stock.", ToastType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/AddStock.fxml"));
            Parent root = loader.load();

            AddStockController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Add Stock");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadInventoryCards();
                loadInventoryTable();
                updateLowStockAlert();

                if (inventoryTable != null) {
                    inventoryTable.refresh();
                }

                showToast("✔ Stock added successfully. Inventory updated.", ToastType.SUCCESS);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to open Add Stock window: " + e.getMessage(), ToastType.ERROR);
        }
    }

    @FXML
    private void handleViewLowStock() {
        if (cbStatus != null) cbStatus.setValue("⚠ LOW STOCK");
        filterInventory();
    }

    @FXML
    private void handleDismissAlert() {
        if (lowStockAlertBox != null) {
            lowStockAlertBox.setVisible(false);
            lowStockAlertBox.setManaged(false);
        }
    }

    @FXML
    private void goToProductStats() throws IOException {
        if (!AuthorizationService.canViewStatistics()) return;
        App.setRoot("ProductStats", 1100, 700);
    }

    @FXML
    private void handleSearch() {
        filterInventory();
    }

    @FXML
    private void handleFilter() {
        filterInventory();
    }

    @FXML
    private void handleRefresh() {
        if (txtSearch != null) txtSearch.clear();
        if (cbCategory != null) cbCategory.setValue("All");
        if (cbBrand != null) cbBrand.setValue("All");
        if (cbStatus != null) cbStatus.setValue("All");

        loadInventoryCards();
        loadInventoryTable();

        if (inventoryTable != null) {
            inventoryTable.setItems(FXCollections.observableArrayList(inventoryList));
            inventoryTable.refresh();
        }

        updateLowStockAlert();
        showToast("Inventory refreshed.", ToastType.INFO);
    }

    private void filterInventory() {
        ObservableList<InventoryModel> filteredList = FXCollections.observableArrayList();

        String searchText =
                (txtSearch != null && txtSearch.getText() != null)
                        ? txtSearch.getText().toLowerCase().trim()
                        : "";

        String selectedCategory = cbCategory != null ? cbCategory.getValue() : "All";
        String selectedBrand = cbBrand != null ? cbBrand.getValue() : "All";
        String selectedStatus = cbStatus != null ? cbStatus.getValue() : "All";

        for (InventoryModel item : inventoryList) {
            boolean matchesSearch =
                    searchText.isEmpty() ||
                    (item.getBarcode() != null && item.getBarcode().toLowerCase().contains(searchText)) ||
                    (item.getProductName() != null && item.getProductName().toLowerCase().contains(searchText));

            boolean matchesCategory =
                    selectedCategory == null ||
                    selectedCategory.equals("All") ||
                    (item.getCategoryName() != null &&
                            item.getCategoryName().equalsIgnoreCase(selectedCategory));

            boolean matchesBrand =
                    selectedBrand == null ||
                    selectedBrand.equals("All") ||
                    (item.getBrandName() != null &&
                            item.getBrandName().equalsIgnoreCase(selectedBrand));

            boolean matchesStatus =
                    selectedStatus == null ||
                    selectedStatus.equals("All") ||
                    (item.getStockStatus() != null &&
                            item.getStockStatus().equalsIgnoreCase(selectedStatus));

            if (matchesSearch && matchesCategory && matchesBrand && matchesStatus) {
                filteredList.add(item);
            }
        }

        inventoryTable.setItems(filteredList);
    }

    private void showToast(String message, ToastType type) {
        try {
            Stage stage = null;

            if (btnAddStock != null && btnAddStock.getScene() != null) {
                stage = (Stage) btnAddStock.getScene().getWindow();
            }

            ToastNotification.show(stage, message, type);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToDashboard() {
        try {
            App.setRoot("Dashboard", 980, 640);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToProducts() {
        if (!AuthorizationService.canManageProducts()) return;

        try {
            App.setRoot("Product", 980, 840);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToCustomers() {
        try {
            App.setRoot("Customers", 980, 640);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToSales() {
        try {
            App.setRoot("Sales", 1006, 710);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToInventory() {
        if (!AuthorizationService.canViewInventory()) return;

        try {
            App.setRoot("Inventory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToUsers() {
        if (!AuthorizationService.canManageUsers()) return;

        try {
            App.setRoot("Users", 1100, 720);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            UserSession.clearSession();
            App.setRoot("Login", 600, 400);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}