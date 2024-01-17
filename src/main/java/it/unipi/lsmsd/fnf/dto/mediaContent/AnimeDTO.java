package it.unipi.lsmsd.fnf.dto.mediaContent;

import org.bson.types.ObjectId;


public class AnimeDTO extends MediaContentDTO{

    public AnimeDTO() {
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl, float averageRating, int year) {
        super(id, title, imageUrl, averageRating);
        this.year = year;
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl, float averageRating) {
        super(id, title, imageUrl, averageRating);
    }

    public AnimeDTO(ObjectId id, String title, String imageUrl) {
        super(id, title, imageUrl);
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
