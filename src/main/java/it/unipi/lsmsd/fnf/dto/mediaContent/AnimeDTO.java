package it.unipi.lsmsd.fnf.dto.mediaContent;

import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.mediaContent.Anime;

import java.util.List;

public class AnimeDTO extends MediaContentDTO{
    public AnimeDTO(String id, String title, String imageUrl, float averageRating, int year) {
        super(id, title, imageUrl, averageRating);
        this.year = year;
    }
    private int year;



    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }




    @Override
    public String toString() {
        return "AnimeDTO{" +
                "year=" + year +

                '}';
    }
}
