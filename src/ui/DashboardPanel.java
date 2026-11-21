package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import logic.Scheduler;
import model.*;

public class DashboardPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private Scheduler scheduler;
    private Administrator administrator;
    private Runnable refreshParent;

    private JLabel summaryLabel;
    private JLabel utilizationLabel;
    private JTextField searchField;

    private DefaultTableModel gpuModel;
    private DefaultTableModel queueModel;
    private DefaultTableModel jobModel;
    private JTable jobTable;

    private JButton searchButton;
    private JButton detailsButton;
    private JButton cancelButton;

    public DashboardPanel(Scheduler scheduler,
            Administrator administrator, Runnable refreshParent) {

        this.scheduler = scheduler;
        this.administrator = administrator;
        this.refreshParent = refreshParent;

        setLayout(new BorderLayout(10, 10));

        JPanel summaryPanel = new JPanel(new GridLayout(2, 1));
        summaryLabel = new JLabel();
        utilizationLabel = new JLabel();

        summaryPanel.add(summaryLabel);
        summaryPanel.add(utilizationLabel);
        add(summaryPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        gpuModel = createModel(new String[] {
                "GPU", "Status", "Current job", "Student ID",
                "Remaining minutes", "Expected finish"
        });

        tabs.addTab("GPUs", new JScrollPane(new JTable(gpuModel)));

        queueModel = createModel(new String[] {
                "Position", "Job ID", "Student ID", "Title",
                "Duration", "Predicted start", "Predicted finish",
                "Remaining wait"
        });

        tabs.addTab("Waiting queue",
                new JScrollPane(new JTable(queueModel)));

        JPanel jobsPanel = new JPanel(new BorderLayout(10, 10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(22);
        searchButton = createButton("Search");

        searchPanel.add(new JLabel("Job ID, title or application:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        jobsPanel.add(searchPanel, BorderLayout.NORTH);

        jobModel = createModel(new String[] {
                "Job ID", "Student ID", "Title", "Status",
                "GPU", "Runtime", "Queue wait", "Charged"
        });

        jobTable = new JTable(jobModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobsPanel.add(new JScrollPane(jobTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        detailsButton = createButton("View details");
        cancelButton = createButton("Cancel selected job");

        buttons.add(detailsButton);
        buttons.add(cancelButton);
        jobsPanel.add(buttons, BorderLayout.SOUTH);

        tabs.addTab("All jobs", jobsPanel);
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    private DefaultTableModel createModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.addActionListener(this);
        return button;
    }

    public void refresh() {
        summaryLabel.setText(
                "Queued: " + scheduler.countJobs(JobStatus.QUEUED)
                + " | Running: " + scheduler.countJobs(JobStatus.RUNNING)
                + " | Completed: " + scheduler.countJobs(JobStatus.COMPLETED)
                + " | Cancelled: " + scheduler.countJobs(JobStatus.CANCELLED));

        utilizationLabel.setText(
                "Busy GPUs: " + scheduler.getBusyGpuCount()
                + " / " + Scheduler.GPU_COUNT
                + " | Elapsed: " + scheduler.getNow() + " minutes"
                + " | GPU busy time: " + scheduler.getBusyMinutes()
                + " minutes | Utilization: " + scheduler.getUtilizationText());

        gpuModel.setRowCount(0);

        for (GPU gpu : scheduler.getGpus()) {
            Job job = gpu.getCurrentJob();

            gpuModel.addRow(new Object[] {
                    gpu.getId(),
                    gpu.isFree() ? "Available" : "Busy",
                    job == null ? "-" : job.getJobId(),
                    job == null ? "-" : job.getStudentId(),
                    gpu.getRemainingRuntime(scheduler.getNow()),
                    gpu.isFree() ? "-" : scheduler.getExpectedFinishText(gpu)
            });
        }

        queueModel.setRowCount(0);
        int position = 1;

        for (Job job : scheduler.getQueuedJobs()) {
            queueModel.addRow(new Object[] {
                    position++,
                    job.getJobId(),
                    job.getStudentId(),
                    job.getTitle(),
                    job.getEstimatedDuration(),
                    scheduler.getPredictedStartText(job),
                    scheduler.getPredictedFinishText(job),
                    scheduler.getRemainingWait(job)
            });
        }

        jobModel.setRowCount(0);

        for (Job job : scheduler.searchJobs(null, searchField.getText())) {
            jobModel.addRow(new Object[] {
                    job.getJobId(),
                    job.getStudentId(),
                    job.getTitle(),
                    job.getStatus().getName(),
                    job.getAssignedGpuId() == 0
                            ? "-" : job.getAssignedGpuId(),
                    job.getRuntime(scheduler.getNow()),
                    job.getQueueWait(scheduler.getNow()),
                    job.getChargedMinutes()
            });
        }
    }

    private Job selectedJob() {
        int row = jobTable.getSelectedRow();

        if (row == -1) {
            throw new IllegalArgumentException("Select a job first.");
        }

        Job job = scheduler.findJob(
                (String) jobModel.getValueAt(row, 0));

        if (job == null) {
            throw new IllegalArgumentException("Job not found.");
        }

        return job;
    }

    private String time(int minute) {
        return minute < 0 ? "-" : scheduler.formatTime(minute);
    }

    private void showDetails() {
        Job job = selectedJob();

        String text =
                "Job: " + job.getJobId()
                + "\nStudent ID: " + job.getStudentId()
                + "\nTitle: " + job.getTitle()
                + "\nApplication: " + job.getApplicationName()
                + "\nDescription: " + job.getDescription()
                + "\nTask size: " + job.getTaskSize()
                + "\nStatus: " + job.getStatus().getName()
                + "\nSubmitted: " + time(job.getSubmissionTime())
                + "\nGPU: " + (job.getAssignedGpuId() == 0
                        ? "-" : job.getAssignedGpuId())
                + "\nActual start: " + time(job.getStartTime())
                + "\nActual finish: " + time(job.getFinishTime())
                + "\nRuntime: " + job.getRuntime(scheduler.getNow())
                + " minutes"
                + "\nQueue wait: " + job.getQueueWait(scheduler.getNow())
                + " minutes"
                + "\nCharged: " + job.getChargedMinutes() + " minutes";

        if (job.getStatus() == JobStatus.QUEUED) {
            text += "\nQueue position: " + scheduler.getQueuePosition(job)
                    + "\nPredicted start: "
                    + scheduler.getPredictedStartText(job)
                    + "\nPredicted finish: "
                    + scheduler.getPredictedFinishText(job)
                    + "\nRemaining wait: "
                    + scheduler.getRemainingWait(job) + " minutes";

        } else if (job.getStatus() == JobStatus.RUNNING) {
            text += "\nExpected finish: "
                    + scheduler.getPredictedFinishText(job)
                    + "\nRemaining runtime: "
                    + job.getRemainingRuntime(scheduler.getNow())
                    + " minutes";

        } else if (job.getStatus() == JobStatus.CANCELLED) {
            text += "\nCancelled at: " + time(job.getCancelTime())
                    + "\nCancelled by: " + job.getCancelledBy()
                    + "\nReason: " + job.getCancelReason();
        }

        JTextArea area = new JTextArea(text, 20, 48);
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
                this, "Administrator's reason for cancelling "
                        + job.getJobId() + ":");

        if (reason == null) {
            return;
        }

        reason = reason.trim();

        if (reason.isEmpty() || reason.contains("|")
                || reason.contains("\n") || reason.contains("\r")) {
            throw new IllegalArgumentException(
                    "Enter a reason without | or line breaks.");
        }

        int charge = job.getStatus() == JobStatus.RUNNING
                ? job.getRuntime(scheduler.getNow()) : 0;

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Cancel " + job.getJobId()
                + " belonging to " + job.getStudentId() + "?\n"
                + "Charge: " + charge + " minutes.",
                "Confirm cancellation",
                JOptionPane.YES_NO_OPTION);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        scheduler.cancelJob(job, administrator.getUsername(), reason);
        refreshParent.run();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == searchButton) {
                refresh();
            } else if (e.getSource() == detailsButton) {
                showDetails();
            } else if (e.getSource() == cancelButton) {
                cancelJob();
            }

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Action failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}