package it.unipi.lsmsd.fnf.controller.exception;

/**
 * Exception thrown when the user is not authorized to perform an operation.
 */
public class NotAuthorizedException extends Exception {
    public NotAuthorizedException(String message) {
        super(message);
    }
}
