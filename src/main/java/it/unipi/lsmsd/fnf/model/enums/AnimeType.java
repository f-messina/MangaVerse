package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;

/**
 * Enumeration representing different types of anime.
 */
public enum AnimeType {
    TV(1),
    MOVIE(2),
    OVA(3),
    SPECIAL(4),
    ONA(5),
    UNKNOWN(6);

    private final int code;

    /**
     * Constructor for AnimeType enum.
     * @param code The code associated with the enum value.
     */
    AnimeType(int code){
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
            case TV -> "TV";
            case MOVIE -> "Movie";
            case OVA -> "OVA";
            case SPECIAL -> "Special";
            case ONA -> "ONA";
            case UNKNOWN -> Constants.NULL_STRING;
        };
    }

    /**
     * Returns the AnimeType enum value corresponding to the given string value.
     * @param value The string value to convert to AnimeType enum.
     * @return The AnimeType enum value corresponding to the given string value.
     * @throws IllegalArgumentException if no enum constant is found for the given string value.
     */
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
