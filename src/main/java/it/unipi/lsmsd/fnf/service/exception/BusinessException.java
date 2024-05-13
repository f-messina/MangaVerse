package it.unipi.lsmsd.fnf.service.exception;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;

/**
 * The BusinessException class represents exceptions that occur during business logic processing.
 * These exceptions encapsulate information about the type of business exception that occurred.
 */
public class BusinessException extends Exception {


    /**
     * Constructs a new BusinessException with a generic type.
     * @param ex The underlying exception causing this BusinessException.
     */
    private final BusinessExceptionType type;
    public BusinessException(Exception ex){
        super(ex);
        this.type = BusinessExceptionType.GENERIC_ERROR;
    }

    /**
     * Constructs a new BusinessException with a generic type and the specified detail message.
     * @param message The detail message.
     */
    public BusinessException(String message){
        super(message);
        this.type = BusinessExceptionType.GENERIC_ERROR;
    }

    /**
     * Constructs a new BusinessException with a generic type, the specified detail message, and the underlying exception.
     * @param message The detail message.
     * @param ex The underlying exception causing this BusinessException.
     */
    public BusinessException(String message, Exception ex){
        super(message, ex);
        this.type = BusinessExceptionType.GENERIC_ERROR;
    }

    /**
     * Constructs a new BusinessException with the specified type and detail message.
     * @param type The type of business exception.
     * @param message The detail message.
     */
    public BusinessException(BusinessExceptionType type,String message){
        super(message);
        this.type = type;
    }

    /**
     * Constructs a new BusinessException with the specified type, detail message, and underlying exception.
     * @param type The type of business exception.
     * @param message The detail message.
     * @param e The underlying exception causing this BusinessException.
     */
    public BusinessException(BusinessExceptionType type,String message,Exception e){
        super(message,e);
        this.type = type;
    }

    /**
     * Gets the type of this BusinessException.
     * @return The type of BusinessException.
     */
    public BusinessExceptionType getType() {
        return type;
    }

    public static void handleDAOException(DAOException e) throws BusinessException {
        switch (e.getType()) {
            case TRANSIENT_ERROR:
                throw new BusinessException(BusinessExceptionType.RETRYABLE_ERROR, e.getMessage());
            case DATABASE_ERROR:
                throw new BusinessException(BusinessExceptionType.DATABASE_ERROR, e.getMessage());
            default:
                throw new BusinessException(BusinessExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }
}
