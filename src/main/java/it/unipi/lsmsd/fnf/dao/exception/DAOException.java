package it.unipi.lsmsd.fnf.dao.exception;

import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;

/**
 * Custom exception class for handling Data Access Object (DAO) related errors.
 */
public class DAOException extends Exception{
    private final DAOExceptionType type;
    /**
     * Constructs a new DAOException with a specified message.
     *
     * @param message The detail message.
     */
    public DAOException(String message){
        super(message);
        this.type = DAOExceptionType.GENERIC_ERROR;
    }

    /**
     * Constructs a new DAOException with a specified nested exception.
     *
     * @param e The nested exception.
     */
    public DAOException(Exception e) {
        super(e);
        this.type = DAOExceptionType.GENERIC_ERROR;
    }

    /**
     * Constructs a new DAOException with a specified message and nested exception.
     *
     * @param message The detail message.
     * @param ex      The nested exception.
     */
    public DAOException(String message, Exception ex){
        super(message, ex);
        this.type = DAOExceptionType.GENERIC_ERROR;
    }

    /**
     * Constructs a new DAOException with a specified type and message.
     *
     * @param type    The type of DAOException.
     * @param message The detail message.
     */
    public DAOException(DAOExceptionType type, String message){
        super(message);
        this.type = type;
    }

    /**
     * Constructs a new DAOException with a specified type, message, and nested exception.
     *
     * @param type    The type of DAOException.
     * @param message The detail message.
     * @param ex      The nested exception.
     */
    public DAOException(DAOExceptionType type, String message, Exception ex){
        super(message, ex);
        this.type = type;
    }

    /**
     * Retrieves the type of DAOException.
     *
     * @return The type of DAOException.
     */
    public DAOExceptionType getType() {
        return type;
    }
}

