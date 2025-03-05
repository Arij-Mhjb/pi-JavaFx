package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.demo.model.Order;
import io.github.palexdev.materialfx.demo.services.StoreService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class OrdersController implements Initializable {
    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, Integer> productIdColumn;

    @FXML
    private TableColumn<Order, Integer> quantityColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private MFXButton proceedButton;

    private int userId;
    private final StoreService storeService = new StoreService();

    public void setUserId(int userId) {
        this.userId = userId;
        loadOrders();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up the proceed button
        proceedButton.setOnAction(event -> proceedToCheckout());
    }

    private void loadOrders() {
        try {
            List<Order> orders = storeService.getOrdersByUserId(userId);
            ordersTable.getItems().setAll(orders);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteOrder() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                storeService.deleteOrder(selectedOrder.getOrderId()); // Pass the order ID
                loadOrders(); // Refresh the table
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void proceedToCheckout() {
        // Implement checkout logic here
        System.out.println("Proceeding to checkout...");
    }
}