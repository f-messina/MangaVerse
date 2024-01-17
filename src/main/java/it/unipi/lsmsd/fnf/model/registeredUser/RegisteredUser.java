package it.unipi.lsmsd.fnf.model.registeredUser;

import org.bson.types.ObjectId;

import java.time.LocalDate;

public abstract class RegisteredUser {
    private ObjectId id;
    private String username;
    private String password;
    private String email;
    private String fullname;
    private String profilePicUrl;
    private LocalDate joinedDate;

    public ObjectId getId() {
        return id;
    }

    public String getUsername() {
        return username;
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

    public String getprofilePicUrl() {
        return profilePicUrl;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void setprofilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    @Override
    public String toString() {
        return "RegisteredUser{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", joinedDate='" + joinedDate + '\'' +
                '}';
    }
}
