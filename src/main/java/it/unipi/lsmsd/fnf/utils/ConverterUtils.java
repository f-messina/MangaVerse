package it.unipi.lsmsd.fnf.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class ConverterUtils {
    private final static Logger logger = getLogger(ConverterUtils.class.getName());

    // Convert Date to LocalDate
    public static LocalDate dateToLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Convert LocalDate to Date
    public static Date localDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Convert Date to LocalDateTime
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // Convert LocalDateTime to Date
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

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

    public static List<Map<String, Object>> fromRequestToMangaFilters(HttpServletRequest request) {
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
            ).filter(filter -> !filter.isEmpty()).toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JSON data for manga filters");
        }
    }

    public static List<Map<String, Object>> fromRequestToAnimeFilters(HttpServletRequest request) {
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
            ).filter(filter -> !filter.isEmpty()).toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JSON data for manga filters");
        }
    }

    private static Map<String, Object> buildTitleFilter(HttpServletRequest request) {
        String title = request.getParameter("title");
        Map<String, Object> titleFilter = new HashMap<>();
        titleFilter = !StringUtils.isEmpty(title) ? Map.of("$regex", Map.of("title", title)) : Collections.emptyMap();
        return titleFilter;
    }

    private static Map<String, Object> buildGenreFilter(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> genreFilter = new HashMap<>();
        String genreSelectMode = request.getParameter("genreSelectMode");
        try {
            String[] selectedGenres = mapper.readValue(request.getParameter("genreSelected"), String[].class);
            String[] avoidedGenres = mapper.readValue(request.getParameter("genreAvoided"), String[].class);
            String operator = genreSelectMode.equals("and") ? "$all" : "$in";
            if ((selectedGenres != null && selectedGenres.length > 0 && avoidedGenres != null && avoidedGenres.length > 0)) {
                genreFilter = Map.of("$and", Arrays.asList(
                        Map.of(operator, Map.of("genres", Arrays.asList(selectedGenres))),
                        Map.of("$nin", Map.of("genres", Arrays.asList(avoidedGenres)))
                ));
            } else if (selectedGenres != null && selectedGenres.length > 0) {
                genreFilter = Map.of(operator, Map.of("genres", Arrays.asList(selectedGenres)));
            } else if (avoidedGenres != null && avoidedGenres.length > 0) {
                genreFilter = Map.of("$nin", Map.of("genres", Arrays.asList(avoidedGenres)));
            }
            return genreFilter;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON data for genre filter");
        }
    }

    private static Map<String, Object> buildTagsFilter(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> genreFilter = new HashMap<>();
        String genreSelectMode = request.getParameter("genreSelectMode");
        try {
            String[] selectedGenres = mapper.readValue(request.getParameter("genreSelected"), String[].class);
            String[] avoidedGenres = mapper.readValue(request.getParameter("genreAvoided"), String[].class);
            String operator = genreSelectMode.equals("and") ? "$all" : "$in";
            if ((selectedGenres != null && selectedGenres.length > 0 && avoidedGenres != null && avoidedGenres.length > 0)) {
                genreFilter = Map.of("$and", Arrays.asList(
                        Map.of(operator, Map.of("tags", Arrays.asList(selectedGenres))),
                        Map.of("$nin", Map.of("tags", Arrays.asList(avoidedGenres)))
                ));
            } else if (selectedGenres != null && selectedGenres.length > 0) {
                genreFilter = Map.of(operator, Map.of("tags", Arrays.asList(selectedGenres)));
            } else if (avoidedGenres != null && avoidedGenres.length > 0) {
                genreFilter = Map.of("$nin", Map.of("tags", Arrays.asList(avoidedGenres)));
            }
            return genreFilter;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON data for genre filter");
        }
    }

    private static Map<String, Object> buildYearFilter(HttpServletRequest request) {
        String year = request.getParameter("year");
        if (request.getAttribute("mediaType").equals("manga") && !StringUtils.isEmpty(year)) {
            return Map.of("$and", Arrays.asList(
                    Map.of("$gte", Map.of("start_date", LocalDate.of(Integer.parseInt(year), 1, 1))),
                    Map.of("$lte", Map.of("end_date", LocalDate.of(Integer.parseInt(year), 12, 31)))
            ));
        } else if (request.getAttribute("mediaType").equals("anime") && !StringUtils.isEmpty(year)) {
            return Map.of("anime_season.year", Integer.parseInt(year));
        }
        return Collections.emptyMap();
    }

    private static Map<String, Object> buildSeasonFilter(HttpServletRequest request) {
        String season = request.getParameter("season");
        return !StringUtils.isEmpty(season) ? Map.of("anime_season.season", season) : Collections.emptyMap();
    }

    private static Map<String, Object> buildStatusFilter(HttpServletRequest request) {
        String status = request.getParameter("status");
        return !StringUtils.isEmpty(status) ? Map.of("status", status) : Collections.emptyMap();
    }

    private static Map<String, Object> buildDemographicsFilter(HttpServletRequest request) {
        String demographics = request.getParameter("demographics");
        return !StringUtils.isEmpty(demographics) ? Map.of("demographics", demographics) : Collections.emptyMap();
    }

    private static Map<String, Object> buildFormatFilter(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String[] formats = mapper.readValue(request.getParameter("format"), String[].class);
            return formats != null && formats.length > 0 ? Map.of("$in", Map.of("type", Arrays.asList(formats))) : Collections.emptyMap();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON data for format filter");
        }
    }

    private static Map<String, Object> buildYearRangeFilter(HttpServletRequest request) {
        String minYear = request.getParameter("year-rangeMin");
        String maxYear = request.getParameter("year-rangeMax");
        if (StringUtils.isEmpty(minYear) || StringUtils.isEmpty(maxYear)) {
            return Collections.emptyMap();
        }
        if (request.getAttribute("mediaType").equals("anime")) {
            return Map.of("$and", Arrays.asList(
                    Map.of("$gte", Map.of("anime_season.year", Integer.parseInt(minYear))),
                    Map.of("$lte", Map.of("anime_season.year", Integer.parseInt(maxYear)))
            ));
        } else {
            return Map.of("$and", Arrays.asList(
                    Map.of("$gte", Map.of("start_date", LocalDate.of(Integer.parseInt(minYear), 1, 1))),
                    Map.of("$lte", Map.of("start_date", LocalDate.of(Integer.parseInt(maxYear), 12, 31)))
            ));
        }
    }

    private static Map<String, Object> buildRatingRangeFilter(HttpServletRequest request) {
        String minRating = request.getParameter("rating-rangeMin");
        String maxRating = request.getParameter("rating-rangeMax");
        if (StringUtils.isEmpty(minRating) || StringUtils.isEmpty(maxRating)) {
            return Collections.emptyMap();
        }
        return Map.of("$and", Arrays.asList(
                Map.of("$gte", Map.of("average_rating", Double.parseDouble(minRating))),
                Map.of("$lte", Map.of("average_rating", Double.parseDouble(maxRating)))
        ));
    }
}
