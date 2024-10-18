package service.task;

import java.io.IOException;

public class ServiceSaveException extends RuntimeException {
    public ServiceSaveException(String message, Exception e) {
        super(message,e);
    }
    public ServiceSaveException(String message) {
        super(message);
    }
}
