package it.unipi.lsmsd.fnf.model.enums;

/**
 * Enumeration representing the status of an anime.
 */
public enum AnimeStatus {
    FINISHED(1),
    ONGOING(2),
    UPCOMING(3),
    UNKNOWN(4);

    private final int code;

    AnimeStatus(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String toString() {
        return switch (this) {
            case FINISHED -> "Finished";
            case ONGOING -> "Ongoing";
            case UPCOMING -> "Upcoming";
            case UNKNOWN -> "Unknown";
        };
    }
}
