package logic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import model.GPU;
import model.Job;
import model.JobStatus;
import model.Student;
import model.TaskSize;
import model.TimeAllowance;

public class Scheduler {

    public static final int GPU_COUNT = 4;
    public static final String ADMIN_USERNAME = "admin";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private GPU[] gpus;
    private ArrayList<Job> jobs;
    private AccountManager accounts;
    private LocalDateTime startTime;
    private int now;
    private int nextJobNumber;

    public Scheduler(AccountManager accountManager) {
        accounts = accountManager;
        gpus = new GPU[GPU_COUNT];
        for (int i = 0; i < GPU_COUNT; i++) {
            gpus[i] = new GPU(i + 1);
        }
        jobs = new ArrayList<Job>();
        startTime = null;
        now = 0;
        nextJobNumber = 101;
    }

    public void startClock(LocalDateTime start) {
        if (startTime != null) {
            throw new IllegalStateException("The simulation clock has already been set");
        }
        if (start == null) {
            throw new IllegalArgumentException("Choose a start date and time");
        }
        startTime = start.withSecond(0).withNano(0);
    }

    public boolean isClockStarted() {
        return startTime != null;
    }

    public int getNow() {
        return now;
    }

    public LocalDateTime toDateTime(int minute) {
        if (startTime == null || minute < 0) {
            return null;
        }
        return startTime.plusMinutes(minute);
    }

    public String formatTime(int minute) {
        LocalDateTime time = toDateTime(minute);
        if (time == null) {
            return "";
        }
        return time.format(FORMAT);
    }

    public String getNowText() {
        if (startTime == null) {
            return "Not set";
        }
        return formatTime(now);
    }

    public Job submitJob(Student s, String title, String appName, String description, TaskSize size) {
        if (startTime == null) {
            throw new IllegalStateException("Set the simulation date and time before submitting jobs");
        }
        if (s == null) {
            throw new IllegalArgumentException("Student account not found");
        }
        if (isBlank(title) || isBlank(appName) || isBlank(description)) {
            throw new IllegalArgumentException("Title, application name and description are required");
        }
        if (title.contains("|") || appName.contains("|") || description.contains("|")) {
            throw new IllegalArgumentException("Job details cannot contain the | character");
        }
        if (size == null) {
            throw new IllegalArgumentException("Choose a task size");
        }
        TimeAllowance allowance = s.getAllowance();
        if (!allowance.canReserve(size.getDuration())) {
            throw new IllegalArgumentException("Not enough GPU minutes: " + allowance.getAvailable()
                    + " available, " + size.getDuration() + " needed");
        }
        allowance.reserve(size.getDuration());
        Job job = new Job(nextJobNumber, s.getUsername(), title.trim(), appName.trim(), description.trim(), size,
                now);
        nextJobNumber++;
        jobs.add(job);
        dispatch();
        return job;
    }

    public void advanceMinute() {
        if (startTime == null) {
            throw new IllegalStateException("Set the simulation date and time first");
        }
        now++;
        for (GPU gpu : gpus) {
            if (!gpu.isFree()) {
                Job job = gpu.getCurrentJob();
                if (now - job.getStartTime() >= job.getEstimatedDuration()) {
                    if (job.complete(now)) {
                        settle(job);
                    }
                    gpu.release();
                }
            }
        }
        dispatch();
    }

    public void cancelJob(Job job, String byUser, String reason) {
        if (job == null) {
            throw new IllegalArgumentException("Choose a job to cancel");
        }
        if (isBlank(reason)) {
            throw new IllegalArgumentException("A reason is required to cancel a job");
        }
        if (byUser == null || (!byUser.equals(ADMIN_USERNAME) && !byUser.equals(job.getStudentId()))) {
            throw new IllegalArgumentException("You can only cancel your own jobs");
        }
        boolean wasRunning = job.getStatus() == JobStatus.RUNNING;
        int gpuId = job.getAssignedGpuId();
        if (!job.cancel(now, byUser, reason.trim())) {
            throw new IllegalArgumentException("Only queued or running jobs can be cancelled");
        }
        settle(job);
        if (wasRunning) {
            gpus[gpuId - 1].release();
        }
        dispatch();
    }

    private void settle(Job job) {
        Student owner = accounts.findStudent(job.getStudentId());
        if (owner != null) {
            TimeAllowance allowance = owner.getAllowance();
            allowance.release(job.getEstimatedDuration());
            allowance.charge(job.getChargedMinutes());
        }
    }

    private void dispatch() {
        ArrayList<Job> queue = getQueuedJobs();
        int next = 0;
        for (GPU gpu : gpus) {
            if (gpu.isFree() && next < queue.size()) {
                Job job = queue.get(next);
                next++;
                job.start(gpu.getId(), now);
                gpu.assign(job);
            }
        }
    }

    private boolean comesBefore(Job a, Job b) {
        if (a.getSubmissionTime() != b.getSubmissionTime()) {
            return a.getSubmissionTime() < b.getSubmissionTime();
        }
        return a.getJobNumber() < b.getJobNumber();
    }

    public ArrayList<Job> getQueuedJobs() {
        ArrayList<Job> queue = new ArrayList<Job>();
        for (Job job : jobs) {
            if (job.getStatus() == JobStatus.QUEUED) {
                int index = queue.size();
                while (index > 0 && comesBefore(job, queue.get(index - 1))) {
                    index--;
                }
                queue.add(index, job);
            }
        }
        return queue;
    }

    public GPU[] getGpus() {
        return gpus;
    }

    public int getBusyGpuCount() {
        int count = 0;
        for (GPU gpu : gpus) {
            if (!gpu.isFree()) {
                count++;
            }
        }
        return count;
    }

    public ArrayList<Job> getAllJobs() {
        return new ArrayList<Job>(jobs);
    }

    public ArrayList<Job> getJobsOf(String studentId) {
        ArrayList<Job> result = new ArrayList<Job>();
        for (Job job : jobs) {
            if (job.getStudentId().equals(studentId)) {
                result.add(job);
            }
        }
        return result;
    }

    public ArrayList<Job> getJobsWithStatus(JobStatus status) {
        ArrayList<Job> result = new ArrayList<Job>();
        for (Job job : jobs) {
            if (job.getStatus() == status) {
                result.add(job);
            }
        }
        return result;
    }

    public ArrayList<Job> searchJobs(String studentId, String text) {
        ArrayList<Job> result = new ArrayList<Job>();
        String key = text.trim().toLowerCase();
        for (Job job : jobs) {
            boolean owner = studentId == null || job.getStudentId().equals(studentId);
            boolean match = job.getJobId().toLowerCase().contains(key) || job.getTitle().toLowerCase().contains(key)
                    || job.getApplicationName().toLowerCase().contains(key);
            if (owner && match) {
                result.add(job);
            }
        }
        return result;
    }

    public Job findJob(String jobId) {
        for (Job job : jobs) {
            if (job.getJobId().equalsIgnoreCase(jobId.trim())) {
                return job;
            }
        }
        return null;
    }

    public int countJobs(JobStatus status) {
        return getJobsWithStatus(status).size();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().equals("");
    }
}
