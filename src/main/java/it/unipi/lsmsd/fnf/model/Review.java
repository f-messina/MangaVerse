package it.unipi.lsmsd.fnf.model;

import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Date;

public class Review<T extends MediaContent> {
    private ObjectId id;
    private LocalDate date;
    private String comment;
    private Integer rating;
    private T mediaContent;
    private User user;

    public ObjectId getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public Integer getRating() {
        return rating;
    }

    public T getMediaContent() {
        return mediaContent;
    }

    public User getUser() {
        return user;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setMediaContent(T mediaContent) {
        this.mediaContent = mediaContent;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", date=" + date +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                ", mediaContent=" + mediaContent +
                ", user=" + user +
                '}';
    }
}
