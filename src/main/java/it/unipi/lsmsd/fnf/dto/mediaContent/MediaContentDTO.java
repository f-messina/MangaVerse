package it.unipi.lsmsd.fnf.dto.mediaContent;

import org.bson.types.ObjectId;

public abstract class MediaContentDTO {
    private ObjectId id;
    private String title;
    private String imageUrl;
    private Double averageRating;

    public MediaContentDTO() {
    }

    public MediaContentDTO(ObjectId id, String title, String imageUrl, Double averageRating) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
    }

    public MediaContentDTO(ObjectId id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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

    @Override
    public String toString() {
        return "MediaContentDTO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", averageRating='" + averageRating + '\'' +
                '}';
    }
}
