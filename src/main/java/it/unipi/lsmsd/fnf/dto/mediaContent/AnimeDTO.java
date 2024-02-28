package it.unipi.lsmsd.fnf.dto.mediaContent;

import org.bson.types.ObjectId;


public class AnimeDTO extends MediaContentDTO{
    private Integer year;

    public AnimeDTO() {
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl, Double averageRating, Integer year) {
        super(id, title, imageUrl, averageRating);
        this.year = year;
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl, Double averageRating) {
        super(id, title, imageUrl, averageRating);
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl) {
        super(id, title, imageUrl);
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "AnimeDTO{" +
                "title=" + getTitle() +
                ", imageUrl=" + getImageUrl() +
                ", averageRating=" + getAverageRating() +
                ", year=" + year +
                '}';
    }
}
