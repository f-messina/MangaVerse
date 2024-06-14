package it.unipi.lsmsd.fnf.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

/**
 * Utility class for converting data types.
 * Provides methods to convert date types, information from HTTP requests to Model/DTO objects
 * or to filter/sort structures used in the search functionality.
 */
public class ConverterUtils {

    /**
     * Converts a Date object to a LocalDate object.
     *
     * @param date The Date object to convert.
     * @return The LocalDate object converted from the Date object.
     *         If the Date object is null, the method returns null.
     */
    public static LocalDate dateToLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converts a LocalDate object to a Date object.
     *
     * @param localDate The LocalDate object to convert.
     * @return The Date object converted from the LocalDate object.
     *         If the LocalDate object is null, the method returns null.
     */
    public static Date localDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts a Date object to a LocalDateTime object.
     *
     * @param date The Date object to convert.
     * @return The LocalDateTime object converted from the Date object.
     *         If the Date object is null, the method returns null.
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Converts a LocalDateTime object to a Date object.
     *
     * @param localDateTime The LocalDateTime object to convert.
     * @return The Date object converted from the LocalDateTime object.
     *         If the LocalDateTime object is null, the method returns null.
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Returns the profile picture URL of a user or the default profile picture URL if the user has no profile picture.
     *
     * @param picture The URL of the user's profile picture.
     * @param request The HttpServletRequest containing the context path.
     * @return The URL of the user's profile picture if the user has a profile picture,
     */
    public static String getProfilePictureUrlOrDefault(String picture, HttpServletRequest request) {
        if (StringUtils.isEmpty(picture)) {
            return request.getContextPath() + "/" + Constants.DEFAULT_PROFILE_PICTURE;
        }
        return picture;
    }

    /**
     * Converts HTTP request parameters to a UserRegistrationDTO object.
     *
     * @param request The HttpServletRequest containing the user registration parameters.
     * @return The UserRegistrationDTO object populated with request parameters.
     */
    public static UserRegistrationDTO fromRequestToUserRegDTO(HttpServletRequest request){
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername(request.getParameter("username"));
        userRegistrationDTO.setPassword(request.getParameter("password"));
        userRegistrationDTO.setEmail(request.getParameter("email"));
        if (StringUtils.isNotBlank(request.getParameter("fullname")))
            userRegistrationDTO.setFullname(request.getParameter("fullname"));
        if (StringUtils.isNotBlank(request.getParameter("country")))
            userRegistrationDTO.setLocation(request.getParameter("country"));
        if (StringUtils.isNotBlank(request.getParameter("birthdate")))
            userRegistrationDTO.setBirthday(LocalDate.parse(request.getParameter("birthdate")));
        userRegistrationDTO.setGender(Gender.valueOf(request.getParameter("gender")));

        return userRegistrationDTO;
    }

    /**
     * Converts HTTP request parameters to a ReviewDTO object.
     *
     * @param request The HttpServletRequest containing the review parameters.
     * @return The ReviewDTO object populated with request parameters.
     */
    public static User fromRequestToUser(HttpServletRequest request){
        User user = new User();
        LoggedUserDTO loggedUser = SecurityUtils.getAuthenticatedUser(request);
        user.setId(loggedUser.getId());

        String username = request.getParameter("username");
        String fullname = request.getParameter("fullname");
        String description = request.getParameter("description");
        String country = request.getParameter("country");
        String birthday = request.getParameter("birthdate");
        String gender = request.getParameter("gender");
        String profilePicUrl = request.getParameter("picture");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> reviewsIds = Arrays.stream(objectMapper.readValue(request.getParameter("reviewsIds"), String[].class)).toList();
            user.setReviewIds(reviewsIds);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (username != null)
            user.setUsername(username);
        if (fullname != null && !fullname.isEmpty())
            user.setFullname(fullname);
        else if (fullname != null)
            user.setFullname(Constants.NULL_STRING);
        if (description != null && !description.isEmpty())
            user.setDescription(description);
        else if (description != null)
            user.setDescription(Constants.NULL_STRING);
        if (country != null && !country.isEmpty())
            user.setLocation(country);
        else if (country != null)
            user.setLocation(Constants.NULL_STRING);
        if (birthday != null && !birthday.isEmpty())
            user.setBirthday(LocalDate.parse(birthday));
        else if (birthday != null)
            user.setBirthday(Constants.NULL_DATE);
        if (gender != null)
            user.setGender(Gender.valueOf(gender));
        if (profilePicUrl != null && !profilePicUrl.isEmpty())
            user.setProfilePicUrl(profilePicUrl);
        else if (profilePicUrl != null)
            user.setProfilePicUrl(Constants.NULL_STRING);

        return user;
    }

