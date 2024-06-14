package it.unipi.lsmsd.fnf.service.exception.enums;

/**
 * Enum that represents the type of the BusinessException
 */
public enum BusinessExceptionType {
    GENERIC_ERROR,
    INVALID_TYPE,
    AUTHENTICATION_ERROR,
    DUPLICATED_USERNAME,
    DUPLICATED_EMAIL,
    DUPLICATED_KEY,
    EMPTY_FIELDS,
    NOT_FOUND,
    RETRYABLE_ERROR,
    DATABASE_ERROR,
    NO_CHANGE,
    INVALID_INPUT;
}
