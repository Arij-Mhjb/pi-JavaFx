package io.github.palexdev.materialfx.demo.services;

import io.github.palexdev.materialfx.demo.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    private Connection connection;

    public UserService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sportifydb", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch a user by username and password
    public User getUserByCredentials(String username, String password) throws SQLException {
        String query = "SELECT * FROM userr WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("Id_client"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                return user;
            }
        }
        return null;
    }
}