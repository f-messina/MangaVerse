package it.unipi.lsmsd.fnf.dto;

public class RegisteredUserDTO {

    private String id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String profilePicUrl;

    public RegisteredUserDTO(String id, String username, String password, String email, String fullName, String profilePicUrl) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.profilePicUrl = profilePicUrl;
    }

    public RegisteredUserDTO(String id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    @Override
    public String toString() {
        return "RegisteredUserDTO{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                '}';
    }
}
