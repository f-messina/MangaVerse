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
            String query = "CREATE (a:Anime {id: $id, title: $title, picture: $picture})";
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
                queryBuilder.append(" a.title = $title");
                param.put("title", anime.getTitle());
            }
            if (anime.getImageUrl() != null) {
                if (anime.getTitle() != null)
                    queryBuilder.append(",");
                queryBuilder.append(" a.picture = $picture");
                param.put("picture", anime.getImageUrl());
            }
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
            String query = "MATCH (a:Anime {id: $id}) DETACH DELETE a";
            session.run(query, Map.of("id", animeId));
        } catch (Exception e) {
            throw new DAOException("Error while deleting anime node", e);
        }

        try (Session session = getSession()) {
            String query = "MATCH (a:Anime {id: $id}) DETACH DELETE a";
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
                    "WHERE NOT (u)-[:LIKE]->(r) CREATE (u)-[:LIKE {date: $date} ]->(r)" +
                    "SET r.date = datetime() ";

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
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(a:Anime {id: $animeId}) DELETE r";

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
     * @param mediaId  The ID of the Anime to check.
     * @return True if the user has liked the Anime, false otherwise.
     * @throws DAOException If an error occurs while checking the like status.
     */
    @Override
    public boolean isLiked(String userId, String mediaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Anime {id: $mediaId}) RETURN r";
            Boolean liked = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId, "mediaId", mediaId)).hasNext()
            );

            if (liked == null)
                throw new Neo4jException("Error while checking like relationship between user " + userId + " and anime " + mediaId);

            return liked;

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
    public List<AnimeDTO> getLiked(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(a:Anime) RETURN a AS anime";
            List<Record> records = session.executeRead(
                    tx -> tx.run(query, parameters("userId", userId))
            ).list();

            return records.isEmpty() ? null : records.stream()
                    .map(this::recordToAnimeDTO)
                    .toList();

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
    public List<AnimeDTO> getSuggested(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(a:Anime) " +
                    "WITH a, COUNT(DISTINCT f) AS num_likes " +
                    "RETURN a.id as id, a.title as title, a.picture as picture " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();

            return records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());

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
    public List<AnimeDTO> getTrendMediaContentByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = """
                    MATCH (a:Anime)<-[r:LIKE]-(u:User)
                    WHERE r.date >= $startDate AND r.date < $endDate
                    WITH a, count(u) AS numLikes\s
                    RETURN a.id as id, a.title as title, a.picture as picture, numLikes\s
                    ORDER BY numLikes DESC
                    LIMIT 5""";
            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();
            return records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a list of trending Anime genres for a specific year from the Neo4j database.
     *
     * @param year The year for which trending Anime genres are to be retrieved.
     * @return A list of Strings representing trending Anime genres for the specified year.
     * @throws DAOException If an error occurs while retrieving trending Anime genres.
     */
    @Override
    public List<String> getMediaContentGenresTrendByYear(int year) throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = """
                    MATCH (a:Anime)<-[r:LIKE]-(u:User)
                    WHERE r.date >= $startDate AND r.date < $endDate
                    WITH a, count(u) AS numLikes
                    ORDER BY numLikes DESC
                    MATCH (a)-[:BELONGS_TO]->(g:Genre)
                    RETURN collect(DISTINCT g.name) AS genreNames""";
            Result result = session.run(query, Values.parameters("startDate", startDate, "endDate", endDate));

            while (result.hasNext()) {
                Record record = result.next();
                List<Object> genreList = record.get("genreNames").asList(Value::asString);
                for (Object obj : genreList) {
                    genreNames.add((String) obj);
                }
            }

            return genreNames;

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
    public List<AnimeDTO> getMediaContentTrendByLikes() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(a:Anime) " +
                    "WITH a, COUNT(r) as numLikes " +
                    "ORDER BY numLikes DESC " +
                    "RETURN a.id as id, a.title as title, a.picture as picture, numLikes " +
                    "LIMIT 5";
            List<Record> records = session.run(query).list();

            return records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    /**
     * Retrieves a list of trending Anime genres from the Neo4j database.
     *
     * @return A list of Strings representing trending Anime genres.
     * @throws DAOException If an error occurs while retrieving trending Anime genres.
     */
    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String query = """
                    MATCH (:User)-[r:LIKE]->(m:Anime)-[:BELONGS_TO]->(g:Genre)
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

        } catch (Neo4jException e) {
            throw new DAOException(DAOExceptionType.DATABASE_ERROR, e.getMessage());

        } catch (Exception e) {
            throw new DAOException(DAOExceptionType.GENERIC_ERROR, e.getMessage());
        }
    }

    private AnimeDTO recordToAnimeDTO(Record record) {
        AnimeDTO animeDTO = new AnimeDTO();
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
    public PageDTO<? extends MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page) throws DAOException {
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
}
