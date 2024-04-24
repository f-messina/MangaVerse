package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;
import it.unipi.lsmsd.fnf.model.Review;

import java.time.LocalDate;

public class ReviewDTO {
    private String id;
    private LocalDate date;
    private String comment;
    private Integer rating;
    private MediaContentDTO mediaContent;
    private UserSummaryDTO user;

    public ReviewDTO() {
    }

    public ReviewDTO(MediaContentDTO mediaContent, UserSummaryDTO user, String comment, Integer rating) {
        this.mediaContent = mediaContent;
        this.user = user;
        this.comment = comment;
        this.rating = rating;
    }

    public ReviewDTO(String id, LocalDate date, String comment, Integer rating, MediaContentDTO mediaContent, UserSummaryDTO user) {
        this.id = id;
        this.date = date;
        this.comment = comment;
        this.rating = rating;
        this.mediaContent = mediaContent;
        this.user = user;
    }

    public String getId() {
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

    public UserSummaryDTO getUser() {
        return user;
    }

    public void setId(String id) {
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

    public void setUser(UserSummaryDTO user) {
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

    public Review toModel() {
        Review review = new Review();
        review.setId(this.getId());
        review.setDate(this.getDate());
        review.setRating(this.getRating());
        review.setComment(this.getComment());
        review.setMediaContent(this.getMediaContent().toModel());
        if (this.getUser() != null)
            review.setUser(this.getUser().toModel());
        return review;
    }
}
