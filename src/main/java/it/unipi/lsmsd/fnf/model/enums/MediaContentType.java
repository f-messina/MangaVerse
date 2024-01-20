package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;

public enum MediaContentType {
    MANGA(1),
    ANIME(2);

    private final int code;

    MediaContentType(int code){
        this.code=code;
    }
    public int getCode() {
        return code;
    }
    public boolean isInstance(MediaContent mediaContent){
        return switch (this) {
            case MANGA -> mediaContent instanceof Manga;
            case ANIME -> mediaContent instanceof Anime;
        };
    }

    public boolean isInstance(MediaContentDTO mediaContent){
        return switch (this) {
            case MANGA -> mediaContent instanceof MangaDTO;
            case ANIME -> mediaContent instanceof AnimeDTO;
        };
    }
}
