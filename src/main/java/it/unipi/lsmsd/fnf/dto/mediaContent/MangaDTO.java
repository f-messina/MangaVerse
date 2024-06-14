package it.unipi.lsmsd.fnf.dto.mediaContent;

import it.unipi.lsmsd.fnf.model.mediaContent.Manga;

import java.time.LocalDate;

/**
 * Data Transfer Object for the Manga class.
 * Extends MediaContentDTO.
 * @see MediaContentDTO
 * @see Manga
 */
public class MangaDTO extends MediaContentDTO{
    private LocalDate startDate;
    private LocalDate endDate;

    public MangaDTO() {}

    public MangaDTO(String id, String title) {
        super(id, title);
    }
    public MangaDTO(String id, String title, String imageUrl) {
        super(id, title, imageUrl);
    }

    public MangaDTO(String id, String title, String imageUrl, Double averageRating, LocalDate startDate, LocalDate endDate) {
        super(id, title, imageUrl, averageRating);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "MangaDTO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", averageRating='" + averageRating + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }

    /**
     * Converts a MangaDTO object to a Manga object.
     *
     * @return The Manga object.
     */
    public Manga toModel() {
        Manga manga = new Manga();
        manga.setId(this.getId());
        manga.setTitle(this.getTitle());
        manga.setImageUrl(this.getImageUrl());
        return manga;
    }
}
