package it.unipi.lsmsd.fnf.model.registeredUser;

import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.Review;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.mediaContent.MediaContent;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class User extends RegisteredUser {
    private String username;
    private LocalDate birthday;
    private String description;
    private Gender gender;
    private String location;
    private List<PersonalList> lists = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    private List<MediaContent> likedMediaContent = new ArrayList<>();

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

    public List<PersonalList> getLists() {
        return lists;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public List<MediaContent> getLikedMediaContent() {
        return likedMediaContent;
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

    public void setLists(List<PersonalList> lists) {
        this.lists = lists;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    /**
     * Sets the media content liked by the user.
     * @param likedMediaContent The media content liked by the user.
     */
    public void setLikedMediaContent(List<MediaContent> likedMediaContent) {
        this.likedMediaContent = likedMediaContent;
    }

    /**
     * Adds a new list created by the user.
     * @param list The list created by the user.
     */
    public void addList(PersonalList list) {
        this.lists.add(list);
    }

    /**
     * Removes a list created by the user.
     * @param listId The ID of the list to be removed.
     */
    public void removeList(String listId) {
        this.lists.removeIf(personalList -> personalList.getId().equals(listId));
    }

    /**
     * Adds a new review created by the user.
     * @param review The review created by the user.
     */
    public void addReview(Review review) {
        this.reviews.add(review);
    }

    /**
     * Removes a review created by the user.
     * @param review The review to be removed.
     */
    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    /**
     * Adds new media content liked by the user.
     * @param mediaContent The media content liked by the user.
     */
    public void addLikedMediaContent(MediaContent mediaContent) {
        this.likedMediaContent.add(mediaContent);
    }

    /**
     * Removes media content liked by the user.
     * @param mediaContentId The ID of the media content to be removed.
     */
    public void removeLikedMediaContent(String mediaContentId) {
        this.likedMediaContent.removeIf(content -> content.getId().equals(mediaContentId));
    }

    /**
     * Overrides the default toString method to provide a custom string representation of the User object.
     * @return A string representation of the User object.
     */
    @Override
    public String toString() {
        return "User{" +
                super.toString() +
                ", username='" + username + '\'' +
                ", birthday='" + birthday + '\'' +
                ", description='" + description + '\'' +
                ", gender='" + gender + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
