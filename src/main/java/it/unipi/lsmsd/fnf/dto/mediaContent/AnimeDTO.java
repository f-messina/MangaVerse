package it.unipi.lsmsd.fnf.dto.mediaContent;

import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;

import java.util.List;

public class AnimeDTO extends MediaContentDTO{
    public AnimeDTO(String id, String title, String imageUrl, String type, String averageRating, String synopsis, String status) {
        super(id, title, imageUrl, type, averageRating, synopsis, status);
    }
    private int year;
    private String season;
    private String episodeCount;
    private List<String> tags;
    private List<String> relatedAnime;
    private List<Review<Anime>> reviews;
    private String producers;
    private String studios;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(String episodeCount) {
        this.episodeCount = episodeCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getRelatedAnime() {
        return relatedAnime;
    }

    public void setRelatedAnime(List<String> relatedAnime) {
        this.relatedAnime = relatedAnime;
    }

    public List<Review<Anime>> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review<Anime>> reviews) {
        this.reviews = reviews;
    }

    public String getProducers() {
        return producers;
    }

    public void setProducers(String producers) {
        this.producers = producers;
    }

    public String getStudios() {
        return studios;
    }

    public void setStudios(String studios) {
        this.studios = studios;
    }

    @Override
    public String toString() {
        return "AnimeDTO{" +
                "year=" + year +
                ", season='" + season + '\'' +
                ", episodeCount='" + episodeCount + '\'' +
                ", tags=" + tags +
                ", relatedAnime=" + relatedAnime +
                ", reviews=" + reviews +
                ", producers='" + producers + '\'' +
                ", studios='" + studios + '\'' +
                '}';
    }
}
