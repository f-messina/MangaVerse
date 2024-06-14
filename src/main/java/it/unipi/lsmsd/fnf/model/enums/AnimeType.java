package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;
import org.apache.commons.lang3.StringUtils;

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

    AnimeType(int code){
        this.code = code;
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

    /**
     * Returns the AnimeType enum value corresponding to the given string value.
     * It is case-insensitive and returns the UNKNOWN enum value if the string value is null or empty.
     * @param value The string value to convert to AnimeType enum.
     * @return The AnimeType enum value corresponding to the given string value.
     * @throws IllegalArgumentException if no enum constant is found for the given string value.
     */
    public static AnimeType fromString(String value) {
        if (StringUtils.isEmpty(value)) {
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
