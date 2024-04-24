package it.unipi.lsmsd.fnf.model.enums;

public enum AnimeStatus {
    FINISHED(1),
    ONGOING(2),
    UPCOMING(3),
    UNKNOWN(4);

    private final int code;

    /**
     * Constructor for Status enum.
     * @param code The code associated with the enum value.
     */
    AnimeStatus(int code){
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
            case UPCOMING -> "Upcoming";
            case UNKNOWN -> "Unknown";
        };
    }
}
