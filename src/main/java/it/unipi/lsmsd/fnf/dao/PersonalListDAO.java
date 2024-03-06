package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.enums.SearchCriteriaEnum;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import java.util.List;

 public interface PersonalListDAO {
     List<PersonalListDTO> findByUser(String userId, boolean reducedInfo) throws DAOException;
     List<PersonalListDTO> findAll() throws DAOException;
     PersonalListDTO find(String id) throws DAOException;
     String insert(PersonalListDTO list) throws DAOException;
     void update(PersonalListDTO list) throws DAOException;
     void delete(String id) throws DAOException;
     void deleteByUser(String userId) throws DAOException;
     void addToList(String listId, MediaContentDTO anime) throws DAOException;
     void removeFromList(String listId, String animeId, MediaContentType type) throws DAOException;
     void updateItem(MediaContentDTO anime) throws DAOException;
     void removeItem(String animeId) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria) throws DAOException;
     List<AnimeDTO> findPopularAnime(SearchCriteriaEnum criteria, String value) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria) throws DAOException;
     List<MangaDTO> findPopularManga(SearchCriteriaEnum criteria, String value) throws DAOException;

     //MongoDB queries
     //Find tha anime/manga most present in all of the lists
     PageDTO<? extends MediaContentDTO> popularMediaContentList(MediaContentType mediaContentType) throws DAOException;

 }
