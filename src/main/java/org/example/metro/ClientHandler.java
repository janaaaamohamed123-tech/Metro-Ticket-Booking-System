package org.example.metro;
import org.example.metro.Route;
import org.example.metro.User;
import org.example.metro.Booking;

import java.io.*;
import java.net.Socket;
import java.util.List;
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[ClientHandler] Received: " + line);
                String response = handleRequest(new Request(line));
                out.println(response);
                System.out.println("[ClientHandler] Sent:     " + response);
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] Connection error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("[ClientHandler] Client disconnected.");
        }
    }

    /**
     * Dispatches the request to the appropriate handler method.
     * Returns a pipe-separated response string: OK|payload  or  ERROR|message
     */
    private String handleRequest(Request request) {
        try {
            String[] parts = request.getParts();
            switch (request.getCommand()) {
                case Request.LOGIN:          return handleLogin(parts);
                case Request.REGISTER:       return handleRegister(parts);
                case Request.GET_ROUTES:     return handleGetRoutes();
                case Request.BOOK_TICKET:    return handleBookTicket(parts);
                case Request.CANCEL_BOOKING: return handleCancelBooking(parts);
                case Request.GET_BOOKINGS:   return handleGetBookings(parts);
                default:
                    return Request.ERROR + "|Unknown command: " + request.getCommand();
            }
        } catch (Exception e) {
            return Request.ERROR + "|" + e.getMessage();
        }
    }

    // ── Command handlers ──────────────────────────────────────────────────────

    private String handleLogin(String[] parts) {
        if (parts.length < 3) return Request.ERROR + "|Missing credentials.";
        String username = parts[1];
        String password = parts[2];
        User user = server.findUser(username);
        if (user == null || !user.getPassword().equals(password)) {
            return Request.ERROR + "|Invalid username or password.";
        }
        return Request.OK + "|Welcome, " + user.getFullName() + "!";
    }

    private String handleRegister(String[] parts) {
        if (parts.length < 4) return Request.ERROR + "|Missing registration fields.";
        String username = parts[1];
        String password = parts[2];
        String fullName = parts[3];
        if (username.isBlank() || password.isBlank() || fullName.isBlank()) {
            return Request.ERROR + "|All fields are required.";
        }
        User newUser = new User(username, password, fullName);
        if (!server.registerUser(newUser)) {
            return Request.ERROR + "|Username already exists.";
        }
        return Request.OK + "|Registration successful.";
    }

    private String handleGetRoutes() {
        List<Route> routes = server.getRoutes();
        StringBuilder sb = new StringBuilder(Request.OK);
        for (Route r : routes) {
            sb.append("|").append(r.toString());
        }
        return sb.toString();
    }

    private String handleBookTicket(String[] parts) {
        if (parts.length < 3) return Request.ERROR + "|Missing booking details.";
        String username = parts[1];
        int routeId;
        try {
            routeId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return Request.ERROR + "|Invalid route ID.";
        }
        Booking booking = server.addBooking(username, routeId);
        return Request.OK + "|" + booking.toString();
    }

    private String handleCancelBooking(String[] parts) {
        if (parts.length < 3) return Request.ERROR + "|Missing cancellation details.";
        String username = parts[1];
        int bookingId;
        try {
            bookingId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return Request.ERROR + "|Invalid booking ID.";
        }
        if (!server.cancelBooking(username, bookingId)) {
            return Request.ERROR + "|Booking not found.";
        }
        return Request.OK + "|Booking cancelled successfully.";
    }

    private String handleGetBookings(String[] parts) {
        if (parts.length < 2) return Request.ERROR + "|Missing username.";
        String username = parts[1];
        List<Booking> bookings = server.getBookingsForUser(username);
        StringBuilder sb = new StringBuilder(Request.OK);
        for (Booking b : bookings) {
            sb.append("|").append(b.toString());
        }
        return sb.toString();
    }
}




