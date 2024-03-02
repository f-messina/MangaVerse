package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MangaDAONeo4JImpl extends BaseNeo4JDAO implements MediaContentDAO<Manga> {

    //Create a Neo4J Manga node
    @Override
    public void createMediaContentNode(String id, String title, String picture) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (m:Manga {id: $id, title: $title, picture: $picture})";
            session.run(query, Map.of("id", id, "title", title, "picture", picture));

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public void likeMediaContent(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (m:Manga {id: $mangaId}) " +
                    "MERGE (u)-[r:LIKE]->(m) " +
                    "SET r.date = datetime() ";
            session.run(query, Map.of("userId", userId, "mangaId", mangaId));

        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    @Override
    public void unlikeMediaContent(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Manga {id: $mangaId}) DELETE r";
            session.run(query, Map.of("userId", userId, "mangaId", mangaId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //OK
    @Override
    public List<MangaDTO> getLikedMediaContent(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(m:Manga) RETURN m.id as id, m.title as title, m.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private MangaDTO recordToMangaDTO(Record record) {
        Map<String, Object> map = record.asMap();
        MangaDTO mangaDTO = new MangaDTO();
        mangaDTO.setId(new ObjectId(String.valueOf(map.get("id"))));
        mangaDTO.setTitle((String)map.get("title"));
        if (map.get("picture") != null) {
            mangaDTO.setImageUrl((String)map.get("picture"));
        }

        return mangaDTO;
    }

    //OK
    @Override
    public List<MangaDTO> suggestMediaContent(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:Manga) " +
                    "WITH m, COUNT(DISTINCT f) AS num_likes  " +
                    "RETURN m.id as id, m.title as title, m.picture as picture " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    //OK
    @Override
    public List<MangaDTO> getTrendMediaContentByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (m:Manga)<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH m, count(u) AS numLikes \n" +
                    "RETURN m.id as id, m.title as title, m.picture as picture, numLikes \n" +
                    "ORDER BY numLikes DESC\n" +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //OK
    @Override
    public List<String> getMediaContentGenresTrendByYear(int year) throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (m:Manga)<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH m, count(u) AS numLikes\n" +
                    "ORDER BY numLikes DESC\n" +
                    "MATCH (m)-[b:BELONGS_TO]->(g:Genre)\n" +
                    "WITH collect(g.name) AS genreNames\n" +
                    "RETURN genreNames[..10] AS genreNames\n";

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
            throw new DAOException(e);
        }
    }

    @Override
    public List<MangaDTO> getMediaContentTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:Manga)-[:BELONGS_TO]->(g:Genre)\n" +
                    "WITH m, COUNT(m) as numMediaContents \n" +
                    "ORDER BY numMediaContents DESC\n" +
                    "RETURN m.id as id, m.title as title, m.picture as picture\n" +
                    "LIMIT 5";
            List<Record> records = session.run(query).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
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
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    //OK
    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String query = "MATCH (m:Manga)<-[r:LIKE]-(u:User)\n" +
                    "WITH m, count(u) AS numLikes\n" +
                    "ORDER BY numLikes DESC\n" +
                    "MATCH (m)-[b:BELONGS_TO]->(g:Genre)\n" +
                    "WITH collect(g.name) AS genreNames\n" +
                    "RETURN genreNames[..10] AS genreNames";
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

    //Suggest the manga based on the most liked genres of a user (not implemented)



    @Override
    public ObjectId insert(Manga mediaContent) throws DAOException {
        return null;
    }

    @Override
    public void update(Manga mediaContent) throws DAOException {

    }

    @Override
    public Manga find(ObjectId id) throws DAOException {
        return null;
    }

    @Override
    public void delete(ObjectId id) throws DAOException {

    }

    @Override
    public PageDTO<? extends MediaContentDTO> search(Map<String, Object> filters, Map<String, Integer> orderBy, int page) throws DAOException {
        return null;
    }

}
