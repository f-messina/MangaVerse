package it.unipi.lsmsd.fnf.dao.exception;

public class DAOException extends Exception{
    public DAOException(Exception ex){super(ex);}
    public DAOException(String message){super(message);}
    public DAOException(String message, Exception ex){super(message, ex);}
}
