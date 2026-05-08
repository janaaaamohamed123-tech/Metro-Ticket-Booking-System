package org.example.metro;

public class Booking {private int bookingId;
    private String username;
    private int routeId;
    private String routeDescription;

    public Booking(int bookingId, String username, int routeId, String routeDescription) {
        this.bookingId = bookingId;
        this.username = username;
        this.routeId = routeId;
        this.routeDescription = routeDescription;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getBookingId()         { return bookingId; }
    public String getUsername()       { return username; }
    public int getRouteId()           { return routeId; }
    public String getRouteDescription() { return routeDescription; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setBookingId(int bookingId)             { this.bookingId = bookingId; }
    public void setUsername(String username)             { this.username = username; }
    public void setRouteId(int routeId)                 { this.routeId = routeId; }
    public void setRouteDescription(String desc)        { this.routeDescription = desc; }

    /**
     * Serialised form sent over the network: bookingId|username|routeId|routeDescription
     * Overrides Object.toString() – demonstrates polymorphism (method overriding).
     */
    @Override
    public String toString() {
        return bookingId + "|" + username + "|" + routeId + "|" + routeDescription;
    }

    /** Human-readable label used in the GUI ListView. */
    public String toDisplayString() {
        return "Booking #" + bookingId + "  –  " + routeDescription;
    }
}
