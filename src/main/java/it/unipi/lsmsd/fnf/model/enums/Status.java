package it.unipi.lsmsd.fnf.model.enums;
/**
 * Enumeration representing the status of media content.
 */
public enum Status {
    DISCONTINUED(1),
    ON_HIATUS(2),
    FINISHED(3),
    ONGOING(4),
    UPCOMING(5),
    UNKNOWN(6);
    private final int code;
    /**
     * Constructor for Status enum.
     * @param code The code associated with the enum value.
     */
    Status(int code){
        this.code=code;
    }
    /**
     * Returns the code associated with the enum value.
     * @return The code associated with the enum value.
     */
    public int getCode() {
        return code;
    }
    /**
     * Returns a string representation of the enum value.
     * @return A string representation of the enum value.
     */
    public String toString() {
        return switch (this) {
            case FINISHED -> "Finished";
            case ONGOING -> "Ongoing";
            case DISCONTINUED -> "Discontinued";
            case ON_HIATUS -> "On Hiatus";
            case UPCOMING -> "Upcoming";
            case UNKNOWN -> "Unknown";
        };
    }
}
