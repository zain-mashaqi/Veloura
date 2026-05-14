package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.AuthorizationService;
import com.veloura.security.UserSession;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductStatsController {

    @FXML private Label lblTotalSoldItems;
    @FXML private Label lblBestProduct;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalLegend;

    @FXML private PieChart productPieChart;
    @FXML private LineChart<String, Number> revenueLineChart;

    @FXML private VBox legendBox;

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCustomers;
    @FXML private Button btnSales;
    @FXML private Button btnInventory;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;
    @FXML private Button btnRefresh;
    @FXML private Button btnProductStats;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat percentFormat = new DecimalFormat("0.0");

    private final String[] colors = {
            "#D4A64A",
            "#1E6FD9",
            "#6548C7",
            "#20B7B4",
            "#CF4B7D",
            "#37A56B",
            "#44A3E8",
            "#F2C94C",
            "#9B59B6",
            "#E67E22"
    };

    @FXML
    public void initialize() {
        if (!AuthorizationService.canViewStatistics()) {
            try {
                App.setRoot("Dashboard", 980, 640);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        loadStatistics();
        applyPermissions();
        playStatsAnimations();
    }

    private void applyPermissions() {
        setButtonVisible(btnDashboard, AuthorizationService.canViewDashboard());
        setButtonVisible(btnProducts, AuthorizationService.canManageProducts());
        setButtonVisible(btnCustomers, AuthorizationService.canViewCustomers());
        setButtonVisible(btnSales, AuthorizationService.canViewSales());
        setButtonVisible(btnInventory, AuthorizationService.canViewInventory());
        setButtonVisible(btnUsers, AuthorizationService.canManageUsers());
        setButtonVisible(btnProductStats, AuthorizationService.canViewStatistics());
    }

    private void setButtonVisible(Button button, boolean allowed) {
        if (button == null) return;

        button.setVisible(allowed);
        button.setManaged(allowed);
    }

    private void playStatsAnimations() {
        animateNode(lblTotalSoldItems, 0);
        animateNode(lblBestProduct, 150);
        animateNode(lblTotalRevenue, 300);
        animateNode(lblTotalLegend, 450);

        animateNode(revenueLineChart, 500);
        animateNode(productPieChart, 650);
        animateNode(legendBox, 780);

        animateSidebarButton(btnDashboard);
        animateSidebarButton(btnProducts);
        animateSidebarButton(btnCustomers);
        animateSidebarButton(btnSales);
        animateSidebarButton(btnInventory);
        animateSidebarButton(btnUsers);
        animateSidebarButton(btnLogout);
        animateSidebarButton(btnRefresh);

        pulseNode(productPieChart);
        pulseNode(revenueLineChart);
    }

    private void animateNode(Node node, int delay) {
        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(850), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide = new TranslateTransition(Duration.millis(850), node);
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

            btn.setStyle(originalStyle +
                    "-fx-effect:dropshadow(gaussian,#D4A64A,18,0.35,0,0);");
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

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.5), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.012);
        pulse.setToY(1.012);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    @FXML
    private void loadStatistics() {
        if (!AuthorizationService.canViewStatistics()) {
            return;
        }

        List<ProductRow> rows = loadProductRows();

        int totalQty = rows.stream()
                .mapToInt(ProductRow::qty)
                .sum();

        double totalRevenue = getTotalRevenue();

        loadRevenueLineChart();

        lblTotalSoldItems.setText(String.valueOf(totalQty));
        lblTotalRevenue.setText(moneyFormat.format(totalRevenue));

        if (rows.isEmpty()) {
            lblBestProduct.setText("-");
            lblTotalLegend.setText("0 (100%)");

            productPieChart.setData(
                    FXCollections.observableArrayList(
                            new PieChart.Data("No Sales", 1)
                    )
            );

            legendBox.getChildren().clear();

            Platform.runLater(() -> {
                applyChartColors(productPieChart.getData());
                removePieChartBackground();
            });

            return;
        }

        lblBestProduct.setText(rows.get(0).name());
        lblTotalLegend.setText(totalQty + " (100%)");

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

        for (ProductRow row : rows) {
            double pct = totalQty == 0 ? 0 : row.qty() * 100.0 / totalQty;

            chartData.add(
                    new PieChart.Data(
                            row.name() + "\n" + row.qty() + " (" + percentFormat.format(pct) + "%)",
                            row.qty()
                    )
            );
        }

        productPieChart.setData(chartData);
        productPieChart.setLabelsVisible(true);
        productPieChart.setLegendVisible(false);
        productPieChart.setStartAngle(90);
        productPieChart.setClockwise(true);

        buildLegend(rows, totalQty);

        Platform.runLater(() -> {
            applyChartColors(chartData);
            removePieChartBackground();
        });
    }

    private void loadRevenueLineChart() {
        if (revenueLineChart == null) return;

        revenueLineChart.getData().clear();

        String sql = """
            SELECT 
                DATE_FORMAT(sale_date, '%b') AS month_name,
                MONTH(sale_date) AS month_number,
                COALESCE(SUM(total_amount), 0) AS revenue
            FROM sales
            GROUP BY MONTH(sale_date), DATE_FORMAT(sale_date, '%b')
            ORDER BY month_number
        """;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                series.getData().add(
                        new XYChart.Data<>(
                                rs.getString("month_name"),
                                rs.getDouble("revenue")
                        )
                );
            }

            revenueLineChart.getData().add(series);
            revenueLineChart.setLegendVisible(false);
            revenueLineChart.setAnimated(true);

            Platform.runLater(() -> {
                revenueLineChart.lookupAll(".chart-plot-background").forEach(n ->
                        n.setStyle("-fx-background-color: transparent;")
                );

                revenueLineChart.lookupAll(".chart").forEach(n ->
                        n.setStyle("-fx-background-color: transparent;")
                );

                revenueLineChart.lookupAll(".chart-content").forEach(n ->
                        n.setStyle("-fx-background-color: transparent;")
                );

                revenueLineChart.lookupAll(".axis").forEach(n ->
                        n.setStyle("-fx-tick-label-fill: #F8FAFC;")
                );

                revenueLineChart.lookupAll(".chart-series-line").forEach(n ->
                        n.setStyle("-fx-stroke: #D4A64A; -fx-stroke-width: 3px;")
                );

                revenueLineChart.lookupAll(".chart-line-symbol").forEach(n ->
                        n.setStyle("-fx-background-color: #D4A64A, #06101F;")
                );
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ProductRow> loadProductRows() {
        List<ProductRow> rows = new ArrayList<>();

        String sql = """
            SELECT p.name AS product_name,
                   COALESCE(SUM(si.qty),0) AS sold_qty
            FROM sale_items si
            JOIN product_variants pv ON si.variant_id = pv.variant_id
            JOIN products p ON pv.product_id = p.product_id
            GROUP BY p.product_id, p.name
            HAVING sold_qty > 0
            ORDER BY sold_qty DESC
        """;

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new ProductRow(
                        rs.getString("product_name"),
                        rs.getInt("sold_qty")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    private double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM sales";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    private void buildLegend(List<ProductRow> rows, int totalQty) {
        legendBox.getChildren().clear();

        for (int i = 0; i < rows.size(); i++) {
            ProductRow row = rows.get(i);

            double pct = totalQty == 0 ? 0 : row.qty() * 100.0 / totalQty;

            Pane colorBox = new Pane();
            colorBox.setPrefSize(14, 14);
            colorBox.setStyle(
                    "-fx-background-color:" + colors[i % colors.length] + ";" +
                    "-fx-background-radius:4;"
            );

            Label name = new Label(row.name());
            name.setStyle("-fx-text-fill:white;-fx-font-size:13;");

            Label value = new Label(row.qty() + " (" + percentFormat.format(pct) + "%)");
            value.setStyle("-fx-text-fill:#D4A64A;-fx-font-size:13;-fx-font-weight:bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox item = new HBox(10, colorBox, name, spacer, value);

            item.setOpacity(0);

            FadeTransition fade = new FadeTransition(Duration.millis(700), item);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(i * 120));
            fade.play();

            legendBox.getChildren().add(item);
        }
    }

    private void applyChartColors(ObservableList<PieChart.Data> data) {
        for (int i = 0; i < data.size(); i++) {
            PieChart.Data d = data.get(i);

            if (d.getNode() != null) {
                d.getNode().setStyle(
                        "-fx-pie-color:" + colors[i % colors.length] + ";"
                );
            }
        }
    }

    private void removePieChartBackground() {
        if (productPieChart == null) return;

        productPieChart.lookupAll(".chart").forEach(n ->
                n.setStyle("-fx-background-color:transparent;")
        );

        productPieChart.lookupAll(".chart-content").forEach(n ->
                n.setStyle("-fx-background-color:transparent;")
        );
    }

    @FXML
    private void goToDashboard() {
        open("Dashboard", 980, 640);
    }

    @FXML
    private void goToProducts() {
        if (AuthorizationService.canManageProducts()) {
            open("Product", 980, 840);
        }
    }

    @FXML
    private void goToCustomers() {
        if (AuthorizationService.canViewCustomers()) {
            open("Customers", 980, 640);
        }
    }

    @FXML
    private void goToSales() {
        if (AuthorizationService.canViewSales()) {
            open("Sales", 1006, 710);
        }
    }

    @FXML
    private void goToInventory() {
        if (AuthorizationService.canViewInventory()) {
            open("Inventory", 1006, 710);
        }
    }

    @FXML
    private void goToUsers() {
        if (AuthorizationService.canManageUsers()) {
            open("Users", 1100, 720);
        }
    }

    @FXML
    private void goToProductStats() {
        if (AuthorizationService.canViewStatistics()) {
            open("ProductStats", 1006, 710);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.clearSession();
            App.setRoot("Login", 600, 400);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void open(String fxml, double width, double height) {
        try {
            App.setRoot(fxml, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private record ProductRow(String name, int qty) {}
}