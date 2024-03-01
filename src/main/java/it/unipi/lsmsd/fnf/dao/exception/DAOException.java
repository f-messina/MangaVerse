package it.unipi.lsmsd.fnf.dao.exception;

public class DAOException extends Exception{
    private final DAOExceptionType type;

    public DAOException(Exception ex){
        super(ex);
        this.type = DAOExceptionType.GENERIC;
    }
    public DAOException(String message){
        super(message);
        this.type = DAOExceptionType.GENERIC;
    }
    public DAOException(String message, Exception ex){
        super(message, ex);
        this.type = DAOExceptionType.GENERIC;
    }

}

