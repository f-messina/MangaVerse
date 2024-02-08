package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.Neo4JDAO;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import junit.framework.TestCase;
import  org.junit.Test;
import org.neo4j.driver.Record;

import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.Assert.assertEquals;

public class Neo4JDAOImplTest {

    /*@Test
    public void testLikeMediaContent() throws DAOException {
        Neo4JDAOImpl dao = new Neo4JDAOImpl();
        dao.likeMediaContent("6577877be68376234760585f","65789bb52f5d29465d0abcff");
    }*/

    /*@Test
    public void testFollowUser() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        neo4JDAO.followUser("6577877be68376234760585a", "6577877be683762347605859");
    }*/

    /*@Test
    public void testUnlikeMediaContent() throws DAOException {
        Neo4JDAOImpl dao = new Neo4JDAOImpl();
        dao.unlikeMediaContent("6577877be68376234760585f","65789bb52f5d29465d0abcff");
    }*/

    /*@Test
    public void testUnfollowUser() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        neo4JDAO.unfollowUser("6577877be68376234760585a", "6577877be683762347605859");
    }*/

    /*@Test
    public void testGetLikedMediaContents() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MediaContentDTO> records = neo4JDAO.getLikedMediaContents("6577877be68376234760585f");
    }*/

    @Test
    public void testGetLikedAnime() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<AnimeDTO> anime = neo4JDAO.getLikedAnime("6577877be68376234760585f");

        System.out.println(anime);
    }

    @Test
    public void testGetLikedManga() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.getLikedManga("6577877be68376234760585f");
        System.out.println(manga);
    }

    @Test
    public void testGetFollowing() throws DAOException {
        Neo4JDAO neo4JDAO = new Neo4JDAOImpl();
        List<RegisteredUserDTO> followingUsers = neo4JDAO.getFollowing("6577877be68376234760585d");
        for (RegisteredUserDTO user : followingUsers)
            System.out.println(user);

    }

    @Test
    public void testGetFollowers() throws DAOException {
        Neo4JDAO neo4JDAO = new Neo4JDAOImpl();
        List<RegisteredUserDTO> followerUsers = neo4JDAO.getFollowers("6577877be68376234760585d");
        System.out.println(followerUsers);
    }

    @Test
    public void testSuggestUsers() throws DAOException {
        Neo4JDAO neo4JDAO = new Neo4JDAOImpl();
        List<RegisteredUserDTO> followerUsers = neo4JDAO.suggestUsers("6577877be68376234760585d");
        System.out.println(followerUsers);

    }



    @Test
    public void testSuggestAnime() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        String userId = "6577877be6837623476063e4";

        List<AnimeDTO> suggestedAnime = neo4JDAO.suggestAnime(userId);

        System.out.println(suggestedAnime);
    }

    @Test
    public void testSuggestManga() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.suggestManga("6577877be68376234760585f");
        System.out.println(manga);
    }



    public void testGetTrendAnimeByYear() throws DAOException {
    }

    public void testGetTrendMangaByYear() throws DAOException {
    }



    public void testGetAnimeByGenre() throws DAOException {
    }

    public void testGetMangaByGenre() throws DAOException {
    }



    public void testSuggestAnimeByGenre() throws DAOException {
    }

    public void testSuggestMangaByGenre() throws DAOException {
    }



    public void testGetAnimeGenresTrendByYear() throws DAOException {
    }

    public void testGetMangaGenresTrendByYear() throws DAOException {
    }



    public void testGetAnimeTrendByGenre() throws DAOException {
    }

    public void testGetMangaTrendByGenre() throws DAOException{
    }



    public void testGetAnimeTrendByLikes() throws DAOException{
    }

    public void testGetMangaTrendByLikes() throws DAOException{
    }



    public void testGetAnimeGenresTrend() throws DAOException{
    }

    public void testGetMangaGenresTrend() throws DAOException{
    }

    public void testSuggestedAnime() {
    }
}