package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MediaContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class Neo4JDAOImplTest{

    @BeforeEach
    public void setUp() throws Exception {
        BaseMongoDBDAO.openConnection();
        BaseNeo4JDAO.openConnection();
    }

    @AfterEach
    public void tearDown() throws DAOException {
        BaseMongoDBDAO.closeConnection();
        BaseNeo4JDAO.closeConnection();
    }

    @Test
    public void testLikeAnime() throws DAOException {
        try {
            AnimeDAONeo4JImpl dao = new AnimeDAONeo4JImpl();
            dao.like("6577877be68376234760585f","65789bb52f5d29465d0abd09");
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testLikeManga() throws DAOException {
        try {
            MangaDAONeo4JImpl dao = new MangaDAONeo4JImpl();
            dao.like("6577877be68376234760585f","657ac61bb34f5514b91ea235");
        }   catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testFollowUser() throws DAOException {
        try {
            UserDAONeo4JImpl neo4JDAO = new UserDAONeo4JImpl();
            neo4JDAO.follow("6577877be68376234760585a", "6577877be683762347605859");
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testUnlikeAnime() throws DAOException {
        try {
            AnimeDAONeo4JImpl dao = new AnimeDAONeo4JImpl();
            dao.unlike("6577877be68376234760585f","65789bb52f5d29465d0abd09");
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testUnlikeManga() throws DAOException {
        try {

            MangaDAONeo4JImpl dao = new MangaDAONeo4JImpl();
            dao.unlike("6577877be68376234760585f","657ac61bb34f5514b91ea233");

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testUnfollowUser() throws DAOException {
        try {

            UserDAONeo4JImpl neo4JDAO = new UserDAONeo4JImpl();
            neo4JDAO.unfollow("6577877be68376234760585a", "6577877be683762347605859");

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testGetLikedAnime() throws DAOException {
        try {

            AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
            List<AnimeDTO> anime = neo4JDAO.getLiked("6577877be68376234760585f");
            for (AnimeDTO animeDTO : anime) {
                System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
            }


        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testGetLikedManga() throws DAOException{
        try {

            MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
            List<MangaDTO> manga = neo4JDAO.getLiked("6577877be68376234760585f");
            for (MangaDTO mangaDTO : manga) {
                System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
            }

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }


    @Test
    public void testGetFollowing() throws DAOException {
        try {

            UserDAONeo4JImpl neo4JDAO = new UserDAONeo4JImpl();
            List<UserSummaryDTO> followingUsers = neo4JDAO.getFollowedUsers("6577877be68376234760585d", null);
            for (UserSummaryDTO user : followingUsers)
                System.out.println(user);
        }   catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

     @Test
    public void testGetFollowers() throws DAOException {
        try {
            UserDAONeo4JImpl neo4JDAO = new UserDAONeo4JImpl();
            List<UserSummaryDTO> followerUsers = neo4JDAO.getFollowers("6577877be68376234760585d", null);
            for(UserSummaryDTO user : followerUsers)
                System.out.println(user);
        }  catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }


    @Test
    public void testSuggestUsersByCommonFollows() throws DAOException {
        UserDAONeo4JImpl  neo4JDAO = new UserDAONeo4JImpl ();
        List<UserSummaryDTO> followerUsers = neo4JDAO.suggestUsersByCommonFollows("6577877be683762347605859", 10);
        for(UserSummaryDTO user : followerUsers)
            System.out.println(user);

    }

    @Test
    public void testSuggestUsersByCommonLikes() throws DAOException {
        UserDAONeo4JImpl neo4JDAO = new UserDAONeo4JImpl();
        List<UserSummaryDTO> followerUsers = neo4JDAO.suggestUsersByCommonLikes("6577877be683762347605859", 10, MediaContentType.ANIME);
        for(UserSummaryDTO user : followerUsers)
            System.out.println(user);
    }


    @Test
    public void testGetTrendAnimeByYear() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        Map<AnimeDTO, Integer> anime = neo4JDAO.getTrendMediaContentByYear(2019);
        for (Map.Entry<AnimeDTO, Integer> entry : anime.entrySet()) {
            System.out.println("id: " + entry.getKey().getId() + ", title: " + entry.getKey().getTitle() + ", picture: " + entry.getKey().getImageUrl() + ", likes: " + entry.getValue());
        }
    }

    @Test
    public void testGetTrendMangaByYear() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        Map<MangaDTO, Integer> manga = neo4JDAO.getTrendMediaContentByYear(2019);
        for (Map.Entry<MangaDTO, Integer> entry : manga.entrySet()) {
            System.out.println("id: " + entry.getKey().getId() + ", title: " + entry.getKey().getTitle() + ", picture: " + entry.getKey().getImageUrl() + ", likes: " + entry.getValue());
        }
    }

    @Test
    public void testGetAnimeTrendByLikes() throws DAOException{
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();

        List<AnimeDTO> anime = neo4JDAO.getMediaContentTrendByLikes();

        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testGetMangaTrendByLikes() throws DAOException{
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();

        List<MangaDTO> manga = neo4JDAO.getMediaContentTrendByLikes();

        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }

}