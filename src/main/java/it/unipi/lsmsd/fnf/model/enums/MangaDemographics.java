package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;

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

    /**
     * Constructor for MangaDemographics enum.
     * @param code The code associated with the enum value.
     */
    MangaDemographics(int code){
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
     * @param value The string value to convert to MangaDemographics enum.
     * @return The MangaDemographics enum value corresponding to the given string value.
     * @throws IllegalArgumentException if no enum constant is found for the given string value.
     */
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
