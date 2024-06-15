package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration representing different types of manga.
 */
public enum MangaType {
    MANGA(1),
    MANHWA(2),
    MANHUA(3),
    NOVEL(4),
    ONE_SHOT(5),
    DOUJINSHI(6),
    LIGHT_NOVEL(7),
    UNKNOWN(8);

    private final int code;

    MangaType(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String toString() {
        return switch (this) {
            case MANGA -> "Manga";
            case MANHWA -> "Manhwa";
            case MANHUA -> "Manhua";
            case NOVEL -> "Novel";
            case ONE_SHOT -> "One Shot";
            case DOUJINSHI -> "Doujinshi";
            case LIGHT_NOVEL -> "Light Novel";
            case UNKNOWN -> Constants.NULL_STRING;
        };
    }

    /**
     * Returns the MangaType enum value corresponding to the given string value.
     * It is case-insensitive and returns the UNKNOWN enum value if the string value is null or empty.
     * @param value                         The string value to convert to MangaType enum.
     * @return                              The MangaType enum value corresponding to the given string value.
     * @throws IllegalArgumentException     If no enum constant is found for the given string value.
     */
    public static MangaType fromString(String value) {
        if (StringUtils.isEmpty(value)) {
            return MangaType.UNKNOWN;
        }
        for (MangaType type : MangaType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + value);
    }
}
