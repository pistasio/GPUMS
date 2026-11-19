
package model;

public class AccessRequest {

    // Request types
    public static final String REGISTRATION = "REGISTRATION";
    public static final String EXTRA_TIME = "EXTRA_TIME";

    // Request statuses
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private static int nextId = 1;

    // Attributes
    private final int requestId;
    private final String username;
    private final String requestType;
    private String status;
    private final int requestedMinutes;
    private final String reason;

    // Constructor
    public AccessRequest(String username, String requestType,
                         int requestedMinutes, String reason) {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Username cannot be empty"
            );
        }

        if (!REGISTRATION.equals(requestType)
                && !EXTRA_TIME.equals(requestType)) {
            throw new IllegalArgumentException(
                "Invalid request type"
            );
        }

        if (EXTRA_TIME.equals(requestType)) {
            if (requestedMinutes <= 0) {
                throw new IllegalArgumentException(
                    "Requested minutes must be positive"
                );
            }

            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "A reason is required for extra time"
                );
            }
        } else {
            if (requestedMinutes != 0) {
                throw new IllegalArgumentException(
                    "Registration requests cannot request minutes"
                );
            }
        }

        this.requestId = nextId++;
        this.username = username.trim();
        this.requestType = requestType;
        this.status = PENDING;
        this.requestedMinutes = requestedMinutes;
        this.reason = reason == null ? "" : reason.trim();
    }

    // Getters
    public int getRequestId() {
        return requestId;
    }

    public String getUsername() {
        return username;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getStatus() {
        return status;
    }

    public int getRequestedMinutes() {
        return requestedMinutes;
    }

    public String getReason() {
        return reason;
    }

    public boolean isPending() {
        return PENDING.equals(status);
    }

    // Approve request
    public void approve() {
        if (!isPending()) {
            throw new IllegalStateException(
                "Only pending requests can be approved"
            );
        }

        status = APPROVED;
    }

    // Reject request
    public void reject() {
        if (!isPending()) {
            throw new IllegalStateException(
                "Only pending requests can be rejected"
            );
        }

        status = REJECTED;
    }

    @Override
    public String toString() {
        return "Request ID: " + requestId
                + " | Username: " + username
                + " | Type: " + requestType
                + " | Status: " + status;
    }
}
