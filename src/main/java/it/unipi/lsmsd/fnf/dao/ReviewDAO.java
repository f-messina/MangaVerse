package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;

import java.util.List;

public interface ReviewDAO {
    String insert(ReviewDTO review) throws DAOException;
    void delete(String id) throws DAOException;
    void deleteByMedia(String mediaId) throws DAOException;
    void update(ReviewDTO review) throws DAOException;
    List<ReviewDTO> findByUser(String userId) throws DAOException;
    List<ReviewDTO> findByMedia(String mediaId) throws DAOException;
}

