package it.unipi.lsmsd.fnf.dao.exception;

import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;

/**
 * Custom exception class for handling Data Access Object (DAO) related errors.
 * This class extends the Exception class and provides additional information about the type of exception that occurred.
 */
public class DAOException extends Exception{
    private final DAOExceptionType type;

    public DAOException(String message){
        super(message);
        this.type = DAOExceptionType.GENERIC_ERROR;
    }

    public DAOException(Exception e) {
        super(e);
        this.type = DAOExceptionType.GENERIC_ERROR;
    }

    public DAOException(String message, Exception ex){
        super(message, ex);
        this.type = DAOExceptionType.GENERIC_ERROR;
    }

    public DAOException(DAOExceptionType type, String message){
        super(message);
        this.type = type;
    }

    public DAOException(DAOExceptionType type, String message, Exception ex){
        super(message, ex);
        this.type = type;
    }

    public DAOExceptionType getType() {
        return type;
    }
}

