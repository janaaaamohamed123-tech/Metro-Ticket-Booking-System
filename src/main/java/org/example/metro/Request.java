package org.example.metro;

public class Request { public static final String LOGIN           = "LOGIN";
    public static final String REGISTER        = "REGISTER";
    public static final String GET_ROUTES      = "GET_ROUTES";
    public static final String BOOK_TICKET     = "BOOK_TICKET";
    public static final String CANCEL_BOOKING  = "CANCEL_BOOKING";
    public static final String GET_BOOKINGS    = "GET_BOOKINGS";

    public static final String OK    = "OK";
    public static final String ERROR = "ERROR";

    private final String rawMessage;

    public Request(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new IllegalArgumentException("Request message must not be empty.");
        }
        this.rawMessage = rawMessage.trim();
    }

    /** Convenience factory – joins parts with '|'. */
    public static Request of(String... parts) {
        return new Request(String.join("|", parts));
    }

    /** Returns the full raw message string to be sent over the socket. */
    public String getRawMessage() {
        return rawMessage;
    }

    /** Returns the command (first token). */
    public String getCommand() {
        return rawMessage.split("\\|")[0];
    }

    /** Returns the pipe-split parts array. */
    public String[] getParts() {
        return rawMessage.split("\\|", -1);
    }

    /**
     * Overrides Object.toString() – demonstrates polymorphism (method overriding).
     */
    @Override
    public String toString() {
        return "Request{" + rawMessage + "}";
    }
}
