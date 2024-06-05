package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;

import java.util.List;

public abstract class MediaContent {
    protected String id;
    protected String title;
    protected String imageUrl;
    protected Double averageRating;
    protected String synopsis;
    protected Integer likes;
    //review_ids
    protected List<String> reviewIds;

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
                '}';
    }

    public abstract MediaContentDTO toDTO();
}
