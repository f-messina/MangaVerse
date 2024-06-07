package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import it.unipi.lsmsd.fnf.utils.Constants;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            StringBuilder queryBuilder = new StringBuilder("MATCH (m:Manga {id: $id}) SET");

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
            queryBuilder.append("RETURN m");
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
            String query = "MATCH (m:Manga {id: $id}) DETACH " +
                    "DELETE a " +
                    "RETURN m";

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
                    "CREATE (u)-[r:LIKE {date: date($date)} ]->(m)" +
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
    public PageDTO<MediaContentDTO> getLiked(String userId, int page) throws DAOException {
        try (Session session = getSession()) {
            // Query per ottenere il conteggio totale degli elementi piaciuti
            String countQuery = "MATCH (:User {id: $userId})-[:LIKE]->(m:Manga) " +
                    "RETURN COUNT(m) AS totalLikes";

            // Query per ottenere gli elementi piaciuti con paginazione
            String dataQuery = "MATCH (:User {id: $userId})-[:LIKE]->(m:Manga) " +
                    "RETURN m AS manga " +
                    "SKIP $skip " +
                    "LIMIT $limit";

            Value params = parameters("userId", userId, "skip", (page - 1) * Constants.PAGE_SIZE, "limit", Constants.PAGE_SIZE);

            Record countRecord = session.executeRead(tx -> tx.run(countQuery, parameters("userId", userId)).single());
            int totalLikes = countRecord.get("totalLikes").asInt();

            List<Record> records = session.executeRead(
                    tx -> tx.run(dataQuery, params).list()
            );

            List<MediaContentDTO> likedMangas = records.stream()
                    .map(record -> (MangaDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            return new PageDTO<>(likedMangas, totalLikes);

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
    public List<MediaContentDTO> getSuggestedByFollowings(String userId, Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;
        List<MediaContentDTO> suggested;
        LocalDate now = LocalDate.now();

        try (Session session = getSession()) {
            String query1 = """
                MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[r:LIKE]->(m:Manga)
                WHERE NOT (u)-[:LIKE]->(m) AND r.date >= date($startDate)
                WITH m, COUNT(DISTINCT f) AS num_likes
                RETURN m AS manga
                ORDER BY num_likes DESC
                LIMIT $n""";

            // try to get suggestions based on likes in the last 6 months
            Value params1 = parameters("userId", userId, "n", n, "startDate", now.minusMonths(6));
            List<Record> records = session.executeRead(
                    tx -> tx.run(query1, params1).list()
            );
            n -= records.isEmpty() ? 0 : records.size();
            suggested = records.stream()
                    .map(record -> (MangaDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            // if there are not enough suggestions, add more results from the last 2 years
            if (n > 0) {
                String query2 = """
                MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[r:LIKE]->(m:Manga)
                WHERE NOT (u)-[:LIKE]->(m) AND r.date >= date($startDate) AND r.date <= date($endDate)
                WITH m, COUNT(DISTINCT f) AS num_likes
                RETURN m AS manga
                ORDER BY num_likes DESC
                LIMIT $n""";
                Value params2 = parameters("userId", userId, "n", n, "startDate", now.minusYears(2), "endDate", now.minusMonths(6));
                records = session.executeRead(
                        tx -> tx.run(query2, params2).list()
                );
                n -= records.isEmpty() ? 0 : records.size();
                for (Record record : records) {
                    MangaDTO mangaDTO = (MangaDTO) recordToMediaContentDTO(record);
                    suggested.add(mangaDTO);
                }
            }

            // if there are not enough suggestions, try to get suggestions based on all likes
            if (n > 0) {
                String query3 = """
                    MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[r:LIKE]->(m:Manga)
                    WHERE NOT (u)-[:LIKE]->(m) AND r.date <= date($endDate)
                    WITH m, COUNT(DISTINCT f) AS num_likes
                    RETURN m AS manga
                    ORDER BY num_likes DESC
                    LIMIT $n""";
                Value params3 = parameters("userId", userId, "n", n, "endDate", now.minusYears(2));
                records = session.executeRead(
                        tx -> tx.run(query3, params3).list()
                );

                for (Record record : records) {
                    MangaDTO mangaDTO = (MangaDTO) recordToMediaContentDTO(record);
                    suggested.add(mangaDTO);
                }
            }

            return suggested.isEmpty() ? null : suggested;

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    public List<MediaContentDTO> getSuggestedByLikes(String userId, Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;
        List<MediaContentDTO> suggested;
        LocalDate today = LocalDate.now();

        try (Session session = getSession()) {
            String query1 = """
                    MATCH (u:User {id: $userId})-[r1:LIKE]->(m:Manga)<-[:LIKE]-(f:User)
                    WHERE r1.date >= date($startDate)
                    WITH u, f, COUNT(m) AS common_likes
                    ORDER BY common_likes DESC
                    LIMIT 50
                    MATCH (f)-[:LIKE]->(m2:Manga)
                    WHERE NOT (u)-[:LIKE]->(m2)
                    WITH m2, COUNT(DISTINCT f) AS num_likes
                    RETURN m2 AS manga
                    ORDER BY num_likes DESC
                    LIMIT $n""";

            // Try to get suggestions based on likes in the last 6 months
            Value params1 = parameters("userId", userId, "n", n, "startDate", today.minusMonths(6));
            List<Record> records = session.executeRead(
                    tx -> tx.run(query1, params1).list()
            );
            n -= records.isEmpty() ? 0 : records.size();
            suggested = records.stream()
                    .map(record -> (MangaDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            // If there are not enough suggestions, add more results from the last 2 years
            if (n > 0) {
                String query2 = """
                    MATCH (u:User {id: $userId})-[r1:LIKE]->(m:Manga)<-[:LIKE]-(f:User)
                    WHERE r1.date >= date($startDate) AND r1.date <= date($endDate)
                    WITH u, f, COUNT(m) AS common_likes
                    ORDER BY common_likes DESC
                    LIMIT 50
                    MATCH (f)-[:LIKE]->(m2:Manga)
                    WHERE NOT (u)-[:LIKE]->(m2)
                    WITH m2, COUNT(DISTINCT f) AS num_likes
                    RETURN m2 AS manga
                    ORDER BY num_likes DESC
                    LIMIT $n""";
                Value params2 = parameters("userId", userId, "n", n, "startDate", today.minusYears(2), "endDate", today.minusMonths(6));
                records = session.executeRead(
                        tx -> tx.run(query2, params2).list()
                );
                n -= records.isEmpty() ? 0 : records.size();
                for (Record record : records) {
                    MangaDTO mangaDTO = (MangaDTO) recordToMediaContentDTO(record);
                    suggested.add(mangaDTO);
                }
            }

            // If there are not enough suggestions, add more results based on all likes
            if (n > 0) {
                String query3 = """
                        MATCH (u:User {id: $userId})-[r1:LIKE]->(m:Manga)<-[:LIKE]-(f:User)
                        WHERE r1.date <= date($endDate)
                        WITH u, f, COUNT(m) AS common_likes
                        ORDER BY common_likes DESC
                        MATCH (f)-[:LIKE]->(m2:Manga)
                        WHERE NOT (u)-[:LIKE]->(m2)
                        WITH m2, COUNT(DISTINCT f) AS num_likes
                        RETURN m2 AS manga
                        ORDER BY num_likes DESC
                        LIMIT $n""";
                Value params3 = parameters("userId", userId, "n", n, "endDate", today.minusYears(2));
                records = session.executeRead(
                        tx -> tx.run(query3, params3).list()
                );

                for (Record record : records) {
                    MangaDTO mangaDTO = (MangaDTO) recordToMediaContentDTO(record);
                    suggested.add(mangaDTO);
                }
            }

            return suggested.isEmpty() ? null : suggested;

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
    public Map<MediaContentDTO, Integer> getTrendMediaContentByYear(int year, Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;
        try (Session session = getSession()) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year + 1, 1, 1);

            String query = """
            MATCH (m:Manga)<-[r:LIKE]-(u:User)
            WHERE r.date >= date($startDate) AND r.date < date($endDate)
            WITH m, count(r) AS numLikes
            ORDER BY numLikes DESC
            RETURN m AS manga, numLikes
            LIMIT $n
            """;

            Value params = parameters("startDate", startDate, "endDate", endDate, "n", n);
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, params).list()
            );

            return records.stream().map(record -> {
                MangaDTO mangaDTO = (MangaDTO) recordToMediaContentDTO(record);
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
    public List<MediaContentDTO> getMediaContentTrendByLikes(Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;

        List<MediaContentDTO> trendingContent;
        LocalDate now = LocalDate.now();

        try (Session session = getSession()) {
            String query1 = """
                MATCH (u:User)-[r:LIKE]->(m:Manga)
                WHERE r.date >= date($startDate)
                WITH m, COUNT(r) AS numLikes
                ORDER BY numLikes DESC
                RETURN m AS manga, numLikes
                LIMIT $n
                """;

            // First attempt: last 6 months
            Value params1 = parameters("startDate", now.minusMonths(6), "n", n);
            List<Record> records = session.executeRead(
                    tx -> tx.run(query1, params1).list()
            );
            n -= records.isEmpty() ? 0 : records.size();
            trendingContent = records.stream()
                    .map(record -> (MangaDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            // If not enough results, add more results from the last year
            String query2 = """
                MATCH (u:User)-[r:LIKE]->(m:Manga)
                WHERE r.date >= date($startDate) AND r.date <= date($endDate)
                WITH m, COUNT(r) AS numLikes
                ORDER BY numLikes DESC
                RETURN m AS manga, numLikes
                LIMIT $n
                """;
            if (n > 0) {
                Value params2 = parameters("startDate", now.minusYears(1), "endDate", now.minusMonths(6), "n", n);
                records = session.executeRead(
                        tx -> tx.run(query2, params2).list()
                );
                n -= records.isEmpty() ? 0 : records.size();
                for (Record record : records) {
                    MangaDTO mangaDTO = (MangaDTO) recordToMediaContentDTO(record);
                    trendingContent.add(mangaDTO);
                }
            }

            // If still not enough results, add more results from the last 5 years
            if (n > 0) {
                Value params3 = parameters("startDate", now.minusYears(5), "endDate", now.minusYears(1), "n", n);
                records = session.executeRead(
                        tx -> tx.run(query2, params3).list()
                );

                for (Record record : records) {
                    MangaDTO mangaDTO = (MangaDTO) recordToMediaContentDTO(record);
                    if (!trendingContent.contains(mangaDTO)) {
                        trendingContent.add(mangaDTO);
                    }
                    if (trendingContent.size() == n)
                        break;
                }
            }

            return trendingContent.isEmpty() ? null : trendingContent;

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }


    private MediaContentDTO recordToMediaContentDTO(Record record) {
        MediaContentDTO mangaDTO = new MangaDTO();
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
    public PageDTO<MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page, boolean reducedInfo) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public void upsertReview(ReviewDTO reviewDTO) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public void refreshLatestReviews(String mangaId, List<String> reviewIds) throws DAOException {
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
