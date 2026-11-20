
package model;

import java.util.Objects;

public abstract class User {

    private final String username;
    private String password;
    private final String fullName;
    private final String email;
    private final String phoneNumber;

    // Original constructor: keeps existing classes working
    public User(String username, String password, String fullName) {
        this(username, password, fullName, "", "");
    }

    // Full constructor with all required account details
    public User(String username, String password, String fullName,
                String email, String phoneNumber) {

        if (isBlank(username) || isBlank(password)
                || isBlank(fullName)) {
            throw new IllegalArgumentException(
                "Username, password and full name are required"
            );
        }

        if (email == null || phoneNumber == null) {
            throw new IllegalArgumentException(
                "Email and phone number cannot be null"
            );
        }

        if (username.contains("|") || password.contains("|")
                || fullName.contains("|") || email.contains("|")
                || phoneNumber.contains("|")) {
            throw new IllegalArgumentException(
                "Account details cannot contain |"
            );
        }

        this.username = username.trim();
        this.password = password;
        this.fullName = fullName.trim();
        this.email = email.trim();
        this.phoneNumber = phoneNumber.trim();
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public boolean checkPassword(String enteredPassword) {
        return Objects.equals(password, enteredPassword);
    }

    public void changePassword(String oldPassword, String newPassword) {

        if (!checkPassword(oldPassword)) {
            throw new IllegalArgumentException(
                "Incorrect current password"
            );
        }

        if (isBlank(newPassword)) {
            throw new IllegalArgumentException(
                "New password cannot be empty"
            );
        }

        this.password = newPassword;
    }

    public abstract String getRole();

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    

public String getPasswordHash() {
    return hashPassword(password);
}

private static String hashPassword(String password) {
    try {
        java.security.MessageDigest digest =
            java.security.MessageDigest.getInstance("SHA-256");

        byte[] bytes = digest.digest(
            password.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            result.append(String.format("%02x", b & 0xff));
        }

        return result.toString();

    } catch (java.security.NoSuchAlgorithmException e) {
        throw new IllegalStateException(
            "Password hashing unavailable", e
        );
    }
}

}
