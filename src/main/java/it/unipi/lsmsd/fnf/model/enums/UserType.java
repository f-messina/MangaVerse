package it.unipi.lsmsd.fnf.model.enums;

/**
 * Enumeration representing the type of user.
 */
public enum UserType {
    MANAGER(1),
    USER(2);
    private final int code;

    UserType(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
