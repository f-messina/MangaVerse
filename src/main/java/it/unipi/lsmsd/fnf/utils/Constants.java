package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.model.enums.Status;

import java.time.LocalDate;

public class Constants {
    public static final String AUTHENTICATED_USER_KEY = "authenticatedUser";
    public static final int PAGE_SIZE = 25;
    public static final String PAGINATION_FACET = "paginationResults";
    public static final String COUNT_FACET = "totalResults";
    public static final String NULL_STRING = "null";
    public static final LocalDate NULL_DATE = LocalDate.of(1, 1, 1);
    public static final String NULL_GENDER = "Prefer not to say";
    public static final String[] MANGA_GENRES = {"Supernatural", "Adventure", "Boys Love", "Comedy", "Girls Love",
            "Mystery", "Horror", "Drama", "Gourmet", "Award Winning", "Fantasy", "Romance", "Avant Garde", "Action",
            "Slice of Life", "Sports", "Sci-Fi", "Suspense", "Ecchi", "Erotica"};

    public static final Status[] MANGA_STATUS = {Status.DISCONTINUED, Status.ON_HIATUS, Status.FINISHED, Status.ONGOING};


}
