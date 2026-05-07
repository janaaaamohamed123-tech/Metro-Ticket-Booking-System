package metro.model;

/**
 * Represents a registered user in the Metro Ticket Booking System.
 * Demonstrates encapsulation via private fields and public getters/setters.
 */
public class User {

    private String username;
    private String password;
    private String fullName;

    public User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Returns a human-readable summary of the user.
     * Overrides Object.toString() – demonstrates polymorphism (method overriding).
     */
    @Override
    public String toString() {
        return "User{username='" + username + "', fullName='" + fullName + "'}";
    }
}
