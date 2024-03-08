package it.unipi.lsmsd.fnf.model.enums;
/**
 * Enumeration representing the type of user.
 */
public enum UserType {
    MANAGER(1),
    USER(2);
    private final int code;
    /**
     * Constructor for UserType enum.
     * @param code The code associated with the enum value.
     */
    UserType(int code){
        this.code=code;
    }
    /**
     * Returns the code associated with the enum value.
     * @return The code associated with the enum value.
     */
    public int getCode() {
        return code;
    }
}
