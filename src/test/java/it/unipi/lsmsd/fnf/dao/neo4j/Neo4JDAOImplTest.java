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

    @Test
    public void testLikeAnime() throws DAOException {
        Neo4JDAOImpl dao = new Neo4JDAOImpl();
        dao.likeAnime("6577877be68376234760585f","65789bb52f5d29465d0abd09");
    }

    @Test
    public void testLikeManga() throws DAOException {
        Neo4JDAOImpl dao = new Neo4JDAOImpl();
        dao.likeManga("6577877be68376234760585f","657ac61bb34f5514b91ea235");
    }

    @Test
    public void testFollowUser() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        neo4JDAO.followUser("6577877be68376234760585a", "6577877be683762347605859");
    }

    @Test
    public void testUnlikeAnime() throws DAOException {
        Neo4JDAOImpl dao = new Neo4JDAOImpl();
        dao.unlikeAnime("6577877be68376234760585f","65789bb52f5d29465d0abd09");
    }

    @Test
    public void testUnlikeManga() throws DAOException {
        Neo4JDAOImpl dao = new Neo4JDAOImpl();
        dao.unlikeManga("6577877be68376234760585f","657ac61bb34f5514b91ea233");
    }

    @Test
    public void testUnfollowUser() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        neo4JDAO.unfollowUser("6577877be68376234760585a", "6577877be683762347605859");
    }


    @Test
    public void testGetLikedAnime() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<AnimeDTO> anime = neo4JDAO.getLikedAnime("6577877be68376234760585f");
        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }


    @Test
    public void testGetLikedManga() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.getLikedManga("6577877be68376234760585f");
        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
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
        for(RegisteredUserDTO user : followerUsers)
            System.out.println(user);
    }

    /*
    @Test
    public void testSuggestUsers() throws DAOException {
        Neo4JDAO neo4JDAO = new Neo4JDAOImpl();
        List<RegisteredUserDTO> followerUsers = neo4JDAO.suggestUsers("6577877be68376234760585d");
        for(RegisteredUserDTO user : followerUsers)
            System.out.println(user);

    }


    @Test
    public void testSuggestAnime() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        String userId = "6577877be6837623476063e4";

        List<AnimeDTO> anime = neo4JDAO.suggestAnime(userId);

        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testSuggestManga() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.suggestManga("6577877be6837623476063e4");
        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }



    @Test
    public void testGetTrendAnimeByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<AnimeDTO> anime = neo4JDAO.getTrendAnimeByYear(2019);
        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testGetTrendMangaByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.getTrendMangaByYear(2019);
        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }



    @Test
    public void testGetAnimeByGenre() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<AnimeDTO> anime = neo4JDAO.getAnimeByGenre("comedy");
        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testGetMangaByGenre() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<MangaDTO> manga = neo4JDAO.getMangaByGenre("Fantasy");
        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }


    //check
    @Test
    public void testGetAnimeGenresTrendByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<String> genre = neo4JDAO.getAnimeGenresTrendByYear(2019);
        System.out.println(genre);
    }

    //check
    @Test
    public void testGetMangaGenresTrendByYear() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<String> genre = neo4JDAO.getMangaGenresTrendByYear(2019);
        System.out.println(genre);
    }



    @Test
    public void testGetAnimeTrendByGenre() throws DAOException {
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<AnimeDTO> anime = neo4JDAO.getAnimeTrendByGenre();

        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testGetMangaTrendByGenre() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<MangaDTO> manga = neo4JDAO.getMangaTrendByGenre();

        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }



    @Test
    public void testGetAnimeTrendByLikes() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<AnimeDTO> anime = neo4JDAO.getAnimeTrendByLikes();

        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    public void testGetMangaTrendByLikes() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();

        List<MangaDTO> manga = neo4JDAO.getMangaTrendByLikes();

        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }


    //check
    @Test
    public void testGetAnimeGenresTrend() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<String> genre = neo4JDAO.getAnimeGenresTrend();
        System.out.println(genre);
    }

    //check
    @Test
    public void testGetMangaGenresTrend() throws DAOException{
        Neo4JDAOImpl neo4JDAO = new Neo4JDAOImpl();
        List<String> genre = neo4JDAO.getMangaGenresTrend();
        System.out.println(genre);
    }
*/
}