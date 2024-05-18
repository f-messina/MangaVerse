package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.enums.AnimeStatus;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MediaContentServiceImplTest {
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
    void saveMediaContent() throws InterruptedException {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        Anime anime = createSampleAnime();
        System.out.println("Anime to save: " + anime);
        assertDoesNotThrow(() -> mediaContentService.saveMediaContent(anime));
        System.out.println("Id anime created: " + anime.getId());
        Thread.sleep(4*1000);
    }

    @Test
    void updateMediaContent() {
    }

    @Test
    void deleteMediaContent() {
    }

    @Test
    void getMediaContentById() {
    }

    @Test
    void searchByFilter() {
    }

    @Test
    void searchByTitle() {
    }

    @Test
    void addLike() {
    }

    @Test
    void removeLike() {
    }

    @Test
    void isLiked() {
    }

    @Test
    void getLikedMediaContent() {
    }

    @Test
    void getSuggestedMediaContent() {
    }

    @Test
    void getTrendMediaContentByYear() {
    }

    @Test
    void getMediaContentGenresTrendByYear() {
    }

    @Test
    void getMediaContentTrendByLikes() {
    }

    @Test
    void getBestAnimeCriteria() {
    }

    @Test
    void getBestMangaCriteria() {
    }

    private Anime createSampleAnime() {
        Anime anime = new Anime();
        anime.setTitle("Sample Anime");
        anime.setImageUrl("sample.jpg");
        anime.setEpisodeCount(12);
        anime.setProducers("StudioProduction I.G");
        anime.setYear(2019);
        anime.setStatus(AnimeStatus.FINISHED);
        return anime;
    }
}