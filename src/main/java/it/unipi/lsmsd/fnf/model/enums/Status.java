package it.unipi.lsmsd.fnf.model.enums;

public enum Status {
    FINISHED(1),
    ON_GOING(2),
    DISCONTINUED(3),
    ON_HIATUS(4),
    UPCOMING(5),
    UNKNOWN(6);
    int code;
    Status(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
