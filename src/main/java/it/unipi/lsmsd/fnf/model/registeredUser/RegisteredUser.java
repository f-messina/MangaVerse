package it.unipi.lsmsd.fnf.model.registeredUser;

import java.time.LocalDate;

public abstract class RegisteredUser {
    protected String id;
    protected String password;
    protected String email;
    protected String fullname;
    protected String profilePicUrl;
    protected LocalDate joinedDate;

    public String getId() {
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

    public void setId(String id) {
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


    /**
     * Overrides the default toString method to provide a custom string representation of the RegisteredUser object.
     * @return A string representation of the RegisteredUser object.
     */
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
