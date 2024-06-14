package it.unipi.lsmsd.fnf.dao.exception;

import it.unipi.lsmsd.fnf.dao.exception.enums.DuplicatedExceptionType;

/**
 * This class represents an exception that is thrown when a duplicated entity is found
 * or an element with unique constraints is duplicated.
 */
public class DuplicatedException extends Exception{
    private final DuplicatedExceptionType type;
    /**
     * Constructs a new DuplicatedException with a specified message.
     *
     * @param message The detail message.
     */
    public DuplicatedException(String message){
        super(message);
        this.type = DuplicatedExceptionType.GENERIC;
    }

    /**
     * Constructs a new DuplicatedException with a specified type and message.
     *
     * @param type    The type of DuplicatedException.
     * @param message The detail message.
     */
    public DuplicatedException(DuplicatedExceptionType type, String message){
        super(message);
        this.type = type;
    }

    /**
     * Retrieves the type of DuplicatedException.
     *
     * @return The type of DuplicatedException.
     */
    public DuplicatedExceptionType getType() {
        return type;
    }
}
