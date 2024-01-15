package it.unipi.lsmsd.fnf.model.enums;

public enum MediaContentType {
    MANGA(1),
    ANIME(2);
    int code;
    MediaContentType(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
