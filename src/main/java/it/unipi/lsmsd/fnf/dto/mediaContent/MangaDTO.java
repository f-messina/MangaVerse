package it.unipi.lsmsd.fnf.dto.mediaContent;

import org.bson.types.ObjectId;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDate;

public class MangaDTO extends MediaContentDTO{
    private LocalDate startDate;
    private LocalDate endDate;

    public MangaDTO() {
    }

    public MangaDTO(String id, String title) {
        super(id, title);
    }

    public MangaDTO(String id, String title, String imageUrl, Double averageRating, LocalDate startDate, LocalDate endDate) {
        super(id, title, imageUrl, averageRating);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public MangaDTO(String id, String title, String imageUrl, Double averageRating) {
        super(id, title, imageUrl, averageRating);
    }

    public MangaDTO(String id, String title, String imageUrl) {
        super(id, title, imageUrl);
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
        return super.toString() + "MangaDTO{" +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
