package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class PersonalListDTO {
    private ObjectId id;
    private String name;
    private RegisteredUserDTO user;
    private List<MangaDTO> manga = new ArrayList<>();
    private List<AnimeDTO> anime = new ArrayList<>();

    public PersonalListDTO() {
    }

    public PersonalListDTO(ObjectId id, String name, RegisteredUserDTO user, List<MangaDTO> manga, List<AnimeDTO> anime) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.manga = manga;
        this.anime = anime;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RegisteredUserDTO getUser() {
        return user;
    }

    public List<MangaDTO> getManga() {
        return manga;
    }

    public List<AnimeDTO> getAnime() {
        return anime;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name= name;
    }

    public void setUser(RegisteredUserDTO user) {
        this.user = user;
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
        return "PersonalList{" +
                "id=" + id +
                "name='" + name + '\'' +
                ", user='" + user + '\'' +
                ", manga=" + manga +
                ", anime=" + anime +
                '}';
    }
}