    /**
     * Converts HTTP request parameters to a List of Filters for the search functionality of manga.
     * The filter list is built using a pair for each filter condition.
     * For logical operator $and, the key is the operator and the value is a list of pairs with the field and value to compare.
     * For the equality operation, the pair has the field as key and the value to compare as value.
     * For $regex, $gte, $lte, the key is the operator and the value is a pair with the field as key and the value to compare as value.
     * For $all, $in, $nin, the key is the operator and the value is a pair with the field as key and a list of values to compare as value.
     *
     * @param request The HttpServletRequest containing the filters parameters for manga.
     * @return The ReviewDTO object populated with request parameters.
     */
    public static List<Pair<String, Object>> fromRequestToMangaFilters(HttpServletRequest request) {
        try {
            return Stream.of(
                    buildTitleFilter(request),
                    buildGenreFilter(request),
                    buildDemographicsFilter(request),
                    buildYearFilter(request),
                    buildStatusFilter(request),
                    buildFormatFilter(request),
                    buildYearRangeFilter(request),
                    buildRatingRangeFilter(request)
            ).filter(filter -> filter.getLeft() != null).toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JSON data for manga filters");
        }
    }

    /**
     * Converts HTTP request parameters to a List of Filters for the search functionality of anime.
     * The filter list is built using a pair for each filter condition.
     * For logical operator $and, the key is the operator and the value is a list of pairs with the field and value to compare.
     * For the equality operation, the pair has the field as key and the value to compare as value.
     * For $regex, $gte, $lte, the key is the operator and the value is a pair with the field as key and the value to compare as value.
     * For $all, $in, $nin, the key is the operator and the value is a pair with the field as key and a list of values to compare as value.
     *
     * @param request The HttpServletRequest containing the filters parameters for anime.
     * @return The ReviewDTO object populated with request parameters.
     */
    public static List<Pair<String, Object>> fromRequestToAnimeFilters(HttpServletRequest request) {
        try {
            return Stream.of(
                    buildTitleFilter(request),
                    buildTagsFilter(request),
                    buildYearFilter(request),
                    buildSeasonFilter(request),
                    buildStatusFilter(request),
                    buildFormatFilter(request),
                    buildYearRangeFilter(request),
                    buildRatingRangeFilter(request)
            ).filter(filter -> filter.getLeft() != null).toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JSON data for manga filters");
        }
    }

    private static Pair<String, Object> buildTitleFilter(HttpServletRequest request) {
        String title = request.getParameter("title");
        return !StringUtils.isEmpty(title) ? Pair.of("$regex", Pair.of("title", title)) : Pair.of(null, null);
    }

    private static Pair<String, Object> buildGenreFilter(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        Pair<String, Object> genreFilter = Pair.of(null, null);
        String genreSelectMode = request.getParameter("genreSelectMode");
        try {
            String[] selectedGenres = mapper.readValue(request.getParameter("genreSelected"), String[].class);
            String[] avoidedGenres = mapper.readValue(request.getParameter("genreAvoided"), String[].class);
            String operator = genreSelectMode.equals("and") ? "$all" : "$in";
            if ((selectedGenres != null && selectedGenres.length > 0 && avoidedGenres != null && avoidedGenres.length > 0)) {
                genreFilter = Pair.of("$and", Arrays.asList(
                        Pair.of(operator, Pair.of("genres", Arrays.asList(selectedGenres))),
                        Pair.of("$nin", Pair.of("genres", Arrays.asList(avoidedGenres)))
                ));
            } else if (selectedGenres != null && selectedGenres.length > 0) {
                genreFilter = Pair.of(operator, Pair.of("genres", Arrays.asList(selectedGenres)));
            } else if (avoidedGenres != null && avoidedGenres.length > 0) {
                genreFilter = Pair.of("$nin", Pair.of("genres", Arrays.asList(avoidedGenres)));
            }

            return genreFilter;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON data for genre filter");
        }
    }

