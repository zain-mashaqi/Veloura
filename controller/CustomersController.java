package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.AuthorizationService;
import com.veloura.security.UserSession;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class CustomersController {

    @FXML private TableView<CustomerModel> tblCustomers;
    @FXML private TextField txtSearchCustomer;
    @FXML private ComboBox<String> cmbCityFilter;
    @FXML private ComboBox<String> cmbGenderFilter;
    @FXML private Label lblPaginationInfo;

    @FXML private TableColumn<CustomerModel, Integer> colCustomerNumber;
    @FXML private TableColumn<CustomerModel, String> colCustomerName;
    @FXML private TableColumn<CustomerModel, String> colCustomerEmail;
    @FXML private TableColumn<CustomerModel, String> colCustomerPhone;
    @FXML private TableColumn<CustomerModel, String> colCustomerCity;
    @FXML private TableColumn<CustomerModel, String> colCustomerActions;

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCustomers;
    @FXML private Button btnSales;
    @FXML private Button btnInventory;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;
    @FXML private Button btnProductStats;

    @FXML private Button btnAddCustomer;
    @FXML private Button btnDeleteSelected;
    @FXML private Button btnRefreshCustomers;
    @FXML private Button btnPreviousPage;
    @FXML private Button btnNextPage;

    @FXML
    public void initialize() {
        System.out.println("CustomersController Loaded");
        System.out.println("Logged in user: " + UserSession.getUsername());
        System.out.println("Role: " + UserSession.getRole());

        if (!AuthorizationService.canViewCustomers()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "You are not allowed to access Customers.");
            try {
                App.setRoot("Dashboard", 980, 640);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        applyPermissions();

        if (cmbCityFilter != null) {
            cmbCityFilter.getItems().addAll("All", "Nablus", "Ramallah", "Jerusalem", "Jenin", "Tulkarm");
            cmbCityFilter.setValue("All");
        }

        if (cmbGenderFilter != null) {
            cmbGenderFilter.getItems().addAll("All", "Male", "Female");
            cmbGenderFilter.setValue("All");
        }

        setupCustomerTable();
        applyBirthdayCouponIfEligible();
        loadCustomers();
        playCustomersAnimations();
    }

    private void playCustomersAnimations() {
        animateNode(tblCustomers, 0);
        animateNode(txtSearchCustomer, 120);
        animateNode(cmbCityFilter, 180);
        animateNode(cmbGenderFilter, 240);

        animateButton(btnDashboard);
        animateButton(btnProducts);
        animateButton(btnCustomers);
        animateButton(btnSales);
        animateButton(btnInventory);
        animateButton(btnUsers);
        animateButton(btnLogout);
        animateButton(btnAddCustomer);
        animateButton(btnDeleteSelected);
        animateButton(btnRefreshCustomers);
        animateButton(btnPreviousPage);
        animateButton(btnNextPage);
    }

    private void animateNode(javafx.scene.Node node, int delay) {
        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(25);

        FadeTransition fade = new FadeTransition(Duration.millis(750), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide = new TranslateTransition(Duration.millis(750), node);
        slide.setFromY(25);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delay));

        fade.play();
        slide.play();
    }

    private void animateButton(Button btn) {
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

    private void applyPermissions() {
        if (btnProducts != null) {
            boolean allowed = AuthorizationService.canManageProducts();
            btnProducts.setVisible(allowed);
            btnProducts.setManaged(allowed);
        }

        if (btnUsers != null) {
            boolean allowed = AuthorizationService.canManageUsers();
            btnUsers.setVisible(allowed);
            btnUsers.setManaged(allowed);
        }

        if (btnProductStats != null) {
            boolean allowed = AuthorizationService.canViewStatistics();
            btnProductStats.setVisible(allowed);
            btnProductStats.setManaged(allowed);
        }

        if (btnAddCustomer != null) {
            boolean allowed = AuthorizationService.canManageCustomers();
            btnAddCustomer.setVisible(allowed);
            btnAddCustomer.setManaged(allowed);
        }

        if (btnDeleteSelected != null) {
            boolean allowed = AuthorizationService.hasRole("ADMIN");
            btnDeleteSelected.setVisible(allowed);
            btnDeleteSelected.setManaged(allowed);
        }
    }

    private void setupCustomerTable() {
        colCustomerNumber.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getNumber()));

        colCustomerName.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getName()));

        colCustomerEmail.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getEmail()));

        colCustomerPhone.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getPhone()));

        colCustomerCity.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getCity()));

        colCustomerActions.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getActions()));

        styleColumn(colCustomerNumber);
        styleColumn(colCustomerName);
        styleColumn(colCustomerEmail);
        styleColumn(colCustomerPhone);
        styleColumn(colCustomerCity);
        styleColumn(colCustomerActions);
    }

    private <T> void styleColumn(TableColumn<CustomerModel, T> column) {
        column.setCellFactory(col -> new TableCell<CustomerModel, T>() {
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

    @FXML
    public void handleAddCustomer() {
        if (!AuthorizationService.canManageCustomers()) {
            showAlert(Alert.AlertType.ERROR,
                    "Access Denied",
                    "You are not allowed to add customers.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/AddCustomer.fxml"));
            Parent root = loader.load();

            AddCustomerController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Add Customer");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isSaved()) {
                loadCustomers();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "Failed to open Add Customer window: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteSelected() {
        if (!AuthorizationService.hasRole("ADMIN")) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only admin can delete customers.");
            return;
        }

        CustomerModel selected = tblCustomers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a customer first.");
            return;
        }

        String sql = "DELETE FROM customers WHERE email = ?";

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selected.getEmail());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer deleted successfully.");
                loadCustomers();
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No customer found to delete.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete customer: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        loadCustomers();
    }

    @FXML
    public void handlePreviousPage() {
        System.out.println("Previous Page");
    }

    @FXML
    public void handleNextPage() {
        System.out.println("Next Page");
    }

    private void applyBirthdayCouponIfEligible() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        double couponValue = 10.0;

        String selectSql = """
            SELECT customer_id, name, birth_date, birthday_coupon_amount, last_birthday_coupon_year
            FROM customers
            WHERE birth_date IS NOT NULL
        """;

        String updateSql = """
            UPDATE customers
            SET birthday_coupon_amount = birthday_coupon_amount + ?,
                last_birthday_coupon_year = ?
            WHERE customer_id = ?
        """;

        try (var conn = DBConnection.connect()) {
            if (conn == null) return;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 ResultSet rs = selectStmt.executeQuery()) {

                while (rs.next()) {
                    int customerId = rs.getInt("customer_id");
                    java.sql.Date birthSqlDate = rs.getDate("birth_date");
                    Integer lastCouponYear = (Integer) rs.getObject("last_birthday_coupon_year");

                    if (birthSqlDate == null) continue;

                    LocalDate birthDate = birthSqlDate.toLocalDate();

                    boolean isBirthdayToday =
                            birthDate.getMonthValue() == today.getMonthValue()
                                    && birthDate.getDayOfMonth() == today.getDayOfMonth();

                    boolean alreadyReceivedThisYear =
                            lastCouponYear != null && lastCouponYear == currentYear;

                    if (isBirthdayToday && !alreadyReceivedThisYear) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setDouble(1, couponValue);
                            updateStmt.setInt(2, currentYear);
                            updateStmt.setInt(3, customerId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCustomers() {
        ObservableList<CustomerModel> customerList = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder("SELECT name, email, phone, city FROM customers");

        String searchText = txtSearchCustomer != null ? txtSearchCustomer.getText() : null;
        String selectedCity = cmbCityFilter != null ? cmbCityFilter.getValue() : null;
        String selectedGender = cmbGenderFilter != null ? cmbGenderFilter.getValue() : null;

        boolean hasWhere = false;

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" WHERE (name LIKE ? OR email LIKE ?)");
            hasWhere = true;
        }

        if (selectedCity != null && !selectedCity.equals("All") && !selectedCity.trim().isEmpty()) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" city = ?");
            hasWhere = true;
        }

        if (selectedGender != null && !selectedGender.equals("All") && !selectedGender.trim().isEmpty()) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" gender = ?");
        }

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (searchText != null && !searchText.trim().isEmpty()) {
                String pattern = "%" + searchText.trim() + "%";
                stmt.setString(index++, pattern);
                stmt.setString(index++, pattern);
            }

            if (selectedCity != null && !selectedCity.equals("All") && !selectedCity.trim().isEmpty()) {
                stmt.setString(index++, selectedCity);
            }

            if (selectedGender != null && !selectedGender.equals("All") && !selectedGender.trim().isEmpty()) {
                stmt.setString(index++, selectedGender);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                int counter = 1;

                while (rs.next()) {
                    CustomerModel customer = new CustomerModel(
                            counter++,
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("city"),
                            "Edit/Delete"
                    );
                    customerList.add(customer);
                }
            }

            tblCustomers.setItems(customerList);

            if (lblPaginationInfo != null) {
                if (customerList.isEmpty()) {
                    lblPaginationInfo.setText("0-0 of 0");
                } else {
                    lblPaginationInfo.setText("1-" + customerList.size() + " of " + customerList.size());
                }
            }

            System.out.println("Customers loaded: " + customerList.size());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load customers: " + e.getMessage());
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
        if (!AuthorizationService.canViewCustomers()) return;

        try {
            App.setRoot("Customers", 980, 640);
        } catch (IOException e) {
            e.printStackTrace();
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
    public void goToSales() {
        try {
            App.setRoot("Sales", 1006, 710);
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
    private void goToProductStats() throws IOException {
        App.setRoot("ProductStats", 1100, 700);
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