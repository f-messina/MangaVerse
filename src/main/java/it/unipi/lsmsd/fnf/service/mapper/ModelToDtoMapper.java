package it.unipi.lsmsd.fnf.service.mapper;

import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
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

public class ModelToDtoMapper {
    public static AnimeDTO convertToDTO(Anime anime) {
        return new AnimeDTO(anime.getId(), anime.getTitle(), anime.getImageUrl(), anime.getAverageRating(), anime.getYear(), anime.getSeason());
    }

    public static MangaDTO convertToDTO(Manga manga) {
        return new MangaDTO(manga.getId(), manga.getTitle(), manga.getImageUrl(), manga.getAverageRating(), manga.getStartDate(), manga.getEndDate());
    }

    public static PersonalListDTO convertToDTO(PersonalList list) {
        if(list == null) {
            throw new IllegalArgumentException("The list can't be null.");
        }

        PersonalListDTO dto = new PersonalListDTO();
        dto.setId(list.getId());
        dto.setName(list.getName());

        User user = list.getUser();
        if(user != null) {
            dto.setUserId(user.getId());
            dto.setUserLocation(user.getLocation());
            dto.setUserBirthDate(user.getBirthday());
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
