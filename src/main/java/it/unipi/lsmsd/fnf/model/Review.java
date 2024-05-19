package it.unipi.lsmsd.fnf.model;

import it.unipi.lsmsd.fnf.dto.ReviewDTO;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.time.LocalDate;

public class Review {
    private String id;
    private LocalDate date;
    private String comment;
    private Integer rating;
    private MediaContent mediaContent;
    private User user;

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

    public MediaContent getMediaContent() {
        return mediaContent;
    }

    public User getUser() {
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

    public void setMediaContent(MediaContent mediaContent) {
        this.mediaContent = mediaContent;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Overrides the default toString method to provide a custom string representation of the Review object.
     * @return A string representation of the Review object.
     */
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

    public ReviewDTO toDTO() {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(this.getId());
        dto.setComment(this.getComment());
        dto.setRating(this.getRating());
        dto.setDate(this.getDate());
        if (this.getMediaContent() != null)
            dto.setMediaContent(this.getMediaContent().toDTO());
        dto.setUser(this.getUser().toSummaryDTO());

        return dto;
    }
}
