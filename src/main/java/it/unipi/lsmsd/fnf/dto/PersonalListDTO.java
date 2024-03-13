package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PersonalListDTO {
    private String id;
    private String name;
    private List<String> mangaIds = new ArrayList<>();
    private List<String> animeIds = new ArrayList<>();

    public PersonalListDTO() {
    }

    public PersonalListDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PersonalListDTO(String id, String name, List<String> mangaIds, List<String> animeIds) {
        this.id = id;
        this.name = name;
        this.mangaIds = mangaIds;
        this.animeIds = animeIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getMangaIds() {
        return mangaIds;
    }

    public List<String> getAnimeIds() {
        return animeIds;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name= name;
    }

    public void setMangaIds(List<String> mangaIds) {
        this.mangaIds = mangaIds;
    }

    public void setAnimeIds(List<String> animeIds) {
        this.animeIds = animeIds;
    }

    public void addMangaId(String mangaId) {
        this.mangaIds.add(mangaId);
    }

    public void addAnimeId(String animeId) {
        this.animeIds.add(animeId);
    }

    public void removeMangaId(String mangaId) {
        this.mangaIds.remove(mangaId);
    }

    public void removeAnimeId(String animeId) {
        this.animeIds.remove(animeId);
    }

    @Override
    public String toString() {
        return "PersonalListDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mangaIds=" + mangaIds +
                ", animeIds=" + animeIds +
                '}';
    }
}
