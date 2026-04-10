package strms.exceptions;

public class DependencyNotCompletedException extends RuntimeException {
    public DependencyNotCompletedException(String message) {
        super(message);
    }
}
