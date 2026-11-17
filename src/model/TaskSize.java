package model;

public class TaskSize {

    public static final TaskSize SMALL = new TaskSize("Small", 5);
    public static final TaskSize MEDIUM = new TaskSize("Medium", 15);
    public static final TaskSize LARGE = new TaskSize("Large", 30);
    public static final TaskSize VERY_LARGE = new TaskSize("Very large", 60);

    private String name;
    private int workloadUnits;

    private TaskSize(String sizeName, int units) {
        name = sizeName;
        workloadUnits = units;
    }

    public String getName() {
        return name;
    }

    public int getWorkloadUnits() {
        return workloadUnits;
    }

    public int getDuration() {
        return workloadUnits;
    }

    public static TaskSize[] getAllSizes() {
        TaskSize[] sizes = {SMALL, MEDIUM, LARGE, VERY_LARGE};
        return sizes;
    }

    public static TaskSize fromName(String sizeName) {
        for (TaskSize size : getAllSizes()) {
            if (size.getName().equals(sizeName)) {
                return size;
            }
        }
        return null;
    }

    public String toString() {
        return name + " (" + workloadUnits + " minutes)";
    }
}
