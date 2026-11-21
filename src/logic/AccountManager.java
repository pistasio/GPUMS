package logic;

import java.util.ArrayList;

import model.AccessRequest;
import model.Administrator;
import model.Student;
import model.User;

public class AccountManager {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "gpu2026";

    private ArrayList<User> users;
    private ArrayList<AccessRequest> requests;

    public AccountManager() {
        users = new ArrayList<User>();
        requests = new ArrayList<AccessRequest>();
    }

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

    public Student findStudent(String username) {
        User user = findUser(username);
        if (user instanceof Student) {
            return (Student) user;
        }
        return null;
    }

    public Student registerStudent(String username, String password, String fullName) {
        if (findUser(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        Student student = new Student(username, password, fullName);
        AccessRequest request = new AccessRequest(username, AccessRequest.REGISTRATION, 0, "New student registration");
        users.add(student);
        requests.add(request);
        return student;
    }

    public Student registerStudent(String username, String password, String fullName, String email,
            String phoneNumber) {
        if (findUser(username) != null) {
            throw new IllegalArgumentException("A student with this university ID already exists");
        }
        Student student = new Student(username, password, fullName, email, phoneNumber);
        AccessRequest request = new AccessRequest(username, AccessRequest.REGISTRATION, 0, "New student registration");
        users.add(student);
        requests.add(request);
        return student;
    }

    public void addDefaultAdministrator() {
        if (findUser(ADMIN_USERNAME) == null) {
            users.add(new Administrator(ADMIN_USERNAME, ADMIN_PASSWORD, "System Administrator"));
        }
    }

    public void addAdministrator(Administrator admin) {
        if (admin == null) {
            throw new IllegalArgumentException("Administrator cannot be null");
        }
        if (findUser(admin.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        users.add(admin);
    }

    public User login(String username, String password) {
        User user = findUser(username);
        if (user == null || !user.checkPassword(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (user instanceof Student) {
            Student student = (Student) user;
            if (!student.isApproved()) {
                throw new IllegalStateException("Student account is not approved");
            }
        }
        return user;
    }

    public AccessRequest requestExtraTime(String username, int minutes, String reason) {
        Student student = findStudent(username);
        if (student == null || !student.isApproved()) {
            throw new IllegalArgumentException("Approved student account required");
        }
        AccessRequest request = new AccessRequest(username, AccessRequest.EXTRA_TIME, minutes, reason);
        requests.add(request);
        return request;
    }

    public AccessRequest findRequest(int requestId) {
        for (AccessRequest request : requests) {
            if (request.getRequestId() == requestId) {
                return request;
            }
        }
        return null;
    }

    public void approveRequest(int requestId, Administrator admin) {
        checkAdmin(admin);
        AccessRequest request = findPendingRequest(requestId);
        Student student = findStudent(request.getUsername());
        if (student == null) {
            throw new IllegalStateException("Student account not found");
        }
        if (request.getRequestType().equals(AccessRequest.REGISTRATION)) {
            admin.approveStudent(student);
        } else if (request.getRequestType().equals(AccessRequest.EXTRA_TIME)) {
            admin.grantExtraMinutes(student, request.getRequestedMinutes());
        }
        request.approve();
    }
    
    public void approveRegistration(int requestId,
            Administrator admin, int initialMinutes) {
        checkAdmin(admin);

        AccessRequest request = findPendingRequest(requestId);
        if (!AccessRequest.REGISTRATION.equals(request.getRequestType())) {
            throw new IllegalArgumentException(
                    "Select a registration request.");
        }

        Student student = findStudent(request.getUsername());
        if (student == null) {
            throw new IllegalStateException("Student account not found.");
        }
        if (student.isApproved()) {
            throw new IllegalStateException(
                    "Student account is already approved.");
        }

        student.getAllowance().setTotalMinutes(initialMinutes);
        admin.approveStudent(student);
        request.approve();
    }

    public void rejectRequest(int requestId, Administrator admin) {
        checkAdmin(admin);
        AccessRequest request = findPendingRequest(requestId);
        request.reject();
    }

    private AccessRequest findPendingRequest(int requestId) {
        AccessRequest request = findRequest(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found");
        }
        if (!request.isPending()) {
            throw new IllegalStateException("Request has already been processed");
        }
        return request;
    }

    private void checkAdmin(Administrator admin) {
        if (admin == null || findUser(admin.getUsername()) != admin) {
            throw new IllegalArgumentException("Registered administrator required");
        }
    }

    public ArrayList<User> getUsers() {
        return new ArrayList<User>(users);
    }

    public ArrayList<AccessRequest> getRequests() {
        return new ArrayList<AccessRequest>(requests);
    }

    public ArrayList<AccessRequest> getPendingRequests() {
        ArrayList<AccessRequest> pending = new ArrayList<AccessRequest>();
        for (AccessRequest request : requests) {
            if (request.isPending()) {
                pending.add(request);
            }
        }
        return pending;
    }

    public void restoreUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (findUser(user.getUsername()) != null) {
            throw new IllegalArgumentException("Duplicate username in saved data");
        }
        users.add(user);
    }

    public void restoreRequest(AccessRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (findRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException("Duplicate request ID in saved data");
        }
        requests.add(request);
    }
}
