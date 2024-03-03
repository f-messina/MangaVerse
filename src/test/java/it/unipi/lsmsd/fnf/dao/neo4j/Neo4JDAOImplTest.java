package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import org.bson.types.ObjectId;
import  org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class Neo4JDAOImplTest {

    @Test
    public void testLikeAnime() throws DAOException {
        AnimeDAONeo4JImpl dao = new AnimeDAONeo4JImpl();
        dao.likeMediaContent("6577877be68376234760585f","65789bb52f5d29465d0abd09");
    }

    @Test
    public void testLikeManga() throws DAOException {
        MangaDAONeo4JImpl dao = new MangaDAONeo4JImpl();
        dao.likeMediaContent("6577877be68376234760585f","657ac61bb34f5514b91ea235");
    }

    @Test
    public void testFollowUser() throws DAOException {
        UserDAONeo4JImpl neo4JDAO = new UserDAONeo4JImpl ();
        neo4JDAO.followUser("6577877be68376234760585a", "6577877be683762347605859");
    }

    @Test
    public void testUnlikeAnime() throws DAOException {
        AnimeDAONeo4JImpl dao = new AnimeDAONeo4JImpl();
        dao.unlikeMediaContent("6577877be68376234760585f","65789bb52f5d29465d0abd09");
    }

    @Test
    public void testUnlikeManga() throws DAOException {
        MangaDAONeo4JImpl dao = new MangaDAONeo4JImpl();
        dao.unlikeMediaContent("6577877be68376234760585f","657ac61bb34f5514b91ea233");
    }

    @Test
    public void testUnfollowUser() throws DAOException {
        UserDAONeo4JImpl  neo4JDAO = new UserDAONeo4JImpl ();
        neo4JDAO.unfollowUser("6577877be68376234760585a", "6577877be683762347605859");
    }


    @Test
    public void testGetLikedAnime() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        List<AnimeDTO> anime = neo4JDAO.getLikedMediaContent("6577877be68376234760585f");

        for (AnimeDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }


    @Test
    public void testGetLikedManga() throws DAOException{
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<MangaDTO> manga = neo4JDAO.getLikedMediaContent("6577877be68376234760585f");
        for (MangaDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }

    @Test
    public void testGetFollowing() throws DAOException {
        UserDAONeo4JImpl  neo4JDAO = new UserDAONeo4JImpl ();
        List<RegisteredUserDTO> followingUsers = neo4JDAO.getFollowing("6577877be68376234760585d");
        for (RegisteredUserDTO user : followingUsers)
            System.out.println(user);

    }

    @Test
    public void testGetFollowers() throws DAOException {
        UserDAONeo4JImpl  neo4JDAO = new UserDAONeo4JImpl ();
        List<RegisteredUserDTO> followerUsers = neo4JDAO.getFollowers("6577877be68376234760585d");
        for(RegisteredUserDTO user : followerUsers)
            System.out.println(user);
    }

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


}