package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.enums.*;

import it.unipi.lsmsd.fnf.model.registeredUser.User;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class ConverterUtils {

    // Convert Date to LocalDate
    /**
     * Converts a Date object to a LocalDate object.
     * @param date The Date object to be converted.
     * @return The LocalDate equivalent of the input Date.
     */
    public static LocalDate dateToLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Convert LocalDate to Date
    /**
     * Converts a LocalDate object to a Date object.
     * @param localDate The LocalDate object to be converted.
     * @return The Date equivalent of the input LocalDate.
     */
    public static Date localDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    //
    public static String getProfilePictureUrlOrDefault(String picture, HttpServletRequest request) {
        Logger logger = getLogger(ConverterUtils.class.getName());
        if (StringUtils.isEmpty(picture)) {
            return request.getContextPath() + "/" + Constants.DEFAULT_PROFILE_PICTURE;
        }
        return picture;
    }

    /**
     * Converts HTTP request parameters to a UserRegistrationDTO object.
     * @param request The HttpServletRequest containing request parameters.
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
        if (StringUtils.isNotBlank(request.getParameter("birthday")))
            userRegistrationDTO.setBirthday(LocalDate.parse(request.getParameter("birthday")));
        userRegistrationDTO.setGender(Gender.fromString(request.getParameter("gender")));

        return userRegistrationDTO;
    }

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
     * Converts HTTP request parameters to a ReviewDTO object.
     * @param request The HttpServletRequest containing request parameters.
     * @param mediaType The type of media content being reviewed.
     * @return The ReviewDTO object populated with request parameters.
     */
    public static ReviewDTO fromRequestToReviewDTO(HttpServletRequest request, MediaContentType mediaType){
        UserSummaryDTO userDTO = new UserSummaryDTO();
        userDTO.setId(SecurityUtils.getAuthenticatedUser(request).getId());
        userDTO.setUsername(SecurityUtils.getAuthenticatedUser(request).getUsername());
        userDTO.setProfilePicUrl(SecurityUtils.getAuthenticatedUser(request).getProfilePicUrl());
        MediaContentDTO mediaContentDTO = mediaType.equals(MediaContentType.MANGA) ? new MangaDTO(): new AnimeDTO();
        mediaContentDTO.setId(request.getParameter("mediaId"));
        mediaContentDTO.setTitle(request.getParameter("mediaTitle"));
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setComment(request.getParameter("comment"));
        if (StringUtils.isNotBlank(request.getParameter("rating"))) {
            reviewDTO.setRating(Integer.parseInt(request.getParameter("rating")));
        }
        reviewDTO.setMediaContent(mediaContentDTO);
        reviewDTO.setUser(userDTO);
        return reviewDTO;
    }

    /**
     * Converts HTTP request parameters to filters for manga search.
     * @param request The HttpServletRequest containing request parameters.
     * @return A list of filters as Map objects.
     */
    public static List<Map<String, Object>> fromRequestToMangaFilters(HttpServletRequest request) {
        return Stream.of(
                buildGenreFilter(request, "select", request.getParameter("genreOperator").equals("and")? "$all": "$in"),
                buildGenreFilter(request, "avoid", "$nin"),
                buildEnumFilter(request.getParameterValues("mangaTypes"), MangaType.values(), "type"),
                buildEnumFilter(request.getParameterValues("mangaDemographics"), MangaDemographics.values(), "demographics"),
                buildEnumFilter(request.getParameterValues("status"), MangaStatus.values(), "status"),
                buildScoreFilter(request),
                buildDateFilter(request)
        )
                .filter(filter -> !filter.isEmpty())
                .toList();
    }

    /**
     * Converts HTTP request parameters to filters for anime search.
     * @param request The HttpServletRequest containing request parameters.
     * @return A list of filters as Map objects.
     */
    public static List<Map<String, Object>> fromRequestToAnimeFilters(HttpServletRequest request) {
        return Stream.of(
                        buildGenreFilter(request, "select", request.getParameter("genreOperator").equals("and")? "$all": "$in"),
                        buildGenreFilter(request, "avoid", "$nin"),
                        buildEnumFilter(request.getParameterValues("animeTypes"), AnimeType.values(), "type"),
                        buildEnumFilter(request.getParameterValues("status"), AnimeStatus.values(), "status"),
                        buildScoreFilter(request),
                        buildYearFilter(request),
                        buildSeasonFilter(request)
                )
                .filter(filter -> !filter.isEmpty())
                .toList();
    }

    /**
     * Builds a genre filter based on HTTP request parameters.
     * @param request The HttpServletRequest containing request parameters.
     * @param condition The condition for genre selection.
     * @param operator The operator for combining genre filters.
     * @return A Map representing the genre filter.
     */
    private static Map<String, Object> buildGenreFilter(HttpServletRequest request, String condition, String operator) {
        if (request.getServletPath().equals("/mainPage/manga")) {
            List<String> genres = Arrays.stream(Constants.MANGA_GENRES)
                    .filter(genre -> request.getParameter(genre) != null && condition.equals(request.getParameter(genre)))
                    .toList();

            return genres.isEmpty() ? Map.of() : Map.of(operator, Map.of("genres", genres));
        } else {
            List<String> tags = Arrays.stream(Constants.ANIME_TAGS)
                    .filter(tag -> request.getParameter(tag) != null && condition.equals(request.getParameter(tag)))
                    .toList();
            return tags.isEmpty() ? Map.of() : Map.of(operator, Map.of("tags", tags));
        }
    }

    /**
     * Builds an enum filter based on HTTP request parameters.
     * @param values The values to filter on.
     * @param enumConstants The enum constants corresponding to the filter values.
     * @param key The key for the filter.
     * @return A Map representing the enum filter.
     */
    private static Map<String, Object> buildEnumFilter(String[] values, Enum<?>[] enumConstants, String key) {
        List<String> enumValues = Optional.ofNullable(values).stream().flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(value -> Arrays.stream(enumConstants).anyMatch(enumValue -> value.equals(enumValue.name())))
                .toList();

        return enumValues.isEmpty() ? Map.of() : Map.of("$in", Map.of(key, enumValues));
    }

    /**
     * Builds a score filter based on HTTP request parameters.
     * @param request The HttpServletRequest containing request parameters.
     * @return A Map representing the score filter.
     */
    private static Map<String, Object> buildScoreFilter(HttpServletRequest request) {
        String min = request.getParameter("minScore");
        String max = request.getParameter("maxScore");

        if (StringUtils.isBlank(min) || StringUtils.isBlank(max)) return Map.of();

        List<Map<String,Object>> rangeList = new ArrayList<>();
        List<Map<String,Object>> rangeListWithNull = new ArrayList<>();
        rangeList.add(Map.of("$gte", Map.of("average_rating", Double.parseDouble(min))));
        rangeList.add(Map.of("$lte", Map.of("average_rating", Double.parseDouble(max))));
        rangeListWithNull.add(Map.of("$and", rangeList));
        rangeListWithNull.add(Map.of("$exists", Map.of("average_rating", false)));

        return Map.of("$or", rangeListWithNull);
    }

    /**
     * Builds a date filter based on HTTP request parameters.
     * @param request The HttpServletRequest containing request parameters.
     * @return A Map representing the date filter.
     */
    private static Map<String, Object> buildDateFilter(HttpServletRequest request) {
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) return Map.of();
        else if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return startDate != null ? Map.of("start_date", Map.of("$gte", ConverterUtils.localDateToDate(LocalDate.parse(startDate)))) :
                    Map.of("end_date", Map.of("$lte", ConverterUtils.localDateToDate(LocalDate.parse(endDate))));
        } else {
            List<Map<String,Object>> dateRange = new ArrayList<>();
            dateRange.add(Map.of("$gte", Map.of("start_date", ConverterUtils.localDateToDate(LocalDate.parse(startDate)))));
            dateRange.add(Map.of("$lte", Map.of("end_date", ConverterUtils.localDateToDate(LocalDate.parse(endDate)))));
            return Map.of("$and", dateRange);
        }
    }

    /**
     * Builds a year filter based on HTTP request parameters.
     * @param request The HttpServletRequest containing request parameters.
     * @return A Map representing the year filter.
     */
    private static Map<String, Object> buildYearFilter(HttpServletRequest request) {
        String min = request.getParameter("minYear");
        String max = request.getParameter("maxYear");

        if (StringUtils.isBlank(min) || StringUtils.isBlank(max)) return Map.of();

        List<Map<String,Object>> rangeList = new ArrayList<>();
        rangeList.add(Map.of("$gte", Map.of("anime_season.year", Integer.parseInt(min))));
        rangeList.add(Map.of("$lte", Map.of("anime_season.year", Integer.parseInt(max))));
        return Map.of("$and", rangeList);
    }

    /**
     * Builds a season filter based on HTTP request parameters.
     * @param request The HttpServletRequest containing request parameters.
     * @return A Map representing the season filter.
     */
    private static Map<String, Object> buildSeasonFilter(HttpServletRequest request) {
        String season = request.getParameter("season");
        String year = request.getParameter("year");
        if (StringUtils.isBlank(year)) return Map.of();
        List<Map<String,Object>> animeSeason = new ArrayList<>();
        animeSeason.add(Map.of("anime_season.year", Integer.parseInt(year)));
        animeSeason.add(Map.of("anime_season.season", season));
        return Map.of("$and", animeSeason);
    }
}
