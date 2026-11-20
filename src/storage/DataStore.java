
package storage;

import java.io.*;
import java.util.*;

import logic.*;

import model.*;

public class DataStore {

    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String REQUESTS_FILE = "requests.txt";
    private static final String JOBS_FILE = "jobs.txt";
    private static final String CLOCK_FILE = "clock.txt";

    // Save all student and administrator accounts
    public static void saveAccounts(AccountManager manager)
            throws IOException {

        if (manager == null) {
            throw new IllegalArgumentException(
                "Account manager cannot be null"
            );
        }

        PrintWriter writer = new PrintWriter(
            new FileWriter(ACCOUNTS_FILE)
        );

        try {
            for (User user : manager.getUsers()) {

                if (user instanceof Student) {

                    Student student = (Student) user;
                    TimeAllowance allowance = student.getAllowance();

                    writer.println(
                        student.getUsername() + "|" +
                        student.getPassword() + "|" +
                        student.getFullName() + "|" +
                        student.getEmail() + "|" +
                        student.getPhoneNumber() + "|" +
                        student.getRole() + "|" +
                        student.isApproved() + "|" +
                        allowance.getTotalMinutes() + "|" +
                        allowance.getUsedMinutes() + "|" +
                        allowance.getReservedMinutes()
                    );

                } else if (user instanceof Administrator) {

                    writer.println(
                        user.getUsername() + "|" +
                        user.getPassword() + "|" +
                        user.getFullName() + "|" +
                        user.getEmail() + "|" +
                        user.getPhoneNumber() + "|" +
                        user.getRole()
                    );
                }
            }

            if (writer.checkError()) {
                throw new IOException("Error saving accounts");
            }

        } finally {
            writer.close();
        }
    }

    // Load saved accounts
    public static AccountManager loadAccounts() throws IOException {

        AccountManager manager = new AccountManager();

        ArrayList<String> lines = readLines(ACCOUNTS_FILE);

        try {
            for (String line : lines) {

                String[] data = line.split("\\|", -1);

                if (data.length != 10 && data.length != 6) {
                    throw new IOException("Invalid account record");
                }

                String username = data[0];
                String password = data[1];
                String fullName = data[2];
                String email = data[3];
                String phone = data[4];
                String role = data[5];

                if (role.equals("STUDENT") && data.length == 10) {

                    if (!data[6].equals("true")
                            && !data[6].equals("false")) {
                        throw new IOException("Invalid approval status");
                    }

                    boolean approved = Boolean.parseBoolean(data[6]);

                    int total = Integer.parseInt(data[7]);
                    int used = Integer.parseInt(data[8]);
                    int reserved = Integer.parseInt(data[9]);

                    TimeAllowance allowance =
                        new TimeAllowance(total, used, reserved);

                    Student student;

                    if (email.isEmpty() && phone.isEmpty()) {
                        student = new Student(
                            username, password, fullName,
                            approved, allowance
                        );
                    } else {
                        student = new Student(
                            username, password, fullName,
                            email, phone, approved, allowance
                        );
                    }

                    manager.restoreUser(student);

                } else if (role.equals("ADMIN") && data.length == 6) {

                    Administrator admin = new Administrator(
                        username, password, fullName, email, phone
                    );

                    manager.restoreUser(admin);

                } else {
                    throw new IOException("Invalid account type");
                }
            }

        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid account data", e);
        }

        manager.addDefaultAdministrator();

        return manager;
    }

    // Save registration and extra-time requests
    public static void saveRequests(AccountManager manager)
            throws IOException {

        if (manager == null) {
            throw new IllegalArgumentException(
                "Account manager cannot be null"
            );
        }

        PrintWriter writer = new PrintWriter(
            new FileWriter(REQUESTS_FILE)
        );

        try {
            for (AccessRequest request : manager.getRequests()) {

                writer.println(
                    request.getRequestId() + "|" +
                    request.getUsername() + "|" +
                    request.getRequestType() + "|" +
                    request.getStatus() + "|" +
                    request.getRequestedMinutes() + "|" +
                    request.getReason()
                );
            }

            if (writer.checkError()) {
                throw new IOException("Error saving requests");
            }

        } finally {
            writer.close();
        }
    }

    // Load requests into an existing account manager
    public static void loadRequests(AccountManager manager)
            throws IOException {

        if (manager == null) {
            throw new IllegalArgumentException(
                "Account manager cannot be null"
            );
        }

        ArrayList<String> lines = readLines(REQUESTS_FILE);

        try {
            for (String line : lines) {

                String[] data = line.split("\\|", -1);

                if (data.length != 6) {
                    throw new IOException("Invalid request record");
                }

                int id = Integer.parseInt(data[0]);
                String username = data[1];
                String type = data[2];
                String status = data[3];
                int minutes = Integer.parseInt(data[4]);
                String reason = data[5];

                if (manager.findStudent(username) == null) {
                    throw new IOException(
                        "Request belongs to unknown student"
                    );
                }

                AccessRequest request = new AccessRequest(
                    id, username, type, status, minutes, reason
                );

                manager.restoreRequest(request);
            }

        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid request data", e);
        }
    }

    // Save scheduler clock and jobs
    public static void saveScheduler(Scheduler scheduler)
            throws IOException {

        if (scheduler == null) {
            throw new IllegalArgumentException(
                "Scheduler cannot be null"
            );
        }

        PrintWriter clockWriter = new PrintWriter(
            new FileWriter(CLOCK_FILE)
        );

        try {
            clockWriter.println(scheduler.toFileLine());

            if (clockWriter.checkError()) {
                throw new IOException("Error saving clock");
            }
        } finally {
            clockWriter.close();
        }

        PrintWriter jobWriter = new PrintWriter(
            new FileWriter(JOBS_FILE)
        );

        try {
            for (Job job : scheduler.getAllJobs()) {
                jobWriter.println(job.toFileLine());
            }

            if (jobWriter.checkError()) {
                throw new IOException("Error saving jobs");
            }
        } finally {
            jobWriter.close();
        }
    }

    // Restore scheduler clock and jobs
    public static void loadScheduler(Scheduler scheduler)
            throws IOException {

        if (scheduler == null) {
            throw new IllegalArgumentException(
                "Scheduler cannot be null"
            );
        }

        ArrayList<String> clockLines = readLines(CLOCK_FILE);

        if (clockLines.size() != 1) {
            throw new IOException("Invalid clock file");
        }

        ArrayList<Job> savedJobs = new ArrayList<Job>();

        try {
            for (String line : readLines(JOBS_FILE)) {
                savedJobs.add(Job.fromFileLine(line));
            }

            scheduler.restore(clockLines.get(0), savedJobs);

        } catch (IllegalArgumentException e) {
            throw new IOException(
                "Invalid scheduler save data", e
            );
        }
    }

    // Read a text file line by line
    private static ArrayList<String> readLines(String filename)
            throws IOException {

        ArrayList<String> lines = new ArrayList<String>();

        Scanner scanner = new Scanner(
            new FileReader(filename)
        );

        try {
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();

                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } finally {
            scanner.close();
        }

        return lines;
    }

    // Check whether saved files exist
    public static boolean saveFilesExist() {

        return new File(ACCOUNTS_FILE).exists()
            && new File(REQUESTS_FILE).exists()
            && new File(JOBS_FILE).exists()
            && new File(CLOCK_FILE).exists();
    }
}
