package model;

public class TimeAllowance {

    public static final int DEFAULT_MINUTES = 100;

    private int totalMinutes;
    private int usedMinutes;
    private int reservedMinutes;

    public TimeAllowance() {
        this(DEFAULT_MINUTES);
    }

    public TimeAllowance(int initialMinutes) {
        this(initialMinutes, 0, 0);
    }

    public TimeAllowance(int totalMinutes, int usedMinutes, int reservedMinutes) {
        if (totalMinutes < 0 || usedMinutes < 0 || reservedMinutes < 0) {
            throw new IllegalArgumentException("GPU minutes cannot be negative");
        }
        if (usedMinutes + reservedMinutes > totalMinutes) {
            throw new IllegalArgumentException("Used and reserved minutes exceed total allowance");
        }
        this.totalMinutes = totalMinutes;
        this.usedMinutes = usedMinutes;
        this.reservedMinutes = reservedMinutes;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public int getUsedMinutes() {
        return usedMinutes;
    }

    public int getReservedMinutes() {
        return reservedMinutes;
    }

    public int getAvailable() {
        return totalMinutes - usedMinutes - reservedMinutes;
    }

    public boolean canReserve(int minutes) {
        return minutes > 0 && minutes <= getAvailable();
    }

    public void reserve(int minutes) {
        if (!canReserve(minutes)) {
            throw new IllegalArgumentException("Insufficient GPU minutes available");
        }
        reservedMinutes += minutes;
    }

    public void release(int minutes) {
        if (minutes <= 0 || minutes > reservedMinutes) {
            throw new IllegalArgumentException("Invalid number of minutes to release");
        }
        reservedMinutes -= minutes;
    }

    public void charge(int minutes) {
        if (minutes < 0 || minutes > getAvailable()) {
            throw new IllegalArgumentException("Invalid GPU minute charge");
        }
        usedMinutes += minutes;
    }

    public void addMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Additional minutes must be positive");
        }
        totalMinutes += minutes;
    }

    @Override
    public String toString() {
        return "Total: " + totalMinutes + " | Used: " + usedMinutes + " | Reserved: " + reservedMinutes
                + " | Available: " + getAvailable();
    }
}
