package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.driver.types.Node;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

/**
 * Implementation of the MediaContentDAO interface for handling Manga objects in Neo4j.
 */
public class MangaDAONeo4JImpl extends BaseNeo4JDAO implements MediaContentDAO<Manga> {

    /**
     * Creates a node for a Manga in the Neo4j database.
     *
     * @param manga The Manga object to be saved.
     * @throws DAOException If an error occurs while creating the Manga node.
     */
    @Override
    public void saveMediaContent(Manga manga) throws DAOException {
        try (Session session = getSession()) {
            String query = "CREATE (m:Manga {id: $id, title: $title, picture: $picture}) RETURN m";

            session.executeWrite(tx -> {
                boolean created = tx.run(query, parameters("id", manga.getId(), "title", manga.getTitle(), "picture", manga.getImageUrl())).hasNext();

                if(!created)
                    throw new Neo4jException("Error while creating user node with username " + manga.getTitle());

                return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Updates an Manga node in the Neo4j database.
     *
     * @param manga The Manga object to be updated.
     * @throws DAOException If an error occurs while updating the Manga node.
     */
    @Override
    public void updateMediaContent(Manga manga) throws DAOException {
        try (Session session = getSession()) {
            StringBuilder queryBuilder = new StringBuilder("MATCH (a:Manga {id: $id}) SET");

            if (manga.getTitle() == null && manga.getImageUrl() == null) {
                throw new IllegalArgumentException("Manga object must have at least one field to update");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("id", manga.getId());
            if (manga.getTitle() != null) {
                queryBuilder.append(" a.title = $title ");
                param.put("title", manga.getTitle());
            }
            if (manga.getImageUrl() != null) {
                if (manga.getTitle() != null)
                    queryBuilder.append(",");
                queryBuilder.append(" a.picture = $picture ");
                param.put("picture", manga.getImageUrl());
            }
            queryBuilder.append("RETURN a");
            String query = queryBuilder.toString();

            session.executeWrite(tx -> {
                boolean updated = tx.run(query, param).hasNext();

                if(!updated)
                    throw new Neo4jException("Error while updating manga node with ID " + manga.getId());

                return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Deletes an Manga node from the Neo4j database.
     *
     * @param mangaId The ID of the Manga node to be deleted.
     * @throws DAOException If an error occurs while deleting the Manga node.
     */
    @Override
    public void deleteMediaContent(String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (a:Manga {id: $id}) DETACH " +
                    "DELETE a " +
                    "RETURN a";

            session.executeWrite(tx -> {
                boolean deleted = tx.run(query, parameters("id", mangaId)).hasNext();

                if(!deleted)
                    throw new Neo4jException("Error while deleting manga node with ID " + mangaId);

                return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
                    "WHERE NOT (u)-[:LIKE]->(m) " +
                    "CREATE (u)-[r:LIKE {date: $date} ]->(m)" +
                    "RETURN r";

            session.executeWrite(tx -> {
                boolean created = tx.run(query, parameters("userId", userId, "mangaId", mangaId, "date", LocalDateTime.now())).hasNext();

                if(!created)
                    throw new Neo4jException("Error while creating like relationship between user " + userId + " and manga " + mangaId);

                return null;
            });
        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Manga {id: $mangaId}) " +
                    "DELETE r " +
                    "RETURN r";

            session.executeWrite(tx -> {
                boolean deleted = tx.run(query, Map.of("userId", userId, "mangaId", mangaId)).hasNext();

                if(!deleted)
                    throw new Neo4jException("Error while deleting like relationship between user " + userId + " and manga " + mangaId);

                return null;
            });

        } catch (TransientException e) {
            throw new DAOException(DAOExceptionType.TRANSIENT_ERROR, e.getMessage());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Checks if a user has liked a specific Manga in the Neo4j database.
     *
     * @param userId   The ID of the user to check.
     * @param mangaId  The ID of the Manga to check.
     * @return True if the user has liked the Manga, false otherwise.
     * @throws DAOException If an error occurs while checking the like status.
     */
    @Override
    public boolean isLiked(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Manga {id: $mangaId}) " +
                    "RETURN r";

            Boolean liked = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "mangaId", mangaId)).hasNext()
            );

            if (liked == null)
                throw new Neo4jException("Error while checking if user with ID " + userId + " has liked manga with ID " + mangaId);

            return liked;

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Integer getNumOfLikes(String mangaId) throws DAOException {
        try(Session session = getSession()){
            String query = "MATCH (:Manga {id: $mangaId})<-[r:LIKE]-() " +
                    "RETURN count(r) as numOfLikes";

            Value value = session.executeRead(
                    tx -> tx.run(query, parameters("mangaId", mangaId)).single().get("numOfLikes")
            );

            if (value == null) {
                throw new Neo4jException("Error while retrieving number of likes for manga " + mangaId);
            }

            return value.asInt();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
            String query = "MATCH (:User {id: $userId})-[:LIKE]->(m:Manga) " +
                    "RETURN m as manga";

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId)).list()
            );

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToMangaDTO)
                    .collect(Collectors.toList());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a list of suggested MangaDTO objects for a user from the Neo4j database.
     *
     * @param userId The ID of the user for whom suggested Manga are to be retrieved.
     * @return A list of MangaDTO objects representing suggested Manga for the user.
     * @throws DAOException If an error occurs while retrieving suggested Manga.
     */
    @Override
    public List<MangaDTO> getSuggested(String userId, Integer limit) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:Manga) " +
                    "WITH m, COUNT(DISTINCT f) AS num_likes  " +
                    "RETURN m as manga " +
                    "LIMIT $n";

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "n", limit == null ? 5 : limit)).list()
            );

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToMangaDTO)
                    .collect(Collectors.toList());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
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
    public Map<MangaDTO, Integer> getTrendMediaContentByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(year + 1, 1, 1, 0, 0);
            String query = """
                    MATCH (m:Manga)<-[r:LIKE]-(u:User)
                    WHERE r.date >= $startDate AND r.date < $endDate
                    WITH m, count(r) AS numLikes
                    ORDER BY numLikes DESC
                    RETURN m as manga, numLikes
                    LIMIT 5
                    """;

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("startDate", startDate, "endDate", endDate)).list()
            );

            return records.stream().map(record -> {
                MangaDTO mangaDTO = recordToMangaDTO(record);
                Integer likes = record.get("numLikes").asInt();
                return Map.entry(mangaDTO, likes);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

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
                    "WHERE r.date >= $startDate AND r.date <= $endDate " +
                    "WITH m, COUNT(r) as numLikes " +
                    "ORDER BY numLikes DESC " +
                    "RETURN m as manga, numLikes " +
                    "LIMIT 5";
            LocalDateTime today = LocalDateTime.now();

            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("startDate", today.minusMonths(6), "endDate", today)).list()
            );

            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    private MangaDTO recordToMangaDTO(Record record) {
        MangaDTO mangaDTO = new MangaDTO();
        Node userNode = record.get("manga").asNode();
        mangaDTO.setId(userNode.get("id").asString());
        mangaDTO.setTitle(userNode.get("title").asString());
        mangaDTO.setImageUrl(userNode.get("picture").asString());

        return mangaDTO;
    }

    // Methods available only in MongoDB
    @Override
    public Manga readMediaContent(String id) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public PageDTO<MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page) throws DAOException {
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

    @Override
    public void updateNumOfLikes(String mediaId, Integer likes) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
}
