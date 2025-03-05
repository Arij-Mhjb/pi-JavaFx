package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.Product;
import io.github.palexdev.materialfx.demo.model.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private Connection connection;

    public ProductService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sportifydb", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Product> filterProducts(String searchText, String category, String stock) throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product WHERE nameproduct LIKE ? AND (category = ? OR ? IS NULL) AND (stock = ? OR ? IS NULL)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchText + "%");
            pstmt.setString(2, category);
            pstmt.setString(3, category);
            pstmt.setString(4, stock);
            pstmt.setString(5, stock);
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


    // Fetch all products from the database
    public List<Product> showAll() throws SQLException {
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

    // Insert a new product into the database
    public void insert(Product product) throws SQLException {
        String query = "INSERT INTO product (nameproduct, priceproduct, stock, category) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getNameproduct());
            pstmt.setDouble(2, product.getPriceproduct());
            pstmt.setString(3, product.getStock());
            pstmt.setString(4, product.getCategory());
            pstmt.executeUpdate();
        }
    }

    // Update an existing product in the database
    public int update(Product product) throws SQLException {
        String query = "UPDATE product SET nameproduct = ?, priceproduct = ?, stock = ?, category = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, product.getNameproduct());
            pstmt.setDouble(2, product.getPriceproduct());
            pstmt.setString(3, product.getStock());
            pstmt.setString(4, product.getCategory());
            pstmt.setInt(5, product.getId());
            return pstmt.executeUpdate();
        }
    }

    // Delete a product from the database
    public int delete(Product product) throws SQLException {
        String query = "DELETE FROM product WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, product.getId());
            return pstmt.executeUpdate();
        }
    }

    public void addToBasket(int userId, int productId, int quantity) throws SQLException {
        System.out.println("Adding to basket - User ID: " + userId + ", Product ID: " + productId + ", Quantity: " + quantity); // Debug

        String query = "INSERT INTO panier (client_id, product_id, quantity, total, status) VALUES (?, ?, ?, (SELECT priceproduct FROM product WHERE id = ?) * ?, 'Pending')";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, productId);
            pstmt.setInt(5, quantity);
            pstmt.executeUpdate();

            // Get the generated panier ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int panierId = rs.getInt(1);
                createOrder(userId, productId, quantity, panierId);
            }
        }
    }
    // Create an order
    private void createOrder(int userId, int productId, int quantity, int panierId) throws SQLException {
        String query = "INSERT INTO order_ (id_user, id_product, quantity_order, id_panier) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, panierId);
            pstmt.executeUpdate();
        }
    }
    // Fetch all orders for a user
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM order_ WHERE id_user = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setUserId(rs.getInt("id_user"));
                order.setProductId(rs.getInt("id_product"));
                order.setQuantity(rs.getInt("quantity_order"));
                order.setPanierId(rs.getInt("id_panier"));
                orders.add(order);
            }
        }
        return orders;
    }
}