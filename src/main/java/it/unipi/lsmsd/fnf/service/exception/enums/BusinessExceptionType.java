package it.unipi.lsmsd.fnf.service.exception.enums;

public enum BusinessExceptionType {
    GENERIC_ERROR,
    INVALID_TYPE,
    AUTHENTICATION_ERROR,
    DUPLICATED_USERNAME,
    DUPLICATED_EMAIL,
    DUPLICATED_KEY,
    NO_NAME,
    NO_USER,
    EMPTY_FIELDS,
    NOT_FOUND,
    RETRYABLE_ERROR,
    DATABASE_ERROR, NO_CHANGE,
    NO_LIKES,
    NO_REVIEWS,
    INVALID_INPUT;
}
