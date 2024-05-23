package it.unipi.lsmsd.fnf.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.AnimeStatus;
import it.unipi.lsmsd.fnf.model.enums.MangaStatus;
import it.unipi.lsmsd.fnf.model.enums.MangaType;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.service.ServiceLocator;
import it.unipi.lsmsd.fnf.service.enums.ExecutorTaskServiceType;
import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.UpdateNumberOfLikesTask;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.MediaContentService;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;
import it.unipi.lsmsd.fnf.service.interfaces.UserService;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.exists;
import static it.unipi.lsmsd.fnf.service.ServiceLocator.getExecutorTaskService;
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
        // Test save anime
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        Anime anime = createSampleAnime();
        System.out.println("Anime to save: " + anime);
        assertDoesNotThrow(() -> mediaContentService.saveMediaContent(anime));
        System.out.println("Id anime created: " + anime.getId());

        // Test save manga
        Manga manga = createSampleManga();
        System.out.println("Manga to save: " + manga);
        assertDoesNotThrow(() -> mediaContentService.saveMediaContent(manga));
        System.out.println("Id manga created: " + manga.getId());
        Thread.sleep(1000);
    }

    @Test
    void updateMediaContent() {
        try {
            MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
            String id;
            id = mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            Anime anime = (Anime) mediaContentService.getMediaContentById(id, MediaContentType.ANIME);
            anime.setTitle("Updated Anime");
            anime.setEpisodeCount(24);
            anime.setProducers("Studio Madhouse");
            anime.setYear(2020);
            anime.setStatus(AnimeStatus.ONGOING);
            mediaContentService.updateMediaContent(anime);
            System.out.println("Anime updated: " + anime);

            id = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            Manga manga = (Manga) mediaContentService.getMediaContentById(id, MediaContentType.MANGA);
            manga.setImageUrl("Updated Cover URL");
            manga.setGenres(List.of("Updated Genre"));
            manga.setType(MangaType.LIGHT_NOVEL);
            manga.setStartDate(LocalDate.of(2020, 1, 1));
            manga.setStatus(MangaStatus.FINISHED);
            mediaContentService.updateMediaContent(manga);
            System.out.println("Manga updated: " + manga);

            Thread.sleep(1000);

        } catch (BusinessException e) {
            System.out.println("Error updating media content: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteMediaContent() {
        try {
            MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
            String id = mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.deleteMediaContent(id, MediaContentType.ANIME);
            System.out.println("Anime deleted");

            id = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.deleteMediaContent(id, MediaContentType.MANGA);
            System.out.println("Manga deleted");
            Thread.sleep(1000);

        } catch (BusinessException e) {
            System.out.println("Error deleting media content: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getMediaContentById() {
        try {
            MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
            String id = mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            Anime anime = (Anime) mediaContentService.getMediaContentById(id, MediaContentType.ANIME);
            System.out.println("Anime found: " + anime);

            id = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            Manga manga = (Manga) mediaContentService.getMediaContentById(id, MediaContentType.MANGA);
            System.out.println("Manga found: " + manga);

        } catch (BusinessException e) {
            System.out.println("Error getting media content by id: " + e.getMessage());
        }
    }

    @Test
    void searchByFilter() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        List<Map<String, Object>> animeFilters = List.of(
                Map.of("$in",Map.of("tags", List.of("school clubs", "manwha"))),
                Map.of("$gte", Map.of("average_rating", 8.0))
        );
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> anime = mediaContentService.searchByFilter(animeFilters, Map.of("average_rating", -1), 1, MediaContentType.ANIME);
            System.out.println("Anime found: " + anime);
        });

        List<Map<String, Object>> mangaFilters = List.of(
                Map.of("$in", Map.of("genres", List.of("Fantasy", "Adventure"))),
                Map.of("$gte", Map.of("start_date", LocalDate.of(2015, 1, 1)))
        );
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> manga = mediaContentService.searchByFilter(mangaFilters, Map.of("start_date", -1), 1, MediaContentType.MANGA);
            System.out.println("Manga found: " + manga);
        });
    }

    @Test
    void searchByTitle() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> anime = mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME);
            System.out.println("Anime found: " + anime);
        });

        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> manga = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA);
            System.out.println("Manga found: " + manga);
        });
    }

    @Test
    void addLike() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            UserService userService = ServiceLocator.getUserService();

            String userId = userService.searchFirstNUsers("exampleUser", 1, null).getFirst().getId();
            String animeId = mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.addLike(userId, animeId, MediaContentType.ANIME);
            System.out.println("Like added to anime");

            String mangaId = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.addLike(userId, mangaId, MediaContentType.MANGA);
            System.out.println("Like added to manga");

            Thread.sleep(1000);

        } catch (BusinessException e) {
            System.out.println("Error adding like: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void removeLike() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            UserService userService = ServiceLocator.getUserService();

            String userId = userService.searchFirstNUsers("exampleUser", 1, null).getFirst().getId();
            String animeId = mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.removeLike(userId, animeId, MediaContentType.ANIME);
            System.out.println("Like removed from anime");

            String mangaId = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.removeLike(userId, mangaId, MediaContentType.MANGA);
            System.out.println("Like removed from manga");

            Thread.sleep(1000);

        } catch (BusinessException e) {
            System.out.println("Error removing like: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void isLiked() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            UserService userService = ServiceLocator.getUserService();

            String userId = userService.searchFirstNUsers("exampleUser", 1, null).getFirst().getId();
            String animeId = mediaContentService.searchByTitle("Sample Anime", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            System.out.println("Anime is liked: " + mediaContentService.isLiked(userId, animeId, MediaContentType.ANIME));

            String mangaId = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            System.out.println("Manga is liked: " + mediaContentService.isLiked(userId, mangaId, MediaContentType.MANGA));

        } catch (BusinessException e) {
            System.out.println("Error checking if media content is liked: " + e.getMessage());
        }
    }

    @Test
    void getLikedMediaContent() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            UserService userService = ServiceLocator.getUserService();

            String userId = userService.searchFirstNUsers("exampleUser", 1, null).getFirst().getId();
            List<AnimeDTO> likedAnime = (List<AnimeDTO>) mediaContentService.getLikedMediaContent(userId, 0, MediaContentType.ANIME);
            System.out.println("Liked anime: " + likedAnime);

            List<MangaDTO> likedManga = (List<MangaDTO>) mediaContentService.getLikedMediaContent(userId, 0, MediaContentType.MANGA);
            System.out.println("Liked manga: " + likedManga);

        } catch (BusinessException e) {
            System.out.println("Error getting liked media content: " + e.getMessage());
        }
    }

    @Test
    void getSuggestedMediaContent() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            UserService userService = ServiceLocator.getUserService();

            String userId = userService.searchFirstNUsers("exampleUser", 1, null).getFirst().getId();
            List<AnimeDTO> suggestedAnime = (List<AnimeDTO>) mediaContentService.getSuggestedMediaContent(userId, MediaContentType.ANIME, 5);
            System.out.println("Suggested anime: " + suggestedAnime);

            List<MangaDTO> suggestedManga = (List<MangaDTO>) mediaContentService.getSuggestedMediaContent(userId, MediaContentType.MANGA, 5);
            System.out.println("Suggested manga: " + suggestedManga);

        } catch (BusinessException e) {
            System.out.println("Error getting suggested media content: " + e.getMessage());
        }
    }

    @Test
    void getTrendMediaContentByYear() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            Map<AnimeDTO, Integer> trendAnime =  (Map<AnimeDTO, Integer>) mediaContentService.getTrendMediaContentByYear(2024, MediaContentType.ANIME);
            System.out.println("Trend anime: " + trendAnime);

            Map<MangaDTO, Integer> trendManga =  (Map<MangaDTO, Integer>) mediaContentService.getTrendMediaContentByYear(2024, MediaContentType.MANGA);
            System.out.println("Trend manga: " + trendManga);
        } catch (BusinessException e) {
            System.out.println("Error getting trend media content by year: " + e.getMessage());
        }
    }

    @Test
    void getMediaContentTrendByLikes() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            List<AnimeDTO> trendAnime = (List<AnimeDTO>) mediaContentService.getMediaContentTrendByLikes(MediaContentType.ANIME);
            System.out.println("Trend anime: " + trendAnime);

            List<MangaDTO> trendManga = (List<MangaDTO>) mediaContentService.getMediaContentTrendByLikes(MediaContentType.MANGA);
            System.out.println("Trend manga: " + trendManga);
        } catch (BusinessException e) {
            System.out.println("Error getting trend media content by year: " + e.getMessage());
        }
    }

    @Test
    void getBestCriteria() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            Map<String, Double> bestCriteriaAnime = mediaContentService.getBestCriteria("tags", 1, MediaContentType.ANIME);
            System.out.println("Best criteria anime: " + bestCriteriaAnime);

            Map<String, Double> bestCriteriaManga = mediaContentService.getBestCriteria("authors", 1, MediaContentType.MANGA);
            System.out.println("Best criteria manga: " + bestCriteriaManga);
        } catch (BusinessException e) {
            System.out.println("Error getting best criteria: " + e.getMessage());
        }
    }

    @Test
    void reloadNumberOfLikes() {
        ExecutorTaskService aperiodicExecutorTaskService = getExecutorTaskService(ExecutorTaskServiceType.APERIODIC);
        try {
            //Get all anime and manga ids
            List<String> animeIds = getAnimeIds();
            List<String> mangaIds = getMangaIds();
            // Create a task which updates the number of likes in MongoDB

            for(String animeId : animeIds) {
                UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(animeId, MediaContentType.ANIME);
                aperiodicExecutorTaskService.executeTask(task);
            }

            // Create a task which updates the number of likes in MongoDB
            for(String mangaId : mangaIds) {
                UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(mangaId, MediaContentType.MANGA);
                aperiodicExecutorTaskService.executeTask(task);
            }

            Thread.sleep(60000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    List <String> getAnimeIds() {

        MongoCollection<Document> animeCollection = BaseMongoDBDAO.getCollection("anime");
        List<String> animeIds = new ArrayList<>();
        animeCollection.find(exists("likes", false)).projection(new Document("_id", 1))
                .map(doc -> doc.getObjectId("_id").toHexString())
                .into(animeIds);


        return animeIds;
    }

    List <String> getMangaIds() {

        MongoCollection<Document> mangaCollection = BaseMongoDBDAO.getCollection("manga");
        List<String> mangaIds = new ArrayList<>();
        mangaCollection.find(exists("likes", false)).projection(new Document("_id", 1))
                .map(doc -> doc.getObjectId("_id").toHexString())
                .into(mangaIds);
        return mangaIds;
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

    private Manga createSampleManga() {
        Manga manga = new Manga();
        manga.setTitle("Sample Manga");
        manga.setGenres(List.of("Sample Genre"));
        manga.setType(MangaType.MANGA);
        manga.setStartDate(LocalDate.of(2021, 1, 1));
        manga.setStatus(MangaStatus.ONGOING);
        manga.setImageUrl("Sample Cover URL");
        return manga;
    }
}
