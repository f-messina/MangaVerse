package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class ReviewDTO {
    private ObjectId id;
    private LocalDate date;
    private String comment;
    private Integer rating;
    private MediaContentDTO mediaContent;
    private RegisteredUserDTO user;

    public ReviewDTO() {
    }

    public ReviewDTO(ObjectId id, LocalDate date, String comment, Integer rating, MediaContentDTO mediaContent, RegisteredUserDTO user) {
        this.id = id;
        this.date = date;
        this.comment = comment;
        this.rating = rating;
        this.mediaContent = mediaContent;
        this.user = user;
    }

    public ReviewDTO(ObjectId id, LocalDate date, String comment, MediaContentDTO mediaContent, RegisteredUserDTO user) {
        this.id = id;
        this.date = date;
        this.comment = comment;
        this.mediaContent = mediaContent;
        this.user = user;
    }

    public ReviewDTO(ObjectId id, LocalDate date, Integer rating, MediaContentDTO mediaContent, RegisteredUserDTO user) {
        this.id = id;
        this.date = date;
        this.rating = rating;
        this.mediaContent = mediaContent;
        this.user = user;
    }

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

    public MediaContentDTO getMediaContent() {
        return mediaContent;
    }

    public RegisteredUserDTO getUser() {
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

    public void setMediaContent(MediaContentDTO mediaContent) {
        this.mediaContent = mediaContent;
    }

    public void setUser(RegisteredUserDTO user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                ", mediaContent=" + mediaContent +
                ", user=" + user +
                '}';
    }
}
