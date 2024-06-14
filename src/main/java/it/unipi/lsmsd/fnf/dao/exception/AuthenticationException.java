package it.unipi.lsmsd.fnf.dao.exception;

/**
 * Custom exception class for handling authentication errors.
 */
public class AuthenticationException extends Exception{

    public AuthenticationException(String message){
        super(message);
    }
}
