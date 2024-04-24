package it.unipi.lsmsd.fnf.controller;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.ReviewService;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO.closeConnection;
import static org.junit.jupiter.api.Assertions.*;

public class ManagerServletTest {

    @Test
    public void handleBestCriteria() throws DAOException {
        BaseMongoDBDAO.openConnection();

        String criteria = "tags";
        int page = 2;

        boolean isManga = true;

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        switch (criteria) {
            case "tags" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestAnimeCriteria("tags", page);
                    System.out.println(bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "genres" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestMangaCriteria("genres", page);
                    System.out.println(bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "themes" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestMangaCriteria("themes", page);
                    System.out.println(bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "demographics" -> {
                try {
                    Map<String, Double> bestCriteria = mediaContentService.getBestMangaCriteria("demographics", page);
                    System.out.println(bestCriteria);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        closeConnection();
    }

    @Test
    public void handleUserAverageRating() throws DAOException, BusinessException {
        BaseMongoDBDAO.openConnection();

        String userId = ("6577877be6837623476058a9");
        ReviewService reviewService = ServiceLocator.getReviewService();


        try {
            Double averageRating = reviewService.averageRatingUser (userId);
            System.out.println(averageRating);


        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
        closeConnection();

    }


    @Test
    public void handleMediaContentAverageRatingByYear() throws DAOException, BusinessException {
        BaseMongoDBDAO.openConnection();

        String mediaContentId = ("65789bb52f5d29465d0abd4d");
        int startYear = 2010;
        int endYear = 2020;


        ReviewService reviewService = ServiceLocator.getReviewService();

        try {
            Map<String, Double> averageRatingByYear = reviewService.ratingMediaContentByYear(MediaContentType.ANIME, mediaContentId, startYear, endYear);
            System.out.println(averageRatingByYear);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }

        closeConnection();

    }

    @Test
    public void handleMediaContentAverageRatingByMonth() throws DAOException, BusinessException {
        BaseMongoDBDAO.openConnection();
        String mediaContentId = ("657ac622b34f5514b91ee5f0");
        int year = (2015);

        boolean isManga = true;

        ReviewService reviewService = ServiceLocator.getReviewService();

        try {
            Map<String, Double> averageRatingByMonth = reviewService.ratingMediaContentByMonth(MediaContentType.MANGA, mediaContentId, year);
            System.out.println(averageRatingByMonth);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void handleUsersDistribution() throws DAOException {
        BaseMongoDBDAO.openConnection();

        String criteria = ("joined_on");

        UserService userService = ServiceLocator.getUserService();


        switch (criteria) {
            case "gender" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("gender");
                    System.out.println(distribution);

                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "location" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("location");
                    System.out.println(distribution);


                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "birthday" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("birthday");
                    System.out.println(distribution);


                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
            case "joined_on" -> {
                try {
                    Map<String, Integer> distribution = userService.getDistribution("joined_on");
                    System.out.println(distribution);


                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void handleUsersAverageAge() throws DAOException {
        BaseMongoDBDAO.openConnection();

        UserService userService = ServiceLocator.getUserService();

        try {
            Double usersAverageAge = userService.averageAgeUsers();
            System.out.println(usersAverageAge);

        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void handleUsersAverageAppRatingCriteria() throws DAOException {
        BaseMongoDBDAO.openConnection();

        String criteria = ("gender");

        UserService userService = ServiceLocator.getUserService();

        if (criteria.equals("location")) {
            try {
                Map<String, Double> averageAppRatingByCriteria = userService.averageAppRating("location");
                System.out.println(averageAppRatingByCriteria);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        } else if (criteria.equals("gender")) {
            try {
                Map<String, Double> averageAppRatingByCriteria = userService.averageAppRating("gender");
                System.out.println(averageAppRatingByCriteria);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void handleUsersAverageAppRatingAgeRange() throws DAOException {
        BaseMongoDBDAO.openConnection();

        UserService userService = ServiceLocator.getUserService();


        try {
            Map<String, Double> averageAppRatingByAgeRange = userService.averageAppRatingByAgeRange();
            System.out.println(averageAppRatingByAgeRange);
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void handleTrendMediaContentByYear() throws DAOException {
        BaseNeo4JDAO.openConnection();
        int year = (2018);

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<? extends MediaContentDTO> trendMediaContentByYear = mediaContentService.getTrendMediaContentByYear(year, MediaContentType.MANGA);
            System.out.println(trendMediaContentByYear);
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void handleTrendGenresByYear() throws DAOException {
        BaseNeo4JDAO.openConnection();

        int year = (2017);

        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<String> trendGenresByYear = mediaContentService.getMediaContentGenresTrendByYear(year, MediaContentType.MANGA);
            System.out.println(trendGenresByYear);
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    public void handleTrendMediaContentByLikes() throws DAOException {
        BaseNeo4JDAO.openConnection();


        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<? extends MediaContentDTO> trendMediaContentByLikes = mediaContentService.getMediaContentTrendByLikes(MediaContentType.ANIME);
            System.out.println(trendMediaContentByLikes);
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void handleTrendGenres() throws DAOException {
        BaseNeo4JDAO.openConnection();


        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();

        try {
            List<String> trendGenres = mediaContentService.getMediaContentGenresTrend(MediaContentType.ANIME);
            System.out.println(trendGenres);
        } catch (BusinessException e) {
            throw new RuntimeException(e);
        }


    }






}