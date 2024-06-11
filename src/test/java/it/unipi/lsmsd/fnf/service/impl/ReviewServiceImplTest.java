package it.unipi.lsmsd.fnf.service.impl;

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
import it.unipi.lsmsd.fnf.service.interfaces.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    //The test works correctly
    void addReview() {
        ReviewServiceImpl reviewService = new ReviewServiceImpl();
        try {
            ReviewDTO reviewAnime = createSampleAnimeReview();
            System.out.println(reviewAnime);
            reviewService.addReview(reviewAnime);
            System.out.println("Anime review created: " + reviewAnime);

            ReviewDTO reviewManga = createSampleMangaReview();
            reviewService.addReview(reviewManga);
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
    //Test works correctly
    void deleteReview() throws BusinessException {
        ReviewServiceImpl reviewService = new ReviewServiceImpl();
        //reviewService.deleteReview("6660f39aa6c85e0d7c211a87", "65789bb52f5d29465d0abd00", MediaContentType.ANIME);

        System.out.println("Anime review deleted");
        assertDoesNotThrow(() -> reviewService.deleteReview("6661812cfe743958f861d08b", "657ac61bb34f5514b91ea233", MediaContentType.MANGA, null, false));
        System.out.println("Manga review deleted");
        //System.out.println("Review deleted");
    }

    @Test
    //Test passed
    void findByUser() throws BusinessException {
        ReviewServiceImpl reviewService = new ReviewServiceImpl();
        List<String> review_ids = Arrays.asList("657b300906c134f188830bf8", "657b300a06c134f188834275",
                "657b300b06c134f188835e91",
                "657b300c06c134f188838569",
                "657b300f06c134f18883f896",
                "657b301406c134f18884c6de",
                "657b301406c134f18884d131",
                "657b301806c134f188855e29",
                "657b301806c134f18885676e",
                "657b301806c134f18885772d",
                "657b301906c134f18885897c",
                "657b301906c134f1888592e2",
                "657b301a06c134f18885b5f0",
                "657b301d06c134f1888626f2",
                "657b301d06c134f188862f62",
                "657b301d06c134f188862f55",
                "657b302006c134f18886a0c2",
                "657b302306c134f1888738ee",
                "657b302706c134f18887c04b",
                "657b302906c134f188880aba",
                "657b302906c134f188880c9e",
                "657b302906c134f188881634",
                "657b302a06c134f188884318",
                "657b302a06c134f18888600c",
                "657b302b06c134f188886ca2",
                "657b302c06c134f18888b955",
                "657b302d06c134f18888ddfb",
                "657b302e06c134f18888f85a",
                "657b302e06c134f1888905c8",
                "657b303006c134f188894c83",
                "657ebc330481d3954cf83408",
                "657ebc350481d3954cf849dc",
                "657ebc350481d3954cf8560c",
                "657ebc380481d3954cf87e07",
                "657ed1b20481d3954cf8b464",
                "657ed1b30481d3954cf8c77c",
                "657ed1b40481d3954cf8d9a9",
                "657ed1b60481d3954cf8fe8a",
                "657f8614bf2fc2829aa64738",
                "657f8615bf2fc2829aa658c7",
                "657f8619bf2fc2829aa68e5e",
                "657f8619bf2fc2829aa68ebe",
                "657f861cbf2fc2829aa6b356",
                "657f8623bf2fc2829aa72753",
                "657f8623bf2fc2829aa72986",
                "657f8625bf2fc2829aa73dc7",
                "657f862cbf2fc2829aa7bd33");
        reviewService.getReviewsByIdsList(review_ids, 1, "user");

    }

    @Test
    //Test passed
    void findByMedia() throws BusinessException {
        ReviewServiceImpl reviewService = new ReviewServiceImpl();
        List<String> review_ids = Arrays.asList("657b301306c134f188848204",
                "657ebc330481d3954cf82e95",
                "657ebc350481d3954cf84ad1",
                "657ed1b10481d3954cf89455",
                "66617bb5c1785e44801a3b46",
                "66617fb10e359869b8355537",
                "6661838400d2e33cdcb3a76d",
                "666188df2dd2d41031129197",
                "66618e521c38e86b6dc78d88");
        reviewService.getReviewsByIdsList(review_ids, 1, "media");
    }

    @Test
    void getMediaContentRatingByYear() {
    }

    @Test
    void getMediaContentRatingByMonth() throws BusinessException {
        String id = "657ac61bb34f5514b91ea223";
        int year = 2019;
        ReviewService reviewService = ServiceLocator.getReviewService();
        Map<String,Double> map = reviewService.getMediaContentRatingByMonth(MediaContentType.MANGA, id,year);
        System.out.println(map);
    }

    @Test
    void suggestMediaContent() {
    }

    private ReviewDTO createSampleAnimeReview() throws BusinessException {
        ReviewDTO review = new ReviewDTO();
        UserService userService = ServiceLocator.getUserService();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        UserSummaryDTO user = userService.searchFirstNUsers("Arcane", 1, null).getFirst();
        AnimeDTO anime = (AnimeDTO) mediaContentService.searchByTitle("\"Ai\" wo Taberu", 1, MediaContentType.ANIME).getEntries().getFirst();
        review.setUser(user);
        review.setMediaContent(anime);
        review.setRating(8);
        review.setComment("I liked it a lot!");
        return review;
    }

    private ReviewDTO createSampleMangaReview() throws BusinessException {
        ReviewDTO review = new ReviewDTO();
        UserService userService = ServiceLocator.getUserService();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        UserSummaryDTO user = userService.searchFirstNUsers("Arcane", 1, null).getFirst();
        MangaDTO manga = (MangaDTO) mediaContentService.searchByTitle("Slam Dunk", 1, MediaContentType.MANGA).getEntries().getFirst();
        review.setUser(user);
        review.setMediaContent(manga);
        review.setRating(8);
        review.setComment("I liked it a lot!");
        return review;
    }

    @Test
    public void createSampleMangaReviewTest() throws BusinessException {
        ReviewDTO review = new ReviewDTO();
        UserService userService = ServiceLocator.getUserService();
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        UserSummaryDTO user = userService.searchFirstNUsers("exampleUser", 1, null).getFirst();
        MangaDTO manga = (MangaDTO) mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst();
        review.setUser(user);
        review.setMediaContent(manga);
        review.setRating(5);
        review.setComment("This is a test review");
        System.out.println(review);
    }
}