    private static Pair<String, Object> buildTagsFilter(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        Pair<String, Object> genreFilter = Pair.of(null, null);
        String genreSelectMode = request.getParameter("genreSelectMode");
        try {
            String[] selectedGenres = mapper.readValue(request.getParameter("genreSelected"), String[].class);
            String[] avoidedGenres = mapper.readValue(request.getParameter("genreAvoided"), String[].class);
            String operator = genreSelectMode.equals("and") ? "$all" : "$in";
            if ((selectedGenres != null && selectedGenres.length > 0 && avoidedGenres != null && avoidedGenres.length > 0)) {
                genreFilter = Pair.of("$and", Arrays.asList(
                        Pair.of(operator, Pair.of("tags", Arrays.asList(selectedGenres))),
                        Pair.of("$nin", Pair.of("tags", Arrays.asList(avoidedGenres)))
                ));
            } else if (selectedGenres != null && selectedGenres.length > 0) {
                genreFilter = Pair.of(operator, Pair.of("tags", Arrays.asList(selectedGenres)));
            } else if (avoidedGenres != null && avoidedGenres.length > 0) {
                genreFilter = Pair.of("$nin", Pair.of("tags", Arrays.asList(avoidedGenres)));
            }

            return genreFilter;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON data for genre filter");
        }
    }

    private static Pair<String, Object> buildYearFilter(HttpServletRequest request) {
        String year = request.getParameter("year");
        if (StringUtils.isEmpty(year)) {
            return Pair.of(null, null);
        }
        if (request.getAttribute("mediaType").equals("manga")) {
            return Pair.of("$and", Arrays.asList(
                    Pair.of("$gte", Pair.of("start_date", LocalDate.of(Integer.parseInt(year), 1, 1))),
                    Pair.of("$lte", Pair.of("end_date", LocalDate.of(Integer.parseInt(year), 12, 31)))
            ));
        } else {
            return Pair.of("anime_season.year", Integer.parseInt(year));
        }
    }

    private static Pair<String, Object> buildSeasonFilter(HttpServletRequest request) {
        String season = request.getParameter("season");
        return !StringUtils.isEmpty(season) ? Pair.of("anime_season.season", season) : Pair.of(null, null);
    }

    private static Pair<String, Object> buildStatusFilter(HttpServletRequest request) {
        String status = request.getParameter("status");
        return !StringUtils.isEmpty(status) ? Pair.of("status", status) : Pair.of(null, null);
    }

    private static Pair<String, Object> buildDemographicsFilter(HttpServletRequest request) {
        String demographics = request.getParameter("demographics");
        return !StringUtils.isEmpty(demographics) ? Pair.of("demographics", demographics) : Pair.of(null, null);
    }

    private static Pair<String, Object> buildFormatFilter(HttpServletRequest request) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String[] formats = mapper.readValue(request.getParameter("format"), String[].class);
            return formats != null && formats.length > 0 ? Pair.of("$in", Pair.of("type", Arrays.asList(formats))) : Pair.of(null, null);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON data for format filter");
        }
    }

    private static Pair<String, Object> buildYearRangeFilter(HttpServletRequest request) {
        String minYear = request.getParameter("year-rangeMin");
        String maxYear = request.getParameter("year-rangeMax");
        if (StringUtils.isEmpty(minYear) || StringUtils.isEmpty(maxYear)) {
            return Pair.of(null, null);
        }
        if (request.getAttribute("mediaType").equals("anime")) {
            return Pair.of("$and", Arrays.asList(
                    Pair.of("$gte", Pair.of("anime_season.year", Integer.parseInt(minYear))),
                    Pair.of("$lte", Pair.of("anime_season.year", Integer.parseInt(maxYear)))
            ));
        } else {
            return Pair.of("$and", Arrays.asList(
                    Pair.of("$gte", Pair.of("start_date", LocalDate.of(Integer.parseInt(minYear), 1, 1))),
                    Pair.of("$lte", Pair.of("start_date", LocalDate.of(Integer.parseInt(maxYear), 12, 31)))
            ));
        }
    }

    private static Pair<String, Object> buildRatingRangeFilter(HttpServletRequest request) {
        String minRating = request.getParameter("rating-rangeMin");
        String maxRating = request.getParameter("rating-rangeMax");
        if (StringUtils.isEmpty(minRating) || StringUtils.isEmpty(maxRating)) {
            return Pair.of(null, null);
        }
        return Pair.of("$and", Arrays.asList(
                Pair.of("$gte", Pair.of("average_rating", Double.parseDouble(minRating))),
                Pair.of("$lte", Pair.of("average_rating", Double.parseDouble(maxRating)))
        ));
    }
}
