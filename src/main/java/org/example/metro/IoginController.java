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
 * Controller for the Login Screen (login.fxml).
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         messageLabel;
    @FXML private Button        loginButton;
    @FXML private Button        registerButton;

    private final Client client = new Client();

    @FXML
    public void initialize() {
        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                client.connect();
            } catch (IOException e) {
                Platform.runLater(() ->
                        showMessage("Cannot connect to server. Is it running?", true));
            }
        }).start();
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password.", true);
            return;
        }

        loginButton.setDisable(true);
        new Thread(() -> {
            try {
                String response = client.login(username, password);
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    if (response.startsWith(Request.OK)) {
                        openRoutesScreen(username);
                    } else {
                        String[] parts = response.split("\\|", 2);
                        showMessage(parts.length > 1 ? parts[1] : "Login failed.", true);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    showMessage("Server error: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    @FXML
    private void onGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metro/fxml/register.fxml"));
            Parent root = loader.load();
            RegisterController rc = loader.getController();
            rc.setClient(client);
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 480, 500));
            stage.setTitle("Metro Tickets – Register");
        } catch (IOException e) {
            showMessage("Cannot open register screen.", true);
        }
    }

    private void openRoutesScreen(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metro/fxml/routes.fxml"));
            Parent root = loader.load();
            RoutesController rc = loader.getController();
            rc.init(client, username);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 520));
            stage.setTitle("Metro Tickets – Routes");
        } catch (IOException e) {
            showMessage("Cannot open routes screen.", true);
        }
    }

    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}