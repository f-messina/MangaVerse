package it.unipi.lsmsd.fnf.dto.mediaContent;

import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;

import java.util.Objects;

/**
 * Data Transfer Object for the MediaContent class.
 * It is used to store information to be displayed in lists (inside reviews, in search results, etc.),
 * to store redundant information used in reviews collection in MongoDB and
 * to manipulate nodes in Neo4j.
 * @see MediaContent
 */
public abstract class MediaContentDTO {
    protected String id;
    protected String title;
    protected String imageUrl;
    protected Double averageRating;
    protected Integer likes;

    public MediaContentDTO() {}

    public MediaContentDTO(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public MediaContentDTO(String id, String title, String imageUrl, Double averageRating) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
    }

    public MediaContentDTO(String id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "MediaContentDTO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", averageRating='" + averageRating + '\'' +
                '}';
    }

    public abstract MediaContent toModel();

    /**
     * Compares two MediaContentDTO objects.
     *
     * @param o The object to compare to.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaContentDTO that = (MediaContentDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(imageUrl, that.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, imageUrl);
    }
}
