package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
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

public class AnimeDAOImpl extends BaseNeo4JDAO implements MediaContentDAO<Anime> {

    @Override
    public void createNode(MediaContentDTO animeDTO) throws DAOException {
        try (Session session = getSession()) {
            String query = "CREATE (a:Anime {id: $id, title: $title, picture: $picture})";
            session.run(query, Map.of("id", animeDTO.getId(), "title", animeDTO.getTitle(), "picture", animeDTO.getImageUrl()));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public void like(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (a:Anime {id: $animeId}) " +
                    "MERGE (u)-[r:LIKE]->(a) " +
                    "SET r.date = datetime() ";

            session.run(query, Map.of("userId", userId, "animeId", animeId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public void unlike(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(a:Anime {id: $animeId}) DELETE r";
            session.run(query, Map.of("userId", userId, "animeId", animeId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public boolean isLiked(String userId, String mediaId) throws DAOException {
        try (Session session = getSession()) {

            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Anime {id: $mediaId}) RETURN count(r) > 0 as isLiked";
            Record record = session.run(query, Map.of("userId", userId, "mediaId", mediaId)).single();
            return record.get("isLiked").asBoolean();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<AnimeDTO> getLiked(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(a:Anime) RETURN a.id as id, a.title as title, a.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

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
            throw new DAOException(e);
        }
    }

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
            throw new DAOException(e);
        }
    }

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
            throw new DAOException(e);
        }
    }

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
            throw new DAOException(e);
        }
    }

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
            throw new DAOException(e);
        }
    }

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
            throw new DAOException(e);
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
    public String insert(Anime mediaContent) throws DAOException {
        return null;
    }
    @Override
    public void update(Anime mediaContent) throws DAOException {
    }
    @Override
    public Anime find(String id) throws DAOException {
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
