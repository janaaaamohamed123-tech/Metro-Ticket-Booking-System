package org.example.metro;

import metro.model.Booking;
import metro.model.Route;
import metro.model.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {

    public static final int PORT = 5000;

    // ── Shared state (accessed by multiple ClientHandler threads) ─────────────
    private final List<User>    users    = new ArrayList<>();
    private final List<Route>   routes   = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final AtomicInteger bookingIdCounter = new AtomicInteger(1);

    // ─────────────────────────────────────────────────────────────────────────

    public Server() {
        initRoutes();
    }

    /** Hard-coded metro routes as required by the spec. */
    private void initRoutes() {
        routes.add(new Route(1, "Central Station",  "Airport",          30));
        routes.add(new Route(2, "Airport",          "Harbour View",     25));
        routes.add(new Route(3, "Central Station",  "University",       40));
        routes.add(new Route(4, "University",       "Tech Park",        35));
        routes.add(new Route(5, "Tech Park",        "Harbour View",     20));
        routes.add(new Route(6, "Harbour View",     "Old Town",         50));
        routes.add(new Route(7, "Old Town",         "Central Station",  45));
    }

    /** Starts listening and spawns a ClientHandler for every incoming connection. */
    public void start() {
        System.out.println("[Server] Metro Ticket Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] New client connected: "
                        + clientSocket.getInetAddress().getHostAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                Thread thread = new Thread(handler);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("[Server] Server error: " + e.getMessage());
        }
    }


    public synchronized User findUser(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst().orElse(null);
    }

    public synchronized boolean registerUser(User user) {
        if (findUser(user.getUsername()) != null) return false;
        users.add(user);
        return true;
    }

    public synchronized List<Route> getRoutes() {
        return routes;
    }

    public synchronized Route findRoute(int routeId) {
        return routes.stream()
                .filter(r -> r.getId() == routeId)
                .findFirst().orElse(null);
    }

    public synchronized List<Booking> getBookingsForUser(String username) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getUsername().equalsIgnoreCase(username)) result.add(b);
        }
        return result;
    }

    public synchronized Booking addBooking(String username, int routeId) {
        Route route = findRoute(routeId);
        if (route == null)              throw new IllegalArgumentException("Route not found.");
        if (route.getAvailableSeats() == 0) throw new IllegalStateException("No seats available.");
        route.bookSeat();
        int id = bookingIdCounter.getAndIncrement();
        Booking booking = new Booking(id, username, routeId, route.toDisplayString());
        bookings.add(booking);
        return booking;
    }

    public synchronized boolean cancelBooking(String username, int bookingId) {
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            if (b.getBookingId() == bookingId && b.getUsername().equalsIgnoreCase(username)) {
                Route route = findRoute(b.getRouteId());
                if (route != null) route.cancelSeat();
                bookings.remove(i);
                return true;
            }
        }
        return false;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        new Server().start();
    }
}
