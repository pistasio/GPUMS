
package model;

public class Student extends User {

    private boolean approved;
    private final TimeAllowance allowance;

    public Student(String username, String password, String fullName) {
        super(username, password, fullName);

        this.approved = false;
        this.allowance = new TimeAllowance(100);
    }
	
	public Student(String username, String password,
	               String fullName, boolean approved,
	               TimeAllowance allowance) {
	
	    super(username, password, fullName);
	
	    if (allowance == null) {
	        throw new IllegalArgumentException(
	            "Time allowance cannot be null"
	        );
	    }
	
	    this.approved = approved;
	    this.allowance = allowance;
	}
	

public Student(String username, String password,
               String fullName, String email,
               String phoneNumber, boolean approved,
               TimeAllowance allowance) {

    super(username, password, fullName, email, phoneNumber);

    if (email == null || email.trim().isEmpty()
            || phoneNumber == null || phoneNumber.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Email and phone number are required"
        );
    }

    if (allowance == null) {
        throw new IllegalArgumentException(
            "Time allowance cannot be null"
        );
    }

    this.approved = approved;
    this.allowance = allowance;
}

	

public Student(String username, String password,
               String fullName, String email,
               String phoneNumber) {

    super(username, password, fullName, email, phoneNumber);

    if (email == null || email.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Email is required"
        );
    }

    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Phone number is required"
        );
    }

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
