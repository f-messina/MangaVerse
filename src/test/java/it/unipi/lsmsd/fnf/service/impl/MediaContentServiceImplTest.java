package it.unipi.lsmsd.fnf.service.impl;

import com.mongodb.client.MongoCollection;
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
import it.unipi.lsmsd.fnf.utils.Constants;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.exists;
import static it.unipi.lsmsd.fnf.dao.neo4j.BaseNeo4JDAO.getSession;
import static it.unipi.lsmsd.fnf.service.ServiceLocator.getExecutorTaskService;
import static java.lang.String.valueOf;
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
    void updateAnime() {
        try {
            MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
            String id = mediaContentService.searchByTitle("Slayers Revolution", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            Anime anime = (Anime) mediaContentService.getMediaContentById(id, MediaContentType.ANIME);
            System.out.println(anime.getReviewIds());
            //anime.setTitle("Slayers Revolution");
            //anime.setEpisodeCount(13); //put back to 13
            //mediaContentService.updateMediaContent(anime, review_ids_anime);
            //System.out.println("Anime updated: " + anime);


            Thread.sleep(1000);

        } catch (BusinessException e) {
            System.out.println("Error updating media content: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateManga() {
        try {
            MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
            String id;


            List<String> review_ids_manga = new ArrayList<>();
            review_ids_manga.add("657b301906c134f18885a314");
            id = mediaContentService.searchByTitle("Sakamichi no Apollon: Bonus Track Updated", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            Manga manga = (Manga) mediaContentService.getMediaContentById(id, MediaContentType.MANGA);
            manga.setTitle("Sakamichi no Apollon: Bonus Track");
            mediaContentService.updateMediaContent(manga, review_ids_manga);
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
            String id = mediaContentService.searchByTitle("Updated Anime", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.deleteMediaContent(id, List.of("657b301906c134f18885a314"), MediaContentType.ANIME);
            System.out.println("Anime deleted");

            id = mediaContentService.searchByTitle("Sample Manga", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.deleteMediaContent(id, List.of("657b301906c134f18885a314"), MediaContentType.MANGA);
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
        List<Pair<String, Object>> animeFilters = List.of(
                Pair.of("$in",Pair.of("tags", List.of("school clubs", "manwha"))),
                Pair.of("$gte", Pair.of("average_rating", 8.0))
        );
        assertDoesNotThrow(() -> {
            PageDTO<MediaContentDTO> anime = mediaContentService.searchByFilter(animeFilters, Map.of("average_rating", -1), 1, MediaContentType.ANIME);
            System.out.println("Anime found: " + anime);
        });

        List<Pair<String, Object>> mangaFilters = List.of(
                Pair.of("$in", Pair.of("genres", List.of("Fantasy", "Adventure"))),
                Pair.of("$gte", Pair.of("start_date", LocalDate.of(2015, 1, 1)))
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

            String userId = userService.searchFirstNUsers("Crystal", 1, null).getFirst().getId();
            System.out.println("User id: " + userId);

            String animeId = mediaContentService.searchByTitle("\"Aesop\" no Ohanashi yori: Ushi to Kaeru, Yokubatta Inu", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            System.out.println("Anime id: " + animeId);
            mediaContentService.addLike(userId, animeId, MediaContentType.ANIME);
            System.out.println("Like added to anime");

            String mangaId = mediaContentService.searchByTitle("Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            System.out.println("Manga id: " + mangaId);
            mediaContentService.addLike(userId, mangaId, MediaContentType.MANGA);
            System.out.println("Like added to manga");

            String userId1 = userService.searchFirstNUsers("Crystal", 1, null).getFirst().getId();

            String animeId1 = mediaContentService.searchByTitle("18X", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, animeId1, MediaContentType.ANIME);
            System.out.println("Like added to anime");

            String mangaId1 = mediaContentService.searchByTitle("Neon Genesis Evangelion", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, mangaId1, MediaContentType.MANGA);
            System.out.println("Like added to manga");

            String animeId2 = mediaContentService.searchByTitle("Angel Beats! Stairway to Heaven", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, animeId2, MediaContentType.ANIME);
            System.out.println("Like added to anime");

            String mangaId2 = mediaContentService.searchByTitle("Given", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, mangaId2, MediaContentType.MANGA);
            System.out.println("Like added to manga");


            String animeId3 = mediaContentService.searchByTitle("AKB0048", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, animeId3, MediaContentType.ANIME);
            System.out.println("Like added to anime");

            String mangaId3 = mediaContentService.searchByTitle("Azumanga Daioh", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, mangaId3, MediaContentType.MANGA);
            System.out.println("Like added to manga");

            String animeId4 = mediaContentService.searchByTitle("Ai Yori Aoshi: Yumegatari", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, animeId4, MediaContentType.ANIME);
            System.out.println("Like added to anime");

            String mangaId4 = mediaContentService.searchByTitle("Natsume Yuujinchou", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, mangaId4, MediaContentType.MANGA);
            System.out.println("Like added to manga");

            String animeId5 = mediaContentService.searchByTitle("Aerover: Pinigseuui Buhwal", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, animeId5, MediaContentType.ANIME);
            System.out.println("Like added to anime");

            String mangaId5 = mediaContentService.searchByTitle("Kusuriya no Hitorigoto", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
            mediaContentService.addLike(userId1, mangaId5, MediaContentType.MANGA);
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

            String userId = userService.searchFirstNUsers("Crystal", 1, null).getFirst().getId();
            String animeId = mediaContentService.searchByTitle("\"Oshi no Ko\" Season 2", 1, MediaContentType.ANIME).getEntries().getFirst().getId();
            System.out.println("Anime is liked: " + mediaContentService.isLiked(userId, animeId, MediaContentType.ANIME));

            String mangaId = mediaContentService.searchByTitle("Slam Dunk", 1, MediaContentType.MANGA).getEntries().getFirst().getId();
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

            String userId = userService.searchFirstNUsers("Crystal", 1, null).getFirst().getId();
            PageDTO<MediaContentDTO> likedAnime = mediaContentService.getLikedMediaContent(userId, 1, MediaContentType.ANIME);
            System.out.println("Liked anime: " + likedAnime.getEntries());

            PageDTO<MediaContentDTO>  likedManga = mediaContentService.getLikedMediaContent(userId, 1, MediaContentType.MANGA);
            System.out.println("Liked manga: " + likedManga.getEntries());

        } catch (BusinessException e) {
            System.out.println("Error getting liked media content: " + e.getMessage());
        }
    }

    @Test
    void getSuggestedMediaContentByFollowings() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            List<MediaContentDTO> suggestedAnime = mediaContentService.getSuggestedMediaContentByFollowings("6577877be68376234760585b", MediaContentType.ANIME, Constants.PAGE_SIZE);
            System.out.println("Suggested anime: " + suggestedAnime);
            System.out.println("Suggested anime size: " + suggestedAnime.size());

            List<MediaContentDTO> suggestedManga = mediaContentService.getSuggestedMediaContentByFollowings("6577877be68376234760585b", MediaContentType.MANGA, Constants.PAGE_SIZE);
            System.out.println("Suggested manga: " + suggestedManga);
            System.out.println("Suggested manga size: " + suggestedManga.size());

        } catch (BusinessException e) {
            System.out.println("Error getting suggested media content: " + e.getMessage());
        }
    }

    @Test
    void getSuggestedMediaContentByLikes() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            List<MediaContentDTO> suggestedAnime = mediaContentService.getSuggestedMediaContentByLikes("6577877be68376234760585b", MediaContentType.ANIME, Constants.PAGE_SIZE);
            System.out.println("Suggested anime: " + suggestedAnime);
            System.out.println("Suggested anime size: " + suggestedAnime.size());

            List<MediaContentDTO> suggestedManga = mediaContentService.getSuggestedMediaContentByLikes("6577877be68376234760585b", MediaContentType.MANGA, Constants.PAGE_SIZE);
            System.out.println("Suggested manga: " + suggestedManga);
            System.out.println("Suggested manga size: " + suggestedManga.size());

        } catch (BusinessException e) {
            System.out.println("Error getting suggested media content: " + e.getMessage());
        }
    }

    @Test
    //Test ok
    void getTrendMediaContentByYear() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            Map<MediaContentDTO, Integer> trendAnime = mediaContentService.getMediaContentTrendByYear(2024, Constants.PAGE_SIZE, MediaContentType.ANIME);
            System.out.println("Trend anime: " + trendAnime);
            System.out.println("Trend anime size: " + trendAnime.size());

            Map<MediaContentDTO, Integer> trendManga = mediaContentService.getMediaContentTrendByYear(2024, Constants.PAGE_SIZE, MediaContentType.MANGA);
            System.out.println("Trend manga: " + trendManga);
            System.out.println("Trend manga size: " + trendManga.size());
        } catch (BusinessException e) {
            System.out.println("Error getting trend media content by year: " + e.getMessage());
        }
    }

    @Test
    //Test ok
    void getMediaContentTrendByLikes() {
        MediaContentService mediaContentService = ServiceLocator.getMediaContentService();
        try {
            List<MediaContentDTO> trendAnime = mediaContentService.getMediaContentTrendByLikes(Constants.PAGE_SIZE, MediaContentType.ANIME);
            System.out.println("Trend anime: " + trendAnime);
            System.out.println("Trend anime size: " + trendAnime.size());

            List<MediaContentDTO> trendManga = mediaContentService.getMediaContentTrendByLikes(Constants.PAGE_SIZE, MediaContentType.MANGA);
            System.out.println("Trend manga: " + trendManga);
            System.out.println("Trend manga size: " + trendManga.size());
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

            for(String animeId : animeIds) {
                // Create a task which updates the number of likes in MongoDB
                UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(animeId, MediaContentType.ANIME, 1);
                aperiodicExecutorTaskService.executeTask(task);
            }

            // Create a task which updates the number of likes in MongoDB
            for(String mangaId : mangaIds) {
                // Create a task which updates the number of likes in MongoDB
                UpdateNumberOfLikesTask task = new UpdateNumberOfLikesTask(mangaId, MediaContentType.MANGA, 1);
                aperiodicExecutorTaskService.executeTask(task);
            }


            Thread.sleep(60000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    //Test passed but it took 3 minutes
    @Test
    void matchIds() throws DAOException, InterruptedException {
        //take the ids of the anime and manga from mongo
        List<String> animeIds = getAnimeIds();
        List<String> mangaIds = getMangaIds();
        //take the ids of the anime and manga from neo4j
        List<String> animeIdsNeo4j = getAnimeIdsNeo4J();
        List<String> mangaIdsNeo4j = getMangaIdsNeo4J();
        //check if the ids are the same, if the id is not on mongo, delete the node in neo4j
        for (String animeId : animeIdsNeo4j) {
            if (!animeIds.contains(animeId)) {
                try (Session session = getSession()) {
                    session.writeTransaction(tx -> {
                        tx.run("MATCH (a:Anime {id: $id}) DETACH DELETE a", Map.of("id", animeId));
                        return null; // Return null because the lambda must return a value
                    });
                } catch (Exception e) {
                    throw new DAOException("Error deleting anime node from Neo4J", e);
                }
            }
        }
        for (String mangaId : mangaIdsNeo4j) {
            if (!mangaIds.contains(mangaId)) {
                try (Session session = getSession()) {
                    session.writeTransaction(tx -> {
                        tx.run("MATCH (m:Manga {id: $id}) DETACH DELETE m", Map.of("id", mangaId));
                        return null; // Return null because the lambda must return a value
                    });
                } catch (Exception e) {
                    throw new DAOException("Error deleting manga node from Neo4J", e);
                }
            }
        }
        Thread.sleep(1000);
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


    List <String> getAnimeIdsNeo4J() throws DAOException {
        List<String> animeIdsNeo4j = new ArrayList<>();
        try (Session session = getSession()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (a:Anime) RETURN a.id AS id");
                while (result.hasNext()) {
                    animeIdsNeo4j.add(result.next().get("id").asString());
                }
                return null; // Return null because the lambda must return a value
            });
        } catch (Exception e) {
            throw new DAOException("Error retrieving anime IDs from Neo4J", e);
        }
        return animeIdsNeo4j;
    }
    List <String> getMangaIdsNeo4J() throws DAOException {
        List<String> mangaIdsNeo4j = new ArrayList<>();
        try (Session session = getSession()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (m:Manga) RETURN m.id AS id");
                while (result.hasNext()) {
                    mangaIdsNeo4j.add(result.next().get("id").asString());
                }
                return null; // Return null because the lambda must return a value
            });
        } catch (Exception e) {
            throw new DAOException("Error retrieving anime IDs from Neo4J", e);
        }
        return mangaIdsNeo4j;
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