package com.veloura.controller;

import com.mycompany.velourafx.App;
import com.veloura.database.DBConnection;
import com.veloura.security.AuthorizationService;
import com.veloura.security.UserSession;
import com.veloura.util.ToastNotification;
import com.veloura.util.ToastNotification.ToastType;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ProductController {

    @FXML private TextField txtProductName;
    @FXML private TextField txtDescription;
    @FXML private ComboBox<String> cmbBrand;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbAudience;

    @FXML private TextField txtVariantSize;
    @FXML private TextField txtVariantColor;
    @FXML private TextField txtVariantBarcode;
    @FXML private TextField txtVariantPrice;
    @FXML private TextField txtVariantQty;

    @FXML private TextField txtSearchVariant;
    @FXML private ComboBox<String> cmbColorFilter;
    @FXML private ComboBox<String> cmbSizeFilter;
    @FXML private Label lblPaginationInfo;

    @FXML private TableView<ProductVariant> tblVariants;
    @FXML private TableColumn<ProductVariant, Integer> colVariantNumber;
    @FXML private TableColumn<ProductVariant, String> colVariantBarcode;
    @FXML private TableColumn<ProductVariant, String> colProductName;
    @FXML private TableColumn<ProductVariant, String> colVariantSize;
    @FXML private TableColumn<ProductVariant, String> colVariantColor;
    @FXML private TableColumn<ProductVariant, Double> colVariantPrice;
    @FXML private TableColumn<ProductVariant, Integer> colVariantQuantity;
    @FXML private TableColumn<ProductVariant, String> colVariantActions;

    @FXML private Button btnDashboard;
    @FXML private Button btnProducts;
    @FXML private Button btnCustomers;
    @FXML private Button btnSales;
    @FXML private Button btnInventory;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;
    @FXML private Button btnAddProduct;
    @FXML private Button btnAddVariant;
    @FXML private Button btnDeleteSelected;
    @FXML private Button btnProductStats;

    @FXML private Label lblPageTitle;
    @FXML private Pane productsTitleLine;
    @FXML private Pane productDetailsPanel;
    @FXML private Pane productVariantsPanel;
    @FXML private VBox sidebarMenu;

    @FXML private Pane smartSuggestionsPanel;
    @FXML private Label lblSmartTitle;
    @FXML private Label lblSuggestedBrands;
    @FXML private Label lblCompleteLook;

    private int currentProductId = -1;

    @FXML
    public void initialize() {
        System.out.println("ProductController Loaded");
        System.out.println("Logged in user: " + UserSession.getUsername());
        System.out.println("Role: " + UserSession.getRole());

        if (!AuthorizationService.canManageProducts()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "You are not allowed to access Products.");
            try {
                App.setRoot("Dashboard", 980, 640);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        applyPermissions();
        loadBrands();
        loadCategories();

        if (cmbAudience != null) {
            cmbAudience.getItems().addAll("Men", "Women", "Kids");
        }

        if (cmbColorFilter != null) {
            cmbColorFilter.getItems().addAll("All", "Black", "White", "Red", "Blue");
            cmbColorFilter.setValue("All");
        }

        if (cmbSizeFilter != null) {
            cmbSizeFilter.getItems().addAll("All", "XS", "S", "M", "L", "XL");
            cmbSizeFilter.setValue("All");
        }

        setupVariantTable();
        loadVariants();
        setupLiveSearch();
        setupAutoBarcodeGenerator();
        setupSmartSuggestions();
        playProductsAnimations();
    }

    private void setupVariantTable() {
        if (colVariantNumber != null) {
            colVariantNumber.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getNumber()));
            styleColumn(colVariantNumber);
        }

        if (colVariantBarcode != null) {
            colVariantBarcode.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getBarcode()));
            styleColumn(colVariantBarcode);
        }

        if (colProductName != null) {
            colProductName.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
            styleColumn(colProductName);
        }

        if (colVariantSize != null) {
            colVariantSize.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getSize()));
            styleColumn(colVariantSize);
        }

        if (colVariantColor != null) {
            colVariantColor.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getColor()));
            styleColumn(colVariantColor);
        }

        if (colVariantPrice != null) {
            colVariantPrice.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice()));
            styleColumn(colVariantPrice);
        }

        if (colVariantQuantity != null) {
            colVariantQuantity.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getCurrentQty()));
            styleQuantityColumn();
        }

        if (colVariantActions != null) {
            colVariantActions.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getActions()));
            styleColumn(colVariantActions);
        }
    }

    private void loadVariants() {
        if (tblVariants == null) return;

        ObservableList<ProductVariant> variantList = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                pv.size,
                pv.color,
                pv.barcode,
                pv.price,
                pv.current_qty,
                p.name AS product_name
            FROM product_variants pv
            JOIN products p ON pv.product_id = p.product_id
        """);

        String selectedColor = cmbColorFilter != null ? cmbColorFilter.getValue() : null;
        String selectedSize = cmbSizeFilter != null ? cmbSizeFilter.getValue() : null;
        String searchText = txtSearchVariant != null ? txtSearchVariant.getText() : null;

        boolean hasWhere = false;

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" WHERE (pv.size LIKE ? OR pv.color LIKE ? OR pv.barcode LIKE ? OR p.name LIKE ?)");
            hasWhere = true;
        }

        if (selectedColor != null && !selectedColor.equals("All") && !selectedColor.trim().isEmpty()) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" pv.color = ?");
            hasWhere = true;
        }

        if (selectedSize != null && !selectedSize.equals("All") && !selectedSize.trim().isEmpty()) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" pv.size = ?");
        }

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (searchText != null && !searchText.trim().isEmpty()) {
                String pattern = "%" + searchText.trim() + "%";
                stmt.setString(index++, pattern);
                stmt.setString(index++, pattern);
                stmt.setString(index++, pattern);
                stmt.setString(index++, pattern);
            }

            if (selectedColor != null && !selectedColor.equals("All") && !selectedColor.trim().isEmpty()) {
                stmt.setString(index++, selectedColor);
            }

            if (selectedSize != null && !selectedSize.equals("All") && !selectedSize.trim().isEmpty()) {
                stmt.setString(index++, selectedSize);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                int counter = 1;

                while (rs.next()) {
                    ProductVariant variant = new ProductVariant(
                            counter++,
                            rs.getString("product_name"),
                            rs.getString("size"),
                            rs.getString("color"),
                            rs.getString("barcode"),
                            rs.getDouble("price"),
                            rs.getInt("current_qty"),
                            "Edit/Delete"
                    );

                    variantList.add(variant);
                }
            }

            tblVariants.setItems(variantList);

            if (lblPaginationInfo != null) {
                if (variantList.isEmpty()) {
                    lblPaginationInfo.setText("0-0 of 0");
                } else {
                    lblPaginationInfo.setText("1-" + variantList.size() + " of " + variantList.size());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load variants: " + e.getMessage());
        }
    }

    private void setupSmartSuggestions() {
        if (txtProductName != null) {
            txtProductName.textProperty().addListener((obs, oldVal, newVal) -> updateSmartSuggestions());
        }

        if (cmbCategory != null) {
            cmbCategory.setOnAction(e -> updateSmartSuggestions());
        }

        updateSmartSuggestions();
    }

    private void updateSmartSuggestions() {
        String category = cmbCategory != null && cmbCategory.getValue() != null
                ? cmbCategory.getValue().toLowerCase()
                : "";

        String productName = txtProductName != null && txtProductName.getText() != null
                ? txtProductName.getText().toLowerCase()
                : "";

        String key = detectSmartCategory(category + " " + productName);

        switch (key) {
            case "CLOTHING" -> applySmartSuggestion(
                    "👗 Clothing",
                    "ZARA • H&M • MANGO • GUCCI • PRADA",
                    "👠 Heels • 👜 Bag • 🌸 Perfume • 💍 Necklace",
                    new String[]{"ZARA", "H&M", "MANGO", "GUCCI", "PRADA"}
            );
            case "SHOES" -> applySmartSuggestion(
                    "👠 Shoes",
                    "NIKE • ADIDAS • ALDO • PUMA • NEW BALANCE",
                    "👖 Jeans • 👜 Bag • ⌚ Watch • 🧥 Jacket",
                    new String[]{"NIKE", "ADIDAS", "ALDO", "PUMA", "NEW BALANCE"}
            );
            case "PERFUME" -> applySmartSuggestion(
                    "🌸 Perfume",
                    "DIOR • CHANEL • YSL • VERSACE • TOM FORD",
                    "⌚ Watch • 👔 Suit • 👗 Dress • 👜 Bag",
                    new String[]{"DIOR", "CHANEL", "YSL", "VERSACE", "TOM FORD"}
            );
            case "MAKEUP" -> applySmartSuggestion(
                    "💄 Makeup",
                    "DIOR • MAC • HUDA BEAUTY • MAYBELLINE • RARE BEAUTY",
                    "🌸 Perfume • 💍 Earrings • 👜 Bag • 💅 Nail Polish",
                    new String[]{"DIOR", "MAC", "HUDA BEAUTY", "MAYBELLINE", "RARE BEAUTY"}
            );
            case "ACCESSORIES" -> applySmartSuggestion(
                    "👜 Accessories",
                    "LOUIS VUITTON • GUCCI • ALDO • ROLEX • CARTIER",
                    "👗 Dress • 👠 Heels • 🌸 Perfume • ⌚ Watch",
                    new String[]{"LOUIS VUITTON", "GUCCI", "ALDO", "ROLEX", "CARTIER"}
            );
            default -> applySmartSuggestion(
                    "✨ Smart Suggestions",
                    "-",
                    "Select or type a category to get luxury suggestions.",
                    new String[]{}
            );
        }
    }

    private String detectSmartCategory(String text) {
        if (text == null) return "DEFAULT";
        text = text.toLowerCase();

        if (text.contains("clothing") || text.contains("clothes") || text.contains("dress")
                || text.contains("shirt") || text.contains("jeans") || text.contains("hoodie")
                || text.contains("jacket") || text.contains("skirt") || text.contains("pants")
                || text.contains("اواعي") || text.contains("ملابس") || text.contains("فستان")
                || text.contains("قميص") || text.contains("بنطال") || text.contains("جاكيت")) {
            return "CLOTHING";
        }

        if (text.contains("shoes") || text.contains("heels") || text.contains("sneaker")
                || text.contains("sneakers") || text.contains("boots") || text.contains("احذية")
                || text.contains("أحذية") || text.contains("حذاء") || text.contains("كعب")
                || text.contains("بوط")) {
            return "SHOES";
        }

        if (text.contains("perfume") || text.contains("fragrance")
                || text.contains("عطر") || text.contains("عطور")) {
            return "PERFUME";
        }

        if (text.contains("makeup") || text.contains("lipstick") || text.contains("foundation")
                || text.contains("mascara") || text.contains("مكياج") || text.contains("روج")
                || text.contains("فاونديشن") || text.contains("مسكارا")) {
            return "MAKEUP";
        }

        if (text.contains("accessories") || text.contains("accessory") || text.contains("bag")
                || text.contains("watch") || text.contains("necklace") || text.contains("ring")
                || text.contains("earrings") || text.contains("اكسسوار") || text.contains("اكسسوارات")
                || text.contains("شنطة") || text.contains("ساعة") || text.contains("عقد")
                || text.contains("خاتم")) {
            return "ACCESSORIES";
        }

        return "DEFAULT";
    }

    private void applySmartSuggestion(String title,
                                      String brands,
                                      String completeLook,
                                      String[] suggestedBrands) {

        if (lblSmartTitle != null) lblSmartTitle.setText(title);
        if (lblSuggestedBrands != null) lblSuggestedBrands.setText("Brands: " + brands);
        if (lblCompleteLook != null) lblCompleteLook.setText("Complete with: " + completeLook);

        addSuggestedBrandsToCombo(suggestedBrands);
        animateSmartSuggestions();
    }

    private void addSuggestedBrandsToCombo(String[] brands) {
        if (cmbBrand == null || brands == null) return;

        for (String brand : brands) {
            if (brand != null && !brand.isBlank() && !cmbBrand.getItems().contains(brand)) {
                cmbBrand.getItems().add(brand);
            }
        }
    }

    private void animateSmartSuggestions() {
        if (smartSuggestionsPanel == null) return;

        ScaleTransition scale = new ScaleTransition(Duration.millis(250), smartSuggestionsPanel);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.025);
        scale.setToY(1.025);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    private void playProductsAnimations() {
        animateTitle();
        animatePanel(productDetailsPanel, 0);
        animatePanel(productVariantsPanel, 220);

        animateButton(btnDashboard);
        animateButton(btnProducts);
        animateButton(btnCustomers);
        animateButton(btnSales);
        animateButton(btnInventory);
        animateButton(btnUsers);
        animateButton(btnLogout);
        animateButton(btnAddProduct);
        animateButton(btnAddVariant);
        animateButton(btnDeleteSelected);

        pulsePanel(productDetailsPanel);
        pulsePanel(productVariantsPanel);
    }

    private void animateTitle() {
        if (lblPageTitle != null) {
            lblPageTitle.setOpacity(0);
            lblPageTitle.setTranslateY(-25);

            FadeTransition fade = new FadeTransition(Duration.millis(900), lblPageTitle);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(900), lblPageTitle);
            slide.setFromY(-25);
            slide.setToY(0);

            fade.play();
            slide.play();
        }

        if (productsTitleLine != null) {
            productsTitleLine.setScaleX(0);

            ScaleTransition lineScale = new ScaleTransition(Duration.millis(900), productsTitleLine);
            lineScale.setFromX(0);
            lineScale.setToX(1);
            lineScale.setDelay(Duration.millis(180));
            lineScale.play();
        }
    }

    private void animatePanel(Pane panel, int delay) {
        if (panel == null) return;

        panel.setOpacity(0);
        panel.setTranslateY(35);

        FadeTransition fade = new FadeTransition(Duration.millis(750), panel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));

        TranslateTransition slide = new TranslateTransition(Duration.millis(750), panel);
        slide.setFromY(35);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delay));

        fade.play();
        slide.play();
    }

    private void pulsePanel(Pane panel) {
        if (panel == null) return;

        String originalStyle = panel.getStyle();

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.5), panel);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.006);
        pulse.setToY(1.006);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        panel.setOnMouseEntered(e ->
                panel.setStyle(originalStyle + "-fx-effect:dropshadow(gaussian,#D4A64A,24,0.38,0,0);")
        );

        panel.setOnMouseExited(e ->
                panel.setStyle(originalStyle)
        );
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

        if (btnAddProduct != null) {
            boolean allowed = AuthorizationService.canManageProducts();
            btnAddProduct.setVisible(allowed);
            btnAddProduct.setManaged(allowed);
        }

        if (btnAddVariant != null) {
            boolean allowed = AuthorizationService.canManageProducts();
            btnAddVariant.setVisible(allowed);
            btnAddVariant.setManaged(allowed);
        }

        if (btnDeleteSelected != null) {
            boolean allowed = AuthorizationService.hasRole("ADMIN");
            btnDeleteSelected.setVisible(allowed);
            btnDeleteSelected.setManaged(allowed);
        }
    }

    private void loadBrands() {
        if (cmbBrand == null) return;

        cmbBrand.getItems().clear();

        String sql = "SELECT brand_name FROM brands ORDER BY brand_name";

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cmbBrand.getItems().add(rs.getString("brand_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load brands: " + e.getMessage());
        }
    }

    private void loadCategories() {
        if (cmbCategory == null) return;

        cmbCategory.getItems().clear();

        String sql = "SELECT category_name FROM categories ORDER BY category_name";

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cmbCategory.getItems().add(rs.getString("category_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private <T> void styleColumn(TableColumn<ProductVariant, T> column) {
        column.setCellFactory(col -> new TableCell<ProductVariant, T>() {
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

    private void styleQuantityColumn() {
        if (colVariantQuantity == null) return;

        colVariantQuantity.setCellFactory(col -> new TableCell<ProductVariant, Integer>() {
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

                if (qty <= 3) {
                    setStyle("""
                            -fx-alignment: CENTER;
                            -fx-background-color: rgba(180,30,30,0.50);
                            -fx-font-weight: bold;
                            """);
                } else if (qty <= 10) {
                    setStyle("""
                            -fx-alignment: CENTER;
                            -fx-background-color: rgba(212,166,74,0.28);
                            -fx-font-weight: bold;
                            """);
                } else {
                    setStyle("""
                            -fx-alignment: CENTER;
                            -fx-background-color: transparent;
                            """);
                }
            }
        });
    }

    private void setupLiveSearch() {
        if (txtSearchVariant != null) {
            txtSearchVariant.textProperty().addListener((obs, oldVal, newVal) -> loadVariants());
        }

        if (cmbColorFilter != null) {
            cmbColorFilter.setOnAction(e -> loadVariants());
        }

        if (cmbSizeFilter != null) {
            cmbSizeFilter.setOnAction(e -> loadVariants());
        }
    }

    private void setupAutoBarcodeGenerator() {
        if (txtVariantBarcode != null) {
            txtVariantBarcode.focusedProperty().addListener((obs, oldVal, focused) -> {
                if (focused && txtVariantBarcode.getText().trim().isEmpty()) {
                    txtVariantBarcode.setText(generateVelouraBarcode());
                }
            });
        }
    }

    private String generateVelouraBarcode() {
        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "VEL-" + random;
    }

    private void showToast(String message, ToastType type) {
        try {
            Stage stage = null;

            if (btnAddVariant != null && btnAddVariant.getScene() != null) {
                stage = (Stage) btnAddVariant.getScene().getWindow();
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
            App.setRoot("Customers", 980, 840);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToSales() {
        try {
            App.setRoot("Sales", 980, 840);
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
    private void goToProductStats() throws IOException {
        App.setRoot("ProductStats", 1100, 700);
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
            App.setRoot("LogoutWelcome", 1200, 720);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddProduct() {
        if (!AuthorizationService.canManageProducts()) {
            showToast("Access denied. You are not allowed to add products.", ToastType.ERROR);
            return;
        }

        String name = txtProductName.getText();
        String desc = txtDescription.getText();
        String brand = cmbBrand.getValue();
        String category = cmbCategory.getValue();
        String audience = cmbAudience.getValue();

        name = name == null ? "" : name.trim();
        desc = desc == null ? "" : desc.trim();

        if (name.isEmpty()) {
            showToast("Product name is required.", ToastType.WARNING);
            return;
        }

        if (name.length() < 2) {
            showToast("Product name must be at least 2 characters.", ToastType.WARNING);
            return;
        }

        if (name.length() > 100) {
            showToast("Product name is too long.", ToastType.WARNING);
            return;
        }

        if (desc.length() > 500) {
            showToast("Description is too long.", ToastType.WARNING);
            return;
        }

        if (brand == null || brand.trim().isEmpty()) {
            showToast("Brand is required.", ToastType.WARNING);
            return;
        }

        if (category == null || category.trim().isEmpty()) {
            showToast("Category is required.", ToastType.WARNING);
            return;
        }

        String sql = """
                INSERT INTO products (name, description, brand, category, brand_id, category_id, audience)
                VALUES (
                    ?, ?, ?, ?,
                    (SELECT brand_id FROM brands WHERE brand_name = ? LIMIT 1),
                    (SELECT category_id FROM categories WHERE category_name = ? LIMIT 1),
                    ?
                )
                """;

        try (var conn = DBConnection.connect()) {
            if (conn == null) {
                showToast("Connection to database failed.", ToastType.ERROR);
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, desc);
                stmt.setString(3, brand);
                stmt.setString(4, category);
                stmt.setString(5, brand);
                stmt.setString(6, category);
                stmt.setString(7, audience);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            currentProductId = rs.getInt(1);
                        }
                    }

                    showToast("✔ Product saved successfully. ID = " + currentProductId, ToastType.SUCCESS);
                    clearProductFields();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to save product: " + e.getMessage(), ToastType.ERROR);
        }
    }

    @FXML
    public void handleAddVariant() {
        if (!AuthorizationService.canManageProducts()) {
            showToast("Access denied. You are not allowed to add variants.", ToastType.ERROR);
            return;
        }

        if (currentProductId == -1) {
            showToast("Please add a product first.", ToastType.WARNING);
            return;
        }

        String size = txtVariantSize.getText() == null ? "" : txtVariantSize.getText().trim();
        String color = txtVariantColor.getText() == null ? "" : txtVariantColor.getText().trim();
        String barcode = txtVariantBarcode.getText() == null ? "" : txtVariantBarcode.getText().trim();
        String priceText = txtVariantPrice.getText() == null ? "" : txtVariantPrice.getText().trim();
        String qtyText = txtVariantQty.getText() == null ? "" : txtVariantQty.getText().trim();

        if (size.isEmpty() || color.isEmpty() || barcode.isEmpty()
                || priceText.isEmpty() || qtyText.isEmpty()) {
            showToast("Please fill all variant fields.", ToastType.WARNING);
            return;
        }

        double price;
        int qty;

        try {
            price = Double.parseDouble(priceText);
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            showToast("Price must be a valid number and Qty must be an integer.", ToastType.WARNING);
            return;
        }

        if (price <= 0) {
            showToast("Price must be greater than 0.", ToastType.WARNING);
            return;
        }

        if (qty < 0) {
            showToast("Quantity cannot be negative.", ToastType.WARNING);
            return;
        }

        String checkBarcodeSql = "SELECT COUNT(*) FROM product_variants WHERE barcode = ?";

        String insertSql = """
                INSERT INTO product_variants
                (product_id, size, color, barcode, price, current_qty)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (var conn = DBConnection.connect()) {
            if (conn == null) {
                showToast("Connection to database failed.", ToastType.ERROR);
                return;
            }

            try (PreparedStatement checkStmt = conn.prepareStatement(checkBarcodeSql)) {
                checkStmt.setString(1, barcode);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showToast("⚠ This barcode already exists. Use a unique barcode.", ToastType.ERROR);
                        return;
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, currentProductId);
                stmt.setString(2, size);
                stmt.setString(3, color);
                stmt.setString(4, barcode);
                stmt.setDouble(5, price);
                stmt.setInt(6, qty);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    showToast("✔ Variant added successfully", ToastType.SUCCESS);
                    clearVariantFields();
                    loadVariants();
                } else {
                    showToast("Variant was not added.", ToastType.WARNING);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to add variant: " + e.getMessage(), ToastType.ERROR);
        }
    }

    @FXML
    public void handleDeleteSelected() {
        if (!AuthorizationService.hasRole("ADMIN")) {
            showToast("Only admin can delete variants.", ToastType.ERROR);
            return;
        }

        ProductVariant selected = tblVariants.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showToast("Please select a variant first.", ToastType.WARNING);
            return;
        }

        String sql = "DELETE FROM product_variants WHERE barcode = ?";

        try (var conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selected.getBarcode());
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showToast("✔ Variant deleted successfully", ToastType.SUCCESS);
                loadVariants();
            } else {
                showToast("No variant found to delete.", ToastType.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to delete variant: " + e.getMessage(), ToastType.ERROR);
        }
    }

    @FXML
    public void handleRefreshVariants() {
        loadVariants();
        showToast("Variants refreshed", ToastType.INFO);
    }

    @FXML
    public void handlePreviousPage() {
        System.out.println("Previous Page");
    }

    @FXML
    public void handleNextPage() {
        System.out.println("Next Page");
    }

    private void clearProductFields() {
        txtProductName.clear();
        txtDescription.clear();
        cmbBrand.setValue(null);
        cmbCategory.setValue(null);
        cmbAudience.setValue(null);
        updateSmartSuggestions();
    }

    private void clearVariantFields() {
        txtVariantSize.clear();
        txtVariantColor.clear();
        txtVariantBarcode.clear();
        txtVariantPrice.clear();
        txtVariantQty.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}