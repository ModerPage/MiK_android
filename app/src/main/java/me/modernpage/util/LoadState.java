package me.modernpage.util;

public class LoadState<T> {
    private final boolean running;
    private final boolean isVerified;
    private final T data;
    private final String errorMessage;
    private boolean handledError = false;

    public LoadState(boolean running, boolean isVerified, String errorMessage, T data) {
        this.running = running;
        this.isVerified = isVerified;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorMessageIfNotHandled() {
        if (handledError) {
            return null;
        }
        handledError = true;
        return errorMessage;
    }
}