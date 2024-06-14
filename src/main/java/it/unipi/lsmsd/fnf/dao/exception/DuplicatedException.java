package it.unipi.lsmsd.fnf.dao.exception;

import it.unipi.lsmsd.fnf.dao.exception.enums.DuplicatedExceptionType;

/**
 * This class represents an exception that is thrown when a duplicated entity is found
 * or an element with unique constraints is duplicated.
 */
public class DuplicatedException extends Exception{
    private final DuplicatedExceptionType type;

    public DuplicatedException(String message){
        super(message);
        this.type = DuplicatedExceptionType.GENERIC;
    }

    public DuplicatedException(DuplicatedExceptionType type, String message){
        super(message);
        this.type = type;
    }

    public DuplicatedExceptionType getType() {
        return type;
    }
}
