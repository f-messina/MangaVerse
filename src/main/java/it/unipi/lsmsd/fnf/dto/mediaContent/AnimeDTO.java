package it.unipi.lsmsd.fnf.dto.mediaContent;

import it.unipi.lsmsd.fnf.model.mediaContent.Anime;

/**
 * Data Transfer Object for the Anime class.
 * Extends MediaContentDTO.
 * @see MediaContentDTO
 * @see Anime
 */
public class AnimeDTO extends MediaContentDTO {
        private Integer year;
        private String season;

    public AnimeDTO() {}
    public AnimeDTO(String id, String title) {
        super(id, title);
    }
    public AnimeDTO(String id, String title, String imageUrl) {
        super(id, title, imageUrl);
    }

    public AnimeDTO(String id, String title, String imageUrl, Double averageRating, Integer year, String season) {
        super(id, title, imageUrl, averageRating);
        this.year = year;
        this.season = season;
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
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", averageRating='" + averageRating + '\'' +
                ", year='" + year + '\'' +
                ", season='" + season + '\'' +
                '}';
    }

    /**
     * Converts an AnimeDTO object to an Anime object.
     *
     * @return      The Anime object.
     */
    public Anime toModel() {
        Anime anime = new Anime();
        anime.setId(this.getId());
        anime.setTitle(this.getTitle());
        anime.setImageUrl(this.getImageUrl());
        return anime;
    }
}
