package it.unipi.lsmsd.fnf.dto.mediaContent;

import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;

import java.util.Date;
import java.util.List;

public class MangaDTO extends MediaContentDTO{


    public MangaDTO(String id, String title, String imageUrl, String type, String averageRating, String synopsis, String status) {
        super(id, title, imageUrl, type, averageRating, synopsis, status);
    }

    private List<String> genres;
    private List<String> themes;
    private List<String> demographics;
    private List<MangaAuthor> authors;
    private List<String> serializations;
    private String background;
    private String titleEnglish;
    private String titleJapanese;
    private Date startDate;
    private Date endDate;
    private List<Review<Manga>> reviews;



    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getThemes() {
        return themes;
    }

    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

    public List<String> getDemographics() {
        return demographics;
    }

    public void setDemographics(List<String> demographics) {
        this.demographics = demographics;
    }

    public List<MangaAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<MangaAuthor> authors) {
        this.authors = authors;
    }

    public List<String> getSerializations() {
        return serializations;
    }

    public void setSerializations(List<String> serializations) {
        this.serializations = serializations;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getTitleEnglish() {
        return titleEnglish;
    }

    public void setTitleEnglish(String titleEnglish) {
        this.titleEnglish = titleEnglish;
    }

    public String getTitleJapanese() {
        return titleJapanese;
    }

    public void setTitleJapanese(String titleJapanese) {
        this.titleJapanese = titleJapanese;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Review<Manga>> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review<Manga>> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "MangaDTO{" +
                "genres=" + genres +
                ", themes=" + themes +
                ", demographics=" + demographics +
                ", authors=" + authors +
                ", serializations=" + serializations +
                ", background='" + background + '\'' +
                ", titleEnglish='" + titleEnglish + '\'' +
                ", titleJapanese='" + titleJapanese + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reviews=" + reviews +
                '}';
    }
}
