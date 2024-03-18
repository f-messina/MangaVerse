package it.unipi.lsmsd.fnf.utils.mapper;

import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.PersonalListSummaryDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.ArrayList;
import java.util.List;

/**
 * The ModelToDtoMapper class provides static methods to map model objects to corresponding DTOs (Data Transfer Objects).
 * It is used to convert model objects used in the service layer into DTOs to be transferred to the data layer.
 */
public class ModelToDtoMapper {
    /**
     * Converts an Anime object to an AnimeDTO object.
     * @param anime The Anime object to convert.
     * @return The converted AnimeDTO object.
     */
    public static AnimeDTO convertToDTO(Anime anime) {
        return new AnimeDTO(anime.getId(), anime.getTitle(), anime.getImageUrl(), anime.getAverageRating(), anime.getYear(), anime.getSeason());
    }

    /**
     * Converts a Manga object to a MangaDTO object.
     * @param manga The Manga object to convert.
     * @return The converted MangaDTO object.
     */
    public static MangaDTO convertToDTO(Manga manga) {
        return new MangaDTO(manga.getId(), manga.getTitle(), manga.getImageUrl(), manga.getAverageRating(), manga.getStartDate(), manga.getEndDate());
    }

    /**
     * Converts a PersonalList object to a PersonalListDTO object.
     * @param list The PersonalList object to convert.
     * @return The converted PersonalListDTO object.
     * @throws IllegalArgumentException If the list object is null.
     */
    public static PersonalListSummaryDTO convertToDTO(PersonalList list) {
        if(list == null) {
            throw new IllegalArgumentException("The list can't be null.");
        }

        PersonalListSummaryDTO dto = new PersonalListSummaryDTO();

        // TODO: Implement the conversion of the PersonalList object to a PersonalListDTO object.
        return dto;
    }

    /**
     * Converts a MediaContent object to a MediaContentDTO object.
     * @param content The MediaContent object to convert.
     * @return The converted MediaContentDTO object.
     * @throws IllegalArgumentException If the content object is null.
     */
    public static MediaContentDTO convertToDTO(MediaContent content) {
        if(content == null) {
            throw new IllegalArgumentException(("The media content can't be null."));
        }
        MediaContentDTO dto = null;
        if(content instanceof Anime anime) {
            AnimeDTO animeDTO = new AnimeDTO();
            animeDTO.setYear(anime.getYear());
            animeDTO.setId(anime.getId());
            animeDTO.setTitle(anime.getTitle());
            animeDTO.setImageUrl(anime.getImageUrl());
            animeDTO.setAverageRating(anime.getAverageRating());
            dto = animeDTO;

        } else if (content instanceof Manga manga) {
            MangaDTO mangaDTO = new MangaDTO();
            mangaDTO.setStartDate(manga.getStartDate());
            mangaDTO.setId(manga.getId());
            mangaDTO.setTitle(manga.getTitle());
            mangaDTO.setImageUrl(manga.getImageUrl());
            mangaDTO.setAverageRating(manga.getAverageRating());
            mangaDTO.setEndDate(manga.getEndDate());
            dto = mangaDTO;
        }

        if(dto != null) {
            dto.setId(content.getId());
            dto.setTitle(content.getTitle());
            dto.setAverageRating(content.getAverageRating());
        }


        return dto;
    }

    /**
     * Converts a Review object to a ReviewDTO object.
     * @param review The Review object to convert.
     * @return The converted ReviewDTO object.
     * @throws IllegalArgumentException If the review object is null.
     */
    public static ReviewDTO convertToDTO(Review review) {
        if(review == null) {
            throw new IllegalArgumentException("The review can't be null.");
        }

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setDate(review.getDate());
        dto.setMediaContent(convertToDTO(review.getMediaContent()));
        dto.setUser(convertToDTO(review.getUser()));

        return dto;
    }

    /**
     * Converts a User object to a UserSummaryDTO object.
     * @param user The User object to convert.
     * @return The converted UserSummaryDTO object.
     * @throws IllegalArgumentException If the user object is null.
     */
    public static UserSummaryDTO convertToDTO(User user) {
        if(user == null) {
            throw new IllegalArgumentException("The user can't be null.");
        }

        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setProfilePicUrl(user.getProfilePicUrl());
        
        return dto;
    }
}
