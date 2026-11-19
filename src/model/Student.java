
package model;

public class Student extends User {

    private boolean approved;
    private final TimeAllowance allowance;

    public Student(String username, String password, String fullName) {
        super(username, password, fullName);

        this.approved = false;
        this.allowance = new TimeAllowance(100);
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
            throw new IllegalStateException(
                "Student account is already approved"
            );
        }

        approved = true;
    }

    public TimeAllowance getAllowance() {
        return allowance;
    }
}
