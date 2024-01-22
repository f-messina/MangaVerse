package it.unipi.lsmsd.fnf.service.mapper;

import it.unipi.lsmsd.fnf.dto.PersonalListDTO;
import it.unipi.lsmsd.fnf.dto.RegisteredUserDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.ArrayList;
import java.util.List;

public class DtoToModelMapper {
    public static PersonalList convertFromDTO(PersonalListDTO dto) {
        if(dto == null) {
            throw new IllegalArgumentException("The DTO can't be null.");
        }

        PersonalList list = new PersonalList();
        list.setId(dto.getId());
        list.setName(dto.getName());

        RegisteredUserDTO userDTO = dto.getUser();
        if(userDTO != null) {
            User user = new User();
            user.setId(userDTO.getId());
            user.setLocation(userDTO.getLocation());
            user.setBirthday(userDTO.getBirthday());
            list.setUser(user);
        }


        List<AnimeDTO> animeDTOs = dto.getAnime();
        if(animeDTOs != null) {
            List<Anime> animes = new ArrayList<>();
            for(AnimeDTO animeDTO : animeDTOs) {
                Anime anime = new Anime();
                anime.setId(animeDTO.getId());
                anime.setTitle(animeDTO.getTitle());
                anime.setImageUrl(animeDTO.getImageUrl());
                animes.add(anime);
            }
            list.setAnime(animes);
        }

        List<MangaDTO> mangaDTOs = dto.getManga();
        if(mangaDTOs != null) {
            List<Manga> mangas = new ArrayList<>();
            for(MangaDTO mangaDTO : mangaDTOs) {
                Manga manga = new Manga();
                manga.setId(mangaDTO.getId());
                manga.setTitle(mangaDTO.getTitle());
                manga.setImageUrl(mangaDTO.getImageUrl());
                mangas.add(manga);
            }
            list.setManga(mangas);
        }

        return list;
    }
}
