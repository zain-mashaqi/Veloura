package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.AuthorizationService;
import com.veloura.security.UserSession;
import java.io.IOException;
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

public class SalesController {

    @FXML private TableView<SaleModel> tblSales;
    @FXML private TextField txtSearchSale;
    @FXML private ComboBox<String> cmbCustomerFilter;
    @FXML private ComboBox<String> cmbExtraFilter;
    @FXML private Label lblPaginationInfo;

    @FXML private TableColumn<SaleModel, Integer> colSaleNumber;
    @FXML private TableColumn<SaleModel, Integer> colSaleId;
    @FXML private TableColumn<SaleModel, String> colCustomerName;
    @FXML private TableColumn<SaleModel, String> colUsername;
    @FXML private TableColumn<SaleModel, String> colSaleDate;
    @FXML private TableColumn<SaleModel, Double> colTotalAmount;
    @FXML private TableColumn<SaleModel, String> colSaleActions;

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCustomers;
    @FXML private Button btnSales;
    @FXML private Button btnReturns;
    @FXML private Button btnInventory;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;
    @FXML private Button btnProductStats;

    @FXML private Button btnAddSale;
    @FXML private Button btnDeleteSelectedSale;
    @FXML private Button btnRefreshSales;
    @FXML private Button btnPreviousPage;
    @FXML private Button btnNextPage;

