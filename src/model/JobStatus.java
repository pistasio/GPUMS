package model;

public class JobStatus {

    public static final JobStatus QUEUED = new JobStatus("Queued");
    public static final JobStatus RUNNING = new JobStatus("Running");
    public static final JobStatus COMPLETED = new JobStatus("Completed");
    public static final JobStatus CANCELLED = new JobStatus("Cancelled");

    private String name;

    private JobStatus(String statusName) {
        name = statusName;
    }

    public String getName() {
        return name;
    }

    public static JobStatus[] getAllStatuses() {
        JobStatus[] statuses = {QUEUED, RUNNING, COMPLETED, CANCELLED};
        return statuses;
    }

    public static JobStatus fromName(String statusName) {
        for (JobStatus status : getAllStatuses()) {
            if (status.getName().equals(statusName)) {
                return status;
            }
        }
        return null;
    }

    public String toString() {
        return name;
    }
}
