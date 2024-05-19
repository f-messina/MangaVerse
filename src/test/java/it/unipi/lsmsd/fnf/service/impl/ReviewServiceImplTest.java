package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewServiceImplTest {
    private static final ExecutorTaskService aperiodicTaskService = ServiceLocator.getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);
    private static final TaskManager errorTaskManager = ServiceLocator.getErrorsTaskManager();
    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
        BaseNeo4JDAO.openConnection();
        aperiodicTaskService.start();
        errorTaskManager.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        BaseMongoDBDAO.closeConnection();
        BaseNeo4JDAO.closeConnection();
        aperiodicTaskService.stop();
        errorTaskManager.stop();
    }

    @Test
    void addReview() {
        ReviewServiceImpl reviewService = new ReviewServiceImpl();
        try {
            ReviewDTO reviewAnime = createSampleAnimeReview();
            assertDoesNotThrow(() -> reviewService.addReview(reviewAnime));
            System.out.println("Anime review created: " + reviewAnime);

            ReviewDTO reviewManga = createSampleMangaReview();
            assertDoesNotThrow(() -> reviewService.addReview(reviewManga));
            System.out.println("Manga review created: " + reviewManga);
        } catch (BusinessException e) {
            fail(e);
        }
    }

    @Test
    void updateReview() {
        ReviewServiceImpl reviewService = new ReviewServiceImpl();
        try {
            ReviewDTO reviewAnime = createSampleAnimeReview();
            assertDoesNotThrow(() -> reviewService.addReview(reviewAnime));
            reviewAnime.setComment("This is an updated test review");
            reviewAnime.setRating(4);
            assertDoesNotThrow(() -> reviewService.updateReview(reviewAnime));
            System.out.println("Anime review updated: " + reviewAnime);

            ReviewDTO reviewManga = createSampleMangaReview();
            assertDoesNotThrow(() -> reviewService.addReview(reviewManga));
            reviewManga.setComment("This is an updated test review");
            reviewManga.setRating(4);
            assertDoesNotThrow(() -> reviewService.updateReview(reviewManga));
            System.out.println("Manga review updated: " + reviewManga);
        } catch (BusinessException e) {
            fail(e);
        }
    }

    @Test
    void deleteReview() {
        ReviewServiceImpl reviewService = new ReviewServiceImpl();
        assertDoesNotThrow(() -> reviewService.deleteReview("664a8143e46f76d1dd692183", "664a6d0de46f76d1dd691746", MediaContentType.ANIME));
        System.out.println("Review deleted");
    }

    @Test
    void findByUser() {
    }

    @Test
    void findByMedia() {
    }

    @Test
    void getMediaContentRatingByYear() {
    }

    @Test
    void getMediaContentRatingByMonth() {
    }

    @Test
    void suggestMediaContent() {
    }

    private ReviewDTO createSampleAnimeReview() throws BusinessException {
        ReviewDTO review = new ReviewDTO();
        UserService userService = ServiceLocator.getUserService();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        UserSummaryDTO user = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
        AnimeDTO anime = (AnimeDTO) mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME).getEntries().getFirst();
        review.setUser(user);
        review.setMediaContent(anime);
        review.setRating(5);
        review.setComment("This is a test review");
        return review;
    }

    private ReviewDTO createSampleMangaReview() throws BusinessException {
        ReviewDTO review = new ReviewDTO();
        UserService userService = ServiceLocator.getUserService();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        UserSummaryDTO user = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
        MangaDTO manga = (MangaDTO) mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst();
        review.setUser(user);
        review.setMediaContent(manga);
        review.setRating(5);
        review.setComment("This is a test review");
        return review;
    }
}