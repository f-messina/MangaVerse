package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.time.LocalDate;
import java.util.Objects;

public class UserSummaryDTO {

    private String id;
    private String username;
    private String profilePicUrl;
    private String location;
    private LocalDate birthDate;

    public UserSummaryDTO() {
    }

    // Constructor for UserSummaryDTO to store info to be displayed
    // in lists (inside reviews, in search results, etc.)
    public UserSummaryDTO(String id, String username, String profilePicUrl) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
    }

    // Constructor for UserSummaryDTO to store info used in reviews
    // (user location and birthdate) to do statistics and analysis on reviews
    public UserSummaryDTO(String id, String username, String profilePicUrl, String location, LocalDate birthDate) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.location = location;
        this.birthDate = birthDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return "UserSummaryDTO{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                '}';
    }

    public User toModel() {
        User user = new User();
        user.setId(this.getId());
        user.setUsername(this.getUsername());
        user.setProfilePicUrl(this.getProfilePicUrl());
        user.setLocation(this.getLocation());
        user.setBirthday(this.getBirthDate());
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSummaryDTO that = (UserSummaryDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(profilePicUrl, that.profilePicUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, profilePicUrl);
    }
}
