package it.unipi.lsmsd.fnf.model.mediaContent;

import it.unipi.lsmsd.fnf.dto.mediaContent.AnimeDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.AnimeStatus;
import it.unipi.lsmsd.fnf.model.enums.AnimeType;

import java.util.List;

/**
 * Represents an this media content.
 */
public class Anime extends MediaContent {
    private Integer year;
    private String season;
    private Integer episodeCount;
    private List<String> tags;
    private List<String> relatedAnime;
    private String producers;
    private String studios;
    private AnimeType type;
    private AnimeStatus status;

    public Integer getYear() {
        return year;
    }

    public String getSeason() {
        return season;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getRelatedAnime() {
        return relatedAnime;
    }
    public String getProducers() {
        return producers;
    }

    public String getStudios() {
        return studios;
    }

    public AnimeType getType() {
        return type;
    }

    public AnimeStatus getStatus() {
        return status;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setRelatedAnime(List<String> relatedAnime) {
        this.relatedAnime = relatedAnime;
    }
    public void setProducers(String producers) {
        this.producers = producers;
    }

    public void setStudios(String studios) {
        this.studios = studios;
    }

    public void setType(AnimeType type) {
        this.type = type;
    }

    public void setStatus(AnimeStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Anime{" +
                super.toString() +
                ", year=" + year +
                ", season='" + season + '\'' +
                ", episodeCount='" + episodeCount + '\'' +
                ", tags=" + tags +
                ", relatedAnime=" + relatedAnime +
                ", producers='" + producers + '\'' +
                ", studios='" + studios + '\'' +
                '}';
    }

    public AnimeDTO toDTO() {
        return new AnimeDTO(this.getId(), this.getTitle(), this.getImageUrl(), this.getAverageRating(), this.getYear(), this.getSeason());
    }
}
