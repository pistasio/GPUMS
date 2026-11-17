package model;

public class GPU {

    private int id;
    private Job currentJob;

    public GPU(int gpuId) {
        id = gpuId;
        currentJob = null;
    }

    public int getId() {
        return id;
    }

    public Job getCurrentJob() {
        return currentJob;
    }

    public boolean isFree() {
        return currentJob == null;
    }

    public void assign(Job job) {
        currentJob = job;
    }

    public void release() {
        currentJob = null;
    }

    public int getRemainingRuntime(int now) {
        if (currentJob == null) {
            return 0;
        }
        return currentJob.getRemainingRuntime(now);
    }

    public int getExpectedFinish() {
        if (currentJob == null) {
            return -1;
        }
        return currentJob.getStartTime() + currentJob.getEstimatedDuration();
    }

    public String toString() {
        if (currentJob == null) {
            return "GPU" + id + ": Available";
        }
        return "GPU" + id + ": Busy with " + currentJob.getJobId();
    }
}
