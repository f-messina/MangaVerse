package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
            List<UserSummaryDTO> followingUsers = neo4JDAO.getFollowing("6577877be68376234760585d");
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
            List<UserSummaryDTO> followerUsers = neo4JDAO.getFollowers("6577877be68376234760585d");
            for(UserSummaryDTO user : followerUsers)
                System.out.println(user);
        }  catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }

    /*
    @Test
    public void testSuggestUsers() throws DAOException {
        UserDAONeo4JImpl  neo4JDAO = new UserDAONeo4JImpl ();
        List<RegisteredUserDTO> followerUsers = neo4JDAO.suggestUsers("6577877be68376234760585d");
        for(RegisteredUserDTO user : followerUsers)
            System.out.println(user);

    }


    @Test
    public void testSuggestAnime() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        String userId = "6577877be6837623476063e4";

        List<AnimeDTO> anime = neo4JDAO.suggestMediaContent(userId);

        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testSuggestManga() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<MangaDTO> manga = neo4JDAO.suggestMediaContent("6577877be6837623476063e4");
        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }



    @Test
    public void testGetTrendAnimeByYear() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        List<AnimeDTO> anime = neo4JDAO.getTrendMediaContentByYear(2019);
        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testGetTrendMangaByYear() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<MangaDTO> manga = neo4JDAO.getTrendMediaContentByYear(2019);
        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }



    //check
    @Test
    public void testGetAnimeGenresTrendByYear() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        List<String> genre = neo4JDAO.getMediaContentGenresTrendByYear(2019);
        System.out.println(genre);
    }

    //check
    @Test
    public void testGetMangaGenresTrendByYear() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<String> genre = neo4JDAO.getMediaContentGenresTrendByYear(2019);
        System.out.println(genre);
    }



    @Test
    public void testGetAnimeTrendByGenre() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();

        List<AnimeDTO> anime = neo4JDAO.getMediaContentTrendByGenre();

        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testGetMangaTrendByGenre() throws DAOException{
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();

        List<MangaDTO> manga = neo4JDAO.getMediaContentTrendByGenre();

        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
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


    @Test
    public void testGetAnimeGenresTrend() throws DAOException{
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        List<String> genre = neo4JDAO.getMediaContentGenresTrend();
        System.out.println(genre);
    }

    //check
    @Test
    public void testGetMangaGenresTrend() throws DAOException{
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<String> genre = neo4JDAO.getMediaContentGenresTrend();
        System.out.println(genre);
    }
*/
}