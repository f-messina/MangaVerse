package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.MediaContentDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimeDAONeo4JImpl extends BaseNeo4JDAO implements MediaContentDAO<Anime> {

    //Create a Neo4J Anime node
    @Override
    public void createMediaContentNode(String id, String title, String picture) throws DAOException {
        try (Session session = getSession()) {

            String query = "CREATE (a:Anime {id: $id, title: $title, picture: $picture})";
            session.run(query, Map.of("id", id, "title", title, "picture", picture));

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //like a media content OK
    @Override
    public void likeMediaContent(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (a:Anime {id: $animeId}) " +
                    "MERGE (u)-[r:LIKE]->(a) " +
                    "SET r.date = datetime() ";
            session.run(query, Map.of("userId", userId, "animeId", animeId));

        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    // unlike a media content OK
    @Override
    public void unlikeMediaContent(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(a:Anime {id: $animeId}) DELETE r";
            session.run(query, Map.of("userId", userId, "animeId", animeId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //show list of liked media content

    //OK
    @Override
    public List<AnimeDTO> getLikedMediaContent(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(a:Anime) RETURN a.id as id, a.title as title, a.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return  list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    private AnimeDTO recordToAnimeDTO(Record record) {
        Map<String, Object> map = record.asMap();
        AnimeDTO animeDTO = new AnimeDTO();
        animeDTO.setId(new ObjectId(String.valueOf(map.get("id"))));
        animeDTO.setTitle((String)map.get("title"));
        if (map.get("picture") != null) {
            animeDTO.setImageUrl((String)map.get("picture"));
        }

        return animeDTO;
    }

    //suggest to a user 5 media contents based on the likes of his following:
    //if more than 1 following has liked that media content, show it;

    //OK
    @Override
    public List<AnimeDTO> suggestMediaContent(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(a:Anime) " +
                    "WITH a, COUNT(DISTINCT f) AS num_likes " +
                    "RETURN a.id as id, a.title as title, a.picture as picture " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    //What are the most liked media contents for each year?
    //OK
    @Override
    public List<AnimeDTO> getTrendMediaContentByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (a:Anime)<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH a, count(u) AS numLikes \n" +
                    "RETURN a.id as id, a.title as title, a.picture as picture, numLikes \n" +
                    "ORDER BY numLikes DESC\n" +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //Show the trends of the genres for year

    //OK
    @Override
    public List<String> getMediaContentGenresTrendByYear(int year) throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (a:Anime)<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH a, count(u) AS numLikes\n" +
                    "ORDER BY numLikes DESC\n" +
                    "MATCH (a)-[:BELONGS_TO]->(g:Genre)\n" +
                    "RETURN collect(DISTINCT g.name) AS genreNames";

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

    //Suggest media contents based on the top 3 genres that appear the most

    //OK
    @Override
    public List<AnimeDTO> getMediaContentTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (a:Anime)-[:BELONGS_TO]->(g:Genre)\n" +
                    "WITH a, COUNT(a) as numMediaContents \n" +
                    "ORDER BY numMediaContents DESC\n" +
                    "RETURN a.id as id, a.title as title, a.picture as picture\n" +
                    "LIMIT 5";
            List<Record> records = session.run(query).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    //Show the trends of the likes in general

    //IDK
    @Override
    public List<AnimeDTO> getMediaContentTrendByLikes() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(a:Anime) " +
                    "WITH a, COUNT(r) as numLikes " +
                    "ORDER BY numLikes DESC " +
                    "RETURN a.id as id, a.title as title, a.picture as picture, numLikes " +
                    "LIMIT 5";
            List<Record> records = session.run(query).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<String> getMediaContentGenresTrend() throws DAOException {
        List<String> genreNames = new ArrayList<>();
        try (Session session = getSession()) {
            String query = "MATCH (a:Anime)<-[r:LIKE]-(u:User)\n" +
                    "WITH a, count(u) AS numLikes\n" +
                    "ORDER BY numLikes DESC\n" +
                    "MATCH (a)-[b:BELONGS_TO]->(g:Genre)\n" +
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

    //Suggest the anime based on the most liked genres of a user (not implemented)




    @Override
    public ObjectId insert(Anime mediaContent) throws DAOException {
        return null;
    }

    @Override
    public void update(Anime mediaContent) throws DAOException {

    }

    @Override
    public Anime find(ObjectId id) throws DAOException {
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
