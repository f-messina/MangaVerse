package it.unipi.lsmsd.fnf.model.enums;

/**
 * Enumeration representing the status of a manga.
 */
public enum MangaStatus {
    DISCONTINUED(1),
    ON_HIATUS(2),
    FINISHED(3),
    ONGOING(4),
    UNKNOWN(5);

    private final int code;

    MangaStatus(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

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
