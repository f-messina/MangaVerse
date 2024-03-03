package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;

public enum AnimeType {
    TV(1),
    MOVIE(2),
    OVA(3),
    SPECIAL(4),
    ONA(5),
    UNKNOWN(6);

    private final int code;

    AnimeType(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }

    public String toString() {
        return switch (this) {
            case TV -> "TV";
            case MOVIE -> "Movie";
            case OVA -> "OVA";
            case SPECIAL -> "Special";
            case ONA -> "ONA";
            case UNKNOWN -> Constants.NULL_STRING;
        };
    }

    public static AnimeType fromString(String value) {
        if (value == null) {
            return AnimeType.UNKNOWN;
        }
        for (AnimeType type : AnimeType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + value);
    }
}
