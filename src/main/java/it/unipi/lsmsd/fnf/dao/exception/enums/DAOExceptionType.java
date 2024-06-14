package it.unipi.lsmsd.fnf.dao.exception.enums;

/**
 * Enumerates the types of exceptions that can be thrown by the DAO layer.
 */
public enum DAOExceptionType {
    DUPLICATED_KEY,
    GENERIC_ERROR,
    DATABASE_ERROR,
    DUPLICATED_USERNAME,
    DUPLICATED_EMAIL,
    AUTHENTICATION_ERROR,
    UNSUPPORTED_OPERATION,
    TRANSIENT_ERROR, NO_CHANGES;
}
