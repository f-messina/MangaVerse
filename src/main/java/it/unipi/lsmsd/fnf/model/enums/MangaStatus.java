package it.unipi.lsmsd.fnf.model.enums;

public enum MangaStatus {
    DISCONTINUED(1),
    ON_HIATUS(2),
    FINISHED(3),
    ONGOING(4),
    UNKNOWN(5);
    private final int code;

    /**
     * Constructor for Status enum.
     * @param code The code associated with the enum value.
     */
    MangaStatus(int code){
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
            case UNKNOWN -> "Unknown";
        };
    }
}
