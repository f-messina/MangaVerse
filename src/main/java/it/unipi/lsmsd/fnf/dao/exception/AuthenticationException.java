package it.unipi.lsmsd.fnf.dao.exception;

/**
 * Custom exception class for handling authentication errors.
 */
public class AuthenticationException extends Exception{
    /**
     * Constructs a new AuthenticationException with a specified message.
     *
     * @param message The detail message.
     */
    public AuthenticationException(String message){
        super(message);
    }
}
