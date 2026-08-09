package platepal.exception;

/**
 * Thrown when the logged-in user is not allowed to perform an action.
 */
public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}
