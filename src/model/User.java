package model;

public abstract class User {

    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;

    public User(String username, String password, String fullName) {
        this(username, password, fullName, "", "");
    }

    public User(String username, String password, String fullName, String email, String phoneNumber) {
        if (isBlank(username) || isBlank(password) || isBlank(fullName)) {
            throw new IllegalArgumentException("Username, password and full name are required");
        }
        if (email == null || phoneNumber == null) {
            throw new IllegalArgumentException("Email and phone number cannot be null");
        }
        if (username.contains("|") || password.contains("|") || fullName.contains("|")
                || email.contains("|") || phoneNumber.contains("|")) {
            throw new IllegalArgumentException("Account details cannot contain |");
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

    public String getPassword() {
        return password;
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

    public boolean checkPassword(String enteredPassword) {
        return password.equals(enteredPassword);
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!checkPassword(oldPassword)) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        if (newPassword.contains("|")) {
            throw new IllegalArgumentException("Account details cannot contain |");
        }
        password = newPassword;
    }

    public abstract String getRole();

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
