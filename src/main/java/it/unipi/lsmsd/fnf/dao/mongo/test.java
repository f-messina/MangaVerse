package it.unipi.lsmsd.fnf.dao.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import it.unipi.lsmsd.fnf.dao.exception.DAOException;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;

import java.time.LocalDate;
import java.util.LinkedHashMap;

import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) throws DAOException {
        /*
        AnimeDAOImpl animeDAO = new AnimeDAOImpl();
        Map<String, Object> filterMap = Map.of(
                "title", new Document("$regex", "NVADE"),
                "type", "SPECIAL",
                "status", "FINISHED",
                "average_score", new Document("$gte", 6.0),
                "relations", new Document("$in", List.of("BanG Dream!", "BanG Dream! 2nd Season")),
                "tags", new Document("$all", List.of("band", "music")),
                "latest_reviews", new Document("$elemMatch", Map.of("user.username", "cirnoh")),
                "anime_season", new Document("season", "FALL").append("year", 2020)
        );

        Map<String, Integer> orderBy = new LinkedHashMap<>();  // Example: Sort ascending by fieldName
        orderBy.put("anime_season.year", 1);
        orderBy.put("anime_season.season", 1);  // Example: Sort ascending by fieldName
        List<Anime> result = animeDAO.search(filterMap, orderBy);
        for (Anime anime : result) {
            System.out.println(anime);

        }
        Bson updatedKeys = Updates.combine(Updates.set("date", ConverterUtils.convertLocalDateToDate(LocalDate.now())));

        String ciao = "ciao";
        // Conditionally add update operations for non-null fields in the review object
        if (ciao != null) {
            updatedKeys = Updates.combine(updatedKeys, Updates.set("comment", ciao));
        }

        String lol = null;
        if (lol != null) {
            updatedKeys = Updates.combine(updatedKeys, Updates.set("rating", lol));
        }

        Bson upd2 = Updates.combine(
                Updates.set("date", ConverterUtils.convertLocalDateToDate(LocalDate.now())),
                Updates.set("comment", ciao),
                Updates.set("rating", lol)
        );

        System.out.println(updatedKeys);
        System.out.println(upd2);
         */
        Gender gender = Gender.fromString(null);
        User user = new User();
        user.setGender(gender);
        System.out.println(user.getGender().toString());
    }
}
