package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.dto.mediaContent.MangaDTO;
import it.unipi.lsmsd.fnf.model.enums.MangaDemographics;
import it.unipi.lsmsd.fnf.model.enums.MangaStatus;
import it.unipi.lsmsd.fnf.model.enums.MangaType;

import java.time.LocalDate;
import java.util.List;

/**
 * Model class representing a manga.
 * It extends the MediaContent class and adds the specific attributes of a manga.
 */
public class Manga extends MediaContent {
    private List<String> genres;
    private List<String> themes;
    private List<MangaDemographics> demographics;
    private List<MangaAuthor> authors;
    private String serializations;
    private String background;
    private String titleEnglish;
    private String titleJapanese;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer volumes;
    private Integer chapters;
    private MangaType type;
    private MangaStatus status;

    public List<String> getGenres() {
        return genres;
    }
    public List<String> getThemes() {
        return themes;
    }
    public List<MangaDemographics> getDemographics() {
        return demographics;
    }
    public List<MangaAuthor> getAuthors() {
        return authors;
    }
    public String getSerializations() {
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
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public Integer getVolumes() { return volumes; }
    public Integer getChapters() { return chapters; }
    public MangaType getType() {
        return type;
    }
    public MangaStatus getStatus() {
        return status;
    }
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
    public void setThemes(List<String> themes) {
        this.themes = themes;
    }
    public void setDemographics(List<MangaDemographics> demographics) {
        this.demographics = demographics;
    }
    public void setAuthors(List<MangaAuthor> authors) {
        this.authors = authors;
    }
    public void setSerializations(String serializations) {
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
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public void setVolumes(Integer volumes) { this.volumes = volumes;  }
    public void setChapters(Integer chapters) { this.chapters = chapters; }
    public void setType(MangaType type) {
        this.type = type;
    }
    public void setStatus(MangaStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Manga{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", averageRating='" + averageRating + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", likes='" + likes + '\'' +
                ", reviewIds='" + reviewIds + '\'' +
                ", latestReviews='" + latestReviews + '\'' +
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
                ", type=" + type +
                ", status=" + status +
                '}';
    }

    /**
     * Converts the Manga object to a MangaDTO object.
     *
     * @return      The MangaDTO object.
     */
    public MangaDTO toDTO() {
        return new MangaDTO(this.getId(), this.getTitle(), this.getImageUrl(), this.getAverageRating(), this.getStartDate(), this.getEndDate());
    }
}
