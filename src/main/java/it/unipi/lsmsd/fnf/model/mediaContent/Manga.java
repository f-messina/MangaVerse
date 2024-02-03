package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.model.Review;

import java.util.List;

public class Manga extends MediaContent {
    private List<String> genres;
    private List<String> themes;
    private List<String> demographics;
    private List<MangaAuthor> authors;
    private List<String> serializations;
    private String background;
    private String titleEnglish;
    private String titleJapanese;
    private String startDate;
    private String endDate;
    private Integer volumes;
    private Integer chapters;
    private List<Review> reviews;

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getThemes() {
        return themes;
    }

    public List<String> getDemographics() {
        return demographics;
    }

    public List<MangaAuthor> getAuthors() {
        return authors;
    }

    public List<String> getSerializations() {
        return serializations;
    }

    public String getBackground() {
        return background;
    }

    public String getTitleEnglish() {
        return titleEnglish;
    }

    public String getTitleJapanese() {
        return titleJapanese;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
    public Integer getVolumes() { return volumes; }
    public Integer getChapters() { return chapters; }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

    public void setDemographics(List<String> demographics) {
        this.demographics = demographics;
    }

    public void setAuthors(List<MangaAuthor> authors) {
        this.authors = authors;
    }

    public void setSerializations(List<String> serializations) {
        this.serializations = serializations;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public void setTitleEnglish(String titleEnglish) {
        this.titleEnglish = titleEnglish;
    }

    public void setTitleJapanese(String titleJapanese) {
        this.titleJapanese = titleJapanese;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setVolumes(Integer volumes) { this.volumes = volumes;  }

    public void setChapters(Integer chapters) { this.chapters = chapters; }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    @Override
    public String toString() {
        return "Manga{" +
                super.toString() +
                ", genres=" + genres +
                ", themes=" + themes +
                ", demographics=" + demographics +
                ", authors=" + authors +
                ", serializations=" + serializations +
                ", background='" + background + '\'' +
                ", titleEnglish='" + titleEnglish + '\'' +
                ", titleJapanese='" + titleJapanese + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", volumes=" + volumes +
                ", chapters=" + chapters +
                ", reviews=" + reviews +
                '}';
    }
}
