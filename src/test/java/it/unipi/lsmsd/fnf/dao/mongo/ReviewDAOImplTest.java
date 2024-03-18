package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import junit.framework.TestCase;

import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.closeConnection;
import static it.unipi.lsmsd.fnf.dao.base.BaseMongoDBDAO.openConnection;

public class ReviewDAOImplTest extends TestCase {

    public void testInsert() {
    }

    public void testUpdate() {
    }

    public void testDelete() {
    }

    public void testDeleteByMedia() {
    }

    public void testFindByUser() {
    }

    public void testFindByMedia() {
    }

    public void testFindByUserAndMedia() {
    }

    //OK
    public void testAverageRatingUser() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();
            Double averageRating = reviewDAO.averageRatingUser("6577877ce683762347606d98");
            System.out.println(averageRating);
            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    //OK
    public void testRatingMediaContentByYearAnime() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByYear(MediaContentType.ANIME, "65789bbd2f5d29465d0b243e", 2010, 2020);

            System.out.println(averageRating.toString());

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK
    public void testRatingMediaContentByYearManga() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();
            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByYear(MediaContentType.MANGA, "65789bba2f5d29465d0af82a", 2010, 2020);

            System.out.println(averageRating.toString());

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK
    public void testRatingAnimeByMonth() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByMonth(MediaContentType.ANIME, "65789bb72f5d29465d0ad8e8", 2022);
            System.out.println(averageRating.toString());

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK
    public void testRatingMangaByMonth() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

            Map<String, Double> averageRating = reviewDAO.getMediaContentRatingByMonth(MediaContentType.MANGA, "657ac625b34f5514b91efede", 2019);
            System.out.println(averageRating.toString());

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK but careful to what it returns
    public void testSuggestTopAnimeLocation() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.ANIME, "Brazil", "location");

            System.out.println(pageDTO);

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }


    //OK but careful to what it returns

    public void testSuggestTopMangaLocation() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();
            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.MANGA, "Hungary", "location");

            System.out.println(pageDTO);

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    //OK but careful to what it returns
    public void testSuggestTopAnimeBirthday() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.ANIME, "1990", "birthday");

            System.out.println(pageDTO);

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }
    //OK but careful to what it returns

    public void testSuggestTopMangaBirthday() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

            PageDTO<MediaContentDTO> pageDTO = reviewDAO.suggestMediaContent(MediaContentType.MANGA, "1990", "birthday");

            System.out.println(pageDTO);

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    //OK for location maybe we don't need this
    /*public void testAverageRatingByLocation() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

            Map<String, Double> averageRating = reviewDAO.averageRatingByCriteria( "location");

            System.out.println(averageRating.toString());

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    //OK for birthday
    public void testAverageRatingByBirthday() {
        try {
            openConnection();
            ReviewDAOImpl reviewDAO = new ReviewDAOImpl();

            Map<String, Double> averageRating = reviewDAO.averageRatingByCriteria( "birthday");

            System.out.println(averageRating.toString());

            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }*/
}