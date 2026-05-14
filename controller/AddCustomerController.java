package com.veloura.controller;

import com.veloura.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AddCustomerController {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtCity;
    @FXML private Label lblMessage;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private boolean saved = false;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-\\s]{7,20}$");

    @FXML
    public void initialize() {
        playAddCustomerAnimations();
    }

    private void playAddCustomerAnimations() {
        animateNode(txtName, 0);
        animateNode(txtEmail, 120);
        animateNode(txtPhone, 240);
        animateNode(txtCity, 360);
        animateNode(lblMessage, 480);

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
        String name = getText(txtName);
        String email = getText(txtEmail);
        String phone = getText(txtPhone);
        String city = getText(txtCity);

        if (!validateCustomer(name, email, phone, city)) {
            return;
        }

        String sql = """
            INSERT INTO customers (name, email, phone, city)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.connect()) {

            if (conn == null) {
                showMessage("Database connection failed.");
                return;
            }

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, name);
                pst.setString(2, email.isEmpty() ? null : email);
                pst.setString(3, phone.isEmpty() ? null : phone);
                pst.setString(4, city.isEmpty() ? null : city);

                pst.executeUpdate();
            }

            saved = true;
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Failed to save customer: " + e.getMessage());
        }
    }

    private boolean validateCustomer(String name,
                                     String email,
                                     String phone,
                                     String city) {

        if (name.isEmpty()) {
            showMessage("Customer name is required.");
            focus(txtName);
            return false;
        }

        if (name.length() < 2) {
            showMessage("Customer name must be at least 2 characters.");
            focus(txtName);
            return false;
        }

        if (name.length() > 100) {
            showMessage("Customer name is too long.");
            focus(txtName);
            return false;
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showMessage("Please enter a valid email address.");
            focus(txtEmail);
            return false;
        }

        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showMessage("Please enter a valid phone number.");
            focus(txtPhone);
            return false;
        }

        if (city.length() > 50) {
            showMessage("City name is too long.");
            focus(txtCity);
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private String getText(TextField field) {
        return field == null || field.getText() == null
                ? ""
                : field.getText().trim();
    }

    private void focus(TextField field) {
        if (field != null) {
            field.requestFocus();
        }
    }

    private void showMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}