package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static java.time.ZoneId.systemDefault;

public class ConverterUtils {

    // Convert Date to LocalDate
    public static LocalDate convertDateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        return instant.atZone(systemDefault()).toLocalDate();
    }

    // Convert LocalDate to Date
    public static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(systemDefault()).toInstant());
    }

    /*
    public static ReviewDTO convertModelToDTO(Review<Anime> review) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setDate(review.getDate());
        reviewDTO.setComment(review.getComment());
        reviewDTO.setRating(review.getRating());
        reviewDTO.setMediaContent(convertModelToDTO(review.getMediaContent()));
        reviewDTO.setUser(review.getUser());
        return reviewDTO;
    }

    public static ReviewDTO convertModelToDTO(Review<Manga> review) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setDate(review.getDate());
        reviewDTO.setComment(review.getComment());
        reviewDTO.setRating(review.getRating());
        reviewDTO.setMediaContent(convertModelToDTO(review.getMediaContent()));
        reviewDTO.setUser(review.getUser());
        return reviewDTO;
    }

    public static Review<Anime> convertDTOToModel(ReviewDTO reviewDTO) {
        Review<Anime> review = new Review<>();
        review.setId(reviewDTO.getId());
        review.setDate(reviewDTO.getDate());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        if (reviewDTO.getMediaContent() instanceof AnimeDTO) {
            review.setMediaContent(convertDTOToModel((AnimeDTO) reviewDTO.getMediaContent()));
        }
        review.setUser(reviewDTO.getUser());
        return review;
    }
    public static Review<Manga> convertDTOToModel(ReviewDTO reviewDTO) {
        Review<Manga> review = new Review<>();
        review.setId(reviewDTO.getId());
        review.setDate(reviewDTO.getDate());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        if (reviewDTO.getMediaContent() instanceof MangaDTO) {
            review.setMediaContent(convertDTOToModel((MangaDTO) reviewDTO.getMediaContent()));
        }
        review.setUser(reviewDTO.getUser());
        return review;
    }
    public static AnimeDTO convertModelToDTO(Anime anime) {
        AnimeDTO animeDTO = new AnimeDTO();
        animeDTO.setId(anime.getId());
        animeDTO.setTitle(anime.getTitle());
        animeDTO.setImageUrl(anime.getImageUrl());
        if (anime.getYear() != 0) {
            animeDTO.setYear(anime.getYear());
        }
        animeDTO.setAverageRating(anime.getAverageRating());
        return animeDTO;
    }

    public static Anime convertDTOToModel(AnimeDTO animeDTO) {
        Anime anime = new Anime();
        anime.setId(animeDTO.getId());
        anime.setTitle(animeDTO.getTitle());
        anime.setImageUrl(animeDTO.getImageUrl());
        if (animeDTO.getYear() != 0) {
            anime.setYear(animeDTO.getYear());
        }
        anime.setAverageRating(animeDTO.getAverageRating());
        return anime;
    }

    public static MangaDTO convertModelToDTO(Manga manga) {
        MangaDTO mangaDTO = new MangaDTO();
        mangaDTO.setId(manga.getId());
        mangaDTO.setTitle(manga.getTitle());
        mangaDTO.setImageUrl(manga.getImageUrl());
        mangaDTO.setAverageRating(manga.getAverageRating());
        if (manga.getStartDate() != null) {
            mangaDTO.setStartDate(manga.getStartDate());
        }
        if (manga.getEndDate() != null) {
            mangaDTO.setEndDate(manga.getEndDate());
        }
        return mangaDTO;
    }

    public static Manga convertDTOToModel(MangaDTO mangaDTO) {
        Manga manga = new Manga();
        manga.setId(mangaDTO.getId());
        manga.setTitle(mangaDTO.getTitle());
        manga.setImageUrl(mangaDTO.getImageUrl());
        manga.setAverageRating(mangaDTO.getAverageRating());
        if (mangaDTO.getStartDate() != null) {
            manga.setStartDate(mangaDTO.getStartDate());
        }
        if (mangaDTO.getEndDate() != null) {
            manga.setEndDate(mangaDTO.getEndDate());
        }
        return manga;
    }

     */
}
