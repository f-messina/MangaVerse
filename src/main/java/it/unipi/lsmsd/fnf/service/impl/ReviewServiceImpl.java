package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.ReviewDAO;
import it.unipi.lsmsd.fnf.dao.enums.DataRepositoryEnum;
import it.unipi.lsmsd.fnf.dao.mongo.AnimeDAOImpl;
import it.unipi.lsmsd.fnf.dao.mongo.MangaDAOImpl;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.ReviewService;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;

import java.util.List;
import java.util.Map;

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
            throw new BusinessException(e);
        }
    }

    @Override
    public void deleteReview(String reviewId) throws BusinessException {
        try {
            reviewDAO.delete(reviewId);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public void deleteByMedia(String mediaId) throws BusinessException {
        try {
            reviewDAO.deleteByMedia(mediaId);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public void update(ReviewDTO review) throws BusinessException {
        try {
            reviewDAO.update(review);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public List<ReviewDTO> findByUser(String userId) throws BusinessException {
        try {
         return reviewDAO.findByUser(userId);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public List<ReviewDTO> findByMedia(String mediaId) throws BusinessException {
        try{
            return reviewDAO.findByMedia(mediaId);
        }catch (Exception e){
            throw new BusinessException(e);
        }
    }

    //Service for mongoDB queries
    @Override
    public int averageRatingUser(String userId) throws BusinessException {
        try {
            return reviewDAO.averageRatingUser(userId);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public Map<String, Double> ratingMediaContentByPeriod(MediaContentType type, String mediaContentId, String period) throws BusinessException {
        try {
            return reviewDAO.ratingMediaContentByPeriod(type, mediaContentId, period);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public PageDTO<MediaContentDTO> suggestTopMediaContent(MediaContentType mediaContentType, String criteria, String type) throws BusinessException {
        try {
            return reviewDAO.suggestTopMediaContent(mediaContentType, criteria, type);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }

    @Override
    public Map<String, Double> averageRatingByCriteria(String type) throws BusinessException {
        try {
            return reviewDAO.averageRatingByCriteria(type);
        } catch (Exception e){
            throw new BusinessException(e);
        }
    }
}
