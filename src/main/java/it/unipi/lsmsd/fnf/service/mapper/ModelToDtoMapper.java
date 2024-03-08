package it.unipi.lsmsd.fnf.service.mapper;

import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.types.ObjectId;

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
    public static AnimeDTO animeToAnimeDTO(Anime anime) {
        return new AnimeDTO(anime.getId(), anime.getTitle(), anime.getImageUrl(), anime.getAverageRating(), anime.getYear(), anime.getSeason());
    }

    /**
     * Converts a Manga object to a MangaDTO object.
     * @param manga The Manga object to convert.
     * @return The converted MangaDTO object.
     */
    public static MangaDTO mangaToMangaDTO(Manga manga) {
        return new MangaDTO(manga.getId(), manga.getTitle(), manga.getImageUrl(), manga.getAverageRating(), manga.getStartDate(), manga.getEndDate());
    }

    /**
     * Converts a User object to a RegisteredUserDTO object.
     * @param user The User object to convert.
     * @return The converted RegisteredUserDTO object.
     * @throws IllegalArgumentException If the user object is null.
     */
    public static RegisteredUserDTO convertToRegisteredUserDTO(User user) {
        if(user == null) {
            throw new IllegalArgumentException("The user can't be null.");
        }

        RegisteredUserDTO userDTO = new RegisteredUserDTO();
        userDTO.setId(user.getId());
        userDTO.setLocation(user.getLocation());
        userDTO.setBirthday(user.getBirthday());

        return userDTO;
    }

    /**
     * Converts a PersonalList object to a PersonalListDTO object.
     * @param list The PersonalList object to convert.
     * @return The converted PersonalListDTO object.
     * @throws IllegalArgumentException If the list object is null.
     */
    public static PersonalListDTO convertToDTO(PersonalList list) {
        if(list == null) {
            throw new IllegalArgumentException("The list can't be null.");
        }

        PersonalListDTO dto = new PersonalListDTO();
        dto.setId(list.getId());
        dto.setName(list.getName());

        User user = list.getUser();
        if(user != null) {
            RegisteredUserDTO userDTO = new RegisteredUserDTO();
            userDTO.setId(user.getId());
            userDTO.setLocation(user.getLocation());
            userDTO.setBirthday(user.getBirthday());
            dto.setUser(userDTO);
        }

        List<Anime> animeList = list.getAnime();
        if(animeList != null) {
            List<AnimeDTO> animeDTOs = new ArrayList<>();
            for(Anime anime : animeList) {
                AnimeDTO animeDTO = new AnimeDTO();
                animeDTO.setId(anime.getId());
                animeDTO.setTitle(anime.getTitle());
                animeDTO.setImageUrl(anime.getImageUrl());
                animeDTOs.add(animeDTO);
            }
            dto.setAnime(animeDTOs);
        }

        List<Manga> mangaList = list.getManga();
        if(mangaList != null) {
            List<MangaDTO> mangaDTOs = new ArrayList<>();
            for(Manga manga : mangaList) {
                MangaDTO mangaDTO = new MangaDTO();
                mangaDTO.setId(manga.getId());
                mangaDTO.setTitle(manga.getTitle());
                mangaDTO.setImageUrl(manga.getImageUrl());
                mangaDTOs.add(mangaDTO);
            }
            dto.setManga(mangaDTOs);
        }

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
     * Converts a PersonalListDTO object to a PersonalList object.
     * @param personalListDTO The PersonalListDTO object to convert.
     * @return The converted PersonalList object.
     */
    public static PersonalList convertToPersonalList(PersonalListDTO personalListDTO) {
        PersonalList personalList = new PersonalList();
        personalList.setId(String.valueOf(personalListDTO.getId()));
        personalList.setName(personalListDTO.getName());
        personalList.setUser(convertToUser(personalListDTO.getUser()));
        List<Manga> manga = new ArrayList<>();
        List<Anime> anime = new ArrayList<>();
        for (AnimeDTO animeDTO : personalListDTO.getAnime()) {
            anime.add(convertToAnime(animeDTO));
        }
        for (MangaDTO mangaDTO : personalListDTO.getManga()) {
            manga.add(convertToManga(mangaDTO));
        }
        personalList.setManga(manga);
        personalList.setAnime(anime);

        return personalList;
    }

    /**
     * Converts a RegisteredUserDTO object to a User object.
     * @param userDTO The RegisteredUserDTO object to convert.
     * @return The converted User object.
     * @throws IllegalArgumentException If the userDTO object is null.
     */
    public static User convertToUser(RegisteredUserDTO userDTO) {
        if(userDTO == null) {
            throw new IllegalArgumentException("The RegisteredUserDTO can't be null.");
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setLocation(userDTO.getLocation());
        user.setBirthday(userDTO.getBirthday());

        return user;
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
}
