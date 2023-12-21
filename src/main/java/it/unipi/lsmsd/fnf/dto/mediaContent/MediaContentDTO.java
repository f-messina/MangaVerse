package it.unipi.lsmsd.fnf.dto.mediaContent;

public abstract class MediaContentDTO {
    private String id;
    private String title;
    private String imageUrl;
    private String type;
    private String averageRating;
    private String synopsis;
    private String status;

    public MediaContentDTO(String id, String title, String imageUrl, String type, String averageRating, String synopsis, String status) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.type = type;
        this.averageRating = averageRating;
        this.synopsis = synopsis;
        this.status = status;
    }
    public MediaContentDTO(String id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(String averageRating) {
        this.averageRating = averageRating;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MediaContentDTO{" +
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
