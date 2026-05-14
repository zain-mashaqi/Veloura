package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.PasswordUtil;
import com.veloura.security.UserSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Label lblLoginError;
    @FXML private Button btnLogin;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnForgotPassword;

    @FXML private Pane loginCard;
    @FXML private ImageView imgLogo;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        if (lblLoginError != null) {
            lblLoginError.setVisible(false);
        }

        if (txtPasswordVisible != null) {
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
        }

        playEntranceAnimation();
        applyLogoGlow();
        applyLoginButtonAnimation();
    }

    private void playEntranceAnimation() {
        if (loginCard == null) return;

        loginCard.setOpacity(0);
        loginCard.setTranslateX(70);

        FadeTransition fade = new FadeTransition(Duration.millis(850), loginCard);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(850), loginCard);
        slide.setFromX(70);
        slide.setToX(0);

        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.play();
    }

    private void applyLogoGlow() {
        if (imgLogo == null) return;

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#D4A64A"));
        glow.setRadius(22);
        glow.setSpread(0.22);

        imgLogo.setEffect(glow);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(1400), imgLogo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.045);
        pulse.setToY(1.045);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.play();
    }

    private void applyLoginButtonAnimation() {
        if (btnLogin == null) return;

        DropShadow normalGlow = new DropShadow();
        normalGlow.setColor(Color.web("#B98224"));
        normalGlow.setRadius(10);
        normalGlow.setSpread(0.10);

        DropShadow hoverGlow = new DropShadow();
        hoverGlow.setColor(Color.web("#FFD47A"));
        hoverGlow.setRadius(24);
        hoverGlow.setSpread(0.28);

        btnLogin.setEffect(normalGlow);

        btnLogin.setOnMouseEntered(event -> {
            btnLogin.setEffect(hoverGlow);

            ScaleTransition scale = new ScaleTransition(Duration.millis(170), btnLogin);
            scale.setToX(1.035);
            scale.setToY(1.035);
            scale.play();
        });

        btnLogin.setOnMouseExited(event -> {
            btnLogin.setEffect(normalGlow);

            ScaleTransition scale = new ScaleTransition(Duration.millis(170), btnLogin);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = getEnteredPassword().trim();

        if (username.isEmpty() && password.isEmpty()) {
            showLoginError("Please enter your username and password.");
            return;
        }

        if (username.isEmpty()) {
            showLoginError("Please enter your username.");
            return;
        }

        if (password.isEmpty()) {
            showLoginError("Please enter your password.");
            return;
        }

        String sql = "SELECT user_id, username, password, role FROM users WHERE username = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                showLoginError("Database connection failed.");
                return;
            }

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String dbUsername = rs.getString("username");
                    String dbPasswordHash = rs.getString("password");
                    String dbRole = rs.getString("role");

                    if (PasswordUtil.verifyPassword(password, dbPasswordHash)) {
                        UserSession.startSession(userId, dbUsername, dbRole);

                        hideLoginError();
                        clearPasswordFields();

                        App.setRoot("Welcome", 1100, 720);
                    } else {
                        showLoginError("Invalid username or password.");
                        clearPasswordFields();
                    }
                } else {
                    showLoginError("Invalid username or password.");
                    clearPasswordFields();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showLoginError("Failed to open dashboard.");
        } catch (Exception e) {
            e.printStackTrace();
            showLoginError("Login failed. Please try again.");
        }
    }

    @FXML
    public void handleTogglePassword() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);

            txtPassword.setVisible(false);
            txtPassword.setManaged(false);

            if (btnTogglePassword != null) {
                btnTogglePassword.setText("🙈");
            }
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);

            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);

            if (btnTogglePassword != null) {
                btnTogglePassword.setText("⊙");
            }
        }
    }

    @FXML
    public void handleForgotPassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Password Reset Request");
        dialog.setHeaderText("Forgot your password?");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #0B132B;" +
                "-fx-border-color: #D4A64A;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        ButtonType sendButtonType = new ButtonType("Send Request", ButtonType.OK.getButtonData());
        dialogPane.getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setStyle("-fx-padding: 20; -fx-background-color: #0B132B;");

        Label lblInfo = new Label("Enter your username and we will send a reset request to the admin.");
        lblInfo.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        TextField txtResetUsername = new TextField();
        txtResetUsername.setPromptText("Enter your username");
        txtResetUsername.setStyle(
                "-fx-background-color: #1A274F;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #A0A0A0;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;"
        );

        grid.add(lblInfo, 0, 0);
        grid.add(txtResetUsername, 0, 1);

        dialogPane.setContent(grid);

        dialogPane.lookupButton(sendButtonType).setStyle(
                "-fx-background-color: #D4A64A;" +
                "-fx-text-fill: #06101F;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;"
        );

        dialogPane.lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: #D4A64A;" +
                "-fx-text-fill: #D4A64A;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        var result = dialog.showAndWait();

        if (result.isPresent() && result.get() == sendButtonType) {
            String username = txtResetUsername.getText().trim();

            if (username.isEmpty()) {
                showFancyInfoPopup("Missing Username", "Please enter your username first.", false);
                return;
            }

            String checkSql = "SELECT user_id FROM users WHERE username = ?";
            String updateSql = """
                UPDATE users
                SET reset_requested = TRUE,
                    reset_request_time = NOW()
                WHERE username = ?
            """;

            try (Connection conn = DBConnection.connect()) {
                if (conn == null) {
                    showFancyInfoPopup("Connection Error", "Database connection failed. Please try again later.", false);
                    return;
                }

                boolean userExists = false;

                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, username);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        userExists = rs.next();
                    }
                }

                if (!userExists) {
                    showFancyInfoPopup("User Not Found", "No account was found with this username.", false);
                    return;
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, username);
                    updateStmt.executeUpdate();
                }

                showFancyInfoPopup(
                        "Request Sent",
                        "Your password reset request has been sent to the admin successfully.",
                        true
                );

            } catch (Exception e) {
                e.printStackTrace();
                showFancyInfoPopup("Request Failed", "Something went wrong while sending the reset request.", false);
            }
        }
    }

    private void showFancyInfoPopup(String title, String message, boolean success) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(success ? "Request Submitted" : "Notice");
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: #0B132B;" +
                "-fx-border-color: #D4A64A;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 14;" +
                "-fx-background-radius: 14;"
        );

        dialogPane.lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #D4A64A;" +
                "-fx-text-fill: #06101F;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 18 8 18;"
        );

        dialogPane.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .forEach(label -> label.setStyle("-fx-text-fill: white; -fx-font-size: 13px;"));

        alert.showAndWait();
    }

    private String getEnteredPassword() {
        if (passwordVisible && txtPasswordVisible != null) {
            return txtPasswordVisible.getText();
        }
        return txtPassword != null ? txtPassword.getText() : "";
    }

    private void clearPasswordFields() {
        if (txtPassword != null) {
            txtPassword.clear();
        }

        if (txtPasswordVisible != null) {
            txtPasswordVisible.clear();
        }
    }

    private void showLoginError(String message) {
        if (lblLoginError != null) {
            lblLoginError.setText(message);
            lblLoginError.setVisible(true);
        }

        playShakeAnimation();
    }

    private void hideLoginError() {
        if (lblLoginError != null) {
            lblLoginError.setVisible(false);
        }
    }

    private void playShakeAnimation() {
        if (loginCard == null) return;

        TranslateTransition shake1 = new TranslateTransition(Duration.millis(60), loginCard);
        shake1.setFromX(0);
        shake1.setToX(-8);

        TranslateTransition shake2 = new TranslateTransition(Duration.millis(60), loginCard);
        shake2.setFromX(-8);
        shake2.setToX(8);

        TranslateTransition shake3 = new TranslateTransition(Duration.millis(60), loginCard);
        shake3.setFromX(8);
        shake3.setToX(-5);

        TranslateTransition shake4 = new TranslateTransition(Duration.millis(60), loginCard);
        shake4.setFromX(-5);
        shake4.setToX(0);

        shake1.setOnFinished(e -> shake2.play());
        shake2.setOnFinished(e -> shake3.play());
        shake3.setOnFinished(e -> shake4.play());

        shake1.play();
    }
}