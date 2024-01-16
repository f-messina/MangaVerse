package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import java.util.List;

public interface MangaDAO {
    void addManga(Manga manga);

    void updateManga(Manga manga);

    List<Manga> searchMangaByTitle(String title);

    List<Manga>  searchMangaByStartDate(int startDate);

    List<Manga> searchMangaByGenres(List<String> genres);

    void removeAnime(String mangaId);

    void removeManga(String mangaId);

    void closeConnection();
}
