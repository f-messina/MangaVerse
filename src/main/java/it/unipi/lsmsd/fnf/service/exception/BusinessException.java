package it.unipi.lsmsd.fnf.service.exception;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;

/**
 * Custom exception class for handling business logic related errors.
 * These exceptions encapsulate information about the type of business exception that occurred.
 */
public class BusinessException extends Exception {

    private final BusinessExceptionType type;

    public BusinessException(Exception ex){
        super(ex);
        this.type = BusinessExceptionType.GENERIC_ERROR;
    }

    public BusinessException(String message){
        super(message);
        this.type = BusinessExceptionType.GENERIC_ERROR;
    }

    public BusinessException(String message, Exception ex){
        super(message, ex);
        this.type = BusinessExceptionType.GENERIC_ERROR;
    }

    public BusinessException(BusinessExceptionType type,String message){
        super(message);
        this.type = type;
    }

    public BusinessException(BusinessExceptionType type,String message,Exception e){
        super(message,e);
        this.type = type;
    }

    public BusinessExceptionType getType() {
        return type;
    }

    /**
     * Handles a DAOException by converting it into a BusinessException.
     * This method is used in most of the service methods that communicate with neo4j DAOs.
     * @param e The DAOException to be handled.
     * @throws BusinessException The BusinessException equivalent of the DAOException.
     */
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
