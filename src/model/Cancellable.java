package model;

public interface Cancellable {
    boolean cancel(int minute, String byUser, String reason);
}
