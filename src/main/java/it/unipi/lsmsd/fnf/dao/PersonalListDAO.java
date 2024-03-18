package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
<<<<<<< HEAD
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

public interface PersonalListDAO {

    void insertList(PersonalListSummaryDTO listSummaryDTO) throws DAOException;

    void updateList(PersonalListSummaryDTO listSummaryDTO) throws DAOException;

    void deleteList(String userId, String listId) throws DAOException;

    void addToList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException;

    void removeFromList(String userId, String listId, String mediaId, MediaContentType mediaType) throws DAOException;

    void removeElementInListWithoutMedia() throws DAOException;
}
=======
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;

import java.util.List;
import java.util.Map;

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
     Map<PageDTO<? extends MediaContentDTO>, Integer> popularMediaContentList(MediaContentType mediaContentType) throws DAOException;

 }
>>>>>>> noemi
