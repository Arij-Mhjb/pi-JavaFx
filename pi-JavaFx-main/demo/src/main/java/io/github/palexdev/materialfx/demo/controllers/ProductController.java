package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.services.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;


public class ProductController implements Initializable {
    @FXML
    private MFXButton switchToStoreButton;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private MFXButton addProductButton;
    @FXML
    private MFXTextField searchField;
    @FXML
    private MFXComboBox<String> categoryFilter;
    @FXML
    private MFXComboBox<String> stockFilter;

    private final ProductService productService = new ProductService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProductController initialized.");
        loadProductCardsFromDB();
        initializeFilters();
    }

    private void initializeFilters() {
        categoryFilter.getItems().addAll("Tournament Gear & Apparel", "Gaming & Equipment", "Trophies & Awards");
        stockFilter.getItems().addAll("Yes", "Coming", "No");
    }

    @FXML
    private void filterProducts() {
        String searchText = searchField.getText();
        String selectedCategory = categoryFilter.getValue();
        String selectedStock = stockFilter.getValue();

        try {
            List<Product> filteredProducts = productService.filterProducts(searchText, selectedCategory, selectedStock);
            cardsContainer.getChildren().clear();
            for (Product product : filteredProducts) {
                VBox card = createProductCard(product);
                cardsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to filter products: " + e.getMessage());
        }
    }

    private void loadProductCardsFromDB() {
        try {
            List<Product> products = productService.showAll();
            System.out.println("Number of products loaded: " + products.size());
            for (Product product : products) {
                VBox card = createProductCard(product);
                System.out.println("Adding product: " + product.getNameproduct());
                cardsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading products: " + e.getMessage());
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");

        Label nameLabel = new Label(product.getNameproduct());
        nameLabel.getStyleClass().add("header-label");

        HBox priceBox = createInfoBox("Price:", String.valueOf(product.getPriceproduct()));
        HBox stockBox = createInfoBox("Stock:", product.getStock());
        HBox categoryBox = createInfoBox("Category:", product.getCategory());

        HBox buttonBox = new HBox(10);
        buttonBox.getStyleClass().add("button-container");

        MFXButton updateButton = new MFXButton("Update");
        updateButton.getStyleClass().add("outline-buttonn");
        updateButton.setOnAction(event -> updateProduct(product));

        MFXButton deleteButton = new MFXButton("Delete");
        deleteButton.getStyleClass().add("outline-buttonn");
        deleteButton.setOnAction(event -> deleteProduct(product));

        buttonBox.getChildren().addAll(updateButton, deleteButton);
        card.getChildren().addAll(nameLabel, priceBox, stockBox, categoryBox, buttonBox);
        return card;
    }

    @FXML
    private void resetFilters() {
        // Clear the search field and combo boxes
        searchField.clear();
        categoryFilter.getSelectionModel().clearSelection();
        stockFilter.getSelectionModel().clearSelection();

        // Reload all products
        loadProductCardsFromDB();
    }

    private HBox createInfoBox(String labelText, String valueText) {
        HBox box = new HBox(10);
        Label infoLabel = new Label(labelText);
        infoLabel.getStyleClass().add("info-label");
        Label infoValue = new Label(valueText);
        box.getChildren().addAll(infoLabel, infoValue);
        return box;
    }

    @FXML
    private void switchToStore() {
        try {
            // Load the UserIDEntry.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/UserIDEntry.fxml"));
            Parent userIDEntryRoot = loader.load();

            // Create a new stage for the login page
            Stage loginStage = new Stage();
            loginStage.initModality(Modality.APPLICATION_MODAL); // Make it a modal window
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(userIDEntryRoot));
            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login page: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddNewProduct() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-background-radius: 10px ;-fx-border-color: #1B1B3B;-fx-border-width: 3;-fx-text-fill: #1B1B3B;");

        String textFieldStyle = "-fx-text-fill: #1B1B3B; -fx-prompt-text-fill: #2A2F4FFF; -fx-background-color: transparent; -fx-border-color: #2A2F4FFF;-fx-text-field : #ff9800;";

        MFXTextField nameField = new MFXTextField();
        nameField.setStyle(textFieldStyle);
        nameField.setFloatingText("Product Name");
        nameField.setPrefWidth(300);
        nameField.setPrefHeight(40);

        MFXTextField priceField = new MFXTextField();
        priceField.setFloatingText("Product Price");
        priceField.setStyle(textFieldStyle);
        priceField.setPrefWidth(300);
        priceField.setPrefHeight(40);

        // ComboBox for Stock
        MFXComboBox<String> stockComboBox = new MFXComboBox<>();
        stockComboBox.getItems().addAll("Yes", "Coming", "No");
        stockComboBox.setFloatingText("Stock");
        stockComboBox.setStyle(textFieldStyle);
        stockComboBox.setPrefWidth(300);
        stockComboBox.setPrefHeight(40);

        // ComboBox for Category
        MFXComboBox<String> categoryComboBox = new MFXComboBox<>();
        categoryComboBox.getItems().addAll("Tournament Gear & Apparel", "Gaming & Equipment", "Trophies & Awards");
        categoryComboBox.setFloatingText("Category");
        categoryComboBox.setStyle(textFieldStyle);
        categoryComboBox.setPrefWidth(300);
        categoryComboBox.setPrefHeight(40);

        Label titleLabel = new Label("Add New Product");
        titleLabel.setStyle("-fx-text-fill: #2A2F4FFF; -fx-font-size: 20px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        MFXButton saveButton = new MFXButton("Save");
        saveButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        saveButton.setPrefWidth(100);
        saveButton.setPrefHeight(40);

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        form.getChildren().addAll(
                titleLabel,
                nameField,
                priceField,
                stockComboBox,
                categoryComboBox,
                buttonBox
        );

        saveButton.setOnAction(e -> {
            try {
                Product newProduct = new Product();
                newProduct.setNameproduct(nameField.getText());
                newProduct.setPriceproduct(Double.parseDouble(priceField.getText()));
                newProduct.setStock(stockComboBox.getValue()); // Set stock from ComboBox
                newProduct.setCategory(categoryComboBox.getValue()); // Set category from ComboBox

                // Insert into database (ID will be auto-incremented)
                productService.insert(newProduct);

                // Refresh the product cards
                cardsContainer.getChildren().clear();
                loadProductCardsFromDB();

                popupStage.close();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product: " + ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form, 400, 400);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateProduct(Product product) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);

        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-border-color: #ff9800;-fx-border-width: 2;-fx-text-fill: #1B1B3B;");

        String textFieldStyle = "-fx-text-fill: #1B1B3B; -fx-prompt-text-fill: #1B1B3B; -fx-background-color: transparent; -fx-border-color: #2A2F4FFF;-fx-text-field : #ff9800;";

        // Pre-fill the form with the product's current details
        MFXTextField nameField = new MFXTextField(product.getNameproduct());
        nameField.setStyle(textFieldStyle);
        nameField.setFloatingText("Product Name");
        nameField.setPrefWidth(300);
        nameField.setPrefHeight(40);

        MFXTextField priceField = new MFXTextField(String.valueOf(product.getPriceproduct()));
        priceField.setStyle(textFieldStyle);
        priceField.setFloatingText("Product Price");
        priceField.setPrefWidth(300);
        priceField.setPrefHeight(40);

        // ComboBox for Stock
        MFXComboBox<String> stockComboBox = new MFXComboBox<>();
        stockComboBox.getItems().addAll("Yes", "Coming", "No");
        stockComboBox.setValue(product.getStock()); // Set current value
        stockComboBox.setFloatingText("Stock");
        stockComboBox.setStyle(textFieldStyle);
        stockComboBox.setPrefWidth(300);
        stockComboBox.setPrefHeight(40);

        // ComboBox for Category
        MFXComboBox<String> categoryComboBox = new MFXComboBox<>();
        categoryComboBox.getItems().addAll("Tournament Gear & Apparel", "Gaming & Equipment", "Trophies & Awards");
        categoryComboBox.setValue(product.getCategory()); // Set current value
        categoryComboBox.setFloatingText("Category");
        categoryComboBox.setStyle(textFieldStyle);
        categoryComboBox.setPrefWidth(300);
        categoryComboBox.setPrefHeight(40);

        Label titleLabel = new Label("Edit Product");
        titleLabel.setStyle("-fx-text-fill: #1B1B3B; -fx-font-size: 18px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        MFXButton saveButton = new MFXButton("Save");
        saveButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        saveButton.setPrefWidth(100);
        saveButton.setPrefHeight(40);

        MFXButton cancelButton = new MFXButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        form.getChildren().addAll(
                titleLabel,
                nameField,
                priceField,
                stockComboBox,
                categoryComboBox,
                buttonBox
        );

        saveButton.setOnAction(e -> {
            try {
                // Update the product object
                product.setNameproduct(nameField.getText());
                product.setPriceproduct(Double.parseDouble(priceField.getText()));
                product.setStock(stockComboBox.getValue());
                product.setCategory(categoryComboBox.getValue());

                // Update in the database
                int updateResult = productService.update(product);

                if (updateResult > 0) {
                    // Refresh the product cards
                    cardsContainer.getChildren().clear();
                    loadProductCardsFromDB();

                    popupStage.close();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Update Failed", "No changes were made to the product.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for the price!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update product: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> popupStage.close());

        Scene scene = new Scene(form, 400, 400);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void deleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this product?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int result = productService.delete(product);
                    if (result > 0) {
                        // Refresh the product cards
                        cardsContainer.getChildren().clear();
                        loadProductCardsFromDB();

                        showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Deletion Failed", "No product found with that ID.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete product: " + e.getMessage());
                }
            }
        });
    }
}