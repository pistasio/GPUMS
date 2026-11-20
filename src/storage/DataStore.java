package storage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import logic.AccountManager;
import logic.Scheduler;
import model.AccessRequest;
import model.Administrator;
import model.Job;
import model.Student;
import model.TimeAllowance;
import model.User;

public class DataStore {

    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String REQUESTS_FILE = "requests.txt";
    private static final String JOBS_FILE = "jobs.txt";
    private static final String CLOCK_FILE = "clock.txt";

    public static void saveAccounts(AccountManager manager) throws IOException {
        if (manager == null) {
            throw new IllegalArgumentException("Account manager cannot be null");
        }
        PrintWriter writer = new PrintWriter(ACCOUNTS_FILE);
        for (User user : manager.getUsers()) {
            String line = user.getUsername() + "|" + user.getPassword() + "|" + user.getFullName() + "|"
                    + user.getEmail() + "|" + user.getPhoneNumber() + "|" + user.getRole();
            if (user instanceof Student) {
                Student student = (Student) user;
                TimeAllowance allowance = student.getAllowance();
                line = line + "|" + student.isApproved() + "|" + allowance.getTotalMinutes() + "|"
                        + allowance.getUsedMinutes() + "|" + allowance.getReservedMinutes();
            }
            writer.println(line);
        }
        writer.close();
    }

    public static AccountManager loadAccounts() throws IOException {
        AccountManager manager = new AccountManager();
        try {
            for (String line : readLines(ACCOUNTS_FILE)) {
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
                    if (!data[6].equals("true") && !data[6].equals("false")) {
                        throw new IOException("Invalid approval status");
                    }
                    boolean approved = Boolean.parseBoolean(data[6]);
                    TimeAllowance allowance = new TimeAllowance(Integer.parseInt(data[7]),
                            Integer.parseInt(data[8]), Integer.parseInt(data[9]));
                    Student student;
                    if (email.isEmpty() && phone.isEmpty()) {
                        student = new Student(username, password, fullName, approved, allowance);
                    } else {
                        student = new Student(username, password, fullName, email, phone, approved, allowance);
                    }
                    manager.restoreUser(student);
                } else if (role.equals("ADMIN") && data.length == 6) {
                    manager.restoreUser(new Administrator(username, password, fullName, email, phone));
                } else {
                    throw new IOException("Invalid account type");
                }
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid account data: " + e.getMessage());
        }
        manager.addDefaultAdministrator();
        return manager;
    }

    public static void saveRequests(AccountManager manager) throws IOException {
        if (manager == null) {
            throw new IllegalArgumentException("Account manager cannot be null");
        }
        PrintWriter writer = new PrintWriter(REQUESTS_FILE);
        for (AccessRequest request : manager.getRequests()) {
            writer.println(request.getRequestId() + "|" + request.getUsername() + "|" + request.getRequestType()
                    + "|" + request.getStatus() + "|" + request.getRequestedMinutes() + "|" + request.getReason());
        }
        writer.close();
    }

    public static void loadRequests(AccountManager manager) throws IOException {
        if (manager == null) {
            throw new IllegalArgumentException("Account manager cannot be null");
        }
        try {
            for (String line : readLines(REQUESTS_FILE)) {
                String[] data = line.split("\\|", -1);
                if (data.length != 6) {
                    throw new IOException("Invalid request record");
                }
                String username = data[1];
                if (manager.findStudent(username) == null) {
                    throw new IOException("Request belongs to unknown student");
                }
                AccessRequest request = new AccessRequest(Integer.parseInt(data[0]), username, data[2], data[3],
                        Integer.parseInt(data[4]), data[5]);
                manager.restoreRequest(request);
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid request data: " + e.getMessage());
        }
    }

    public static void saveScheduler(Scheduler scheduler) throws IOException {
        if (scheduler == null) {
            throw new IllegalArgumentException("Scheduler cannot be null");
        }
        PrintWriter clockWriter = new PrintWriter(CLOCK_FILE);
        clockWriter.println(scheduler.toFileLine());
        clockWriter.close();
        PrintWriter jobWriter = new PrintWriter(JOBS_FILE);
        for (Job job : scheduler.getAllJobs()) {
            jobWriter.println(job.toFileLine());
        }
        jobWriter.close();
    }

    public static void loadScheduler(Scheduler scheduler) throws IOException {
        if (scheduler == null) {
            throw new IllegalArgumentException("Scheduler cannot be null");
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
            throw new IOException("Invalid scheduler save data: " + e.getMessage());
        }
    }

    public static boolean saveFilesExist() {
        return new File(ACCOUNTS_FILE).exists() && new File(REQUESTS_FILE).exists() && new File(JOBS_FILE).exists()
                && new File(CLOCK_FILE).exists();
    }

    private static ArrayList<String> readLines(String fileName) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        Scanner inFile = new Scanner(new File(fileName));
        while (inFile.hasNextLine()) {
            String line = inFile.nextLine();
            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }
        inFile.close();
        return lines;
    }
}
