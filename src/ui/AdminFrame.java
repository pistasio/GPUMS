package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import logic.AccountManager;
import logic.Scheduler;
import model.*;

public class AdminFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private AccountManager accounts;
    private Scheduler scheduler;
    private Administrator administrator;
    private JFrame loginFrame;
    private DashboardPanel dashboard;

    private JLabel clockLabel;
    private JTextField searchField;

    private JTable requestTable;
    private JTable studentTable;
    private DefaultTableModel requestModel;
    private DefaultTableModel studentModel;

    private JButton approveButton;
    private JButton rejectButton;
    private JButton searchButton;
    private JButton grantButton;
    private JButton refreshButton;
    private JButton setupButton;
    private JButton advanceButton;
    private JButton logoutButton;

    public AdminFrame(AccountManager accounts, Scheduler scheduler,
            Administrator administrator, JFrame loginFrame) {

        super("GPU Management - Administrator");

        this.accounts = accounts;
        this.scheduler = scheduler;
        this.administrator = administrator;
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.add(
                new JLabel("Administrator: " + administrator.getFullName()),
                BorderLayout.NORTH);

        clockLabel = new JLabel();
        topPanel.add(clockLabel, BorderLayout.SOUTH);

        content.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        // Pending registration and extra-time requests
        requestModel = createTableModel(new String[] {
                "Request ID", "Student ID", "Type", "Minutes", "Reason"
        });

        requestTable = new JTable(requestModel);
        requestTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        JPanel requestsPanel = new JPanel(new BorderLayout(10, 10));
        requestsPanel.add(
                new JScrollPane(requestTable), BorderLayout.CENTER);

        JPanel requestButtons = new JPanel();

        approveButton = createButton("Approve");
        rejectButton = createButton("Reject");

        requestButtons.add(approveButton);
        requestButtons.add(rejectButton);

        requestsPanel.add(requestButtons, BorderLayout.SOUTH);
        tabs.addTab("Pending requests", requestsPanel);

        // Students and their GPU allowances
        studentModel = createTableModel(new String[] {
                "Student ID", "Name", "Approved",
                "Allocated", "Used", "Reserved", "Available"
        });

        studentTable = new JTable(studentModel);

        JPanel studentsPanel = new JPanel(new BorderLayout(10, 10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(20);
        searchButton = createButton("Search");

        searchPanel.add(new JLabel("Student ID or name:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        studentsPanel.add(searchPanel, BorderLayout.NORTH);
        studentsPanel.add(
                new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel studentButtons = new JPanel();
        grantButton = createButton("Grant extra time");
        studentButtons.add(grantButton);
        studentsPanel.add(studentButtons, BorderLayout.SOUTH);
        
        tabs.addTab("Students", studentsPanel);

        dashboard = new DashboardPanel(
                scheduler, administrator, new Runnable() {
                    @Override
                    public void run() {
                        AdminFrame.this.refresh();
                    }
                });

        tabs.addTab("Dashboard", dashboard);
        
        content.add(tabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();

        setupButton = createButton("Set starting time");
        advanceButton = createButton("Advance minute");
        refreshButton = createButton("Refresh");
        logoutButton = createButton("Log out");

        bottomPanel.add(setupButton);
        bottomPanel.add(advanceButton);
        bottomPanel.add(refreshButton);
        bottomPanel.add(logoutButton);

        content.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(950, 550);
        setLocationRelativeTo(null);

        refresh();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.addActionListener(this);
        return button;
    }

    private DefaultTableModel createTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void refresh() {
        if (dashboard != null) {
            dashboard.refresh();
        }
      
        if (scheduler.isClockStarted()) {
            clockLabel.setText(
                    "Simulation time: " + scheduler.getNowText());
        } else {
            clockLabel.setText("Simulation clock has not been started.");
        }

        setupButton.setEnabled(!scheduler.isClockStarted());
        advanceButton.setEnabled(scheduler.isClockStarted());

        requestModel.setRowCount(0);

        for (AccessRequest request : accounts.getPendingRequests()) {
            requestModel.addRow(new Object[] {
                    request.getRequestId(),
                    request.getUsername(),
                    request.getRequestType(),
                    request.getRequestedMinutes(),
                    request.getReason()
            });
        }

        studentModel.setRowCount(0);
        String search = searchField.getText().trim().toLowerCase();

        for (User user : accounts.getUsers()) {
            if (user instanceof Student) {
                Student student = (Student) user;

                if (student.getUsername().toLowerCase().contains(search)
                        || student.getFullName().toLowerCase()
                                .contains(search)) {

                    TimeAllowance allowance = student.getAllowance();

                    studentModel.addRow(new Object[] {
                            student.getUsername(),
                            student.getFullName(),
                            student.isApproved() ? "Yes" : "No",
                            allowance.getTotalMinutes(),
                            allowance.getUsedMinutes(),
                            allowance.getReservedMinutes(),
                            allowance.getAvailable()
                    });
                }
            }
        }
    }

    private void processRequest(boolean approve) {
        int row = requestTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }

        int requestId = (Integer) requestModel.getValueAt(row, 0);
        AccessRequest request = accounts.findRequest(requestId);

        if (request == null || !request.isPending()) {
            throw new IllegalArgumentException(
                    "This request is no longer pending.");
        }

        Student student = accounts.findStudent(request.getUsername());
        if (student == null) {
            throw new IllegalArgumentException("Student account not found.");
        }

        boolean registration =
                AccessRequest.REGISTRATION.equals(request.getRequestType());

        String details =
                "University ID: " + student.getUsername()
                + "\nName: " + student.getFullName()
                + "\nEmail: " + student.getEmail()
                + "\nPhone: " + student.getPhoneNumber();

        if (registration) {
            details += "\nStudent-chosen password: " + student.getPassword();
        }

        details += "\n\nRequest type: " + request.getRequestType()
                + "\nRequested minutes: " + request.getRequestedMinutes()
                + "\nReason: " + request.getReason();

        JTextArea information = new JTextArea(details, 12, 40);
        information.setEditable(false);
        information.setLineWrap(true);
        information.setWrapStyleWord(true);
        information.setCaretPosition(0);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JScrollPane(information), BorderLayout.CENTER);

        JTextField allocationField = new JTextField(
                String.valueOf(student.getAllowance().getTotalMinutes()), 8);

        if (approve && registration) {
            JPanel allocationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            allocationPanel.add(new JLabel("Initial GPU minutes:"));
            allocationPanel.add(allocationField);
            panel.add(allocationPanel, BorderLayout.SOUTH);
        }

        String action = approve ? "Approve" : "Reject";
        int answer = JOptionPane.showConfirmDialog(
                this, panel,
                action + " request " + requestId + "?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (approve && registration) {
            int initialMinutes;
            try {
                initialMinutes = Integer.parseInt(
                        allocationField.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Initial GPU minutes must be a whole number.");
            }

            accounts.approveRegistration(
                    requestId, administrator, initialMinutes);
        } else if (approve) {
            accounts.approveRequest(requestId, administrator);
        } else {
            accounts.rejectRequest(requestId, administrator);
        }

        refresh();
    }
    
    private void grantExtraTime() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            throw new IllegalArgumentException("Select a student first.");
        }

        row = studentTable.convertRowIndexToModel(row);
        String studentId = (String) studentModel.getValueAt(row, 0);
        Student student = accounts.findStudent(studentId);

        if (student == null || !student.isApproved()) {
            throw new IllegalArgumentException(
                    "Extra time can only be granted to an approved student.");
        }

        JTextField minutesField = new JTextField("20");
        JTextField reasonField = new JTextField(25);

        JPanel fields = new JPanel(new GridLayout(2, 2, 10, 10));
        fields.add(new JLabel("Additional GPU minutes:"));
        fields.add(minutesField);
        fields.add(new JLabel("Reason:"));
        fields.add(reasonField);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel(
                student.getFullName() + " (" + studentId + ")"),
                BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);

        int answer = JOptionPane.showConfirmDialog(
                this, panel,
                "Grant extra GPU time",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(minutesField.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Additional minutes must be a whole number.");
        }

        accounts.grantExtraTime(
                studentId, minutes, reasonField.getText(), administrator);

        refresh();

        JOptionPane.showMessageDialog(
                this,
                minutes + " GPU minutes granted to " + studentId
                        + ".\nThe reason has been recorded.",
                "Time granted",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        loginFrame.setVisible(true);
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object source = e.getSource();

            if (source == approveButton) {
                processRequest(true);

            } else if (source == rejectButton) {
                processRequest(false);

            } else if (source == grantButton) {
                grantExtraTime();

            } else if (source == setupButton) {
                ClockSetupDialog dialog =
                        new ClockSetupDialog(this, scheduler);

                dialog.setVisible(true);
                refresh();

            } else if (source == advanceButton) {
                scheduler.advanceMinute();
                refresh();

            } else if (source == searchButton
                    || source == refreshButton) {
                refresh();

            } else if (source == logoutButton) {
                logout();
            }

        } catch (IllegalArgumentException | IllegalStateException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Action failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}