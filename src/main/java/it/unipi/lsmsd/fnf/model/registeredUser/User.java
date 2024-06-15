package it.unipi.lsmsd.fnf.model.registeredUser;

import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.dto.registeredUser.UserSummaryDTO;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.enums.UserType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model Class that represents a User of the platform.
 * It extends the RegisteredUser class and adds the specific attributes of a User.
 */
public class User extends RegisteredUser {
    private String username;
    private LocalDate birthday;
    private String description;
    private Gender gender;
    private String location;
    private List<Review> reviews = new ArrayList<>();
    private Integer followers;
    private Integer followed;
    private List<String> reviewIds;
    private Integer appRating;

    public String getUsername() {
        return username;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getDescription() {
        return description;
    }

    public Gender getGender() {
        return gender;
    }

    public String getLocation() {
        return location;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public Integer getFollowers() {
        return followers;
    }

    public Integer getFollowed() {
        return followed;
    }
    public List<String> getReviewIds () {
        return reviewIds;
    }

    public Integer getAppRating() {
        return appRating;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public void setFollowed(Integer followed) {
        this.followed = followed;
    }
    public void setAppRating(Integer appRating) {
        this.appRating = appRating;
    }

    public void setReviewIds (List<String> reviewIds) {
        this.reviewIds = reviewIds;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", joinedDate='" + joinedDate + '\'' +
                ", username='" + username + '\'' +
                ", birthday='" + birthday + '\'' +
                ", description='" + description + '\'' +
                ", gender='" + gender + '\'' +
                ", location='" + location + '\'' +
                ", reviews=" + reviews +
                ", followers=" + followers +
                ", followed=" + followed +
                ", reviewIds=" + reviewIds +
                ", appRating=" + appRating +
                '}';
    }

    /**
     * Converts a User object to a UserSummaryDTO object.
     *
     * @return      The UserSummaryDTO object.
     */
    public UserSummaryDTO toSummaryDTO () {
        return new UserSummaryDTO(this.getId(), this.getUsername(), this.getProfilePicUrl(), this.getLocation(), this.getBirthday());
    }

    /**
     * Converts a User object to a LoggedUserDTO object.
     *
     * @return      The LoggedUserDTO object.
     */
    public LoggedUserDTO toLoggedUserDTO() {
        return new LoggedUserDTO(this.getId(), this.getUsername(), this.getProfilePicUrl(), this.getLocation(), this.getBirthday(), UserType.USER);
    }
}
