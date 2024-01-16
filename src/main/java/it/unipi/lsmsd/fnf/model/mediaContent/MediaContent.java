package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.model.enums.Status;
import org.bson.types.ObjectId;

import java.util.List;

public abstract class MediaContent {
    private ObjectId id;
    private String title;
    private String imageUrl;
    private String type;
    private float averageRating;
    private String synopsis;
    private Status status;

    public ObjectId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getType() {
        return type;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public Status getStatus() {
        return status;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MediaContent{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", type='" + type + '\'' +
                ", averageRating='" + averageRating + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
