package it.unipi.lsmsd.fnf.service.mapper;

import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

public class ModelToDtoMapper {
    public static AnimeDTO animeToAnimeDTO(Anime anime) {
        return new AnimeDTO(anime.getId(), anime.getTitle(), anime.getImageUrl(), anime.getAverageRating(), anime.getYear());
    }

    public static MangaDTO mangaToMangaDTO(Manga manga) {
        return new MangaDTO(manga.getId(), manga.getTitle(), manga.getImageUrl(), manga.getAverageRating(), manga.getStartDate(), manga.getEndDate());
    }
}
