package it.unipi.lsmsd.fnf.service.exception;

public enum BusinessExceptionType {
    GENERIC,
    INVALID_TYPE,
    AUTHENTICATION_ERROR,
    DUPLICATED_USERNAME,
    DUPLICATED_EMAIL,
    DUPLICATED_KEY,
    NO_NAME,
    NO_USER,
    EMPTY_FIELDS,
    SELF_FOLLOW, NOT_FOUND
}
