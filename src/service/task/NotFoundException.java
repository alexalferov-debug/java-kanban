package service.task;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message, Exception e) {
        super(message, e);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
