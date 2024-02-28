package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.mongo.AnimeDAOImpl;
import it.unipi.lsmsd.fnf.dao.mongo.MangaDAOImpl;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.service.ReviewService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import org.bson.types.ObjectId;

import java.util.List;

import static it.unipi.lsmsd.fnf.dao.DAOLocator.*;

public class ReviewServiceImpl implements ReviewService {

    private static final ReviewDAO reviewDAO;
    private static final MangaDAOImpl mangaDAO;
    private static final AnimeDAOImpl animeDAO;

    static {
        reviewDAO = getReviewDAO(DataRepositoryEnum.MONGODB);
        mangaDAO = (MangaDAOImpl) getMangaDAO(DataRepositoryEnum.MONGODB);
        animeDAO = (AnimeDAOImpl) getAnimeDAO(DataRepositoryEnum.MONGODB);
    }

    @Override
    public void addReview(ReviewDTO review) throws BusinessException {
        try{
            reviewDAO.insert(review);

        } catch (Exception e){
            throw new BusinessException("Error adding review",e);
        }
    }

    @Override
    public void deleteReview(String id) throws BusinessException {
        try {
            reviewDAO.delete(new ObjectId(id));
        } catch (Exception e){
            throw new BusinessException("Error deleting review",e);
        }
    }

    @Override
    public void deleteByMedia(String mediaId) throws BusinessException {
        try {
            reviewDAO.deleteByMedia(new ObjectId(mediaId));
        } catch (Exception e){
            throw new BusinessException("Error deleting by media",e);
        }
    }

    @Override
    public void update(ReviewDTO review) throws BusinessException {
        try {
            reviewDAO.update(review);
        } catch (Exception e){
            throw new BusinessException("Error updating the review",e);
        }
    }

    @Override
    public List<ReviewDTO> findByUser(String userId) throws BusinessException {
        try {
         return reviewDAO.findByUser(new ObjectId(userId));
        } catch (Exception e){
            throw new BusinessException("Eeror finging media by user",e);
        }
    }

    @Override
    public List<ReviewDTO> findByMedia(String mediaId) throws BusinessException {
        try{
            return reviewDAO.findByMedia(new ObjectId(mediaId));
        }catch (Exception e){
            throw new BusinessException("Error finding review by media",e);
        }
    }
}
