package org.example.metro;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.metro.Route;
import org.example.metro.Client;
import org.example.metro.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Metro Routes Screen (routes.fxml).
 */
public class RoutesController {

    @FXML private ListView<String> routesListView;
    @FXML private Label            messageLabel;
    @FXML private Label            welcomeLabel;
    @FXML private Button           bookButton;
    @FXML private Button           myBookingsButton;

    private Client client;
    private String username;

    /** Parsed Route objects matching the displayed list items. */
    private final List<Route> routeObjects = new ArrayList<>();

    public void init(Client client, String username) {
        this.client   = client;
        this.username = username;
        welcomeLabel.setText("Welcome, " + username + "!");
        loadRoutes();
    }

    private void loadRoutes() {
        new Thread(() -> {
            try {
                String response = client.getRoutes();
                Platform.runLater(() -> parseAndDisplayRoutes(response));
            } catch (IOException e) {
                Platform.runLater(() -> showMessage("Failed to load routes: " + e.getMessage(), true));
            }
        }).start();
    }

    private void parseAndDisplayRoutes(String response) {
        routeObjects.clear();
        routesListView.getItems().clear();

        if (!response.startsWith(Request.OK)) {
            showMessage("Error: " + response, true);
            return;
        }

        // Format: OK|id|start|end|seats|id|start|end|seats|...
        String[] parts = response.split("\\|");
        // parts[0] = "OK", then groups of 4
        for (int i = 1; i + 3 < parts.length; i += 4) {
            try {
                int    id    = Integer.parseInt(parts[i]);
                String start = parts[i + 1];
                String end   = parts[i + 2];
                int    seats = Integer.parseInt(parts[i + 3]);
                Route  route = new Route(id, start, end, seats);
                routeObjects.add(route);
                routesListView.getItems().add(route.toDisplayString());
            } catch (NumberFormatException e) {
                // skip malformed entry
            }
        }
        showMessage("Select a route and click Book Ticket.", false);
    }

    @FXML
    private void onBook() {
        int selectedIndex = routesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showMessage("Please select a route first.", true);
            return;
        }
        Route selected = routeObjects.get(selectedIndex);
        if (selected.getAvailableSeats() == 0) {
            showMessage("No seats available on this route.", true);
            return;
        }

        bookButton.setDisable(true);
        new Thread(() -> {
            try {
                String response = client.bookTicket(username, selected.getId());
                Platform.runLater(() -> {
                    bookButton.setDisable(false);
                    if (response.startsWith(Request.OK)) {
                        showMessage("Ticket booked successfully!", false);
                        loadRoutes();   // refresh seat counts
                    } else {
                        String[] p = response.split("\\|", 2);
                        showMessage(p.length > 1 ? p[1] : "Booking failed.", true);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    bookButton.setDisable(false);
                    showMessage("Server error: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    @FXML
    private void onMyBookings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metro/fxml/bookings.fxml"));
            Parent root = loader.load();
            BookingsController bc = loader.getController();
            bc.init(client, username);
            Stage stage = (Stage) myBookingsButton.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 520));
            stage.setTitle("Metro Tickets – My Bookings");
        } catch (IOException e) {
            showMessage("Cannot open bookings screen.", true);
        }
    }

    @FXML
    private void onRefresh() {
        loadRoutes();
    }

    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
