package it.unipi.lsmsd.fnf.utils.mapper;

import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.UserSummaryDTO;
import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
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
import java.util.stream.Collectors;

/**
 * The DtoToModelMapper class provides static methods to map DTOs (Data Transfer Objects) to corresponding model objects.
 * It is used to convert DTOs received from the data layer into model objects used in the service layer.
 */
public class DtoToModelMapper {

    /**
     * Converts a UserRegistrationDTO object to a User object.
     * @param userRegistrationDTO The UserRegistrationDTO object to convert.
     * @return The converted User object.
     */
    public static User userRegistrationDTOToUser(UserRegistrationDTO userRegistrationDTO) {
        User user = new User();
        user.setUsername(userRegistrationDTO.getUsername());
        user.setPassword(userRegistrationDTO.getPassword());
        user.setEmail(userRegistrationDTO.getEmail());
        user.setFullname(userRegistrationDTO.getFullname());
        user.setGender(userRegistrationDTO.getGender());
        user.setLocation(userRegistrationDTO.getLocation());
        user.setBirthday(userRegistrationDTO.getBirthday());
        return user;
    }

    /**
     * Converts a RegisteredUserDTO object to a User object.
     * @param userSummaryDTO The userSummaryDTO object to convert.
     * @return The converted User object.
     */
    public static User registeredUserDTOtoUser(UserSummaryDTO userSummaryDTO) {
        User user = new User();
        user.setId(userSummaryDTO.getId());
        user.setUsername(userSummaryDTO.getUsername());
        user.setProfilePicUrl(userSummaryDTO.getProfilePicUrl());
        return user;
    }

    /**
     * Converts a PersonalListDTO object to a PersonalList object.
     * @param personalListDTO The PersonalListDTO object to convert.
     * @return The converted PersonalList object.
     */
    public static PersonalList personalListDTOtoPersonalList(PersonalListDTO personalListDTO) {
        // TODO: Implement this method
        PersonalList personalList = new PersonalList();
        return personalList;
    }

    /**
     * Converts an AnimeDTO object to an Anime object.
     * @param animeDTO The AnimeDTO object to convert.
     * @return The converted Anime object.
     */
    public static Anime animeDTOtoAnime(AnimeDTO animeDTO) {
        Anime anime = new Anime();
        anime.setId(animeDTO.getId());
        anime.setTitle(animeDTO.getTitle());
        anime.setImageUrl(animeDTO.getImageUrl());
        return anime;
    }

    /**
     * Converts a MangaDTO object to a Manga object.
     * @param mangaDTO The MangaDTO object to convert.
     * @return The converted Manga object.
     */
    public static Manga mangaDTOtoManga(MangaDTO mangaDTO) {
        Manga manga = new Manga();
        manga.setId(mangaDTO.getId());
        manga.setTitle(mangaDTO.getTitle());
        manga.setImageUrl(mangaDTO.getImageUrl());
        return manga;
    }

    /**
     * Converts a MediaContentDTO object to a MediaContent object.
     * @param dto The MediaContentDTO object to convert.
     * @return The converted MediaContent object.
     * @throws IllegalArgumentException If the MediaContentDTO type is unknown.
     */
    public static MediaContent mediaContentDTOtoMediaContent(MediaContentDTO dto) {
        if (dto instanceof AnimeDTO) {
            return animeDTOtoAnime((AnimeDTO) dto);
        } else if (dto instanceof MangaDTO) {
            return mangaDTOtoManga((MangaDTO) dto);
        } else {
            throw new IllegalArgumentException("Unknown MediaContentDTO type");
        }
    }

    /**
     * Converts a ReviewDTO object to a Review object.
     * @param reviewDTO The ReviewDTO object to convert.
     * @return The converted Review object.
     */
    public static Review reviewDTOtoReview(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setId(reviewDTO.getId());
        review.setDate(reviewDTO.getDate());
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setMediaContent(mediaContentDTOtoMediaContent(reviewDTO.getMediaContent()));
        if (reviewDTO.getUser() != null)
            review.setUser(registeredUserDTOtoUser(reviewDTO.getUser()));
        return review;
    }

    /**
     * Converts an AnimeDTO object to an Anime object.
     * @param animeDTO The AnimeDTO object to convert.
     * @return The converted Anime object.
     * @throws IllegalArgumentException If the animeDTO object is null.
     */
    public static Anime convertToAnime(AnimeDTO animeDTO) {
        if(animeDTO == null) {
            throw new IllegalArgumentException("The AnimeDTO can't be null.");
        }

        Anime anime = new Anime();
        anime.setId(animeDTO.getId());
        anime.setTitle(animeDTO.getTitle());
        anime.setImageUrl(animeDTO.getImageUrl());

        return anime;
    }

    /**
     * Converts a MangaDTO object to a Manga object.
     * @param mangaDTO The MangaDTO object to convert.
     * @return The converted Manga object.
     * @throws IllegalArgumentException If the mangaDTO object is null.
     */
    public static Manga convertToManga(MangaDTO mangaDTO) {
        if(mangaDTO == null) {
            throw new IllegalArgumentException("The MangaDTO can't be null.");
        }

        Manga manga = new Manga();
        manga.setId(mangaDTO.getId());
        manga.setTitle(mangaDTO.getTitle());
        manga.setImageUrl(mangaDTO.getImageUrl());

        return manga;
    }

    /**
     * Converts a UserSummaryDTO object to a User object.
     * @param userDTO The UserSummaryDTO object to convert.
     * @return The converted User object.
     * @throws IllegalArgumentException If the userDTO object is null.
     */
    public static User convertToUser(UserSummaryDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("The RegisteredUserDTO can't be null.");
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setProfilePicUrl(userDTO.getProfilePicUrl());

        return user;
    }
}
