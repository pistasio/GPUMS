package model;
import java.util.Objects;

public abstract class User {
	private final String username;
    private String password;
    private final String fullName;

    public User(String username, String password, String fullName) {

        if (isBlank(username) || isBlank(password) || isBlank(fullName)) {
            throw new IllegalArgumentException(
                "Username, password and full name are required"
            );
        }

        this.username = username.trim();
        this.password = password;
        this.fullName = fullName.trim();
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean checkPassword(String enteredPassword) {
        return Objects.equals(password, enteredPassword);
    }

    public void changePassword(String oldPassword, String newPassword) {

        if (!checkPassword(oldPassword)) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        if (isBlank(newPassword)) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        this.password = newPassword;
    }

    public abstract String getRole();

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
