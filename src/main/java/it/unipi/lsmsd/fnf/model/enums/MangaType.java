package it.unipi.lsmsd.fnf.model.enums;

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

    /**
     * Constructor for MangaType enum.
     * @param code The code associated with the enum value.
     */
    MangaType(int code){
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
     * Returns the MangaType enum value corresponding to the given string value.
     * @param value The string value to convert to MangaType enum.
     * @return The MangaType enum value corresponding to the given string value.
     * @throws IllegalArgumentException if no enum constant is found for the given string value.
     */
    public static MangaType fromString(String value) {
        for (MangaType type : MangaType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + value);
    }

    /**
     * Returns a string representation of the enum value.
     * @return A string representation of the enum value.
     */
    public String toString() {
        return switch (this) {
            case MANGA -> "Manga";
            case MANHWA -> "Manhwa";
            case MANHUA -> "Manhua";
            case NOVEL -> "Novel";
            case ONE_SHOT -> "One Shot";
            case DOUJINSHI -> "Doujinshi";
            case LIGHT_NOVEL -> "Light Novel";
            case UNKNOWN -> "Unknown";
        };
    }
}
