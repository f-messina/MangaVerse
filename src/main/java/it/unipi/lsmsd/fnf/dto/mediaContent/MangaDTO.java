package it.unipi.lsmsd.fnf.dto.mediaContent;

import org.bson.types.ObjectId;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MangaDTO extends MediaContentDTO{
    private String startDate;
    private String endDate;

    public MangaDTO() {
    }

    public MangaDTO(ObjectId id, String title, String imageUrl, Double averageRating, String startDate, String endDate) {
        super(id, title, imageUrl, averageRating);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public MangaDTO(ObjectId id, String title, String imageUrl, Double averageRating) {
        super(id, title, imageUrl, averageRating);
    }

    public MangaDTO(ObjectId id, String title, String imageUrl) {
        super(id, title, imageUrl);
    }


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
