package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Order;
import io.github.palexdev.materialfx.demo.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreService {
    private Connection connection;

    public StoreService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sportifydb", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT category FROM product"; // Adjust table name if necessary

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }

        return categories;
    }

    public List<Product> filterProducts(String searchText, String category) throws SQLException {
        List<Product> products = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM product WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.trim().isEmpty()) {
            query.append(" AND nameproduct LIKE ?");
            params.add("%" + searchText + "%");
        }

        if (category != null && !category.trim().isEmpty()) {
            query.append(" AND category = ?");
            params.add(category);
        }

        try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setNameproduct(rs.getString("nameproduct"));
                product.setPriceproduct(rs.getDouble("priceproduct"));
                product.setStock(rs.getString("stock"));
                product.setCategory(rs.getString("category"));
                products.add(product);
            }
        }
        return products;
    }

    public List<Product> resetFilters() throws SQLException {
        return getAllProducts(); // Reset filters by returning all products
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setNameproduct(rs.getString("nameproduct"));
                product.setPriceproduct(rs.getDouble("priceproduct"));
                product.setStock(rs.getString("stock"));
                product.setCategory(rs.getString("category"));
                products.add(product);
            }
        }
        return products;
    }

    public void addToBasket(int userId, int productId, int quantity) throws SQLException {
        // Calculate total price
        double total = getProductPrice(productId) * quantity;

        // Insert into panier table
        String panierQuery = "INSERT INTO panier (client_id, product_id, quantity, total, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(panierQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, total);
            pstmt.setString(5, "Pending");
            pstmt.executeUpdate();

            // Get the generated panier ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int panierId = rs.getInt(1);
                createOrder(userId, productId, quantity, panierId);
            }
        }
    }

    private double getProductPrice(int productId) throws SQLException {
        String query = "SELECT priceproduct FROM product WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("priceproduct");
            }
        }
        throw new SQLException("Product not found with ID: " + productId);
    }

    private void createOrder(int userId, int productId, int quantity, int panierId) throws SQLException {
        String query = "INSERT INTO order_ (id_user, id_product, quantity_order, id_panier, date) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, panierId);
            pstmt.executeUpdate();
        }
    }

    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.order_id, o.id_user, o.id_product, o.quantity_order, " +
                      "o.date, o.id_panier, p.nameproduct, " +
                      "pan.total as total_price, pan.status " +
                      "FROM order_ o " +
                      "JOIN product p ON o.id_product = p.id " +
                      "JOIN panier pan ON o.id_panier = pan.id " +
                      "WHERE o.id_user = ? " +
                      "ORDER BY o.date DESC";
        
        System.out.println("==== Getting Orders for User ====");
        System.out.println("User ID: " + userId);
        System.out.println("SQL Query: " + query);
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            System.out.println("Executing query with userId = " + userId);
            
            ResultSet rs = stmt.executeQuery();
            System.out.println("Query executed successfully");
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("\nProcessing order #" + count);
                try {
                    Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("id_user"),
                        rs.getInt("id_product"),
                        rs.getInt("quantity_order"),
                        rs.getTimestamp("date").toLocalDateTime(),
                        rs.getInt("id_panier"),
                        rs.getString("nameproduct"),
                        rs.getDouble("total_price"),
                        rs.getString("status")
                    );
                    
                    System.out.println("Order details:" +
                        "\n  Order ID: " + order.getOrderId() +
                        "\n  User ID: " + order.getUserId() +
                        "\n  Product ID: " + order.getProductId() +
                        "\n  Product Name: " + order.getProductName() +
                        "\n  Quantity: " + order.getQuantity() +
                        "\n  Total Price: " + order.getTotalPrice() +
                        "\n  Status: " + order.getStatus() +
                        "\n  Date: " + order.getDate());

                    orders.add(order);
                } catch (SQLException e) {
                    System.out.println("Error processing order: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("\n==== Summary ====");
            System.out.println("Total orders found: " + orders.size());
            System.out.println("==================");
        }
        return orders;
    }

    public void deleteOrder(int orderId) throws SQLException {
        // First, get the panier ID associated with the order
        String panierQuery = "SELECT id_panier FROM order_ WHERE order_id = ?";
        int panierId;
        try (PreparedStatement pstmt = connection.prepareStatement(panierQuery)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                panierId = rs.getInt("id_panier");
            } else {
                throw new SQLException("Order not found with ID: " + orderId);
            }
        }

        // Delete the order
        String orderQuery = "DELETE FROM order_ WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(orderQuery)) {
            pstmt.setInt(1, orderId);
            pstmt.executeUpdate();
        }

        // Delete the associated panier entry
        String panierDeleteQuery = "DELETE FROM panier WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(panierDeleteQuery)) {
            pstmt.setInt(1, panierId);
            pstmt.executeUpdate();
        }
    }

    public void deleteOrder(Order order) throws SQLException {
        deleteOrder(order.getOrderId());
    }

    public void updateOrderPaymentIntent(int orderId, String paymentIntentId) throws SQLException {
        String query = "UPDATE order_ SET payment_intent_id = ?, status = 'Processing' WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, paymentIntentId);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
            System.out.println("Updated payment intent for order " + orderId + ": " + paymentIntentId);
        }
    }

    public void updateOrderStatus(int orderId, String status) throws SQLException {
        // First get the panier ID from the order
        String getPanierIdQuery = "SELECT id_panier FROM order_ WHERE order_id = ?";
        int panierId;
        
        try (PreparedStatement pstmt = connection.prepareStatement(getPanierIdQuery)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                panierId = rs.getInt("id_panier");
                
                // Now update the status in the panier table
                String updateStatusQuery = "UPDATE panier SET status = ? WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateStatusQuery)) {
                    updateStmt.setString(1, status);
                    updateStmt.setInt(2, panierId);
                    updateStmt.executeUpdate();
                    System.out.println("Updated status for panier " + panierId + " to: " + status);
                }
            } else {
                throw new SQLException("Order not found with ID: " + orderId);
            }
        }
    }
}