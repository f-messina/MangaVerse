package it.unipi.lsmsd.fnf.model.enums;

/**
 * Enumeration representing different types of media content.
 */
public enum MediaContentType {
    MANGA(1),
    ANIME(2);

    private final int code;

    MediaContentType(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
