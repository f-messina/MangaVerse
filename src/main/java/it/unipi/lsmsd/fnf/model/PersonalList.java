package it.unipi.lsmsd.fnf.model;

import it.unipi.lsmsd.fnf.model.mediaContent.Anime;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class PersonalList {
    private ObjectId id;
    private String name;
    private User user;
    private List<Manga> manga = new ArrayList<>();
    private List<Anime> anime = new ArrayList<>();

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }

    public List<Manga> getManga() {
        return manga;
    }

    public List<Anime> getAnime() {
        return anime;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name= name;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setManga(List<Manga> manga) {
        this.manga = manga;
    }

    public void setAnime(List<Anime> anime) {
        this.anime = anime;
    }

    public void addManga(Manga manga) {
        this.manga.add(manga);
    }

    public void addAnime(Anime anime) {
        this.anime.add(anime);
    }

    public void removeManga(ObjectId mangaId) {
        this.manga.removeIf(manga -> manga.getId().equals(mangaId));
    }

    public void removeAnime(ObjectId animeId) {
        this.anime.removeIf(anime -> anime.getId().equals(animeId));
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
