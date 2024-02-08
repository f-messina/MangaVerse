package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.Neo4JDAO;
import it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import org.bson.types.ObjectId;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Neo4JDAOImpl extends BaseNeo4JDAO implements Neo4JDAO {

    //like a media content
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

    //follow a user
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

    // unlike a media content
    @Override
    public void unlikeMediaContent(String userId, String mediaId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[r:LIKE]->(m:MediaContent {id: $mediaId}) DELETE r";
            session.run(query, Map.of("userId", userId, "mediaId", mediaId));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    // unfollow a user
    @Override
    public void unfollowUser(String followerUserId, String followingUserId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (follower:User {id: $followerUserId})-[r:FOLLOWS]->(following:User {id: $followingUserId}) DELETE r";
            session.run(query, Map.of("followerUserId", followerUserId, "followingUserId", followingUserId));

        }
    }



    //show list of liked media content
    /*@Override
    public List<MediaContentDTO> getLikedMediaContents(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:LIKE]->(m:MediaContent) RETURN m";
            List<MediaContentDTO> list = MediaContentDTO.transform(session.run(query, Map.of("userId", userId)).list());
            List<MediaContentDTO> mediaContents = new ArrayList<>();
            return list;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }*/


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
//    private AnimeDTO recordToAnimeDTO(Record record) {
//        return new AnimeDTO(
//                new ObjectId(record.get("id").asString()),
//                record.get("title").asString(),
//                record.get("picture").asString()
//        );
//    }

    private AnimeDTO recordToAnimeDTO(Record record) {
        Map<String, Object> map = record.get(0).asMap();
        return new AnimeDTO(new ObjectId(String.valueOf(map.get("id"))), (String)map.get("title"), (String)map.get("picture"));
    }


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


    //show list of following and followers
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

    //Suggest users based on common following
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
    /*@Override
    public List<Record> suggestMediaContents(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:MediaContent) " +
                    "WITH m, count(f) AS num_likes " +
                    "WHERE num_likes > 1 " +
                    "RETURN m.title, m.id, m.picture, m.type " +
                    "ORDER BY m.date DESC " +
                    "LIMIT 5";
            return session.run(query, Map.of("userId", userId)).list();
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }*/
    private AnimeDTO mapRecordToAnimeFra(Record record){
        AnimeDTO anime = new AnimeDTO();
        Node animeNode;
        try{
            animeNode=record.get("m").asNode();
            anime.setId(new ObjectId(animeNode.get("id").asString()));
            anime.setTitle(animeNode.get("title").asString());


        }
        catch (Exception e){}
        System.out.println(anime);
        return anime;
    }
    @Override
    public List<AnimeDTO> suggestAnime(String userId) throws DAOException {
        List<AnimeDTO> list = new ArrayList<>();
        try (Session session = getSession()) {
            String query = "MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User)-[:LIKE]->(m:MediaContent {type: 'anime'}) " +
                    "WITH m, count(f) AS num_likes " +
                    "WHERE num_likes > 1 " +
                    "RETURN m " +
                    "LIMIT 5";
            List<Record> records = session.run(query, parameters("userId", userId)).list();
            for (Record r : records){
                list.add(mapRecordToAnimeFra(r));
            }
            return list;
            //List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    public List<AnimeDTO> getSuggestedAnimeList(String followerId) {
        List<AnimeDTO> animeList = new ArrayList<>();
        try (Session session = getSession()) {
            String query = "MATCH (:User {id: $followerId})-[:FOLLOWS]->(following:User)-[:LIKE]->(content:MediaContent {type: 'manga'}) " +
                    "RETURN content.id AS contentId, content.title AS contentTitle, content.picture as contentPicture, COUNT(content) AS likeCount " +
                    "ORDER BY likeCount DESC LIMIT 5";
            Result result = session.run(query, Values.parameters("followerId", followerId));
            while (result.hasNext()) {
                Record record = result.next();

                String contentId = record.get("contentId").asString();
                String contentTitle = record.get("contentTitle").asString();
                String image = record.get("contentPicture").asString();
                int likeCount = record.get("likeCount").asInt();

                AnimeDTO animeDTO = new AnimeDTO();
                animeDTO.setId(new ObjectId(contentId));
                animeDTO.setTitle(contentTitle);
                animeDTO.setImageUrl(image);
                animeList.add(animeDTO);
            }
        }
        return animeList;
    }



    private static final String SUGGESTED_ANIME_ERR_MSG = "Error retrieving suggested anime for user: ";

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
    /*@Override
    public List<Record> getTrendByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent)<-[r:LIKE]-(u:User) " +
                            "WHERE r.date >= date($year + '-01-01') AND r.date < date(($year + 1) + '-01-01') " +
                            "WITH m, count(u) AS numLikes " +
                            "RETURN m.title, m.id, m.picture, m.type, numLikes " +
                            "ORDER BY numLikes DESC " +
                            "LIMIT 5";
            return session.run(query, Map.of("year", year)).list();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }*/

    @Override
    public List<AnimeDTO> getTrendAnimeByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'anime'})<-[r:LIKE]-(u:User) " +
                            "WHERE r.date >= date($year + '-01-01') AND r.date < date(($year + 1) + '-01-01') " +
                            "WITH m, count(u) AS numLikes " +
                            "RETURN m, numLikes " +
                            "ORDER BY numLikes DESC " +
                            "LIMIT 5";
            List<Record> records =  session.run(query, Map.of("year", year)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<MangaDTO> getTrendMangaByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'manga'})<-[r:LIKE]-(u:User) " +
                    "WHERE r.date >= date($year + '-01-01') AND r.date < date(($year + 1) + '-01-01') " +
                    "WITH m, count(u) AS numLikes " +
                    "RETURN m, numLikes " +
                    "ORDER BY numLikes DESC " +
                    "LIMIT 5";
            List<Record> records =  session.run(query, Map.of("year", year)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    // To show the media contents with a certain genre
    /*@Override
    public List<Record> getMediaContentByGenre(String genre) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent)-[:BELONGS_TO]->(g:Genre {name: $genre}) " +
                            "RETURN m.title, m.id, m.picture, m.type";
            return session.run(query, Map.of("genre", genre)).list();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }*/
    @Override
    public List<AnimeDTO> getAnimeByGenre(String genre) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'anime'})-[:BELONGS_TO]->(g:Genre {name: $genre}) " +
                            "RETURN m";
            List<Record> records =  session.run(query, Map.of("genre", genre)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<MangaDTO> getMangaByGenre(String genre) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'manga'})-[:BELONGS_TO]->(g:Genre {name: $genre}) " +
                            "RETURN m";
            List<Record> records =  session.run(query, Map.of("genre", genre)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return null;
    }

    //Suggest the media content based on the most liked genres of a user
    /*@Override
    public List<Record> suggestMediaContentByGenre(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User{id: $userId})-[r:LIKE]->(m:MediaContent)" +
                            "MATCH (m)-[b:BELONGS_TO]->(g:Genre)" +
                            "WITH u, g, COUNT(r) as numLikes" +
                            "WHERE numLikes > 4" +
                            "WITH u, g" +
                            "MATCH (m:MediaContent)-[b:BELONGS_TO]->(g)" +
                            "WHERE NOT (u)-[:LIKE]->(m)" +
                            "WITH g, COLLECT(m) as mediaContents" +
                            "RETURN g, mediaContents";
            return session.run(query, Map.of("userId", userId)).list();
        } catch (Exception e) {
            throw new DAOException(e);
        }



    }*/

    @Override
    public List<AnimeDTO> suggestAnimeByGenre(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User{id: $userId})-[r:LIKE]->(m:MediaContent {type: 'anime'})" +
                    "MATCH (m)-[b:BELONGS_TO]->(g:Genre) " +
                    "WITH u, g, COUNT(r) as numLikes " +
                    "WHERE numLikes > 4 " +
                    "WITH u, g " +
                    "MATCH (m:MediaContent)-[b:BELONGS_TO]->(g) " +
                    "WHERE NOT (u)-[:LIKE]->(m) " +
                    "WITH g, COLLECT(m) as mediaContents " +
                    "RETURN g, mediaContents";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    @Override
    public List<MangaDTO> suggestMangaByGenre(String userId) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User{id: $userId})-[r:LIKE]->(m:MediaContent {type: 'manga'}) " +
                    "MATCH (m)-[b:BELONGS_TO]->(g:Genre) " +
                    "WITH u, g, COUNT(r) as numLikes " +
                    "WHERE numLikes > 4 " +
                    "WITH u, g" +
                    "MATCH (m:MediaContent)-[b:BELONGS_TO]->(g) " +
                    "WHERE NOT (u)-[:LIKE]->(m) " +
                    "WITH g, COLLECT(m) as mediaContents " +
                    "RETURN g, mediaContents";
            List<Record> records = session.run(query, Map.of("userId", userId)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    //Show the trends of the genres for year
    /*@Override
    public List<Record> getGenresTrendByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent)<-[r:LIKE]-(u:User)" +
                            "WHERE r.date >= date($year + '-01-01') AND r.date < date(($year + 1) + '-01-01') " +
                            "WITH m, count(u) AS numLikes" +
                            "ORDER BY numLikes DESC" +
                            "MATCH (m)-[b:BELONGS_TO]->(g:Genre)" +
                            "RETURN g" +
                            "LIMIT 10";

            return session.run(query, Map.of("year", year)).list();

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }*/

    @Override
    public List<AnimeDTO> getAnimeGenresTrendByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'anime'})<-[r:LIKE]-(u:User) " +
                            "WHERE r.date >= date($year + '-01-01') AND r.date < date(($year + 1) + '-01-01') " +
                            "WITH m, count(u) AS numLikes " +
                            "ORDER BY numLikes DESC " +
                            "MATCH (m)-[b:BELONGS_TO]->(g:Genre) " +
                            "RETURN g " +
                            "LIMIT 10 ";

            List<Record> records =  session.run(query, Map.of("year", year)).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<MangaDTO> getMangaGenresTrendByYear(int year) throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'manga'})<-[r:LIKE]-(u:User) " +
                            "WHERE r.date >= date($year + '-01-01') AND r.date < date(($year + 1) + '-01-01') " +
                            "WITH m, count(u) AS numLikes " +
                            "ORDER BY numLikes DESC " +
                            "MATCH (m)-[b:BELONGS_TO]->(g:Genre) " +
                            "RETURN g " +
                            "LIMIT 10";

            List<Record> records =  session.run(query, Map.of("year", year)).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    //Suggest media contents based on the top 3 genres that appear the most
    /*@Override
    public List<Record> getTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent)-[:BELONGS_TO]->(g:Genre)" +
                            "WITH g, COUNT(m) as numMediaContents" +
                            "ORDER BY numMediaContents DESC" +
                            "RETURN g, numMediaContents" +
                            "LIMIT 3";
            return session.run(query).list();
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }*/

    @Override
    public List<AnimeDTO> getAnimeTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'anime'})-[:BELONGS_TO]->(g:Genre) " +
                            "WITH g, COUNT(m) as numMediaContents " +
                            "ORDER BY numMediaContents DESC " +
                            "RETURN g, numMediaContents " +
                            "LIMIT 3";
            List<Record> records = session.run(query).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<MangaDTO> getMangaTrendByGenre() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (m:MediaContent {type: 'manga'})-[:BELONGS_TO]->(g:Genre) " +
                            "WITH g, COUNT(m) as numMediaContents " +
                            "ORDER BY numMediaContents DESC " +
                            "RETURN g, numMediaContents " +
                            "LIMIT 3";
            List<Record> records = session.run(query).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }

    //Show the trends of the likes in general
    /*@Override
    public List<Record> getTrendByLikes() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:MediaContent)" +
                            "WITH m, COUNT(r) as numLikes" +
                            "ORDER BY numLikes DESC" +
                            "RETURN m, numLikes" +
                            "LIMIT 5";
            return session.run(query).list();
        } catch(Exception e) {
            throw new DAOException(e);
        }
    }*/

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
    /*@Override
    public List<Record> getGenresTrend() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:MediaContent)" +
                            "WITH m, COUNT(r) as numLikes" +
                            "ORDER BY numLikes DESC" +
                            "WITH m" +
                            "MATCH (m)-[:BELONGS_TO]->(g:Genre)" +
                            "RETURN  g" +
                            "LIMIT 6";
            return session.run(query).list();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }*/

    @Override
    public List<AnimeDTO> getAnimeGenresTrend() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:MediaContent {type: 'anime'}) " +
                            "WITH m, COUNT(r) as numLikes " +
                            "ORDER BY numLikes DESC " +
                            "WITH m " +
                            "MATCH (m)-[:BELONGS_TO]->(g:Genre) " +
                            "RETURN  g " +
                            "LIMIT 6";
            List<Record> records = session.run(query).list();
            List<AnimeDTO> list = records.stream().map(this::recordToAnimeDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<MangaDTO> getMangaGenresTrend() throws DAOException {
        try (Session session = getSession()) {
            String query = "MATCH (u:User)-[r:LIKE]->(m:MediaContent {type: 'manga'}) " +
                            "WITH m, COUNT(r) as numLikes " +
                            "ORDER BY numLikes DESC " +
                            "WITH m " +
                            "MATCH (m)-[:BELONGS_TO]->(g:Genre) " +
                            "RETURN  g " +
                            "LIMIT 6";
            List<Record> records = session.run(query).list();
            List<MangaDTO> list = records.stream().map(this::recordToMangaDTO).collect(Collectors.toList());
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }





}