    @FXML
    public void initialize() {
        System.out.println("SalesController Loaded");
        System.out.println("Logged in user: " + UserSession.getUsername());
        System.out.println("Role: " + UserSession.getRole());

        if (!AuthorizationService.canViewSales()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "You are not allowed to access Sales.");
            try {
                App.setRoot("Dashboard", 980, 640);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        applyPermissions();
        setupSalesTable();
        loadCustomerFilter();
        loadUserFilter();
        loadSales();
        playSalesAnimations();
    }

    private void playSalesAnimations() {
        animateNode(txtSearchSale, 0);
        animateNode(cmbCustomerFilter, 120);
        animateNode(cmbExtraFilter, 240);
        animateNode(tblSales, 360);
        animateNode(lblPaginationInfo, 500);

        animateMainButton(btnAddSale);
        animateMainButton(btnDeleteSelectedSale);
        animateMainButton(btnRefreshSales);
        animateMainButton(btnPreviousPage);
        animateMainButton(btnNextPage);

        animateSidebarButton(btnDashboard);
        animateSidebarButton(btnProducts);
        animateSidebarButton(btnCustomers);
        animateSidebarButton(btnSales);
        animateSidebarButton(btnReturns);
        animateSidebarButton(btnInventory);
        animateSidebarButton(btnUsers);
        animateSidebarButton(btnLogout);

        pulseNode(tblSales);
    }

    private void animateNode(Node node, int delay) {
        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(28);

        FadeTransition fade = new FadeTransition(Duration.millis(750), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide = new TranslateTransition(Duration.millis(750), node);
        slide.setFromY(28);
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
        pulse.setToX(1.004);
        pulse.setToY(1.004);
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

        setButtonVisible(btnReturns, AuthorizationService.canViewSales());

        setButtonVisible(btnAddSale, AuthorizationService.canCreateSales());
        setButtonVisible(btnProductStats, AuthorizationService.canViewStatistics());
        setButtonVisible(btnDeleteSelectedSale, AuthorizationService.canDeleteSales());
    }

    private void setButtonVisible(Button button, boolean allowed) {
        if (button == null) return;

        button.setVisible(allowed);
        button.setManaged(allowed);
    }

    private void setupSalesTable() {
        if (colSaleNumber != null) {
            colSaleNumber.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getNumber()));
            styleColumn(colSaleNumber);
        }

        if (colSaleId != null) {
            colSaleId.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getSaleId()));
            styleColumn(colSaleId);
        }

        if (colCustomerName != null) {
            colCustomerName.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getCustomerName()));
            styleColumn(colCustomerName);
        }

        if (colUsername != null) {
            colUsername.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getUsername()));
            styleColumn(colUsername);
        }

        if (colSaleDate != null) {
            colSaleDate.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getSaleDate()));
            styleColumn(colSaleDate);
        }

        if (colTotalAmount != null) {
            colTotalAmount.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getTotalAmount()));
            styleColumn(colTotalAmount);
        }

        if (colSaleActions != null) {
            colSaleActions.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getActions()));
            styleColumn(colSaleActions);
        }
    }

    private <T> void styleColumn(TableColumn<SaleModel, T> column) {
        column.setCellFactory(col -> new TableCell<SaleModel, T>() {
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

    private void loadCustomerFilter() {
        if (cmbCustomerFilter == null) return;

        cmbCustomerFilter.getItems().clear();
        cmbCustomerFilter.getItems().add("All");

        String sql = "SELECT name FROM customers ORDER BY name";

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cmbCustomerFilter.getItems().add(rs.getString("name"));
            }

            cmbCustomerFilter.setValue("All");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserFilter() {
        if (cmbExtraFilter == null) return;

        cmbExtraFilter.getItems().clear();
        cmbExtraFilter.getItems().add("All");

        String sql = "SELECT username FROM users ORDER BY username";

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cmbExtraFilter.getItems().add(rs.getString("username"));
            }

            cmbExtraFilter.setValue("All");

        } catch (Exception e) {
            System.out.println("Users table not ready yet.");
            cmbExtraFilter.setValue("All");
        }
    }

    @FXML
    public void handleAddSale() {
        if (!AuthorizationService.canCreateSales()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "You are not allowed to add sales.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/AddSale.fxml"));
            Parent root = loader.load();

            AddSaleController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Add Sale");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadSales();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to open Add Sale window: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteSelected() {
        if (!AuthorizationService.canDeleteSales()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "You are not allowed to delete sales.");
            return;
        }

        SaleModel selected = tblSales.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a sale first.");
            return;
        }

        String sql = "DELETE FROM sales WHERE sale_id = ?";

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selected.getSaleId());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Sale deleted successfully.");
                loadSales();
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No sale found to delete.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sale: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        loadSales();
    }

    @FXML
    public void handlePreviousPage() {
        System.out.println("Previous Page");
    }

    @FXML
    public void handleNextPage() {
        System.out.println("Next Page");
    }

    private void loadSales() {
        if (tblSales == null) return;

        ObservableList<SaleModel> saleList = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "SELECT s.sale_id, c.name AS customer_name, " +
                "COALESCE(u.username, 'User 1') AS username, " +
                "s.sale_date, s.total_amount " +
                "FROM sales s " +
                "LEFT JOIN customers c ON s.customer_id = c.customer_id " +
                "LEFT JOIN users u ON s.user_id = u.user_id"
        );

        String searchText = txtSearchSale != null ? txtSearchSale.getText() : null;
        String selectedCustomer = cmbCustomerFilter != null ? cmbCustomerFilter.getValue() : null;
        String selectedUser = cmbExtraFilter != null ? cmbExtraFilter.getValue() : null;

        boolean hasWhere = false;

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" WHERE (CAST(s.sale_id AS CHAR) LIKE ? OR c.name LIKE ?)");
            hasWhere = true;
        }

        if (selectedCustomer != null && !selectedCustomer.equals("All") && !selectedCustomer.trim().isEmpty()) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" c.name = ?");
            hasWhere = true;
        }

        if (selectedUser != null && !selectedUser.equals("All") && !selectedUser.trim().isEmpty()) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" u.username = ?");
        }

        sql.append(" ORDER BY s.sale_id DESC");

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (searchText != null && !searchText.trim().isEmpty()) {
                String pattern = "%" + searchText.trim() + "%";
                stmt.setString(index++, pattern);
                stmt.setString(index++, pattern);
            }

            if (selectedCustomer != null && !selectedCustomer.equals("All") && !selectedCustomer.trim().isEmpty()) {
                stmt.setString(index++, selectedCustomer);
            }

            if (selectedUser != null && !selectedUser.equals("All") && !selectedUser.trim().isEmpty()) {
                stmt.setString(index++, selectedUser);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                int counter = 1;

                while (rs.next()) {
                    SaleModel sale = new SaleModel(
                            counter++,
                            rs.getInt("sale_id"),
                            rs.getString("customer_name"),
                            rs.getString("username"),
                            rs.getString("sale_date"),
                            rs.getDouble("total_amount"),
                            "Edit/Delete"
                    );
                    saleList.add(sale);
                }
            }

            tblSales.setItems(saleList);

            if (lblPaginationInfo != null) {
                if (saleList.isEmpty()) {
                    lblPaginationInfo.setText("0-0 of 0");
                } else {
                    lblPaginationInfo.setText("1-" + saleList.size() + " of " + saleList.size());
                }
            }

            System.out.println("Sales loaded: " + saleList.size());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load sales: " + e.getMessage());
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
        if (!AuthorizationService.canManageProducts()) {
            return;
        }

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
    private void goToProductStats() throws IOException {
        if (!AuthorizationService.canViewStatistics()) return;
        App.setRoot("ProductStats", 1100, 700);
    }

    @FXML
    public void goToSales() {
        if (!AuthorizationService.canViewSales()) {
            return;
        }

        try {
            App.setRoot("Sales", 1006, 710);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToReturns() {
        try {
            App.setRoot("Returns", 980, 720);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Returns screen.");
        }
    }

    @FXML
    public void goToInventory() {
        try {
            App.setRoot("Inventory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToUsers() {
        if (!AuthorizationService.canManageUsers()) {
            return;
        }

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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}