package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.Neo4JDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Neo4JDAOImpl extends BaseNeo4JDAO implements Neo4JDAO {

    //like a media content OK
    @Override
    public void likeMediaContent(String userId, String mediaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (m:MediaContent {id: $mediaId}) " +
                            "MERGE (u)-[r:LIKE]->(m) " +
                            "SET r.date = datetime() ";
            session.run(query, Map.of("userId", userId, "mediaId", mediaId));

        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    //follow a user OK
    @Override
    public void followUser(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId}), (following:User {id: $followingUserId}) " +
                            "MERGE (follower)-[r:FOLLOWS]->(following) ";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    // unlike a media content OK
    @Override
    public void unlikeMediaContent(String userId, String mediaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:MediaContent {id: $mediaId}) DELETE r";
            session.run(query, Map.of("userId", userId, "mediaId", mediaId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    // unfollow a user OK
    @Override
    public void unfollowUser(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId})-[r:FOLLOWS]->(following:User {id: $followingUserId}) DELETE r";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));

        }
    }



    //show list of liked media content

    //OK
    @Override
    public List<AnimeDTO> getLikedAnime(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(m:MediaContent {type: 'anime'}) RETURN m";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return  list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    private AnimeDTO recordToAnimeDTO(Record record) {
        Map<String, Object> map = record.get(0).asMap();
        return new AnimeDTO(new ObjectId(String.valueOf(map.get("id"))), (String)map.get("title"), (String)map.get("picture"));
    }

    //OK
    @Override
    public List<MangaDTO> getLikedManga(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(m:MediaContent {type: 'manga'}) RETURN m";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private MangaDTO recordToMangaDTO(Record record) {
        Map<String, Object> map = record.get(0).asMap();
        return new MangaDTO(new ObjectId(String.valueOf(map.get("id"))), (String)map.get("title"), (String)map.get("picture"));
    }


    //show list of following and followers OK
    @Override
    public List<RegisteredUserDTO> getFollowing(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $userId})-[:FOLLOWS]-(following:User) RETURN following";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<RegisteredUserDTO> list = records.stream()
                    .map(this::recordToRegisteredUserDTO)
                    .collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    private RegisteredUserDTO recordToRegisteredUserDTO(Record record) {
        Map<String, Object> map = record.get(0).asMap();
        return new RegisteredUserDTO(new ObjectId(String.valueOf(map.get("id"))), (String) map.get("username"), (String) map.get("picture"));
    }

    //OK
    @Override
    public List<RegisteredUserDTO> getFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User)-[:FOLLOWS]->(following:User {id: $userId}) RETURN follower";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<RegisteredUserDTO> list = records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //Suggest users based on common following OK
    @Override
    public List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (:User {id: $userId})-[:FOLLOWS]->(following:User)-[:FOLLOWS]->(suggested:User) " +
                    "WHERE NOT (:User{id: $userId})-[:FOLLOWS]->(suggested) " +
                    "WITH suggested, COUNT(DISTINCT following) AS commonFollowers " +
                    "WHERE commonFollowers > 5 " +
                    "RETURN suggested " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<RegisteredUserDTO> list = records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }



    //suggest to a user 5 media contents based on the likes of his following:
    //if more than 1 following has liked that media content, show it;

    //OK
    @Override
    public List<AnimeDTO> suggestAnime(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:MediaContent {type: 'anime'}) " +
                    "WITH m, count(f) AS num_likes " +
                    "WHERE num_likes > 1 " +
                    "RETURN m " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    //OK
    @Override
    public List<MangaDTO> suggestManga(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:MediaContent {type: 'manga'}) " +
                    "WITH m, count(f) AS num_likes " +
                    "WHERE num_likes > 1 " +
                    "RETURN m " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    //What are the most liked media contents for each year?
    //OK
    @Override
    public List<AnimeDTO> getTrendAnimeByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (m:MediaContent {type: 'anime'})<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH m, count(u) AS numLikes \n" +
                    "RETURN m, numLikes \n" +
                    "ORDER BY numLikes DESC\n" +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //OK
    @Override
    public List<MangaDTO> getTrendMangaByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (m:MediaContent {type: 'manga'})<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH m, count(u) AS numLikes \n" +
                    "RETURN m, numLikes \n" +
                    "ORDER BY numLikes DESC\n" +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    // To show the media contents with a certain genre
    //OK
    @Override
    public List<AnimeDTO> getAnimeByGenre(String genre) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'anime'})-[:BELONGS_TO]->(g:Genre {name: $genre}) " +
                            "RETURN m " +
                            "LIMIT 10";
            List<Record> records =  session.run(query, Map.of("genre", genre)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //OK
    @Override
    public List<MangaDTO> getMangaByGenre(String genre) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'manga'})-[:BELONGS_TO]->(g:Genre {name: $genre}) " +
                            "RETURN m " +
                            "LIMIT 10";
            List<Record> records =  session.run(query, Map.of("genre", genre)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //Suggest the media content based on the most liked genres of a user (not implemented)


    //Show the trends of the genres for year

    //OK
    @Override
    public List<List<String>> getAnimeGenresTrendByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (m:MediaContent {type: 'anime'})<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH m, count(u) AS numLikes\n" +
                    "ORDER BY numLikes DESC\n" +
                    "MATCH (m)-[b:BELONGS_TO]->(g:Genre)\n" +
                    "WITH collect(g.name) AS genreNames\n" +
                    "RETURN genreNames[..10] AS genreNames\n";

            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();

            List<List<String>> genreNamesLists = records.stream()
                    .map(this::recordToGenreNames)
                    .collect(Collectors.toList());

            return genreNamesLists;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    private List<String> recordToGenreNames(Record record) {
        List<String> genreNames = new ArrayList<>();
        List<Object> genreNameObjects = record.get("genreNames").asList();

        genreNameObjects.stream()
                .filter(Objects::nonNull) // Filtra i valori NULL
                .map(Object::toString)
                .forEach(genreNames::add);

        return genreNames;
    }







    //OK
    @Override
    public List<List<String>> getMangaGenresTrendByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String startDate = year + "-01-01";
            String endDate = year + "-12-31";
            String query = "MATCH (m:MediaContent {type: 'manga'})<-[r:LIKE]-(u:User)\n" +
                    "WHERE r.date >= $startDate AND r.date < $endDate\n" +
                    "WITH m, count(u) AS numLikes\n" +
                    "ORDER BY numLikes DESC\n" +
                    "MATCH (m)-[b:BELONGS_TO]->(g:Genre)\n" +
                    "WITH collect(g.name) AS genreNames\n" +
                    "RETURN genreNames[..10] AS genreNames\n";

            List<Record> records = session.run(query, Map.of("startDate", startDate, "endDate", endDate)).list();

            List<List<String>> genreNamesLists = records.stream()
                    .map(this::recordToGenreNames)
                    .collect(Collectors.toList());

            return genreNamesLists;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //Suggest media contents based on the top 3 genres that appear the most

    //OK
    @Override
    public List<AnimeDTO> getAnimeTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'anime'})-[:BELONGS_TO]->(g:Genre)\n" +
                            "WITH m, COUNT(m) as numMediaContents \n" +
                            "ORDER BY numMediaContents DESC\n" +
                            "RETURN m\n" +
                            "LIMIT 5";
            List<Record> records = session.run(query).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    //IDK
    @Override
    public List<MangaDTO> getMangaTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'manga'})-[:BELONGS_TO]->(g:Genre)\n" +
                            "WITH m, COUNT(m) as numMediaContents \n" +
                            "ORDER BY numMediaContents DESC\n" +
                            "RETURN m\n" +
                            "LIMIT 5";
            List<Record> records = session.run(query).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    //Show the trends of the likes in general

    //IDK
    @Override
    public List<AnimeDTO> getAnimeTrendByLikes() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:MediaContent {type: 'anime'}) " +
                            "WITH m, COUNT(r) as numLikes " +
                            "ORDER BY numLikes DESC " +
                            "RETURN m, numLikes " +
                            "LIMIT 5";
            List<Record> records = session.run(query).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }


    //IDK
    @Override
    public List<MangaDTO> getMangaTrendByLikes() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:MediaContent {type: 'manga'}) " +
                            "WITH m, COUNT(r) as numLikes " +
                            "ORDER BY numLikes DESC " +
                            "RETURN m, numLikes " +
                            "LIMIT 5";
            List<Record> records = session.run(query).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    //Show the trends of the genres in general

    //OK
    @Override
    public List<List<String>> getAnimeGenresTrend() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'anime'})<-[r:LIKE]-(u:User)\n" +
                            "WITH m, count(u) AS numLikes\n" +
                            "ORDER BY numLikes DESC\n" +
                            "MATCH (m)-[b:BELONGS_TO]->(g:Genre)\n" +
                            "WITH collect(g.name) AS genreNames\n" +
                            "RETURN genreNames[..10] AS genreNames";
            List<Record> records = session.run(query).list();
            List<List<String>> genreNamesLists = records.stream()
                    .map(this::recordToGenreNames)
                    .collect(Collectors.toList());

            return genreNamesLists;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //OK
    @Override
    public List<List<String>> getMangaGenresTrend() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'manga'})<-[r:LIKE]-(u:User)\n" +
                            "WITH m, count(u) AS numLikes\n" +
                            "ORDER BY numLikes DESC\n" +
                            "MATCH (m)-[b:BELONGS_TO]->(g:Genre)\n" +
                            "WITH collect(g.name) AS genreNames\n" +
                            "RETURN genreNames[..10] AS genreNames";
            List<Record> records = session.run(query).list();
            List<List<String>> genreNamesLists = records.stream()
                    .map(this::recordToGenreNames)
                    .collect(Collectors.toList());

            return genreNamesLists;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

}