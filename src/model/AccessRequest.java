package model;

public class AccessRequest {

    public static final String REGISTRATION = "REGISTRATION";
    public static final String EXTRA_TIME = "EXTRA_TIME";

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private static int nextId = 1;

    private int requestId;
    private String username;
    private String requestType;
    private String status;
    private int requestedMinutes;
    private String reason;

    public AccessRequest(String username, String requestType, int requestedMinutes, String reason) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.contains("|") || (reason != null && reason.contains("|"))) {
            throw new IllegalArgumentException("Request details cannot contain |");
        }
        if (!REGISTRATION.equals(requestType) && !EXTRA_TIME.equals(requestType)) {
            throw new IllegalArgumentException("Invalid request type");
        }
        if (EXTRA_TIME.equals(requestType)) {
            if (requestedMinutes <= 0) {
                throw new IllegalArgumentException("Requested minutes must be positive");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("A reason is required for extra time");
            }
        } else if (requestedMinutes != 0) {
            throw new IllegalArgumentException("Registration requests cannot request minutes");
        }
        this.requestId = nextId;
        nextId++;
        this.username = username.trim();
        this.requestType = requestType;
        this.status = PENDING;
        this.requestedMinutes = requestedMinutes;
        if (reason == null) {
            this.reason = "";
        } else {
            this.reason = reason.trim();
        }
    }

    public AccessRequest(int requestId, String username, String requestType, String status,
            int requestedMinutes, String reason) {
        this(username, requestType, requestedMinutes, reason);
        if (!PENDING.equals(status) && !APPROVED.equals(status) && !REJECTED.equals(status)) {
            throw new IllegalArgumentException("Invalid request status");
        }
        this.requestId = requestId;
        this.status = status;
        if (requestId >= nextId) {
            nextId = requestId + 1;
        }
    }

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

    public void approve() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending requests can be approved");
        }
        status = APPROVED;
    }

    public void reject() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        status = REJECTED;
    }

    @Override
    public String toString() {
        return "Request ID: " + requestId + " | Username: " + username + " | Type: " + requestType
                + " | Status: " + status;
    }
}
