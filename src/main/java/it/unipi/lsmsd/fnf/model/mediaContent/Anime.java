package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.AnimeStatus;
import it.unipi.lsmsd.fnf.model.enums.AnimeType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import it.unipi.lsmsd.fnf.utils.ConverterUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents an this media content.
 */
public class Anime extends MediaContent {
    private Integer year;
    private String season;
    private Integer episodeCount;
    private List<String> tags = new ArrayList<>();
    private List<String> relatedAnime = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private String producers;
    private String studios;
    private AnimeType type;
    private AnimeStatus status;

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

    public AnimeType getType() {
        return type;
    }

    public AnimeStatus getStatus() {
        return status;
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

    public void setType(AnimeType type) {
        this.type = type;
    }

    public void setStatus(AnimeStatus status) {
        this.status = status;
    }


    /**
     * Adds a review to the list of reviews for this this.
     * @param review The review to add.
     */
    public void addReview(Review review) {
        this.reviews.add(review);
    }

    /**
     * Removes a review from the list of reviews for this this.
     * @param review The review to remove.
     */
    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    /**
     * Returns a string representation of the Anime object.
     * @return A string representation of the Anime object.
     */
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

    public AnimeDTO toDTO() {
        return new AnimeDTO(this.getId(), this.getTitle(), this.getImageUrl(), this.getAverageRating(), this.getYear(), this.getSeason());
    }
}
