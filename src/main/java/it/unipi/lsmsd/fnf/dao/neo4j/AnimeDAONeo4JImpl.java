package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.enums.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
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
 * Implementation of the MediaContentDAO interface for handling Anime objects in Neo4j.
 */
public class AnimeDAONeo4JImpl extends BaseNeo4JDAO implements MediaContentDAO<Anime> {

    /**
     * Creates a node for an Anime in the Neo4j database.
     *
     * @param anime The Anime object to be saved.
     * @throws DAOException If an error occurs while creating the Anime node.
     */
    @Override
    public void saveMediaContent(Anime anime) throws DAOException {
        try (Session session = getSession()) {
            String query = "CREATE (a:Anime {id: $id, title: $title, picture: $picture}) RETURN a";
            session.executeWrite(tx -> {
                boolean created = tx.run(query, parameters("id", anime.getId(), "title", anime.getTitle(), "picture", anime.getImageUrl())).hasNext();

                if(!created)
                    throw new Neo4jException("Error while creating user node with username " + anime.getTitle());

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
     * Updates an Anime node in the Neo4j database.
     *
     * @param anime The Anime object to be updated.
     * @throws DAOException If an error occurs while updating the Anime node.
     */
    @Override
    public void updateMediaContent(Anime anime) throws DAOException {
        try (Session session = getSession()) {
            StringBuilder queryBuilder = new StringBuilder("MATCH (a:Anime {id: $id}) SET");

            if (anime.getTitle() == null && anime.getImageUrl() == null) {
                throw new IllegalArgumentException("Anime object must have at least one field to update");
            }

            Map<String, Object> param = new HashMap<>();
            param.put("id", anime.getId());
            if (anime.getTitle() != null) {
                queryBuilder.append(" a.title = $title ");
                param.put("title", anime.getTitle());
            }
            if (anime.getImageUrl() != null) {
                if (anime.getTitle() != null)
                    queryBuilder.append(",");
                queryBuilder.append(" a.picture = $picture ");
                param.put("picture", anime.getImageUrl());
            }
            queryBuilder.append("RETURN a");
            String query = queryBuilder.toString();

            session.executeWrite(tx -> {
                boolean updated = tx.run(query, param).hasNext();

                if(!updated)
                    throw new Neo4jException("Error while updating anime node with ID " + anime.getId());

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
     * Deletes an Anime node from the Neo4j database.
     *
     * @param animeId The ID of the Anime node to be deleted.
     * @throws DAOException If an error occurs while deleting the Anime node.
     */
    @Override
    public void deleteMediaContent(String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (a:Anime {id: $id}) DETACH DELETE a RETURN a";

            session.executeWrite(tx -> {
                boolean deleted = tx.run(query, parameters("id", animeId)).hasNext();

                if(!deleted)
                    throw new Neo4jException("Error while deleting anime node with ID " + animeId);

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
     * Records a user's like for a specific Anime in the Neo4j database.
     *
     * @param userId  The ID of the user liking the Anime.
     * @param animeId The ID of the Anime being liked.
     * @throws DAOException If an error occurs while processing the like operation.
     */
    @Override
    public void like(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (a:Anime {id: $animeId}) " +
                    "WHERE NOT (u)-[:LIKE]->(a) " +
                    "CREATE (u)-[r:LIKE {date: $date} ]->(a)" +
                    "RETURN r";

            session.executeWrite(tx -> {
                 boolean created = tx.run(query, parameters("userId", userId, "animeId", animeId, "date", LocalDateTime.now())).hasNext();
                 if(!created)
                    throw new Neo4jException("Error while creating like relationship between user " + userId + " and anime " + animeId);

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
     * Removes a user's like for a specific Anime from the Neo4j database.
     *
     * @param userId  The ID of the user unliking the Anime.
     * @param animeId The ID of the Anime being unliked.
     * @throws DAOException If an error occurs while processing the unlike operation.
     */
    @Override
    public void unlike(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(a:Anime {id: $animeId}) " +
                    "DELETE r " +
                    "RETURN r";

            session.executeWrite(tx -> {
                boolean deleted = tx.run(query, parameters("userId", userId, "animeId", animeId)).hasNext();

                if(!deleted)
                    throw new Neo4jException("Error while deleting like relationship between user " + userId + " and anime " + animeId);

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
     * Checks if a user has liked a specific Anime in the Neo4j database.
     *
     * @param userId   The ID of the user to check.
     * @param animeId  The ID of the Anime to check.
     * @return True if the user has liked the Anime, false otherwise.
     * @throws DAOException If an error occurs while checking the like status.
     */
    @Override
    public boolean isLiked(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Anime {id: $animeId}) " +
                    "RETURN r";

            Boolean liked = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "animeId", animeId)).hasNext()
            );

            if (liked == null)
                throw new Neo4jException("Error while checking like relationship between user " + userId + " and anime " + animeId);

            return liked;

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    @Override
    public Integer getNumOfLikes(String animeId) throws DAOException {
        try(Session session = getSession()){
            String query = "MATCH (:Anime {id: $animeId})<-[r:LIKE]-() " +
                    "RETURN count(r) as numOfLikes";

            Value value = session.executeRead(
                    tx -> tx.run(query, parameters("animeId", animeId)).single().get("numOfLikes")
            );

            if (value == null) {
                throw new Neo4jException("Error while retrieving number of likes for anime " + animeId);
            }

            return value.asInt();

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a list of AnimeDTO objects that a user has liked from the Neo4j database.
     *
     * @param userId The ID of the user whose liked Anime are to be retrieved.
     * @return A list of AnimeDTO objects representing the Anime liked by the user.
     * @throws DAOException If an error occurs while retrieving the liked Anime.
     */
    @Override
    public PageDTO<MediaContentDTO> getLiked(String userId, int page) throws DAOException {
        try (Session session = getSession()) {
            String countQuery = "MATCH (u:User {id: $userId})-[:LIKE]->(a:Anime) " +
                    "RETURN COUNT(a) AS totalLikes";

            String dataQuery = "MATCH (u:User {id: $userId})-[:LIKE]->(a:Anime) " +
                    "RETURN a AS anime " +
                    "SKIP $skip " +
                    "LIMIT $limit";

            Value params = parameters("userId", userId, "skip", (page - 1) * Constants.PAGE_SIZE, "limit", Constants.PAGE_SIZE);

            // Retrieve the total number of likes
            Record countRecord = session.executeRead(
                    tx -> tx.run(countQuery, parameters("userId", userId)).single()
            );
            int totalLikes = countRecord.get("totalLikes").asInt();

            // If the user has no likes, return an empty list
            if (totalLikes == 0)
                return new PageDTO<>(new ArrayList<>(), 0);

            // Retrieve the liked Anime
            List<Record> records = session.executeRead(
                    tx -> tx.run(dataQuery, params).list()
            );

            List<MediaContentDTO> likedAnimes = records.stream()
                    .map(record -> (AnimeDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            return new PageDTO<>(likedAnimes, totalLikes);

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a list of suggested AnimeDTO objects for a user from the Neo4j database.
     *
     * @param userId The ID of the user for whom suggested Anime are to be retrieved.
     * @return A list of AnimeDTO objects representing suggested Anime for the user.
     * @throws DAOException If an error occurs while retrieving suggested Anime.
     */
    @Override
    public List<MediaContentDTO> getSuggestedByFollowings(String userId, Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;
        List<MediaContentDTO> suggested;
        LocalDate today = LocalDate.now();

        try (Session session = getSession()) {
            String query1 = """
                MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[r:LIKE]->(a:Anime)
                WHERE NOT (u)-[:LIKE]->(a) AND r.date >= $startDate
                WITH a, COUNT(DISTINCT f) AS num_likes
                RETURN a AS anime
                ORDER BY num_likes DESC
                LIMIT $n""";

            // try to get suggestions based on likes in the last 6 months
            Value params1 = parameters("userId", userId, "n", n, "startDate", today.minusMonths(6).toString());
            List<Record> records = session.executeRead(
                    tx -> tx.run(query1, params1).list()
            );
            n -= records.isEmpty() ? 0 : records.size();
            suggested = records.stream()
                    .map(record -> (AnimeDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            // if there are not enough suggestions, add more results from the last 2 years
            if (n > 0) {
                String query2 = """
                MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[r:LIKE]->(a:Anime)
                WHERE NOT (u)-[:LIKE]->(a) AND r.date >= $startDate AND r.date <= $endDate
                WITH a, COUNT(DISTINCT f) AS num_likes
                RETURN a AS anime
                ORDER BY num_likes DESC
                LIMIT $n""";
                Value params2 = parameters("userId", userId, "n", n, "startDate", today.minusYears(2).toString(), "endDate", today.minusMonths(6).toString());
                records = session.executeRead(
                        tx -> tx.run(query2, params2).list()
                );
                n -= records.isEmpty() ? 0 : records.size();
                for (Record record : records) {
                    AnimeDTO animeDTO = (AnimeDTO) recordToMediaContentDTO(record);
                    suggested.add(animeDTO);
                }
            }

            // if there are not enough suggestions, try to get suggestions based on all likes
            if (n > 0) {
                String query3 = """
                    MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[r:LIKE]->(a:Anime)
                    WHERE NOT (u)-[:LIKE]->(a) AND r.date <= $endDate
                    WITH a, COUNT(DISTINCT f) AS num_likes
                    RETURN a AS anime
                    ORDER BY num_likes DESC
                    LIMIT $n""";
                Value params3 = parameters("userId", userId, "n", n, "endDate", today.minusYears(2).toString());
                records = session.executeRead(
                        tx -> tx.run(query3, params3).list()
                );

                for (Record record : records) {
                    AnimeDTO animeDTO = (AnimeDTO) recordToMediaContentDTO(record);
                    suggested.add(animeDTO);
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
                    MATCH (u:User {id: $userId})-[r1:LIKE]->(a:Anime)<-[:LIKE]-(f:User)
                    WHERE r1.date >= $startDate
                    WITH u, f, COUNT(a) AS common_likes
                    ORDER BY common_likes DESC
                    LIMIT 20
                    MATCH (f)-[:LIKE]->(a2:Anime)
                    WHERE NOT (u)-[:LIKE]->(a2)
                    WITH a2, COUNT(DISTINCT f) AS num_likes
                    RETURN a2 AS anime
                    ORDER BY num_likes DESC
                    LIMIT $n
                    """;

            // Try to get suggestions based on likes in the last 6 months
            Value params1 = parameters("userId", userId, "n", n, "startDate", today.minusMonths(6).toString());
            List<Record> records = session.executeRead(
                    tx -> tx.run(query1, params1).list()
            );
            n -= records.isEmpty() ? 0 : records.size();
            suggested = records.stream()
                    .map(record -> (AnimeDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            // If there are not enough suggestions, add more results from the last 2 years
            if (n > 0) {
                String query2 = """
                    MATCH (u:User {id: $userId})-[r1:LIKE]->(a:Anime)<-[:LIKE]-(f:User)
                    WHERE r1.date >= $startDate AND r1.date <= $endDate
                    WITH u, f, COUNT(a) AS common_likes
                    ORDER BY common_likes DESC
                    LIMIT 20
                    MATCH (f)-[:LIKE]->(a2:Anime)
                    WHERE NOT (u)-[:LIKE]->(a2)
                    WITH a2, COUNT(DISTINCT f) AS num_likes
                    RETURN a2 AS anime
                    ORDER BY num_likes DESC
                    LIMIT $n""";
                    Value params2 = parameters("userId", userId, "n", n, "startDate", today.minusYears(2).toString(), "endDate", today.minusMonths(6).toString());
                records = session.executeRead(
                        tx -> tx.run(query2, params2).list()
                );
                n -= records.isEmpty() ? 0 : records.size();
                for (Record record : records) {
                    AnimeDTO animeDTO = (AnimeDTO) recordToMediaContentDTO(record);
                    suggested.add(animeDTO);
                }
            }

            // If there are not enough suggestions, add more results based on all likes
            if (n > 0) {
                String query3 = """
                        MATCH (u:User {id: $userId})-[r1:LIKE]->(a:Anime)<-[:LIKE]-(f:User)
                        WHERE r1.date <= $endDate
                        WITH u, f, COUNT(a) AS common_likes
                        ORDER BY common_likes DESC
                        MATCH (f)-[:LIKE]->(a2:Anime)
                        WHERE NOT (u)-[:LIKE]->(a2)
                        WITH a2, COUNT(DISTINCT f) AS num_likes
                        RETURN a2 AS anime
                        ORDER BY num_likes DESC
                        LIMIT $n""";
                Value params3 = parameters("userId", userId, "n", n, "endDate", today.minusYears(2).toString());
                records = session.executeRead(
                        tx -> tx.run(query3, params3).list()
                );

                for (Record record : records) {
                    AnimeDTO animeDTO = (AnimeDTO) recordToMediaContentDTO(record);
                    suggested.add(animeDTO);
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
     * Retrieves a list of trending AnimeDTO objects for a specific year from the Neo4j database.
     *
     * @param year The year for which trending Anime are to be retrieved.
     * @return A list of AnimeDTO objects representing trending Anime for the specified year.
     * @throws DAOException If an error occurs while retrieving trending Anime.
     */
    @Override
    public Map<MediaContentDTO, Integer> getTrendMediaContentByYear(int year, Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;
        try (Session session = getSession()) {
            String startDate = LocalDate.of(year, 1, 1).toString();
            String endDate = LocalDate.of(year + 1, 1, 1).toString();

            String query = """
            MATCH (a:Anime)<-[r:LIKE]-(u:User)
            WHERE r.date >= $startDate AND r.date < $endDate
            WITH a, count(r) AS numLikes
            ORDER BY numLikes DESC
            RETURN a AS anime, numLikes
            LIMIT $n
            """;

            Value params = parameters("startDate", startDate, "endDate", endDate, "n", n);
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, params).list()
            );

            return records.stream().map(record -> {
                AnimeDTO animeDTO = (AnimeDTO) recordToMediaContentDTO(record);
                Integer likes = record.get("numLikes").asInt();
                return Map.entry(animeDTO, likes);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }



    /**
     * Retrieves a list of trending AnimeDTO objects by likes from the Neo4j database.
     *
     * @return A list of AnimeDTO objects representing trending Anime by likes.
     * @throws DAOException If an error occurs while retrieving trending Anime by likes.
     */
    @Override
    public List<MediaContentDTO> getMediaContentTrendByLikes(Integer limit) throws DAOException {
        int n = limit == null ? 5 : limit;

        List<MediaContentDTO> trendingContent;
        LocalDate today = LocalDate.now();

        try (Session session = getSession()) {
            String query1 = """
                MATCH (u:User)-[r:LIKE]->(a:Anime)
                WHERE r.date >= $startDate
                WITH a, COUNT(r) AS numLikes
                ORDER BY numLikes DESC
                RETURN a AS anime, numLikes
                LIMIT $n
                """;

            // First attempt: last 6 months
            Value params1 = parameters("startDate", today.minusMonths(6).toString(), "n", n);
            List<Record> records = session.executeRead(
                    tx -> tx.run(query1, params1).list()
            );
            n -= records.isEmpty() ? 0 : records.size();
            trendingContent = records.stream()
                    .map(record -> (AnimeDTO) recordToMediaContentDTO(record))
                    .collect(Collectors.toList());

            // If not enough results, add more results from the last year
            String query2 = """
                MATCH (u:User)-[r:LIKE]->(a:Anime)
                WHERE r.date >= $startDate AND r.date <= $endDate
                WITH a, COUNT(r) AS numLikes
                ORDER BY numLikes DESC
                RETURN a AS anime, numLikes
                LIMIT $n
                """;
            if (n > 0) {
                Value params2 = parameters("startDate", today.minusYears(1).toString(), "endDate", today.minusMonths(6).toString(), "n", n);
                records = session.executeRead(
                        tx -> tx.run(query2, params2).list()
                );
                n -= records.isEmpty() ? 0 : records.size();
                for (Record record : records) {
                    AnimeDTO animeDTO = (AnimeDTO) recordToMediaContentDTO(record);
                    trendingContent.add(animeDTO);
                }
            }

            // If still not enough results, add more results from the last 5 years
            if (n > 0) {
                Value params3 = parameters("startDate", today.minusYears(5).toString(), "endDate", today.minusYears(1).toString(), "n", n);
                records = session.executeRead(
                        tx -> tx.run(query2, params3).list()
                );

                for (Record record : records) {
                    AnimeDTO animeDTO = (AnimeDTO) recordToMediaContentDTO(record);
                    if (!trendingContent.contains(animeDTO)) {
                        trendingContent.add(animeDTO);
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
        MediaContentDTO animeDTO = new AnimeDTO();
        Node userNode = record.get("anime").asNode();
        animeDTO.setId(userNode.get("id").asString());
        animeDTO.setTitle(userNode.get("title").asString());
        animeDTO.setImageUrl(userNode.get("picture").asString());

        return animeDTO;
    }

    // Methods available only in MongoDB
    @Override
    public Anime readMediaContent(String id) throws DAOException {
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
    public void refreshLatestReviews(String animeId) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public boolean isInLatestReviews(String animeId, String reviewId) throws DAOException {
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
