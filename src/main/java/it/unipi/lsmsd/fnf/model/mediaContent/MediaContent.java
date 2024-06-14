package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.Review;

import java.util.List;

/**
 * Abstract class representing a media content.
 * It contains the common attributes of all the media content types (Anime, Manga).
 */
public abstract class MediaContent {
    protected String id;
    protected String title;
    protected String imageUrl;
    protected Double averageRating;
    protected String synopsis;
    protected Integer likes;
    protected List<String> reviewIds;
    protected List<Review> latestReviews;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public Integer getLikes() {
        return likes;
    }

    public List<String> getReviewIds () {
        return reviewIds;
    }

    public List<Review> getLatestReviews() {
        return latestReviews;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }
    public void setReviewIds(List<String> reviewIds) {
        this.reviewIds = reviewIds;
    }
    public void setLatestReviews(List<Review> latestReviews) {
        this.latestReviews = latestReviews;
    }
    public void addReview(Review review) {
        this.latestReviews.add(review);
    }
    public void removeReview(Review review) {
        this.latestReviews.remove(review);
    }

    /**
     * Overrides the default toString method to provide a custom string representation of the MediaContent object.
     * @return A string representation of the MediaContent object.
     */
    @Override
    public String toString() {
        return "MediaContent{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", averageRating='" + averageRating + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", likes='" + likes + '\'' +
                ", reviewIds='" + reviewIds + '\'' +
                ", latestReviews='" + latestReviews + '\'' +
                '}';
    }

    public abstract MediaContentDTO toDTO();
}
