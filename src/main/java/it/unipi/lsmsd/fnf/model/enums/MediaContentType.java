package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
/**
 * Enumeration representing different types of media content.
 */
public enum MediaContentType {
    MANGA(1),
    ANIME(2);

    private final int code;

    /**
     * Constructor for MediaContentType enum.
     * @param code The code associated with the enum value.
     */
    MediaContentType(int code){
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
     * Checks if the provided MediaContent instance is an instance of the corresponding type.
     * @param mediaContent The MediaContent instance to check.
     * @return true if the provided MediaContent instance is an instance of the corresponding type, otherwise false.
     */
    public boolean isInstance(MediaContent mediaContent){
        return switch (this) {
            case MANGA -> mediaContent instanceof Manga;
            case ANIME -> mediaContent instanceof Anime;
        };
    }

    /**
     * Checks if the provided MediaContentDTO instance is an instance of the corresponding type.
     * @param mediaContent The MediaContentDTO instance to check.
     * @return true if the provided MediaContentDTO instance is an instance of the corresponding type, otherwise false.
     */
    public boolean isInstance(MediaContentDTO mediaContent){
        return switch (this) {
            case MANGA -> mediaContent instanceof MangaDTO;
            case ANIME -> mediaContent instanceof AnimeDTO;
        };
    }
}
