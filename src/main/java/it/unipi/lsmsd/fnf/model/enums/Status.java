package it.unipi.lsmsd.fnf.model.enums;

public enum Status {
    DISCONTINUED(1),
    ON_HIATUS(2),
    FINISHED(3),
    ONGOING(4),
    UPCOMING(5),
    UNKNOWN(6);
    private final int code;

    Status(int code){
        this.code=code;
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
            case UPCOMING -> "Upcoming";
            case UNKNOWN -> "Unknown";
        };
    }
}
