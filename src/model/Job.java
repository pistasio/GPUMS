package model;

public class Job implements Cancellable, Storable {

    private int jobNumber;
    private String studentId;
    private String title;
    private String applicationName;
    private String description;
    private TaskSize taskSize;
    private int workloadUnits;
    private int estimatedDuration;
    private int submissionTime;
    private JobStatus status;
    private int assignedGpuId;
    private int startTime;
    private int finishTime;
    private int cancelTime;
    private String cancelledBy;
    private String cancelReason;
    private int chargedMinutes;

    public Job(int number, String owner, String jobTitle, String appName, String jobDescription,
            TaskSize size, int submittedAt) {
        jobNumber = number;
        studentId = owner;
        title = jobTitle;
        applicationName = appName;
        description = jobDescription;
        taskSize = size;
        workloadUnits = size.getWorkloadUnits();
        estimatedDuration = size.getDuration();
        submissionTime = submittedAt;
        status = JobStatus.QUEUED;
        assignedGpuId = 0;
        startTime = -1;
        finishTime = -1;
        cancelTime = -1;
        cancelledBy = "";
        cancelReason = "";
        chargedMinutes = 0;
    }

    public int getJobNumber() {
        return jobNumber;
    }

    public String getJobId() {
        return "J" + jobNumber;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getTitle() {
        return title;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDescription() {
        return description;
    }

    public TaskSize getTaskSize() {
        return taskSize;
    }

    public int getWorkloadUnits() {
        return workloadUnits;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public int getSubmissionTime() {
        return submissionTime;
    }

    public JobStatus getStatus() {
        return status;
    }

    public int getAssignedGpuId() {
        return assignedGpuId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public int getCancelTime() {
        return cancelTime;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public int getChargedMinutes() {
        return chargedMinutes;
    }

    public boolean start(int gpuId, int minute) {
        if (status != JobStatus.QUEUED) {
            return false;
        }
        status = JobStatus.RUNNING;
        assignedGpuId = gpuId;
        startTime = minute;
        return true;
    }

    public boolean complete(int minute) {
        if (status != JobStatus.RUNNING) {
            return false;
        }
        status = JobStatus.COMPLETED;
        finishTime = minute;
        chargedMinutes = finishTime - startTime;
        return true;
    }

    public boolean cancel(int minute, String byUser, String reason) {
        if (status == JobStatus.QUEUED) {
            chargedMinutes = 0;
        } else if (status == JobStatus.RUNNING) {
            finishTime = minute;
            chargedMinutes = finishTime - startTime;
        } else {
            return false;
        }
        status = JobStatus.CANCELLED;
        cancelTime = minute;
        cancelledBy = byUser;
        cancelReason = reason;
        return true;
    }

    public int getRuntime(int now) {
        if (startTime == -1) {
            return 0;
        }
        if (finishTime != -1) {
            return finishTime - startTime;
        }
        if (status == JobStatus.RUNNING) {
            return now - startTime;
        }
        return 0;
    }

    public int getRemainingRuntime(int now) {
        if (status != JobStatus.RUNNING) {
            return 0;
        }
        return estimatedDuration - (now - startTime);
    }

    public int getQueueWait(int now) {
        if (startTime != -1) {
            return startTime - submissionTime;
        }
        if (cancelTime != -1) {
            return cancelTime - submissionTime;
        }
        return now - submissionTime;
    }

    public String toFileLine() {
        return jobNumber + "|" + studentId + "|" + title + "|" + applicationName + "|" + description + "|"
                + taskSize.getName() + "|" + submissionTime + "|" + status.getName() + "|" + assignedGpuId + "|"
                + startTime + "|" + finishTime + "|" + cancelTime + "|" + cancelledBy + "|" + cancelReason + "|"
                + chargedMinutes;
    }

    public static Job fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 15) {
            throw new IllegalArgumentException("Damaged job record: " + line);
        }
        TaskSize size = TaskSize.fromName(parts[5]);
        JobStatus savedStatus = JobStatus.fromName(parts[7]);
        if (size == null || savedStatus == null) {
            throw new IllegalArgumentException("Damaged job record: " + line);
        }
        Job job = new Job(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3], parts[4], size,
                Integer.parseInt(parts[6]));
        job.status = savedStatus;
        job.assignedGpuId = Integer.parseInt(parts[8]);
        job.startTime = Integer.parseInt(parts[9]);
        job.finishTime = Integer.parseInt(parts[10]);
        job.cancelTime = Integer.parseInt(parts[11]);
        job.cancelledBy = parts[12];
        job.cancelReason = parts[13];
        job.chargedMinutes = Integer.parseInt(parts[14]);
        return job;
    }

    public String toString() {
        return getJobId() + " | " + studentId + " | " + title + " | " + applicationName + " | " + taskSize.getName()
                + " | " + status.getName();
    }
}
