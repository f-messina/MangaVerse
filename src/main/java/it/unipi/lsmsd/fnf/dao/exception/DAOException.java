package it.unipi.lsmsd.fnf.dao.exception;

public class ExceptionDAO extends Exception{
    public ExceptionDAO(Exception ex){super(ex);}
    public ExceptionDAO(String message){super(message);}
}
