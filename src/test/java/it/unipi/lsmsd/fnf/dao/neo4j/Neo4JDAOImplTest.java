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

    /*@Test
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



    /*@Test
    public void testGetTrendAnimeByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<AnimeDTO> anime = neo4JDAO.getTrendAnimeByYear(2019);
        System.out.println(anime);
    }

    @Test
    public void testGetTrendMangaByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.getTrendMangaByYear(2019);
        System.out.println(manga);
    }*/



    /*@Test
    public void testGetAnimeByGenre() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<AnimeDTO> anime = neo4JDAO.getAnimeByGenre("comedy");
        System.out.println(anime);
    }

    @Test
    public void testGetMangaByGenre() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.getMangaByGenre("Fantasy");
        System.out.println(manga);
    }*/



    /*@Test
    public void testGetAnimeGenresTrendByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<List<String>> genre = neo4JDAO.getAnimeGenresTrendByYear(2019);
        System.out.println(genre);
    }

    @Test
    public void testGetMangaGenresTrendByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<List<String>> genre = neo4JDAO.getMangaGenresTrendByYear(2019);
        System.out.println(genre);
    }*/


    /*
    @Test
    public void testGetAnimeTrendByGenre() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<AnimeDTO> suggestedAnime = neo4JDAO.getAnimeTrendByGenre();

        System.out.println(suggestedAnime);
    }

    @Test
    public void testGetMangaTrendByGenre() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<MangaDTO> suggestedManga = neo4JDAO.getMangaTrendByGenre();

        System.out.println(suggestedManga);
    }



    @Test
    public void testGetAnimeTrendByLikes() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<AnimeDTO> suggestedAnime = neo4JDAO.getAnimeTrendByLikes();

        System.out.println(suggestedAnime);
    }

    @Test
    public void testGetMangaTrendByLikes() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<MangaDTO> suggestedManga = neo4JDAO.getMangaTrendByLikes();

        System.out.println(suggestedManga);
    }



    @Test
    public void testGetAnimeGenresTrend() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<List<String>> genre = neo4JDAO.getAnimeGenresTrend();
        System.out.println(genre);
    }

    @Test
    public void testGetMangaGenresTrend() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<List<String>> genre = neo4JDAO.getMangaGenresTrend();
        System.out.println(genre);
    }*/


}