package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Implementation of the MediaContentDAO interface for handling Manga objects in Neo4j.
 */
public class MangaDAONeo4JImpl extends BaseNeo4JDAO implements MediaContentDAO<Manga> {

    /**
     * Creates a node for a Manga in the Neo4j database.
     *
     * @param mangaDTO The MangaDTO object containing information about the Manga to be created.
     * @throws DAOException If an error occurs while creating the Manga node.
     */
    @Override
    public void createMediaContentNode(MediaContentDTO mangaDTO) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (m:Manga {id: $id, title: $title, picture: $picture})";
            session.run(query, Map.of("id", mangaDTO.getId(), "title", mangaDTO.getTitle(), "picture", mangaDTO.getImageUrl()));

        } catch (Exception e) {
            throw new DAOException("Error while creating manga node", e);
        }
    }

    /**
     * Records a user's like for a specific Manga in the Neo4j database.
     *
     * @param userId  The ID of the user liking the Manga.
     * @param mangaId The ID of the Manga being liked.
     * @throws DAOException If an error occurs while processing the like operation.
     */
    @Override
    public void like(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (m:Manga {id: $mangaId}) " +
                    "MERGE (u)-[r:LIKE]->(m) " +
                    "SET r.date = datetime() ";

            session.run(query, Map.of("userId", userId, "mangaId", mangaId));
        } catch (Exception e) {
            throw new DAOException("Error while liking manga", e);
        }
    }

    /**
     * Removes a user's like for a specific Manga from the Neo4j database.
     *
     * @param userId  The ID of the user unliking the Manga.
     * @param mangaId The ID of the Manga being unliked.
     * @throws DAOException If an error occurs while processing the unlike operation.
     */
    @Override
    public void unlike(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Manga {id: $mangaId}) DELETE r";
            session.run(query, Map.of("userId", userId, "mangaId", mangaId));
        } catch (Exception e) {
            throw new DAOException("Error while unliking manga", e);
        }
    }

    /**
     * Checks if a user has liked a specific Manga in the Neo4j database.
     *
     * @param userId   The ID of the user to check.
     * @param mediaId  The ID of the Manga to check.
     * @return True if the user has liked the Manga, false otherwise.
     * @throws DAOException If an error occurs while checking the like status.
     */
    @Override
    public boolean isLiked(String userId, String mediaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Manga {id: $mediaId}) RETURN count(r) > 0 as isLiked";
            Record record = session.run(query, Map.of("userId", userId, "mediaId", mediaId)).single();
            return record.get("isLiked").asBoolean();
        } catch (Exception e) {
            throw new DAOException("Error while checking if manga is liked", e);
        }
    }

    /**
     * Retrieves a list of MangaDTO objects that a user has liked from the Neo4j database.
     *
     * @param userId The ID of the user whose liked Manga are to be retrieved.
     * @return A list of MangaDTO objects representing the Manga liked by the user.
     * @throws DAOException If an error occurs while retrieving the liked Manga.
     */
    @Override
    public List<MangaDTO> getLiked(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(m:Manga) RETURN m.id as id, m.title as title, m.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while getting liked manga", e);
        }
    }

    private MangaDTO recordToMangaDTO(Record record) {
        Map<String, Object> map = record.asMap();
        MangaDTO mangaDTO = new MangaDTO();
        mangaDTO.setId(String.valueOf(map.get("id")));
        mangaDTO.setTitle((String)map.get("title"));
        if (map.get("picture") != null) {
            mangaDTO.setImageUrl((String)map.get("picture"));
        }

        return mangaDTO;
    }


    /**
     * Retrieves a list of suggested MangaDTO objects for a user from the Neo4j database.
     *
     * @param userId The ID of the user for whom suggested Manga are to be retrieved.
     * @return A list of MangaDTO objects representing suggested Manga for the user.
     * @throws DAOException If an error occurs while retrieving suggested Manga.
     */
    @Override
    public List<MangaDTO> getSuggested(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:Manga) " +
                    "WITH m, COUNT(DISTINCT f) AS num_likes  " +
                    "RETURN m.id as id, m.title as title, m.picture as picture " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while getting suggested manga", e);
        }

    }

    /**
     * Retrieves a list of trending MangaDTO objects for a specific year from the Neo4j database.
     *
     * @param year The year for which trending Manga are to be retrieved.
     * @return A list of MangaDTO objects representing trending Manga for the specified year.
     * @throws DAOException If an error occurs while retrieving trending Manga.
     */
    @Override
    public List<MangaDTO> getTrendMediaContentByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = """
                    MATCH (m:Manga)<-[r:LIKE]-(u:User)
                    WHERE r.date >= $startDate AND r.date < $endDate
                    WITH m, count(u) AS numLikes\s
                    RETURN m.id as id, m.title as title, m.picture as picture, numLikes\s
                    ORDER BY numLikes DESC
                    LIMIT 5""";
            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();

            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while getting trend manga by year", e);
        }
    }

    /**
     * Retrieves a list of trending Manga genres for a specific year from the Neo4j database.
     *
     * @param year The year for which trending Manga genres are to be retrieved.
     * @return A list of Strings representing trending Manga genres for the specified year.
     * @throws DAOException If an error occurs while retrieving trending Manga genres.
     */
    @Override
    public List<String> getMediaContentGenresTrendByYear(int year) throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = """
                    MATCH (m:Manga)<-[r:LIKE]-(u:User)
                    WHERE r.date >= $startDate AND r.date < $endDate
                    WITH m, count(u) AS numLikes
                    ORDER BY numLikes DESC
                    MATCH (m)-[b:BELONGS_TO]->(g:Genre)
                    WITH collect(g.name) AS genreNames
                    RETURN genreNames[..10] AS genreNames
                    """;
            Result result = session.run(query, Values.parameters("startDate", startDate, "endDate", endDate));

            while (result.hasNext()) {
                Record record = result.next();
                List<Object> genreList = record.get("genreNames").asList(Value::asString);
                for (Object obj : genreList) {
                    genreNames.add((String) obj);
                }
            }

            return genreNames;
        } catch (Exception e) {
            throw new DAOException("Error while getting trend manga genres by year", e);
        }
    }

    /**
     * Retrieves a list of trending MangaDTO objects by genre from the Neo4j database.
     *
     * @return A list of MangaDTO objects representing trending Manga by genre.
     * @throws DAOException If an error occurs while retrieving trending Manga by genre.
     */


    /**
     * Retrieves a list of trending MangaDTO objects by likes from the Neo4j database.
     *
     * @return A list of MangaDTO objects representing trending Manga by likes.
     * @throws DAOException If an error occurs while retrieving trending Manga by likes.
     */
    @Override
    public List<MangaDTO> getMediaContentTrendByLikes() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:Manga) " +
                    "WITH m, COUNT(r) as numLikes " +
                    "ORDER BY numLikes DESC " +
                    "RETURN m.id as id, m.title as title, m.picture as picture, numLikes " +
                    "LIMIT 5";
            List<Record> records = session.run(query).list();

            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch(Exception e) {
            throw new DAOException("Error while getting trend manga by likes", e);
        }
    }


    /**
     * Retrieves a list of trending Manga genres from the Neo4j database.
     *
     * @return A list of Strings representing trending Manga genres.
     * @throws DAOException If an error occurs while retrieving trending Manga genres.
     */
    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String query = """
                    MATCH (:User)-[r:LIKE]->(m:Manga)-[:BELONGS_TO]->(g:Genre)
                    WITH g.name AS genreNames, COUNT(r) AS totalLikes
                    RETURN genreNames
                    ORDER BY totalLikes DESC\s""";

            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();
                String genreName = record.get("genreNames").asString();
                genreNames.add(genreName);
            }

            return genreNames;
        } catch (Exception e) {
            throw new DAOException("Error while getting trend manga genres", e);
        }
    }

    // Methods available only in MongoDB
    @Override
    public void saveMediaContent(Manga mediaContent) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public void updateMediaContent(Manga mediaContent) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public Manga readMediaContent(String id) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public void deleteMediaContent(String id) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public PageDTO<? extends MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public void upsertReview(ReviewDTO reviewDTO) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public void refreshLatestReviews(String mangaId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public boolean isInLatestReviews(String mangaId, String reviewId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public void updateUserRedundancy(UserSummaryDTO userSummaryDTO) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public Map<String, Double> getBestCriteria(String criteria, boolean isArray, int page) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
}
