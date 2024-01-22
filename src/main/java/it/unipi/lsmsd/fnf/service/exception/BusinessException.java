package it.unipi.lsmsd.fnf.service.exception;

public class BusinessException extends Exception {
    public BusinessException(Exception ex){
        super(ex);
    }
    public BusinessException(String message){
        super(message);
    }
    public BusinessException(String message, Exception ex){
        super(message, ex);
    }
}
