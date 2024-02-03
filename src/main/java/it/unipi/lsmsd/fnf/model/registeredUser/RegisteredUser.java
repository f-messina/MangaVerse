package it.unipi.lsmsd.fnf.model.registeredUser;

import org.bson.types.ObjectId;

import java.time.LocalDate;

public abstract class RegisteredUser {
    private ObjectId id;
    private String password;
    private String email;
    private String fullname;
    private String profilePicUrl;
    private LocalDate joinedDate;

    public ObjectId getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getFullname() {
        return fullname;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    @Override
    public String toString() {
        return "RegisteredUser{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", joinedDate='" + joinedDate + '\'' +
                '}';
    }
}
