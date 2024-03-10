package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.model.enums.UserType;

import java.time.LocalDate;

public class LoggedUserDTO {

    private String id;
    private String username;
    private String profilePicUrl;
    private UserType type;

    public LoggedUserDTO() {
    }

    public LoggedUserDTO(String id, String username, String profilePicUrl, UserType type) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
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
                ", type='" + type + '\'' +
                '}';
    }
}
