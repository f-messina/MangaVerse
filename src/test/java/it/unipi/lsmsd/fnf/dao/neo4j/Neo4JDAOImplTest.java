package it.unipi.lsmsd.fnf.dao.neo4j;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.dao.mongo.BaseMongoDBDAO;
import it.unipi.lsmsd.fnf.dto.PageDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
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
            dao.like("6577877be68376234760585f","65789bb52f5d29465d0abd10");
        } catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }

    }

    @Test
    public void testLikeManga() throws DAOException {
        try {
            MangaDAONeo4JImpl dao = new MangaDAONeo4JImpl();
            dao.like("6577877be68376234760585f","657ac61bb34f5514b91ea22f");
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
            PageDTO<MediaContentDTO> anime = neo4JDAO.getLiked("6577877be68376234760585d", 1);
            for (MediaContentDTO animeDTO : anime.getEntries()) {
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
            PageDTO<MediaContentDTO> manga = neo4JDAO.getLiked("6577877be68376234760585f", 1);
            for (MediaContentDTO mangaDTO : manga.getEntries()) {
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
            List<UserSummaryDTO> followingUsers = neo4JDAO.getFirstNFollowing("6577877be68376234760585d", null);
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
            List<UserSummaryDTO> followerUsers = neo4JDAO.getFirstNFollowers("6577877be68376234760585d", null);
            for(UserSummaryDTO user : followerUsers)
                System.out.println(user);
        }  catch (DAOException e) {
            fail("Exception not expected: " + e.getMessage());
        }
    }


    @Test
    //Test works correctly
    public void testSuggestUsersByCommonFollows() throws DAOException {
        UserDAONeo4JImpl  neo4JDAO = new UserDAONeo4JImpl ();
        List<UserSummaryDTO> followerUsers = neo4JDAO.suggestUsersByCommonFollowings("6577877be68376234760585d", 10);
        for(UserSummaryDTO user : followerUsers)
            System.out.println(user);

    }

    @Test
    //Test works but doesn't return any user
    public void testSuggestUsersByCommonLikes() throws DAOException {
        UserDAONeo4JImpl neo4JDAO = new UserDAONeo4JImpl();
        List<UserSummaryDTO> followerUsers = neo4JDAO.suggestUsersByCommonLikes("6577877be68376234760585d", 10, MediaContentType.MANGA);
        if (followerUsers != null && !followerUsers.isEmpty()) {
            for (UserSummaryDTO user : followerUsers) {
                System.out.println(user);
            }
        } else {
            System.out.println("No suggested users found.");
        }
    }

    @Test
    //Test works correctly
    public void getSuggestedAnimeByLikes() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        List<MediaContentDTO> anime = neo4JDAO.getSuggestedByLikes("6577877be68376234760585d", 100);
        System.out.println(anime.size());
        if (anime == null || anime.isEmpty()) {
            fail("No suggested anime found");
        }
        for (MediaContentDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    //Test works correctly
    public void getSuggestedMangaByLikes() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<MediaContentDTO> manga = neo4JDAO.getSuggestedByLikes("6577877be68376234760585d", 100);
        if (manga == null || manga.isEmpty()) {
            fail("No suggested manga found");
        }
        for (MediaContentDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }

    @Test
    //Test works correctly
    public void getSuggestedAnimeByFollowings() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        List<MediaContentDTO> anime = neo4JDAO.getSuggestedByFollowings("6577877be68376234760585d", 100);
        if (anime == null) {
            fail("Error occurred while retrieving suggested anime");
        } else if (anime.isEmpty()) {
            System.out.println("No suggested anime found");
        } else {
            System.out.println("Number of suggested anime: " + anime.size());
            for (MediaContentDTO animeDTO : anime) {
                System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
            }
        }
    }

    @Test
    //Test works correctly
    public void getSuggestedMangaByFollowings() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<MediaContentDTO> manga = neo4JDAO.getSuggestedByFollowings("6577877be68376234760585d", 100);
        if (manga == null || manga.isEmpty()) {
            fail("No suggested manga found");
        }
        for (MediaContentDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }

    @Test
    //Test works correctly
    public void getTrendAnimeByYear() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        Map<MediaContentDTO, Integer> anime = neo4JDAO.getTrendMediaContentByYear(2020, 300);
        System.out.println(anime.size());
        if (anime == null || anime.isEmpty()) {
            fail("No trend anime found");
        }
        for (Map.Entry<MediaContentDTO, Integer> entry : anime.entrySet()) {
            System.out.println("id: " + entry.getKey().getId() + ", title: " + entry.getKey().getTitle() + ", picture: " + entry.getKey().getImageUrl() + ", likes: " + entry.getValue());
        }
    }

    @Test
    //Test works correctly
    public void getTrendMangaByYear() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        Map<MediaContentDTO, Integer> manga = neo4JDAO.getTrendMediaContentByYear(2020, 25);
        if (manga == null || manga.isEmpty()) {
            fail("No trend manga found");
        }
        for (Map.Entry<MediaContentDTO, Integer> entry : manga.entrySet()) {
            System.out.println("id: " + entry.getKey().getId() + ", title: " + entry.getKey().getTitle() + ", picture: " + entry.getKey().getImageUrl() + ", likes: " + entry.getValue());
        }
    }

    @Test
    //Test works correctly
    public void getTrendAnimeByLikes() throws DAOException {
        AnimeDAONeo4JImpl neo4JDAO = new AnimeDAONeo4JImpl();
        List<MediaContentDTO> anime = neo4JDAO.getMediaContentTrendByLikes(300);
        System.out.println(anime.size());
        if (anime == null || anime.isEmpty()) {
            fail("No trend anime found");
        }
        for (MediaContentDTO animeDTO : anime) {
            System.out.println("id: " + animeDTO.getId() + ", title: " + animeDTO.getTitle() + ", picture: " + animeDTO.getImageUrl());
        }
    }

    @Test
    //Test works correctly
    public void getTrendMangaByLikes() throws DAOException {
        MangaDAONeo4JImpl neo4JDAO = new MangaDAONeo4JImpl();
        List<MediaContentDTO> manga = neo4JDAO.getMediaContentTrendByLikes(6);
        if (manga == null || manga.isEmpty()) {
            fail("No trend manga found");
        }
        for (MediaContentDTO mangaDTO : manga) {
            System.out.println("id: " + mangaDTO.getId() + ", title: " + mangaDTO.getTitle() + ", picture: " + mangaDTO.getImageUrl());
        }
    }
}
