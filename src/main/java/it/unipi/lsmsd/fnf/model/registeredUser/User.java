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

    public User() {
    }

    public User(String id, String username, String profilePicUrl) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
    }

    public User(String id, LocalDate birthday, String location) {
        this.id = id;
        this.birthday = birthday;
        this.location = location;
    }

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

    public void setLikedMediaContent(List<MediaContent> likedMediaContent) {
        this.likedMediaContent = likedMediaContent;
    }

    public void addList(PersonalList list) {
        this.lists.add(list);
    }

    public void removeList(String listId) {
        this.lists.removeIf(personalList -> personalList.getId().equals(listId));
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
    }

    public void addLikedMediaContent(MediaContent mediaContent) {
        this.likedMediaContent.add(mediaContent);
    }

    public void removeLikedMediaContent(String mediaContentId) {
        this.likedMediaContent.removeIf(content -> content.getId().equals(mediaContentId));
    }

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
