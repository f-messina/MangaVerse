package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import java.util.List;

public interface MangaDAO {

    void updateManga(Manga manga) throws DAOException;

    void insertManga(Manga manga) throws DAOException;

    Manga searchMangaByTitle(String title) throws DAOException;

    List<Manga>  searchMangaByStartDate(int startDate) throws DAOException;

    List<Manga> searchMangaByGenres(List<String> genres) throws DAOException;


    void removeManga(String mangaId) throws DAOException;

}
