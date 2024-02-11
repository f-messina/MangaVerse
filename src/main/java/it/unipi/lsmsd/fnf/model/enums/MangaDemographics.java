package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;

public enum MangaDemographics {
    SHOUNEN(1),
    SHOUJO(2),
    SEINEN(3),
    JOSEI(4),
    KIDS(5),
    UNKNOWN(6);

    private final int code;

    MangaDemographics(int code){
        this.code=code;
    }
    public int getCode() {
        return code;
    }

    public String toString() {
        return switch (this) {
            case SHOUNEN -> "Shounen";
            case SHOUJO -> "Shoujo";
            case SEINEN -> "Seinen";
            case JOSEI -> "Josei";
            case KIDS -> "Kids";
            case UNKNOWN -> Constants.NULL_STRING;
        };
    }

    public static MangaDemographics fromString(String value) {
        if (value == null) {
            return MangaDemographics.UNKNOWN;
        }
        for (MangaDemographics type : MangaDemographics.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + value);
    }
}
