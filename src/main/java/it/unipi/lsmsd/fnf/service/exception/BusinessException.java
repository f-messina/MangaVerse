package it.unipi.lsmsd.fnf.service.exception;

import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;

public class BusinessException extends Exception {

    private final BusinessExceptionType type;
    public BusinessException(Exception ex){
        super(ex);
        this.type = BusinessExceptionType.GENERIC;
    }
    public BusinessException(String message){
        super(message);
        this.type = BusinessExceptionType.GENERIC;
    }
    public BusinessException(String message, Exception ex){
        super(message, ex);
        this.type = BusinessExceptionType.GENERIC;
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
}
