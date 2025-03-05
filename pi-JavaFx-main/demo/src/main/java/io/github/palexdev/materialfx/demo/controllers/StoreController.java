package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.demo.model.Order;
import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.services.StoreService;
import io.github.palexdev.materialfx.demo.services.EmailService;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.scene.layout.Region;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StoreController implements Initializable {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private MFXComboBox<String> categoryFilter;

    private int userId;
    private Label totalAmountLabel;
    private javafx.scene.control.ScrollPane ordersScrollPane;
    private final StoreService storeService = new StoreService();
    private final EmailService emailService = new EmailService();

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("User ID set in StoreController: " + userId);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProductCardsFromDB();
        setupCategoryFilter();
    }

    private void loadProductCardsFromDB() {
        try {
            List<Product> products = storeService.getAllProducts();
            displayProducts(products);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayProducts(List<Product> products) {
        cardsContainer.getChildren().clear();
        for (Product product : products) {
            VBox card = createProductCard(product);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");

        Label nameLabel = new Label(product.getNameproduct());
        nameLabel.getStyleClass().add("header-label");

        Label priceLabel = new Label("$" + product.getPriceproduct());
        priceLabel.getStyleClass().add("price-label");

        // Quantity controls
        TextField quantityField = new TextField("1");
        quantityField.setPrefWidth(50);

        MFXButton addToBasketButton = new MFXButton("Add to Basket");
        addToBasketButton.getStyleClass().add("add-to-basket-button");
        addToBasketButton.setOnAction(event -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                storeService.addToBasket(userId, product.getId(), quantity);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to basket!");
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid quantity!");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product to basket: " + e.getMessage());
            }
        });

        HBox quantityBox = new HBox(10, new Label("Quantity:"), quantityField);
        quantityBox.getStyleClass().add("quantity-box");

        card.getChildren().addAll(nameLabel, priceLabel, quantityBox, addToBasketButton);
        return card;
    }

    @FXML
    private void filterProducts() {
        String searchQuery = searchField.getText().toLowerCase();
        String selectedCategory = categoryFilter.getValue();

        try {
            List<Product> filteredProducts = storeService.getAllProducts();

            // Apply search filter
            if (!searchQuery.isEmpty()) {
                filteredProducts = filteredProducts.stream()
                        .filter(product -> product.getNameproduct().toLowerCase().contains(searchQuery))
                        .collect(Collectors.toList());
            }

            // Apply category filter
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                filteredProducts = filteredProducts.stream()
                        .filter(product -> product.getCategory().equalsIgnoreCase(selectedCategory))
                        .collect(Collectors.toList());
            }

            displayProducts(filteredProducts);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to filter products: " + e.getMessage());
        }
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue(null);
        loadProductCardsFromDB();
    }

    private void setupCategoryFilter() {
        try {
            List<String> categories = storeService.getAllCategories();
            categoryFilter.getItems().addAll(categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 5; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0); " +
                     "-fx-background-radius: 5;");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        // Order ID and Date Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label orderIdLabel = new Label("#" + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label dateLabel = new Label(order.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dateLabel.setStyle("-fx-text-fill: #666666;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label(order.getStatus());
        statusLabel.setStyle("-fx-background-color: " + (order.getStatus().equals("Pending") ? "#FFF3CD" : "#D4EDDA") + ";" +
                           "-fx-text-fill: " + (order.getStatus().equals("Pending") ? "#856404" : "#155724") + ";" +
                           "-fx-padding: 5 10; -fx-background-radius: 15;");
        
        header.getChildren().addAll(orderIdLabel, dateLabel, spacer, statusLabel);

        // Product Info
        Label productNameLabel = new Label(order.getProductName());
        productNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label quantityLabel = new Label("Quantity: " + order.getQuantity());
        quantityLabel.setStyle("-fx-text-fill: #666666;");

        // Price
        Label priceLabel = new Label(String.format("$%.2f", order.getTotalPrice()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // Delete Button
        MFXButton deleteButton = new MFXButton("Delete Order");
        deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; " +
                            "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 5;");
        deleteButton.setOnAction(e -> {
            try {
                storeService.deleteOrder(order);
                // Get the FlowPane parent and remove the card
                FlowPane parent = (FlowPane) card.getParent();
                parent.getChildren().remove(card);
                
                // Update the orders list and total amount
                List<Order> remainingOrders = storeService.getOrdersByUserId(userId);
                updateTotalAmount(remainingOrders);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete order: " + ex.getMessage());
            }
        });

        card.getChildren().addAll(header, new Separator(), 
                                productNameLabel, quantityLabel, 
                                new Separator(), priceLabel, deleteButton);
        return card;
    }

    @FXML
    private void viewOrders() {
        try {
            System.out.println("ViewOrders called with userId: " + userId);
            if (userId <= 0) {
                System.out.println("Warning: Invalid userId = " + userId);
                showAlert(Alert.AlertType.WARNING, "Error", "Invalid user ID");
                return;
            }

            // Create main layout with sidebar
            HBox mainLayout = new HBox(0);
            mainLayout.setPrefSize(1200, 800);
            mainLayout.setStyle("-fx-background-color: #f8f9fa;");

            // Create sidebar
            VBox sidebar = createSidebar();
            
            // Create main content
            VBox mainContent = new VBox(20);
            mainContent.setStyle("-fx-padding: 20; -fx-background-color: #f8f9fa;");
            mainContent.setPrefWidth(1000);

            // Add title
            Label titleLabel = new Label("Your Orders");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            // Create FlowPane for order cards
            FlowPane cardsContainer = new FlowPane(20, 20);
            cardsContainer.setPadding(new javafx.geometry.Insets(20));
            cardsContainer.setPrefWrapLength(960);
            
            // Load orders
            List<Order> orders = storeService.getOrdersByUserId(userId);
            for (Order order : orders) {
                cardsContainer.getChildren().add(createOrderCard(order));
            }

            // Wrap cardsContainer in a ScrollPane
            ordersScrollPane = new javafx.scene.control.ScrollPane(cardsContainer);
            ordersScrollPane.setFitToWidth(true);
            ordersScrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");
            VBox.setVgrow(ordersScrollPane, Priority.ALWAYS);

            // Create bottom section for total and payment
            HBox bottomSection = new HBox(20);
            bottomSection.setAlignment(Pos.CENTER_RIGHT);
            bottomSection.setPadding(new javafx.geometry.Insets(20));
            bottomSection.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

            // Total amount label
            totalAmountLabel = new Label();
            updateTotalAmount(orders);
            totalAmountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            // Payment button
            MFXButton paymentButton = new MFXButton("Proceed to Payment");
            paymentButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                 "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
            paymentButton.setOnAction(e -> handlePayment(orders));

            bottomSection.getChildren().addAll(totalAmountLabel, paymentButton);

            // Add all components to main content
            mainContent.getChildren().addAll(titleLabel, ordersScrollPane, bottomSection);

            // Add sidebar and main content to main layout
            mainLayout.getChildren().addAll(sidebar, mainContent);

            // Create and show stage
            Stage ordersStage = new Stage();
            ordersStage.initModality(Modality.APPLICATION_MODAL);
            ordersStage.setTitle("Your Orders");
            ordersStage.setMinWidth(1200);
            ordersStage.setMinHeight(800);
            Scene scene = new Scene(mainLayout);
            ordersStage.setScene(scene);
            ordersStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders view: " + e.getMessage());
        }
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20;");

        Label menuTitle = new Label("Menu");
        menuTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        MFXButton dashboardBtn = createSidebarButton("Dashboard", "📊");
        dashboardBtn.setOnAction(e -> handleDashboard());

        MFXButton ordersBtn = createSidebarButton("Orders", "📦");
        ordersBtn.setOnAction(e -> viewOrders());

        MFXButton productsBtn = createSidebarButton("Products", "🛍");
        productsBtn.setOnAction(e -> handleProducts());

        MFXButton settingsBtn = createSidebarButton("Settings", "⚙");
        settingsBtn.setOnAction(e -> handleSettings());

        MFXButton logoutBtn = createSidebarButton("Logout", "🚪");
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(menuTitle, dashboardBtn, ordersBtn, productsBtn, settingsBtn, logoutBtn);
        return sidebar;
    }

    private MFXButton createSidebarButton(String text, String icon) {
        MFXButton button = new MFXButton(icon + " " + text);
        String baseStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; " +
                         "-fx-alignment: center-left; -fx-min-width: 160;";
        String hoverStyle = baseStyle + "; -fx-background-color: #34495e;";
        
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        return button;
    }

    private void handleDashboard() {
        try {
            // Close the current window
            Stage currentStage = (Stage) totalAmountLabel.getScene().getWindow();
            currentStage.close();
            
            showAlert(Alert.AlertType.INFORMATION, "Dashboard", "Dashboard feature coming soon!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error opening dashboard: " + e.getMessage());
        }
    }

    private void handleProducts() {
        try {
            // Close the orders window
            Stage ordersStage = (Stage) totalAmountLabel.getScene().getWindow();
            ordersStage.close();
            
            // Refresh the products view
            loadProductCardsFromDB();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error switching to products view: " + e.getMessage());
        }
    }

    private void handleSettings() {
        try {
            // Close the current window
            Stage currentStage = (Stage) totalAmountLabel.getScene().getWindow();
            currentStage.close();
            
            showAlert(Alert.AlertType.INFORMATION, "Settings", "Settings feature coming soon!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error opening settings: " + e.getMessage());
        }
    }

    private void handleLogout() {
        if (showConfirmationDialog("Logout", "Are you sure you want to logout?")) {
            try {
                // Close the orders window if it's open
                if (cardsContainer.getScene() != null && 
                    cardsContainer.getScene().getWindow() instanceof Stage) {
                    Stage currentStage = (Stage) cardsContainer.getScene().getWindow();
                    
                    // Close all other windows
                    for (Stage stage : new ArrayList<>(Stage.getWindows().stream()
                            .filter(window -> window instanceof Stage)
                            .map(window -> (Stage) window)
                            .collect(Collectors.toList()))) {
                        stage.close();
                    }
                }
                
                // You might want to show the login screen here
                showAlert(Alert.AlertType.INFORMATION, "Logout", "Successfully logged out!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error during logout: " + e.getMessage());
            }
        }
    }

    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK;
    }

    private void updateTotalAmount(List<Order> orders) {
        try {
            if (totalAmountLabel != null) {
                double total = orders.stream()
                    .filter(order -> order != null)
                    .mapToDouble(Order::getTotalPrice)
                    .sum();
                totalAmountLabel.setText(String.format("Total Amount: $%.2f", total));
                totalAmountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error updating total amount: " + e.getMessage());
        }
    }

    private void handlePayment(List<Order> orders) {
        if (orders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No orders to process payment for.");
            return;
        }

        try {
            // Calculate total amount
            double totalAmount = orders.stream()
                    .mapToDouble(Order::getTotalPrice)
                    .sum();

            // Create payment window
            Stage paymentStage = new Stage();
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.setTitle("Payment");
            paymentStage.setMinWidth(400);
            paymentStage.setMinHeight(650);

            VBox paymentLayout = new VBox(20);
            paymentLayout.setStyle("-fx-background-color: white; -fx-padding: 20;");
            paymentLayout.setAlignment(Pos.TOP_CENTER);

            // Header
            Label headerLabel = new Label("Payment Details");
            headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            // Order Summary
            VBox summaryBox = new VBox(10);
            summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");
            
            Label summaryLabel = new Label("Order Summary");
            summaryLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            VBox ordersList = new VBox(5);
            for (Order order : orders) {
                Label orderLine = new Label(String.format("%s x%d - $%.2f", 
                    order.getProductName(), order.getQuantity(), order.getTotalPrice()));
                orderLine.setStyle("-fx-text-fill: #666666;");
                ordersList.getChildren().add(orderLine);
            }
            
            Label totalLabel = new Label(String.format("Total Amount: $%.2f", totalAmount));
            totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            
            summaryBox.getChildren().addAll(summaryLabel, ordersList, new Separator(), totalLabel);

            // Payment Form
            VBox paymentForm = new VBox(15);
            paymentForm.setStyle("-fx-padding: 20 0;");

            // Email Address (Moved to top of form)
            Label emailLabel = new Label("Email Address *");
            emailLabel.setStyle("-fx-font-weight: bold;");
            TextField emailField = new TextField();
            emailField.setPromptText("your@email.com");
            emailField.setStyle("-fx-pref-width: 300;");

            // Card Number
            Label cardLabel = new Label("Card Number *");
            cardLabel.setStyle("-fx-font-weight: bold;");
            TextField cardField = new TextField();
            cardField.setPromptText("1234 5678 9012 3456");
            cardField.setStyle("-fx-pref-width: 300;");

            // Expiry and CVV
            HBox cardDetailsBox = new HBox(10);
            
            VBox expiryBox = new VBox(5);
            Label expiryLabel = new Label("Expiry Date *");
            expiryLabel.setStyle("-fx-font-weight: bold;");
            TextField expiryField = new TextField();
            expiryField.setPromptText("MM/YY");
            expiryField.setPrefWidth(100);
            expiryBox.getChildren().addAll(expiryLabel, expiryField);

            VBox cvvBox = new VBox(5);
            Label cvvLabel = new Label("CVV *");
            cvvLabel.setStyle("-fx-font-weight: bold;");
            TextField cvvField = new TextField();
            cvvField.setPromptText("123");
            cvvField.setPrefWidth(70);
            cvvBox.getChildren().addAll(cvvLabel, cvvField);

            cardDetailsBox.getChildren().addAll(expiryBox, cvvBox);

            // Name on Card
            Label nameLabel = new Label("Name on Card *");
            nameLabel.setStyle("-fx-font-weight: bold;");
            TextField nameField = new TextField();
            nameField.setPromptText("John Doe");
            nameField.setStyle("-fx-pref-width: 300;");

            // Required fields note
            Label requiredNote = new Label("* Required fields");
            requiredNote.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

            // Pay Button
            MFXButton payButton = new MFXButton("Pay Now");
            payButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                             "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                             "-fx-pref-width: 300;");
            
            payButton.setOnAction(e -> {
                // Validate email first
                if (emailField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please enter your email address.");
                    emailField.requestFocus();
                    return;
                }

                if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address.");
                    emailField.requestFocus();
                    return;
                }

                // Validate other fields
                if (cardField.getText().isEmpty() || expiryField.getText().isEmpty() || 
                    cvvField.getText().isEmpty() || nameField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all payment details.");
                    return;
                }

                try {
                    // Update order status to Paid
                    for (Order order : orders) {
                        storeService.updateOrderStatus(order.getOrderId(), "Paid");
                        System.out.println("Updated status for order: " + order.getOrderId() + " to Paid");
                    }

                    // Send confirmation email
                    emailService.sendPaymentConfirmation(emailField.getText(), orders, totalAmount);

                    // Close payment window
                    paymentStage.close();

                    // Refresh the orders view to show updated status
                    List<Order> updatedOrders = storeService.getOrdersByUserId(userId);
                    FlowPane cardsContainer = (FlowPane) ordersScrollPane.getContent();
                    cardsContainer.getChildren().clear();
                    for (Order order : updatedOrders) {
                        cardsContainer.getChildren().add(createOrderCard(order));
                    }

                    // Show success message with email confirmation
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            String.format("Payment processed successfully!\n\nA confirmation email has been sent to %s\n\nPlease check your inbox.", 
                            emailField.getText()));

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to process payment: " + ex.getMessage());
                }
            });

            // Cancel Button
            MFXButton cancelButton = new MFXButton("Cancel");
            cancelButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5; " +
                                "-fx-pref-width: 300;");
            cancelButton.setOnAction(e -> paymentStage.close());

            // Update form layout with new order
            paymentForm.getChildren().addAll(
                emailLabel, emailField,  // Email field first
                cardLabel, cardField,
                cardDetailsBox,
                nameLabel, nameField,
                requiredNote,            // Add required fields note
                new Separator(),         // Add separator before buttons
                payButton,
                cancelButton
            );

            paymentLayout.getChildren().addAll(
                headerLabel,
                summaryBox,
                paymentForm
            );

            Scene scene = new Scene(paymentLayout);
            paymentStage.setScene(scene);
            paymentStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Payment Error", 
                     "An error occurred while processing your payment: " + e.getMessage() + 
                     "\nPlease try again or contact support if the problem persists.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
