package it.unipi.lsmsd.fnf.model;

import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.util.Date;

public class Review<T> {
    private String id;
    private Date date;
    private String comment;
    private int rating;
    private T mediaContent;
    private User user;

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }

    public T getMediaContent() {
        return mediaContent;
    }

    public User getUser() {
        return user;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(int rating) {
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
                ", comment='" + comment + '\'' +
                ", rating='" + rating + '\'' +
                ", manga_anime='" + mediaContent + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
