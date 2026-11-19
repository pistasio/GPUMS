
package logic;

import java.util.ArrayList;

import model.User;
import model.Student;
import model.Administrator;
import model.AccessRequest;

public class AccountManager {

    private ArrayList<User> users;
    private ArrayList<AccessRequest> requests;

    public AccountManager() {
        users = new ArrayList<User>();
        requests = new ArrayList<AccessRequest>();
    }

    // Find any user by usernam
    public User findUser(String username) {
        if (username == null) {
            return null;
        }

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    // Find a student account
    public Student findStudent(String username) {
        User user = findUser(username);

        if (user instanceof Student) {
            return (Student) user;
        }
        return null;
    }

    // Register a new student
    public Student registerStudent(String username,
                                   String password,
                                   String fullName) {

        if (findUser(username) != null) {
            throw new IllegalArgumentException(
                "Username already exists"
            );
        }
       
       

        Student student = new Student(username, password, fullName);

        users.add(student);

        AccessRequest request = new AccessRequest(
            username,
            AccessRequest.REGISTRATION,
            0,
            "New student registration"
        );

        requests.add(request);
        return student;
    }
    

public Student registerStudent(String username,
                               String password,
                               String fullName,
                               String email,
                               String phoneNumber) {

    // Prevent duplicate university IDs
    if (findUser(username) != null) {
        throw new IllegalArgumentException(
            "A student with this university ID already exists"
        );
    }

    // Create student with all required details
    Student student = new Student(
        username,
        password,
        fullName,
        email,
        phoneNumber
    );

    // Create a registration request
    AccessRequest request = new AccessRequest(
        username,
        AccessRequest.REGISTRATION,
        0,
        "New student registration"
    );

    users.add(student);
    requests.add(request);

    return student;
}


    // Add an administrator
    public void addAdministrator(Administrator admin) {
        if (admin == null) {
            throw new IllegalArgumentException(
                "Administrator cannot be null"
            );
        }

        if (findUser(admin.getUsername()) != null) {
            throw new IllegalArgumentException(
                "Username already exists"
            );
        }

        users.add(admin);
    }

    // Authenticate an existing user
    public User login(String username, String password) {
        User user = findUser(username);

        if (user == null || !user.checkPassword(password)) {
            throw new IllegalArgumentException(
                "Invalid username or password"
            );
        }

        if (user instanceof Student) {
            Student student = (Student) user;

            if (!student.isApproved()) {
                throw new IllegalStateException(
                    "Student account is not approved"
                );
            }
        }

        return user;
    }

    // Submit an additional GPU time request
    public AccessRequest requestExtraTime(String username,
                                          int minutes,
                                          String reason) {

        Student student = findStudent(username);

        if (student == null || !student.isApproved()) {
            throw new IllegalArgumentException(
                "Approved student account required"
            );
        }

        AccessRequest request = new AccessRequest(
            username,
            AccessRequest.EXTRA_TIME,
            minutes,
            reason
        );

        requests.add(request);
        return request;
    }

    // Find a request using its ID
    public AccessRequest findRequest(int requestId) {
        for (AccessRequest request : requests) {
            if (request.getRequestId() == requestId) {
                return request;
            }
        }
        return null;
    }

    // Approve a pending request
    public void approveRequest(int requestId,
                               Administrator admin) {

        checkAdmin(admin);

        AccessRequest request = findRequest(requestId);

        if (request == null) {
            throw new IllegalArgumentException(
                "Request not found"
            );
        }

        if (!request.isPending()) {
            throw new IllegalStateException(
                "Request has already been processed"
            );
        }

        Student student = findStudent(request.getUsername());

        if (student == null) {
            throw new IllegalStateException(
                "Student account not found"
            );
        }

        if (request.getRequestType().equals(
                AccessRequest.REGISTRATION)) {

            admin.approveStudent(student);

        } else if (request.getRequestType().equals(
                AccessRequest.EXTRA_TIME)) {

            admin.grantExtraMinutes(
                student,
                request.getRequestedMinutes()
            );
        }

        request.approve();
    }

    // Reject a pending request
    public void rejectRequest(int requestId,
                              Administrator admin) {

        checkAdmin(admin);

        AccessRequest request = findRequest(requestId);

        if (request == null) {
            throw new IllegalArgumentException(
                "Request not found"
            );
        }

        if (!request.isPending()) {
            throw new IllegalStateException(
                "Request has already been processed"
            );
        }

        request.reject();
    }

    // Check administrator authorization
    private void checkAdmin(Administrator admin) {
        if (admin == null ||
            findUser(admin.getUsername()) != admin) {

            throw new IllegalArgumentException(
                "Registered administrator required"
            );
        }
    }

    // Return copies of account and request lists
    public ArrayList<User> getUsers() {
        return new ArrayList<User>(users);
    }

    public ArrayList<AccessRequest> getRequests() {
        return new ArrayList<AccessRequest>(requests);
    }

    public ArrayList<AccessRequest> getPendingRequests() {
        ArrayList<AccessRequest> pending =
            new ArrayList<AccessRequest>();

        for (AccessRequest request : requests) {
            if (request.isPending()) {
                pending.add(request);
            }
        }

        return pending;
    }
    

    // Restore an existing user from saved data
    public void restoreUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                "User cannot be null"
            );
        }

        if (findUser(user.getUsername()) != null) {
            throw new IllegalArgumentException(
                "Duplicate username in saved data"
            );
        }

        users.add(user);
    }

    // Restore an existing access request
    public void restoreRequest(AccessRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Request cannot be null"
            );
        }

        if (findRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException(
                "Duplicate request ID in saved data"
            );
        }

        requests.add(request);
    }

}
