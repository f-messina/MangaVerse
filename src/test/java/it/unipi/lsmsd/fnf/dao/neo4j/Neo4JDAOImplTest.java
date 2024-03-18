package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import  org.junit.Test;
import junit.framework.TestCase;
import java.util.List;

import static it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO.closeConnection;
import static it.unipi.lsmsd.fnf.dao.base.BaseNeo4JDAO.openConnection;

public class Neo4JDAOImplTest extends TestCase {

    public void testLikeAnime() throws DAOException {
        try {
            openConnection();
            AnimeDAOImpl dao = new AnimeDAOImpl();
            dao.like("6577877be68376234760585f","65789bb52f5d29465d0abd09");
            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    public void testLikeManga() throws DAOException {
        try {
            openConnection();
            MangaDAOImpl dao = new MangaDAOImpl();
            dao.like("6577877be68376234760585f","657ac61bb34f5514b91ea235");
            closeConnection();
        }   catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    public void testFollowUser() throws DAOException {
        try {
            openConnection();
            UserDAOImpl neo4JDAO = new UserDAOImpl();
            neo4JDAO.follow("6577877be68376234760585a", "6577877be683762347605859");
            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    public void testUnlikeAnime() throws DAOException {
        try {
            openConnection();
            AnimeDAOImpl dao = new AnimeDAOImpl();
            dao.unlike("6577877be68376234760585f","65789bb52f5d29465d0abd09");
            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    public void testUnlikeManga() throws DAOException {
        try {
            openConnection();
            MangaDAOImpl dao = new MangaDAOImpl();
            dao.unlike("6577877be68376234760585f","657ac61bb34f5514b91ea233");
            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    public void testUnfollowUser() throws DAOException {
        try {
            openConnection();
            UserDAOImpl neo4JDAO = new UserDAOImpl();
            neo4JDAO.unfollow("6577877be68376234760585a", "6577877be683762347605859");
            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    public void testGetLikedAnime() throws DAOException {
        try {
            openConnection();
            AnimeDAOImpl neo4JDAO = new AnimeDAOImpl();
            List<AnimeDTO> anime = neo4JDAO.getLiked("6577877be68376234760585f");
            for (AnimeDTO animeDTO : anime) {
                System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
            }
            closeConnection();

        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    public void testGetLikedManga() throws DAOException{
        try {
            openConnection();
            MangaDAOImpl neo4JDAO = new MangaDAOImpl();
            List<MangaDTO> manga = neo4JDAO.getLiked("6577877be68376234760585f");
            for (MangaDTO mangaDTO : manga) {
                System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
            }
            closeConnection();
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }


    public void testGetFollowing() throws DAOException {
<<<<<<< HEAD
        UserDAOImpl neo4JDAO = new UserDAOImpl();
        List<UserSummaryDTO> followingUsers = neo4JDAO.getFollowing("6577877be68376234760585d");
        for (UserSummaryDTO user : followingUsers)
            System.out.println(user);
=======
        try {
            openConnection();
            UserDAOImpl neo4JDAO = new UserDAOImpl();
            List<RegisteredUserDTO> followingUsers = neo4JDAO.getFollowing("6577877be68376234760585d");
            for (RegisteredUserDTO user : followingUsers)
                System.out.println(user);
            closeConnection();
        }   catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

>>>>>>> noemi

    }


    public void testGetFollowers() throws DAOException {
<<<<<<< HEAD
        UserDAOImpl neo4JDAO = new UserDAOImpl();
        List<UserSummaryDTO> followerUsers = neo4JDAO.getFollowers("6577877be68376234760585d");
        for(UserSummaryDTO user : followerUsers)
            System.out.println(user);
=======
        try {
            openConnection();
            UserDAOImpl neo4JDAO = new UserDAOImpl();
            List<RegisteredUserDTO> followerUsers = neo4JDAO.getFollowers("6577877be68376234760585d");
            for(RegisteredUserDTO user : followerUsers)
                System.out.println(user);
            closeConnection();
        }  catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

>>>>>>> noemi
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