package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.model.Review;

import java.util.ArrayList;
import java.util.List;

public class Anime extends MediaContent {
    private Integer year;
    private String season;
    private Integer episodeCount;
    private List<String> tags = new ArrayList<>();
    private List<String> relatedAnime = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private String producers;
    private String studios;

    public Integer getYear() {
        return year;
    }

    public String getSeason() {
        return season;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getRelatedAnime() {
        return relatedAnime;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public String getProducers() {
        return producers;
    }

    public String getStudios() {
        return studios;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setRelatedAnime(List<String> relatedAnime) {
        this.relatedAnime = relatedAnime;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setProducers(String producers) {
        this.producers = producers;
    }

    public void setStudios(String studios) {
        this.studios = studios;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    @Override
    public String toString() {
        return "Anime{" +
                super.toString() +
                ", year=" + year +
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
