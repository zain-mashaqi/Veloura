package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.AuthorizationService;
import com.veloura.security.PasswordUtil;
import com.veloura.security.UserSession;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class UsersController implements Initializable {

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCustomers;
    @FXML private Button btnSales;
    @FXML private Button btnInventory;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;

    @FXML private TextField txtUsername;
    @FXML private TextField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblUserError;
    @FXML private Label lblPendingRequests;

    @FXML private TableView<UserModel> tblUsers;
    @FXML private TableColumn<UserModel, Integer> colUserId;
    @FXML private TableColumn<UserModel, String> colUsername;
    @FXML private TableColumn<UserModel, String> colRole;
    @FXML private TableColumn<UserModel, String> colStatus;
    @FXML private TableColumn<UserModel, String> colResetRequest;

    private final ObservableList<UserModel> userList =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        if (!AuthorizationService.canManageUsers()) {
            showAlert(Alert.AlertType.ERROR,
                    "Access Denied",
                    "Only admin can access Users screen.");

            try {
                App.setRoot("Dashboard", 980, 640);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        if (lblUserError != null) {
            lblUserError.setVisible(false);
        }

        if (cbRole != null) {
            cbRole.getItems().addAll(
                    "ADMIN",
                    "MANAGER",
                    "CASHIER"
            );
        }

        setupUsersTable();
        loadUsers();
        loadPendingRequestsCount();
        playUsersAnimations();
    }

    private void playUsersAnimations() {

        animateNode(txtUsername, 0);
        animateNode(txtPassword, 120);
        animateNode(cbRole, 240);
        animateNode(lblPendingRequests, 350);
        animateNode(tblUsers, 500);

        animateSidebarButton(btnDashboard);
        animateSidebarButton(btnProducts);
        animateSidebarButton(btnCustomers);
        animateSidebarButton(btnSales);
        animateSidebarButton(btnInventory);
        animateSidebarButton(btnUsers);
        animateSidebarButton(btnLogout);

        pulseNode(tblUsers);
    }

    private void animateNode(Node node, int delay) {

        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(28);

        FadeTransition fade =
                new FadeTransition(
                        Duration.millis(750),
                        node
                );

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide =
                new TranslateTransition(
                        Duration.millis(750),
                        node
                );

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

            ScaleTransition scale =
                    new ScaleTransition(
                            Duration.millis(170),
                            btn
                    );

            scale.setToX(1.035);
            scale.setToY(1.035);
            scale.play();

            btn.setStyle(
                    originalStyle +
                    "-fx-effect:dropshadow(gaussian,#D4A64A,18,0.35,0,0);"
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

        if (node == null) return;

        ScaleTransition pulse =
                new ScaleTransition(
                        Duration.seconds(2.5),
                        node
                );

        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.01);
        pulse.setToY(1.01);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void setupUsersTable() {

        colUserId.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getUserId()
                ));

        colUsername.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getUsername()
                ));

        colRole.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getRole()
                ));

        colStatus.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getActiveText()
                ));

        colResetRequest.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue().getResetRequestText()
                ));

        styleColumn(colUserId);
        styleColumn(colUsername);
        styleColumn(colRole);
        styleColumn(colStatus);
        styleColumn(colResetRequest);
    }

    private <T> void styleColumn(TableColumn<UserModel, T> column) {

        column.setCellFactory(col ->
                new TableCell<UserModel, T>() {

                    @Override
                    protected void updateItem(T item, boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {

                            setText(null);
                            setStyle("-fx-background-color: transparent;");

                        } else {

                            setText(String.valueOf(item));
                            setTextFill(Color.WHITE);
                            setStyle(
                                    "-fx-alignment: CENTER;" +
                                    "-fx-background-color: transparent;"
                            );
                        }
                    }
                });
    }

    private void loadUsers() {

        userList.clear();

        String sql = """
            SELECT user_id, username, role, is_active, reset_requested
            FROM users
            ORDER BY user_id
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                userList.add(
                        new UserModel(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("role"),
                                rs.getBoolean("is_active"),
                                rs.getBoolean("reset_requested")
                        )
                );
            }

            tblUsers.setItems(userList);

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Failed to load users: " + e.getMessage()
            );
        }
    }

    private void loadPendingRequestsCount() {

        if (lblPendingRequests == null) return;

        String sql = """
            SELECT COUNT(*)
            FROM users
            WHERE reset_requested = TRUE
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                lblPendingRequests.setText(
                        "Pending Reset Requests: " + rs.getInt(1)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            lblPendingRequests.setText(
                    "Pending Reset Requests: 0"
            );
        }
    }

    @FXML
    public void handleAddUser() {

        if (!AuthorizationService.canManageUsers()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Only admin can add users."
            );
            return;
        }

        String username =
                txtUsername.getText() == null
                        ? ""
                        : txtUsername.getText().trim();

        String password =
                txtPassword.getText() == null
                        ? ""
                        : txtPassword.getText().trim();

        String role =
                cbRole == null
                        ? null
                        : cbRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            lblUserError.setText("Please fill all fields.");
            lblUserError.setVisible(true);
            return;
        }

        if (username.length() < 3) {
            lblUserError.setText("Username must be at least 3 characters.");
            lblUserError.setVisible(true);
            return;
        }

        if (password.length() < 4) {
            lblUserError.setText("Password must be at least 4 characters.");
            lblUserError.setVisible(true);
            return;
        }

        String hashedPassword =
                PasswordUtil.hashPassword(password);

        String sql = """
            INSERT INTO users
            (username, password, role, is_active, reset_requested, reset_request_time)
            VALUES (?, ?, ?, TRUE, FALSE, NULL)
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);

            stmt.executeUpdate();

            lblUserError.setVisible(false);

            clearFields();
            loadUsers();
            loadPendingRequestsCount();

            showStyledMessage(
                    "Success",
                    "User added successfully.",
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();

            lblUserError.setText(
                    "Failed to add user. Username may already exist."
            );

            lblUserError.setVisible(true);
        }
    }

    @FXML
    public void handleChangeRole() {

        if (!AuthorizationService.canManageUsers()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Only admin can change roles."
            );
            return;
        }

        UserModel selected =
                tblUsers.getSelectionModel().getSelectedItem();

        String newRole =
                cbRole == null
                        ? null
                        : cbRole.getValue();

        if (selected == null || newRole == null) {
            showStyledMessage(
                    "Warning",
                    "Select a user and a role first.",
                    false
            );
            return;
        }

        String sql = """
            UPDATE users
            SET role = ?
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole);
            stmt.setInt(2, selected.getUserId());

            stmt.executeUpdate();

            loadUsers();

            showStyledMessage(
                    "Success",
                    "Role updated successfully.",
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();

            showStyledMessage(
                    "Error",
                    "Failed to update role.",
                    false
            );
        }
    }

    @FXML
    private void goToProductStats() throws IOException {
        App.setRoot("ProductStats", 1100, 700);
    }

    @FXML
    public void handleToggleActive() {

        if (!AuthorizationService.canManageUsers()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Only admin can change status."
            );
            return;
        }

        UserModel selected =
                tblUsers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showStyledMessage(
                    "Warning",
                    "Select a user first.",
                    false
            );
            return;
        }

        boolean newStatus =
                !selected.isActive();

        String sql = """
            UPDATE users
            SET is_active = ?
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, newStatus);
            stmt.setInt(2, selected.getUserId());

            stmt.executeUpdate();

            loadUsers();

            showStyledMessage(
                    "Success",
                    "User status updated.",
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();

            showStyledMessage(
                    "Error",
                    "Failed to update status.",
                    false
            );
        }
    }

    @FXML
    public void handleResetPassword() {

        if (!AuthorizationService.canManageUsers()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Access Denied",
                    "Only admin can reset passwords."
            );
            return;
        }

        UserModel selected =
                tblUsers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showStyledMessage(
                    "Warning",
                    "Select a user first.",
                    false
            );
            return;
        }

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle("Reset Password");

        dialog.setHeaderText(
                "Set a new password for: "
                        + selected.getUsername()
        );

        DialogPane dialogPane =
                dialog.getDialogPane();

        dialogPane.setStyle(
                "-fx-background-color:#0B132B;" +
                "-fx-border-color:#D4A64A;" +
                "-fx-border-width:2;" +
                "-fx-border-radius:14;" +
                "-fx-background-radius:14;"
        );

        ButtonType saveButtonType =
                new ButtonType(
                        "Save Password",
                        ButtonType.OK.getButtonData()
                );

        dialogPane.getButtonTypes().addAll(
                saveButtonType,
                ButtonType.CANCEL
        );

        GridPane grid =
                new GridPane();

        grid.setHgap(10);
        grid.setVgap(12);

        grid.setStyle(
                "-fx-padding:20;" +
                "-fx-background-color:#0B132B;"
        );

        Label lblInfo =
                new Label(
                        "Enter a new password for this user."
                );

        lblInfo.setStyle(
                "-fx-text-fill:white;" +
                "-fx-font-size:13px;"
        );

        PasswordField txtNewPassword =
                new PasswordField();

        txtNewPassword.setPromptText(
                "New password"
        );

        txtNewPassword.setStyle(
                "-fx-background-color:#132042;" +
                "-fx-text-fill:white;" +
                "-fx-prompt-text-fill:#B8B8B8;" +
                "-fx-border-color:#D4A64A;" +
                "-fx-border-width:1.2;" +
                "-fx-background-radius:12;" +
                "-fx-border-radius:12;" +
                "-fx-padding:8;"
        );

        PasswordField txtConfirmPassword =
                new PasswordField();

        txtConfirmPassword.setPromptText(
                "Confirm password"
        );

        txtConfirmPassword.setStyle(
                "-fx-background-color:#132042;" +
                "-fx-text-fill:white;" +
                "-fx-prompt-text-fill:#B8B8B8;" +
                "-fx-border-color:#D4A64A;" +
                "-fx-border-width:1.2;" +
                "-fx-background-radius:12;" +
                "-fx-border-radius:12;" +
                "-fx-padding:8;"
        );

        grid.add(lblInfo, 0, 0);
        grid.add(txtNewPassword, 0, 1);
        grid.add(txtConfirmPassword, 0, 2);

        dialogPane.setContent(grid);

        dialogPane.lookupButton(saveButtonType).setStyle(
                "-fx-background-color:linear-gradient(to bottom,#F2C66D,#B98224);" +
                "-fx-text-fill:#06101F;" +
                "-fx-font-weight:bold;" +
                "-fx-font-size:13px;" +
                "-fx-background-radius:12;" +
                "-fx-cursor:hand;"
        );

        dialogPane.lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:#D4A64A;" +
                "-fx-text-fill:#D4A64A;" +
                "-fx-border-radius:12;" +
                "-fx-background-radius:12;" +
                "-fx-font-size:13px;"
        );

        var result =
                dialog.showAndWait();

        if (result.isPresent()
                && result.get() == saveButtonType) {

            String newPassword =
                    txtNewPassword.getText() == null
                            ? ""
                            : txtNewPassword.getText().trim();

            String confirmPassword =
                    txtConfirmPassword.getText() == null
                            ? ""
                            : txtConfirmPassword.getText().trim();

            if (newPassword.isEmpty()
                    || confirmPassword.isEmpty()) {

                showStyledMessage(
                        "Missing Password",
                        "Please fill both password fields.",
                        false
                );

                return;
            }

            if (newPassword.length() < 4) {

                showStyledMessage(
                        "Weak Password",
                        "Password must be at least 4 characters long.",
                        false
                );

                return;
            }

            if (!newPassword.equals(confirmPassword)) {

                showStyledMessage(
                        "Password Mismatch",
                        "The passwords do not match.",
                        false
                );

                return;
            }

            String hashedPassword =
                    PasswordUtil.hashPassword(newPassword);

            String sql = """
                UPDATE users
                SET password = ?,
                    reset_requested = FALSE,
                    reset_request_time = NULL
                WHERE user_id = ?
            """;

            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, hashedPassword);
                stmt.setInt(2, selected.getUserId());

                stmt.executeUpdate();

                loadUsers();
                loadPendingRequestsCount();

                showStyledMessage(
                        "Password Updated",
                        "The password for user '"
                                + selected.getUsername()
                                + "' has been updated successfully.",
                        true
                );

            } catch (Exception e) {

                e.printStackTrace();

                showStyledMessage(
                        "Reset Failed",
                        "Failed to reset password.",
                        false
                );
            }
        }
    }

    @FXML
    public void handleRefreshUsers() {

        loadUsers();
        loadPendingRequestsCount();
        clearFields();

        if (lblUserError != null) {
            lblUserError.setVisible(false);
        }
    }

    private void clearFields() {

        if (txtUsername != null) {
            txtUsername.clear();
        }

        if (txtPassword != null) {
            txtPassword.clear();
        }

        if (cbRole != null) {
            cbRole.setValue(null);
        }
    }

    private void showStyledMessage(String title,
                                   String message,
                                   boolean success) {

        Alert alert =
                new Alert(
                        success
                                ? Alert.AlertType.INFORMATION
                                : Alert.AlertType.WARNING
                );

        alert.setTitle(title);
        alert.setHeaderText(
                success ? "Action Completed" : "Notice"
        );

        alert.setContentText(message);

        DialogPane dialogPane =
                alert.getDialogPane();

        dialogPane.setStyle(
                "-fx-background-color:#0B132B;" +
                "-fx-border-color:#D4A64A;" +
                "-fx-border-width:2;" +
                "-fx-border-radius:14;" +
                "-fx-background-radius:14;"
        );

        dialogPane.lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color:linear-gradient(to bottom,#F2C66D,#B98224);" +
                "-fx-text-fill:#06101F;" +
                "-fx-font-weight:bold;" +
                "-fx-font-size:13px;" +
                "-fx-background-radius:12;" +
                "-fx-padding:8 18 8 18;"
        );

        dialogPane.getChildren()
                .stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .forEach(label ->
                        label.setStyle(
                                "-fx-text-fill:white;" +
                                "-fx-font-size:13px;"
                        ));

        alert.showAndWait();
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

        try {
            App.setRoot("Inventory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToUsers() {

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
            App.setRoot("Login", 860, 620);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type,
                           String title,
                           String message) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}