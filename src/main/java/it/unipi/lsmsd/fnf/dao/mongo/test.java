package it.unipi.lsmsd.fnf.dao.mongo;

import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) throws DAOException {
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
        /*
        Map<String, Object> filterMap = Map.of(
                "$or", List.of(
                        Map.of("anime_season.year", 2022),
                        Map.of("anime_season.season", "SUMMER")
                ),
                "episodes", 1,
                "$not", Map.of("status", "ONGOING")
        );

        Map<String, Integer> orderBy = new LinkedHashMap<>();  // Example: Sort ascending by fieldName
        orderBy.put("anime_season.year", 1);
        orderBy.put("anime_season.season", 1);  // Example: Sort ascending by fieldName
         */
        List<Anime> result = animeDAO.search("One Piece");
        for (Anime anime : result) {
            System.out.println(anime.getImageUrl());

        }
    }
}
