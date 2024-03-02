package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.AnimeType;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.enums.MangaDemographics;
import it.unipi.lsmsd.fnf.model.enums.MangaType;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

public class ConverterUtils {

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

    public static List<Map<String, Object>> fromRequestToMangaFilters(HttpServletRequest request) {
        return Stream.of(
                buildGenreFilter(request, "select", request.getParameter("genreOperator").equals("and")? "$all": "$in"),
                buildGenreFilter(request, "avoid", "$nin"),
                buildEnumFilter(request.getParameterValues("mangaTypes"), MangaType.values(), "type"),
                buildEnumFilter(request.getParameterValues("mangaDemographics"), MangaDemographics.values(), "demographics"),
                buildEnumFilter(request.getParameterValues("status"), Constants.MANGA_STATUS, "status"),
                buildScoreFilter(request),
                buildDateFilter(request)
        )
                .filter(filter -> !filter.isEmpty())
                .toList();
    }

    public static List<Map<String, Object>> fromRequestToAnimeFilters(HttpServletRequest request) {
        return Stream.of(
                        buildGenreFilter(request, "select", request.getParameter("genreOperator").equals("and")? "$all": "$in"),
                        buildGenreFilter(request, "avoid", "$nin"),
                        buildEnumFilter(request.getParameterValues("animeTypes"), AnimeType.values(), "type"),
                        buildEnumFilter(request.getParameterValues("status"), Constants.ANIME_STATUS, "status"),
                        buildScoreFilter(request),
                        buildYearFilter(request),
                        buildSeasonFilter(request)
                )
                .filter(filter -> !filter.isEmpty())
                .toList();
    }

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

    private static Map<String, Object> buildEnumFilter(String[] values, Enum<?>[] enumConstants, String key) {
        List<String> enumValues = Optional.ofNullable(values).stream().flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(value -> Arrays.stream(enumConstants).anyMatch(enumValue -> value.equals(enumValue.name())))
                .toList();

        return enumValues.isEmpty() ? Map.of() : Map.of("$in", Map.of(key, enumValues));
    }

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

    private static Map<String, Object> buildYearFilter(HttpServletRequest request) {
        String min = request.getParameter("minYear");
        String max = request.getParameter("maxYear");

        if (StringUtils.isBlank(min) || StringUtils.isBlank(max)) return Map.of();

        List<Map<String,Object>> rangeList = new ArrayList<>();
        rangeList.add(Map.of("$gte", Map.of("anime_season.year", Integer.parseInt(min))));
        rangeList.add(Map.of("$lte", Map.of("anime_season.year", Integer.parseInt(max))));
        return Map.of("$and", rangeList);
    }

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
