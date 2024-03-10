package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PersonalListDTO {
    private String id;
    private String name;
    private String userId;
    private List<MangaDTO> manga = new ArrayList<>();
    private List<AnimeDTO> anime = new ArrayList<>();

    public PersonalListDTO() {
    }

    public PersonalListDTO(String id, String name, String userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    public PersonalListDTO(String id, String name, String userId, List<MangaDTO> manga, List<AnimeDTO> anime) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.manga = manga;
        this.anime = anime;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public List<MangaDTO> getManga() {
        return manga;
    }

    public List<AnimeDTO> getAnime() {
        return anime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name= name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setManga(List<MangaDTO> manga) {
        this.manga = manga;
    }

    public void setAnime(List<AnimeDTO> anime) {
        this.anime = anime;
    }

    public void addManga(MangaDTO manga) {
        this.manga.add(manga);
    }

    public void addAnime(AnimeDTO anime) {
        this.anime.add(anime);
    }

    public void removeManga(MangaDTO manga) {
        this.manga.remove(manga);
    }

    public void removeAnime(AnimeDTO anime) {
        this.anime.remove(anime);
    }

    @Override
    public String toString() {
        return "PersonalListDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", manga=" + manga +
                ", anime=" + anime +
                '}';
    }
}
