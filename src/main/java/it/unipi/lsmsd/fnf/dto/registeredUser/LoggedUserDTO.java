package it.unipi.lsmsd.fnf.dto.registeredUser;

import it.unipi.lsmsd.fnf.model.enums.UserType;
import it.unipi.lsmsd.fnf.model.registeredUser.User;

import java.time.LocalDate;

/**
 * Data Transfer Object for the User class.
 * It is used to store information of a logged user that are displayed in the frontend
 * or that are necessary to perform some operations and reduce the amount of data exchanged.
 * @see User
 * @see UserType
 */
public class LoggedUserDTO {

    private String id;
    private String username;
    private String profilePicUrl;
    private String location;
    private LocalDate birthday;
    private UserType type;

    public LoggedUserDTO() {}

    public LoggedUserDTO(String id, String username, String profilePicUrl, String location, LocalDate birthday, UserType type) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.location = location;
        this.birthday = birthday;
        this.type = type;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LoggedUserDTO{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", location='" + location + '\'' +
                ", birthday=" + birthday +
                ", type='" + type + '\'' +
                '}';
    }

    /**
     * Converts a LoggedUserDTO object to a User object.
     *
     * @return The User object.
     */
    public User toUserModel() {
        if (this.type == UserType.MANAGER) {
            return null;
        }

        User user = new User();
        user.setId(this.getId());
        user.setUsername(this.getUsername());
        user.setProfilePicUrl(this.getProfilePicUrl());
        user.setLocation(this.getLocation());
        user.setBirthday(this.getBirthday());
        return user;
    }
}
