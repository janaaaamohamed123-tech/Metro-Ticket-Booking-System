package org.example.metro;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import metro.network.Client;
import metro.network.Request;

import java.io.IOException;

/**
 * Controller for the Register Screen (register.fxml).
 */
public class RegisterController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     fullNameField;
    @FXML private Label         messageLabel;
    @FXML private Button        registerButton;
    @FXML private Button        backButton;

    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void onRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showMessage("All fields are required.", true);
            return;
        }

        registerButton.setDisable(true);
        new Thread(() -> {
            try {
                String response = client.register(username, password, fullName);
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    if (response.startsWith(Request.OK)) {
                        showMessage("Registered successfully! You can now log in.", false);
                    } else {
                        String[] parts = response.split("\\|", 2);
                        showMessage(parts.length > 1 ? parts[1] : "Registration failed.", true);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    showMessage("Server error: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metro/fxml/login.fxml"));
            Parent root = loader.load();
            // The LoginController will reuse the same Client via initialize()
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 480, 420));
            stage.setTitle("Metro Tickets – Login");
        } catch (IOException e) {
            showMessage("Cannot go back.", true);
        }
    }

    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
