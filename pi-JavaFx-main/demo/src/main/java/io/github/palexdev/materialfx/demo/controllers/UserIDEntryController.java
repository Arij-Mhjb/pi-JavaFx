package io.github.palexdev.materialfx.demo.controllers;

import io.github.palexdev.materialfx.demo.model.User;
import io.github.palexdev.materialfx.demo.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class UserIDEntryController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void proceedToStore() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter both username and password.");
            return;
        }

        try {
            UserService userService = new UserService();
            User user = userService.getUserByCredentials(username, password);

            if (user != null) {
                // Close the login window
                Stage loginStage = (Stage) usernameField.getScene().getWindow();
                loginStage.close();

                // Load the Store.fxml file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/github/palexdev/materialfx/demo/fxml/Store.fxml"));
                Parent storeRoot = loader.load();

                // Pass the user ID to the StoreController
                StoreController storeController = loader.getController();
                storeController.setUserId(user.getId());

                // Show the store window
                Stage storeStage = new Stage();
                storeStage.setTitle("Store");
                storeStage.setScene(new Scene(storeRoot));
                storeStage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load store interface: " + e.getMessage());
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