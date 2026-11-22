package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import logic.AccountManager;
import logic.Scheduler;
import model.*;

public class StudentFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private AccountManager accounts;
    private Scheduler scheduler;
    private Student student;
    private JFrame loginFrame;

    private JLabel clockLabel;
    private JLabel allowanceLabel;

    private JTextField titleField;
    private JTextField applicationField;
    private JTextField descriptionField;
    private JTextField searchField;
    private JComboBox<TaskSize> sizeBox;

    private JTable jobTable;
    private DefaultTableModel jobModel;

    private JButton submitButton;
    private JButton searchButton;
    private JButton detailsButton;
    private JButton cancelButton;
    private JButton requestButton;
    private JButton refreshButton;
    private JButton logoutButton;

    public StudentFrame(AccountManager accounts, Scheduler scheduler,
            Student student, JFrame loginFrame) {

        super("GPU Management - Student");

        this.accounts = accounts;
        this.scheduler = scheduler;
        this.student = student;
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

        JPanel heading = new JPanel(new GridLayout(3, 1, 5, 5));
        heading.add(new JLabel("Student: " + student.getFullName()));

        clockLabel = new JLabel();
        allowanceLabel = new JLabel();

        heading.add(clockLabel);
        heading.add(allowanceLabel);
        content.add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        JPanel submitPanel = new JPanel(new BorderLayout(10, 10));
        JPanel fields = new JPanel(new GridLayout(4, 2, 10, 10));

        titleField = new JTextField();
        applicationField = new JTextField();
        descriptionField = new JTextField();
        sizeBox = new JComboBox<TaskSize>(TaskSize.getAllSizes());

        fields.add(new JLabel("Job title:"));
        fields.add(titleField);
        fields.add(new JLabel("Application name:"));
        fields.add(applicationField);
        fields.add(new JLabel("Description:"));
        fields.add(descriptionField);
        fields.add(new JLabel("Task size / estimated duration:"));
        fields.add(sizeBox);

        submitPanel.add(fields, BorderLayout.NORTH);

        submitButton = createButton("Submit job");
        JPanel submitButtons = new JPanel();
        submitButtons.add(submitButton);
        submitPanel.add(submitButtons, BorderLayout.SOUTH);

        tabs.addTab("Submit job", submitPanel);

        JPanel jobsPanel = new JPanel(new BorderLayout(10, 10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(22);
        searchButton = createButton("Search");

        searchPanel.add(new JLabel("Job ID, title or application:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        jobsPanel.add(searchPanel, BorderLayout.NORTH);

        jobModel = new DefaultTableModel(new String[] {
                "Job ID", "Title", "Application", "Status", "Duration",
                "GPU", "Queue position", "Start", "Finish", "Wait (min)",
                "Charged"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        jobTable = new JTable(jobModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jobsPanel.add(new JScrollPane(jobTable), BorderLayout.CENTER);

        JPanel jobButtons = new JPanel();
        detailsButton = createButton("View details");
        cancelButton = createButton("Cancel selected job");

        jobButtons.add(detailsButton);
        jobButtons.add(cancelButton);
        jobsPanel.add(jobButtons, BorderLayout.SOUTH);

        tabs.addTab("My jobs", jobsPanel);
        content.add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        requestButton = createButton("Request extra time");
        refreshButton = createButton("Refresh");
        logoutButton = createButton("Log out");

        bottom.add(requestButton);
        bottom.add(refreshButton);
        bottom.add(logoutButton);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(1250, 600);
        setLocationRelativeTo(null);

        refresh();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.addActionListener(this);
        return button;
    }

    private void refresh() {
        clockLabel.setText("Simulation time: " + scheduler.getNowText());

        TimeAllowance allowance = student.getAllowance();

        allowanceLabel.setText(
                "GPU minutes — Allocated: " + allowance.getTotalMinutes()
                + " | Used: " + allowance.getUsedMinutes()
                + " | Reserved: " + allowance.getReservedMinutes()
                + " | Available: " + allowance.getAvailable());

        submitButton.setEnabled(scheduler.isClockStarted());

        jobModel.setRowCount(0);

        for (Job job : scheduler.searchJobs(
                student.getUsername(), searchField.getText())) {

            String gpu = "-";
            String position = "-";
            int wait = job.getQueueWait(scheduler.getNow());

            if (job.getAssignedGpuId() != 0) {
                gpu = String.valueOf(job.getAssignedGpuId());
            }

            if (job.getStatus() == JobStatus.QUEUED) {
                position = String.valueOf(scheduler.getQueuePosition(job));
                wait = scheduler.getRemainingWait(job);
            }

            String start = scheduler.getPredictedStartText(job);
            String finish = scheduler.getPredictedFinishText(job);

            if (start.isEmpty()) {
                start = "-";
            }
            if (finish.isEmpty()) {
                finish = "-";
            }

            jobModel.addRow(new Object[] {
                    job.getJobId(),
                    job.getTitle(),
                    job.getApplicationName(),
                    job.getStatus().getName(),
                    job.getEstimatedDuration(),
                    gpu,
                    position,
                    start,
                    finish,
                    wait,
                    job.getChargedMinutes()
            });
        }
    }

    private String validateText(String text, String label) {
        String value = text.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }

        if (value.contains("|") || value.contains("\n")
                || value.contains("\r")) {
            throw new IllegalArgumentException(
                    label + " cannot contain | or line breaks.");
        }

        return value;
    }

    private void submitJob() {
        String title = validateText(titleField.getText(), "Title");
        String application = validateText(
                applicationField.getText(), "Application name");
        String description = validateText(
                descriptionField.getText(), "Description");

        TaskSize size = (TaskSize) sizeBox.getSelectedItem();

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Submit \"" + title + "\"?\n"
                + "Estimated duration: " + size.getDuration() + " minutes.\n"
                + "These minutes will be reserved from your allowance.",
                "Confirm submission",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        Job job = scheduler.submitJob(
                student, title, application, description, size);

        titleField.setText("");
        applicationField.setText("");
        descriptionField.setText("");
        searchField.setText("");

        refresh();

        JOptionPane.showMessageDialog(
                this,
                "Job " + job.getJobId() + " submitted.\n"
                + "Status: " + job.getStatus().getName());
    }

    private Job selectedJob() {
        int row = jobTable.getSelectedRow();

        if (row == -1) {
            throw new IllegalArgumentException("Select a job first.");
        }

        String jobId = (String) jobModel.getValueAt(row, 0);
        Job job = scheduler.findJob(jobId);

        if (job == null
                || !student.getUsername().equals(job.getStudentId())) {
            throw new IllegalArgumentException("Job not found.");
        }

        return job;
    }

    private String displayTime(int minute) {
        if (minute < 0) {
            return "-";
        }
        return scheduler.formatTime(minute);
    }

    private void showDetails() {
        Job job = selectedJob();

        String details =
                "Job: " + job.getJobId()
                + "\nTitle: " + job.getTitle()
                + "\nApplication: " + job.getApplicationName()
                + "\nDescription: " + job.getDescription()
                + "\nStatus: " + job.getStatus().getName()
                + "\nTask size: " + job.getTaskSize()
                + "\nSubmitted: " + displayTime(job.getSubmissionTime())
                + "\nGPU: " + (job.getAssignedGpuId() == 0
                        ? "-" : job.getAssignedGpuId())
                + "\nActual start: " + displayTime(job.getStartTime())
                + "\nActual finish: " + displayTime(job.getFinishTime())
                + "\nRuntime: " + job.getRuntime(scheduler.getNow())
                + " minutes"
                + "\nQueue wait: " + job.getQueueWait(scheduler.getNow())
                + " minutes"
                + "\nCharged: " + job.getChargedMinutes() + " minutes";

        if (job.getStatus() == JobStatus.QUEUED) {
            details +=
                    "\nQueue position: " + scheduler.getQueuePosition(job)
                    + "\nPredicted start: "
                    + scheduler.getPredictedStartText(job)
                    + "\nPredicted finish: "
                    + scheduler.getPredictedFinishText(job)
                    + "\nRemaining wait: "
                    + scheduler.getRemainingWait(job) + " minutes";

        } else if (job.getStatus() == JobStatus.RUNNING) {
            details +=
                    "\nExpected finish: "
                    + scheduler.getPredictedFinishText(job)
                    + "\nRemaining runtime: "
                    + job.getRemainingRuntime(scheduler.getNow())
                    + " minutes";

        } else if (job.getStatus() == JobStatus.CANCELLED) {
            details +=
                    "\nCancelled at: " + displayTime(job.getCancelTime())
                    + "\nCancelled by: " + job.getCancelledBy()
                    + "\nReason: " + job.getCancelReason();
        }

        JTextArea area = new JTextArea(details, 20, 48);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this, new JScrollPane(area),
                "Job details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelJob() {
        Job job = selectedJob();

        if (job.getStatus() != JobStatus.QUEUED
                && job.getStatus() != JobStatus.RUNNING) {
            throw new IllegalArgumentException(
                    "Only queued or running jobs can be cancelled.");
        }

        String reason = JOptionPane.showInputDialog(
                this, "Reason for cancelling " + job.getJobId() + ":");

        if (reason == null) {
            return;
        }

        reason = validateText(reason, "Cancellation reason");

        int charge = job.getStatus() == JobStatus.RUNNING
                ? job.getRuntime(scheduler.getNow()) : 0;

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Cancel " + job.getJobId() + "?\n"
                + "Charge: " + charge + " minutes.\n"
                + "Its reserved minutes will be released.",
                "Confirm cancellation",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        scheduler.cancelJob(job, student.getUsername(), reason);
        refresh();
    }

    private void requestExtraTime() {
        JTextField minutesField = new JTextField();
        JTextField reasonField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Additional minutes:"));
        panel.add(minutesField);
        panel.add(new JLabel("Reason:"));
        panel.add(reasonField);

        int answer = JOptionPane.showConfirmDialog(
                this, panel, "Request extra time",
                JOptionPane.OK_CANCEL_OPTION);

        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        int minutes;

        try {
            minutes = Integer.parseInt(minutesField.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Enter a whole number of minutes.");
        }

        String reason = validateText(reasonField.getText(), "Reason");

        accounts.requestExtraTime(student.getUsername(), minutes, reason);

        JOptionPane.showMessageDialog(
                this, "Request submitted for administrator approval.");

        refresh();
    }

    private void logout() {
        loginFrame.setVisible(true);
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object source = e.getSource();

            if (source == submitButton) {
                submitJob();
            } else if (source == detailsButton) {
                showDetails();
            } else if (source == cancelButton) {
                cancelJob();
            } else if (source == requestButton) {
                requestExtraTime();
            } else if (source == searchButton
                    || source == refreshButton) {
                refresh();
            } else if (source == logoutButton) {
                logout();
            }

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Action failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}