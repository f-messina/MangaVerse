package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.interfaces.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.exception.DAOExceptionType;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the MediaContentDAO interface for handling Anime objects in Neo4j.
 */
public class AnimeDAONeo4JImpl extends BaseNeo4JDAO implements MediaContentDAO<Anime> {

    /**
     * Creates a node for an Anime in the Neo4j database.
     *
     * @param animeDTO The AnimeDTO object containing information about the Anime to be created.
     * @throws DAOException If an error occurs while creating the Anime node.
     */
    @Override
    public void createNode(MediaContentDTO animeDTO) throws DAOException {
        try (Session session = getSession()) {
            String query = "CREATE (a:Anime {id: $id, title: $title, picture: $picture})";
            session.run(query, Map.of("id", animeDTO.getId(), "title", animeDTO.getTitle(), "picture", animeDTO.getImageUrl()));
        } catch (Exception e) {
            throw new DAOException("Error while creating anime node", e);
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
                    "MERGE (u)-[r:LIKE]->(a) " +
                    "SET r.date = datetime() ";

            session.run(query, Map.of("userId", userId, "animeId", animeId));
        } catch (Exception e) {
            throw new DAOException("Error while liking anime", e);
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
            session.run(query, Map.of("userId", userId, "animeId", animeId));
        } catch (Exception e) {
            throw new DAOException("Error while unliking anime", e);
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

            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Anime {id: $mediaId}) RETURN count(r) > 0 as isLiked";
            Record record = session.run(query, Map.of("userId", userId, "mediaId", mediaId)).single();
            return record.get("isLiked").asBoolean();
        } catch (Exception e) {
            throw new DAOException("Error while checking if anime is liked", e);
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
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(a:Anime) RETURN a.id as id, a.title as title, a.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException("Error while getting liked anime", e);
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
        } catch (Exception e) {
            throw new DAOException("Error while getting suggested anime", e);
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
        } catch (Exception e) {
            throw new DAOException("Error while getting trend anime by year", e);
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
        }
        catch (Exception e) {
            throw new DAOException("Error while getting trend anime genres by year", e);
        }
    }

    /**
     * Retrieves a list of trending AnimeDTO objects by genre from the Neo4j database.
     *
     * @return A list of AnimeDTO objects representing trending Anime by genre.
     * @throws DAOException If an error occurs while retrieving trending Anime by genre.
     */
    @Override
    public List<AnimeDTO> getMediaContentTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = """
                    MATCH (a:Anime)-[:BELONGS_TO]->(g:Genre)
                    WITH a, COUNT(a) as numMediaContents\s
                    ORDER BY numMediaContents DESC
                    RETURN a.id as id, a.title as title, a.picture as picture
                    LIMIT 5""";
            List<Record> records = session.run(query).list();

            return records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
        } catch(Exception e) {
            throw new DAOException("Error while getting trend anime by genre", e);
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
        } catch(Exception e) {
            throw new DAOException("Error while getting trend anime by likes", e);
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
                    MATCH (a:Anime)<-[r:LIKE]-(u:User)
                    WITH a, count(u) AS numLikes
                    ORDER BY numLikes DESC
                    MATCH (a)-[b:BELONGS_TO]->(g:Genre)
                    WITH collect(g.name) AS genreNames
                    RETURN genreNames[..10] AS genreNames""";
            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();
                List<Object> genreList = record.get("genreNames").asList(Value::asString);
                for (Object obj : genreList) {
                    genreNames.add((String) obj);
                }
            }

            return genreNames;
        } catch (Exception e) {
            throw new DAOException("Error while getting trend anime genres", e);
        }
    }

    private AnimeDTO recordToAnimeDTO(Record record) {
        Map<String, Object> map = record.asMap();
        AnimeDTO animeDTO = new AnimeDTO();
        animeDTO.setId(String.valueOf(map.get("id")));
        animeDTO.setTitle((String)map.get("title"));
        if (map.get("picture") != null) {
            animeDTO.setImageUrl((String)map.get("picture"));
        }

        return animeDTO;
    }

    // Methods available only in MongoDB
    @Override
    public void saveMediaContent(Anime mediaContent) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public void updateMediaContent(Anime mediaContent) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }
    @Override
    public Anime readMediaContent(String id) throws DAOException {
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
    public void updateLatestReview(ReviewDTO reviewDTO) throws DAOException {
        throw new DAOException(DAOExceptionType.UNSUPPORTED_OPERATION, "Method not available in Neo4J");
    }

    @Override
    public Map<String, Double> getBestCriteria(String criteria, boolean isArray, int page) throws DAOException {
        return null;
    }
}
