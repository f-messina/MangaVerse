package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;

import java.util.List;

 public interface ListDAO {
     List<PersonalListDTO> findByUserId(String userId) throws DAOException;
     List<PersonalListDTO> findAll() throws DAOException;
     PersonalListDTO find(String id) throws DAOException;
     void insert(PersonalListDTO list) throws DAOException;
     void changeName(String ID, String name) throws DAOException;
     void delete(String id) throws DAOException;
     void deleteByUser(String userId) throws DAOException;
     void addAnime(String listId, AnimeDTO anime) throws DAOException;
     void addManga(String listId, MangaDTO manga) throws DAOException;
     void removeAnime(String listId, String animeId) throws DAOException;
     void removeManga(String listId, String mangaId) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value) throws DAOException;
}
