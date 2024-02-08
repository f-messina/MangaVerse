package it.unipi.lsmsd.fnf.dto.mediaContent;

import org.bson.types.ObjectId;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AnimeDTO extends MediaContentDTO{
    private Integer year;
    public AnimeDTO() {
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl, Double averageRating, Integer year) {
        super(id, title, imageUrl, averageRating);
        this.year = year;
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

    @Override
    public String toString() {
        return super.toString() +
                "year=" + year +
                '}';
    }
}
