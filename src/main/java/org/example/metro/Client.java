package org.example.metro;

import java.io.*;
import java.net.Socket;


public class Client {

    private static final String HOST = "localhost";
    private static final int    PORT = Server.PORT;

    private Socket     socket;
    private BufferedReader in;
    private PrintWriter    out;

    // ── Connection management ─────────────────────────────────────────────────

    /**
     * Opens the TCP connection to the server.
     *
     * @throws IOException if the server is unreachable.
     */
    public void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    /**
     * Closes the TCP connection gracefully.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // ── High-level API used by controllers ────────────────────────────────────

    /** Sends LOGIN request; returns server response string. */
    public String login(String username, String password) throws IOException {
        return send(Request.of(Request.LOGIN, username, password));
    }

    /** Sends REGISTER request; returns server response string. */
    public String register(String username, String password, String fullName) throws IOException {
        return send(Request.of(Request.REGISTER, username, password, fullName));
    }

    /** Sends GET_ROUTES request; returns pipe-delimited routes string. */
    public String getRoutes() throws IOException {
        return send(Request.of(Request.GET_ROUTES));
    }

    /** Sends BOOK_TICKET request; returns server response string. */
    public String bookTicket(String username, int routeId) throws IOException {
        return send(Request.of(Request.BOOK_TICKET, username, String.valueOf(routeId)));
    }

    /** Sends CANCEL_BOOKING request; returns server response string. */
    public String cancelBooking(String username, int bookingId) throws IOException {
        return send(Request.of(Request.CANCEL_BOOKING, username, String.valueOf(bookingId)));
    }

    /** Sends GET_BOOKINGS request; returns pipe-delimited bookings string. */
    public String getBookings(String username) throws IOException {
        return send(Request.of(Request.GET_BOOKINGS, username));
    }

    // ── Core send / receive ───────────────────────────────────────────────────

    /**
     * Sends a {@link Request} over the socket and returns the server's response line.
     *
     * @throws IOException on any network error.
     */
    private synchronized String send(Request request) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to server.");
        }
        out.println(request.getRawMessage());
        String response = in.readLine();
        if (response == null) {
            throw new IOException("Server closed the connection.");
        }
        return response;
    }
}
