package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MangaDAOImpl extends BaseNeo4JDAO implements MediaContentDAO<Manga> {

    @Override
    public void createNode(MediaContentDTO mangaDTO) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (m:Manga {id: $id, title: $title, picture: $picture})";
            session.run(query, Map.of("id", mangaDTO.getId(), "title", mangaDTO.getTitle(), "picture", mangaDTO.getImageUrl()));

        } catch (Exception e) {
            throw new DAOException("Error while creating manga node", e);
        }
    }

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

    @Override
    public void unlike(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Manga {id: $mangaId}) DELETE r";
            session.run(query, Map.of("userId", userId, "mangaId", mangaId));
        } catch (Exception e) {
            throw new DAOException("Error while unliking manga", e);
        }
    }

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

    @Override
    public List<MangaDTO> getMediaContentTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = """
                    MATCH (m:Manga)-[:BELONGS_TO]->(g:Genre)
                    WITH m, COUNT(m) as numMediaContents\s
                    ORDER BY numMediaContents DESC
                    RETURN m.id as id, m.title as title, m.picture as picture
                    LIMIT 5""";
            List<Record> records = session.run(query).list();

            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch(Exception e) {
            throw new DAOException("Error while getting trend manga by genre", e);
        }
    }

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

    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String query = """
                    MATCH (m:Manga)<-[r:LIKE]-(u:User)
                    WITH m, count(u) AS numLikes
                    ORDER BY numLikes DESC
                    MATCH (m)-[b:BELONGS_TO]->(g:Genre)
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
            throw new DAOException("Error while getting trend manga genres", e);
        }
    }

    // Methods available only in MongoDB
    @Override
    public String insert(Manga mediaContent) throws DAOException {
        return null;
    }
    @Override
    public void update(Manga mediaContent) throws DAOException {

    }
    @Override
    public Manga find(String id) throws DAOException {
        return null;
    }
    @Override
    public void delete(String id) throws DAOException {
    }
    @Override
    public PageDTO<? extends MediaContentDTO> search(List<Map<String, Object>> filters, Map<String, Integer> orderBy, int page) throws DAOException {
        return null;
    }
    @Override
    public void updateLatestReview(ReviewDTO reviewDTO) throws DAOException {
    }
}
