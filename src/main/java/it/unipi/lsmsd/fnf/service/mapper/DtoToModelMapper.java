package it.unipi.lsmsd.fnf.service.mapper;

import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
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

public class DtoToModelMapper {
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

    public static User registeredUserDTOtoUser(RegisteredUserDTO registeredUserDTO) {
        User user = new User();
        user.setId(registeredUserDTO.getId());
        user.setUsername(registeredUserDTO.getUsername());
        user.setProfilePicUrl(registeredUserDTO.getProfilePicUrl());
        user.setLocation(registeredUserDTO.getLocation());
        user.setBirthday(registeredUserDTO.getBirthday());
        return user;
    }

    public static PersonalList personalListDTOtoPersonalList(PersonalListDTO personalListDTO) {
        PersonalList personalList = new PersonalList();
        personalList.setId(personalListDTO.getId());
        personalList.setName(personalListDTO.getName());
        if (personalListDTO.getUser() != null)
            personalList.setUser(registeredUserDTOtoUser(personalListDTO.getUser()));
        personalList.setManga(personalListDTO.getManga().stream().map(DtoToModelMapper::mangaDTOtoManga).toList());
        personalList.setAnime(personalListDTO.getAnime().stream().map(DtoToModelMapper::animeDTOtoAnime).toList());
        return personalList;
    }

    public static Anime animeDTOtoAnime(AnimeDTO animeDTO) {
        Anime anime = new Anime();
        anime.setId(animeDTO.getId());
        anime.setTitle(animeDTO.getTitle());
        anime.setImageUrl(animeDTO.getImageUrl());
        return anime;
    }

    public static Manga mangaDTOtoManga(MangaDTO mangaDTO) {
        Manga manga = new Manga();
        manga.setId(mangaDTO.getId());
        manga.setTitle(mangaDTO.getTitle());
        manga.setImageUrl(mangaDTO.getImageUrl());
        return manga;
    }

    public static MediaContent mediaContentDTOtoMediaContent(MediaContentDTO dto) {
        if (dto instanceof AnimeDTO) {
            return animeDTOtoAnime((AnimeDTO) dto);
        } else if (dto instanceof MangaDTO) {
            return mangaDTOtoManga((MangaDTO) dto);
        } else {
            throw new IllegalArgumentException("Unknown MediaContentDTO type");
        }
    }

    public static Review reviewDTOtoReview(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setId(reviewDTO.getId());
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setMediaContent(mediaContentDTOtoMediaContent(reviewDTO.getMediaContent()));
        if (reviewDTO.getUser() != null)
            review.setUser(registeredUserDTOtoUser(reviewDTO.getUser()));
        return review;
    }
}
