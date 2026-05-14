package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.AuthorizationService;
import com.veloura.security.UserSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label lblTotalProducts;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblSalesProcessing;
    @FXML private Label lblTotalSales;

    @FXML private Label lblCurrentUser;
    @FXML private Label lblTodayDate;
    @FXML private Label lblWelcomeTitle;
    @FXML private Label lblSubtitle;

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCustomers;
    @FXML private Button btnSales;
    @FXML private Button btnInventory;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;
    @FXML private Button btnProductStats;

    @FXML private Pane cardProducts;
    @FXML private Pane cardCustomers;
    @FXML private Pane cardSalesProcessing;
    @FXML private Pane cardTotalSales;

    @FXML
    public void initialize() {
        loadDashboardStats();
        loadUserInfo();
        applyPermissions();
        playDashboardAnimations();
    }

    private void loadUserInfo() {
        lblCurrentUser.setText("User: " + UserSession.getUsername());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        lblTodayDate.setText("Today: " + LocalDate.now().format(formatter));
    }

    private void applyPermissions() {
        if (btnDashboard != null) {
            boolean allowed = AuthorizationService.canViewDashboard();
            btnDashboard.setVisible(allowed);
            btnDashboard.setManaged(allowed);
        }

        if (btnProducts != null) {
            boolean allowed = AuthorizationService.canManageProducts();
            btnProducts.setVisible(allowed);
            btnProducts.setManaged(allowed);
        }

        if (btnCustomers != null) {
            boolean allowed = AuthorizationService.canViewCustomers();
            btnCustomers.setVisible(allowed);
            btnCustomers.setManaged(allowed);
        }

        if (btnSales != null) {
            boolean allowed = AuthorizationService.canViewSales();
            btnSales.setVisible(allowed);
            btnSales.setManaged(allowed);
        }

        if (btnInventory != null) {
            boolean allowed = AuthorizationService.canViewInventory();
            btnInventory.setVisible(allowed);
            btnInventory.setManaged(allowed);
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
    }

    private void loadDashboardStats() {
        String totalProductsQuery = "SELECT COUNT(*) AS total FROM products";
        String totalCustomersQuery = "SELECT COUNT(*) AS total FROM customers";
        String salesProcessingQuery = "SELECT COUNT(*) AS total FROM sales";
        String totalSalesQuery = "SELECT COALESCE(SUM(total_amount),0) AS total FROM sales";

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) return;

            try (PreparedStatement pst = conn.prepareStatement(totalProductsQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblTotalProducts.setText(rs.getString("total"));
            }

            try (PreparedStatement pst = conn.prepareStatement(totalCustomersQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblTotalCustomers.setText(rs.getString("total"));
            }

            try (PreparedStatement pst = conn.prepareStatement(salesProcessingQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblSalesProcessing.setText(rs.getString("total"));
            }

            try (PreparedStatement pst = conn.prepareStatement(totalSalesQuery);
                 ResultSet rs = pst.executeQuery()) {
                if (rs.next()) lblTotalSales.setText("$" + String.format("%.1f", rs.getDouble("total")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playDashboardAnimations() {
        if (lblWelcomeTitle != null) {
            lblWelcomeTitle.setOpacity(0);
            lblWelcomeTitle.setTranslateY(-25);

            FadeTransition fade = new FadeTransition(Duration.millis(900), lblWelcomeTitle);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(900), lblWelcomeTitle);
            slide.setFromY(-25);
            slide.setToY(0);

            fade.play();
            slide.play();
        }

        if (lblSubtitle != null) {
            lblSubtitle.setOpacity(0);

            FadeTransition fade = new FadeTransition(Duration.millis(1200), lblSubtitle);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(250));
            fade.play();
        }

        animateCard(cardProducts, 0);
        animateCard(cardCustomers, 180);
        animateCard(cardSalesProcessing, 360);
        animateCard(cardTotalSales, 540);

        animateSidebarButton(btnDashboard);
        animateSidebarButton(btnProducts);
        animateSidebarButton(btnCustomers);
        animateSidebarButton(btnSales);
        animateSidebarButton(btnInventory);
        animateSidebarButton(btnUsers);
        animateSidebarButton(btnProductStats);
        animateSidebarButton(btnLogout);

        pulseCard(cardProducts);
        pulseCard(cardCustomers);
        pulseCard(cardSalesProcessing);
        pulseCard(cardTotalSales);
    }

    private void animateCard(Pane card, int delay) {
        if (card == null) return;

        card.setOpacity(0);
        card.setTranslateY(35);

        FadeTransition fade = new FadeTransition(Duration.millis(750), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide = new TranslateTransition(Duration.millis(750), card);
        slide.setFromY(35);
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

    private void pulseCard(Pane card) {
        if (card == null) return;

        String originalStyle = card.getStyle();

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.2), card);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.012);
        pulse.setToY(1.012);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        card.setOnMouseEntered(e ->
                card.setStyle(originalStyle + "-fx-effect:dropshadow(gaussian,#D4A64A,25,0.45,0,0);")
        );

        card.setOnMouseExited(e -> card.setStyle(originalStyle));
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
    public void goToSales() {
        if (!AuthorizationService.canViewSales()) return;

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
            App.setRoot("Inventory", 1006, 710);
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
    public void goToProductStats() {
        if (!AuthorizationService.canViewStatistics()) return;

        try {
            App.setRoot("ProductStats", 1006, 710);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
public void handleLogout() {

    try {

        UserSession.clearSession();

        App.setRoot(
                "LogoutWelcome",
                1200,
                720
        );

    } catch (IOException e) {

        e.printStackTrace();
    }
}
}