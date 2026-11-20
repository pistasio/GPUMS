package model;

public class Administrator extends User {

    public Administrator(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    public Administrator(String username, String password, String fullName, String email, String phoneNumber) {
        super(username, password, fullName, email, phoneNumber);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public void approveStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        student.approve();
    }

    public void grantExtraMinutes(Student student, int minutes) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (!student.isApproved()) {
            throw new IllegalStateException("Student must be approved first");
        }
        student.getAllowance().addMinutes(minutes);
    }
}
