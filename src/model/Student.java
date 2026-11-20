package model;

public class Student extends User {

    private boolean approved;
    private TimeAllowance allowance;

    public Student(String username, String password, String fullName) {
        this(username, password, fullName, false, new TimeAllowance());
    }

    public Student(String username, String password, String fullName, boolean approved, TimeAllowance allowance) {
        super(username, password, fullName);
        if (allowance == null) {
            throw new IllegalArgumentException("Time allowance cannot be null");
        }
        this.approved = approved;
        this.allowance = allowance;
    }

    public Student(String username, String password, String fullName, String email, String phoneNumber) {
        this(username, password, fullName, email, phoneNumber, false, new TimeAllowance());
    }

    public Student(String username, String password, String fullName, String email, String phoneNumber,
            boolean approved, TimeAllowance allowance) {
        super(username, password, fullName, email, phoneNumber);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (allowance == null) {
            throw new IllegalArgumentException("Time allowance cannot be null");
        }
        this.approved = approved;
        this.allowance = allowance;
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    public boolean isApproved() {
        return approved;
    }

    public void approve() {
        if (approved) {
            throw new IllegalStateException("Student account is already approved");
        }
        approved = true;
    }

    public TimeAllowance getAllowance() {
        return allowance;
    }
}
