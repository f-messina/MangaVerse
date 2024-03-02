package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.Neo4JDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Neo4JDAOImpl extends BaseNeo4JDAO implements Neo4JDAO {

    @Override
    public void likeAnime(String userId, String animeId) throws DAOException {
        try (Driver driver = getDriver();
             Session session = driver.session()) {
            String query = "MATCH (u:User {id: $userId}), (a:Anime {id: $animeId}) " +
                            "MERGE (u)-[r:LIKE]->(a) " +
                            "SET r.date = datetime() ";

            session.run(query, Map.of("userId", userId, "animeId", animeId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public void likeManga(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId}), (m:Manga {id: $mangaId}) " +
                    "MERGE (u)-[r:LIKE]->(m) " +
                    "SET r.date = datetime() ";

            session.run(query, Map.of("userId", userId, "mangaId", mangaId));
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
    public void unlikeAnime(String userId, String animeId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(a:Anime {id: $animeId}) DELETE r";
            session.run(query, Map.of("userId", userId, "animeId", animeId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public void unlikeManga(String userId, String mangaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:Manga {id: $mangaId}) DELETE r";
            session.run(query, Map.of("userId", userId, "mangaId", mangaId));
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
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }



    //show list of liked media content

    //OK
    @Override
    public List<AnimeDTO> getLikedAnime(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(a:Anime) RETURN a.id as id, a.title as title, a.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
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



    //OK
    @Override
    public List<MangaDTO> getLikedManga(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(m:Manga) RETURN m.id as id, m.title as title, m.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException(e);
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


    //show list of following and followers OK
    @Override
    public List<RegisteredUserDTO> getFollowing(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $userId})-[:FOLLOWS]-(f:User) RETURN f.id as id, f.username as username, f.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream()
                    .map(this::recordToRegisteredUserDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    private RegisteredUserDTO recordToRegisteredUserDTO(Record record) {
        Map<String, Object> map = record.asMap();
        RegisteredUserDTO registeredUserDTO = new RegisteredUserDTO();
        registeredUserDTO.setId(String.valueOf(map.get("id")));
        registeredUserDTO.setUsername((String) map.get("username"));
        registeredUserDTO.setProfilePicUrl((String) map.get("picture"));
        return registeredUserDTO;
    }

    //OK
    @Override
    public List<RegisteredUserDTO> getFollowers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (f:User)-[:FOLLOWS]->(following:User {id: $userId}) RETURN f.id as id, f.username as username, f.picture as picture";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    /*
    //Suggest users based on common following OK
    @Override
    public List<RegisteredUserDTO> suggestUsers(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (:User {id: $userId})-[:FOLLOWS]->(following:User)-[:FOLLOWS]->(suggested:User) " +
                    "WHERE NOT (:User{id: $userId})-[:FOLLOWS]->(suggested) " +
                    "WITH suggested, COUNT(DISTINCT following) AS commonFollowers " +
                    "WHERE commonFollowers > 5 " +
                    "RETURN suggested.id as id, suggested.username as username, suggested.picture as picture " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToRegisteredUserDTO).collect(Collectors.toList());
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

    //OK
    @Override
    public List<MangaDTO> suggestManga(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:Manga) " +
                    "WITH m, COUNT(DISTINCT f) AS num_likes  " +
                    "RETURN m.id as id, m.title as title, m.picture as picture " +
                    "LIMIT 5";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
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

    //OK
    @Override
    public List<MangaDTO> getTrendMangaByYear(int year) throws DAOException {
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
            throw new DAOException(e);
        }
    }


    //Show the trends of the genres for year

    //OK
    @Override
    public List<String> getAnimeGenresTrendByYear(int year) throws DAOException {
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



    //OK
    @Override
    public List<String> getMangaGenresTrendByYear(int year) throws DAOException {
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
            throw new DAOException(e);
        }
    }

    //Suggest media contents based on the top 3 genres that appear the most

    //OK
    @Override
    public List<AnimeDTO> getAnimeTrendByGenre() throws DAOException {
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

    //IDK
    @Override
    public List<MangaDTO> getMangaTrendByGenre() throws DAOException {
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
            throw new DAOException(e);
        }
    }

    //Show the trends of the likes in general

    //IDK
    @Override
    public List<AnimeDTO> getAnimeTrendByLikes() throws DAOException {
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


    //IDK
    @Override
    public List<MangaDTO> getMangaTrendByLikes() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:Manga) " +
                            "WITH m, COUNT(r) as numLikes " +
                            "ORDER BY numLikes DESC " +
                            "RETURN m.id as id, m.title as title, m.picture as picture, numLikes " +
                            "LIMIT 5";
            List<Record> records = session.run(query).list();
            return records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    //Show the trends of the genres in general

    //OK
    @Override
    public List<String> getAnimeGenresTrend() throws DAOException {
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

    //OK
    @Override
    public List<String> getMangaGenresTrend() throws DAOException {
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
            throw new DAOException(e);
        }
    }
*/
}