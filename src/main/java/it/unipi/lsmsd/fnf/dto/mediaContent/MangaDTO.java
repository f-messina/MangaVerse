package it.unipi.lsmsd.fnf.dto.mediaContent;

import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.mediaContent.Manga;
import it.unipi.lsmsd.fnf.model.mediaContent.MangaAuthor;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class MangaDTO extends MediaContentDTO{

    public MangaDTO() {
    }

    public MangaDTO(ObjectId id, String title, String imageUrl, float averageRating, String startDate, String endDate) {
        super(id, title, imageUrl, averageRating);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public MangaDTO(ObjectId id, String title, String imageUrl, float averageRating) {
        super(id, title, imageUrl, averageRating);
    }

    public MangaDTO(ObjectId id, String title, String imageUrl) {
        super(id, title, imageUrl);
    }

    private String startDate;
    private String endDate;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "MangaDTO{" +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
