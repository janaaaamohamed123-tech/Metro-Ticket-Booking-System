package org.example.metro;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.metro.Booking;
import org.example.metro.Client;
import org.example.metro.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the My Bookings Screen (bookings.fxml).
 */
public class BookingsController {

    @FXML private ListView<String> bookingsListView;
    @FXML private Label            messageLabel;
    @FXML private Label            titleLabel;
    @FXML private Button           cancelButton;
    @FXML private Button           backButton;

    private Client client;
    private String username;

    private final List<Booking> bookingObjects = new ArrayList<>();

    public void init(Client client, String username) {
        this.client   = client;
        this.username = username;
        titleLabel.setText("Bookings for: " + username);
        loadBookings();
    }

    private void loadBookings() {
        new Thread(() -> {
            try {
                String response = client.getBookings(username);
                Platform.runLater(() -> parseAndDisplayBookings(response));
            } catch (IOException e) {
                Platform.runLater(() -> showMessage("Failed to load bookings: " + e.getMessage(), true));
            }
        }).start();
    }

    private void parseAndDisplayBookings(String response) {
        bookingObjects.clear();
        bookingsListView.getItems().clear();

        if (!response.startsWith(Request.OK)) {
            showMessage("Error loading bookings.", true);
            return;
        }

        // Format: OK|bookingId|username|routeId|routeDescription|...  (groups of 4 after OK)
        String[] parts = response.split("\\|");
        for (int i = 1; i + 3 < parts.length; i += 4) {
            try {
                int    bookingId = Integer.parseInt(parts[i]);
                String uname     = parts[i + 1];
                int    routeId   = Integer.parseInt(parts[i + 2]);
                String desc      = parts[i + 3];
                Booking b = new Booking(bookingId, uname, routeId, desc);
                bookingObjects.add(b);
                bookingsListView.getItems().add(b.toDisplayString());
            } catch (NumberFormatException e) {
                // skip malformed entry
            }
        }

        if (bookingObjects.isEmpty()) {
            showMessage("You have no bookings yet.", false);
        } else {
            showMessage("Select a booking and click Cancel to remove it.", false);
        }
    }

    @FXML
    private void onCancel() {
        int selectedIndex = bookingsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showMessage("Please select a booking to cancel.", true);
            return;
        }
        Booking selected = bookingObjects.get(selectedIndex);

        cancelButton.setDisable(true);
        new Thread(() -> {
            try {
                String response = client.cancelBooking(username, selected.getBookingId());
                Platform.runLater(() -> {
                    cancelButton.setDisable(false);
                    if (response.startsWith(Request.OK)) {
                        showMessage("Booking cancelled successfully.", false);
                        loadBookings();
                    } else {
                        String[] p = response.split("\\|", 2);
                        showMessage(p.length > 1 ? p[1] : "Cancellation failed.", true);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    cancelButton.setDisable(false);
                    showMessage("Server error: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metro/fxml/routes.fxml"));
            Parent root = loader.load();
            RoutesController rc = loader.getController();
            rc.init(client, username);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 520));
            stage.setTitle("Metro Tickets – Routes");
        } catch (IOException e) {
            showMessage("Cannot go back.", true);
        }
    }

    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
