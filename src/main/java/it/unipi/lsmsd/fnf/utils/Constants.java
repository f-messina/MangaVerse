package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;

import java.time.LocalDate;
import java.util.List;

public class Constants {

    public static final String DEFAULT_PROFILE_PICTURE = "images/account-icon.png";
    public static final String DEFAULT_COVER_MANGA = "images/manga-image-default.png";
    public static final String DEFAULT_COVER_ANIME = "images/anime-image-default.png";
    public static final String AUTHENTICATED_USER_KEY = "authenticatedUser";
    public static final int PAGE_SIZE = 30;
    public static final String PAGINATION_FACET = "paginationResults";
    public static final String COUNT_FACET = "totalResults";
    public static final int LATEST_REVIEWS_SIZE = 5;
    public static final String[] MANGA_GENRES = {"Supernatural", "Adventure", "Boys Love", "Comedy", "Girls Love",
            "Mystery", "Horror", "Drama", "Gourmet", "Award Winning", "Fantasy", "Romance", "Avant Garde", "Action",
            "Slice of Life", "Sports", "Sci-Fi", "Suspense"};
    public static final String[] ANIME_TAGS = {
            "comedy", "fantasy", "action", "kids", "adventure", "drama", "present", "music", "slice of life",
            "based on a manga", "family friendly", "sci-fi", "shounen", "romance", "shorts",
            "short episodes", "chinese animation", "science fiction", "school", "non-human protagonists",
            "manga", "male protagonist", "magic", "sci fi", "science-fiction", "original work",
            "female protagonist", "supernatural", "historical", "seinen", "school life", "japan", "earth",
            "anthropomorphic", "animal protagonists", "mecha", "super power", "slapstick", "parody", "cg animation"
        };

    // null values to unset fields in the database
    public static final String NULL_STRING = "null";
    public static final LocalDate NULL_DATE = LocalDate.of(1, 1, 1);
    public static final String NULL_GENDER = "Prefer not to say";
    public static final Integer NULL_INT = -1;
    public static final Double NULL_DOUBLE = -1.0;
    public static final List<String> NULL_LIST = List.of("null");
    public static final List<MangaAuthor> NULL_LIST_AUTHOR = List.of(new MangaAuthor(-1, "null", "null"));
}
