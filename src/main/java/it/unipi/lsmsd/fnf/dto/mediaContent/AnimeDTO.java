package it.unipi.lsmsd.fnf.dto.mediaContent;

import org.bson.types.ObjectId;


public class AnimeDTO extends MediaContentDTO{
    private Integer year;
    private String season;

    public AnimeDTO() {
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl, Double averageRating, Integer year, String season) {
        super(id, title, imageUrl, averageRating);
        this.year = year;
        this.season = season;
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

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
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
