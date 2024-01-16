package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.model.mediaContent.Anime;

import java.util.List;

public interface AnimeDAO {
    void addAnime(Anime anime);

    void updateAnime(Anime anime);

    List<Anime> searchAnimeByTitle(String title);

    List<Anime>  searchAnimeByYear(int year);

    List<Anime> searchAnimeByTags(List<String> tags);

    void removeAnime(String animeId);

    void closeConnection();
}
