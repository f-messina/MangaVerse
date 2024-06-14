package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration representing different demographics for manga.
 */
public enum MangaDemographics {
    SHOUNEN(1),
    SHOUJO(2),
    SEINEN(3),
    JOSEI(4),
    KIDS(5),
    UNKNOWN(6);

    private final int code;

    MangaDemographics(int code){
        this.code = code;
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

    /**
     * Returns the MangaDemographics enum value corresponding to the given string value.
     * It is case-insensitive and returns the UNKNOWN enum value if the string value is null or empty.
     * @param value The string value to convert to MangaDemographics enum.
     * @return The MangaDemographics enum value corresponding to the given string value.
     * @throws IllegalArgumentException if no enum constant is found for the given string value.
     */
    public static MangaDemographics fromString(String value) {
        if (StringUtils.isEmpty(value)) {
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
