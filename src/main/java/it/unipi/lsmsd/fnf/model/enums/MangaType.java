package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;

public enum MangaType {
    MANGA(1),
    MANHWA(2),
    MANHUA(3),
    NOVEL(4),
    ONE_SHOT(5),
    DOUJINSHI(6),
    LIGHT_NOVEL(7);

    private final int code;

    MangaType(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }

    public static MangaType fromString(String value) {
        for (MangaType type : MangaType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + value);
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
        };
    }
}
