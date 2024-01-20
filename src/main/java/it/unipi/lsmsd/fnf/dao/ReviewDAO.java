package it.unipi.lsmsd.fnf.dao;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReviewDAO {
    void insert(ReviewDTO review) throws DAOException;
    void delete(ObjectId id) throws DAOException;
    void update(ReviewDTO review) throws DAOException;
    List<ReviewDTO> findByUser(ObjectId userId) throws DAOException;
    List<ReviewDTO> findByMedia(ObjectId mediaId) throws DAOException;
    List<ReviewDTO> findByUserAndMedia(ObjectId userId, ObjectId mediaId) throws DAOException;
}

