package it.unipi.lsmsd.fnf.dto;

import org.bson.types.ObjectId;
import java.time.LocalDate;

public class RegisteredUserDTO {

    private ObjectId id;
    private String username;
    private String profilePicUrl;
    private String location;
    private LocalDate birthday;
    private Integer age;

    public RegisteredUserDTO() {
    }

    public RegisteredUserDTO(ObjectId id, String username, String profilePicUrl) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
    }

    public RegisteredUserDTO(ObjectId id, String location, LocalDate birthday, Integer age) {
        this.id = id;
        this.location = location;
        this.birthday = birthday;
        this.age = age;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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

    public Integer getAge() {
        if (age != null && birthday != null) {
            age = calculateAge(birthday);
        }
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer calculateAge(LocalDate birthday) {
        LocalDate now = LocalDate.now();
        Integer age = now.getYear() - birthday.getYear();
        if (now.getMonthValue() < birthday.getMonthValue() ||
                (now.getMonthValue() == birthday.getMonthValue() && now.getDayOfMonth() < birthday.getDayOfMonth())) {
            age--;
        }
        return age;
    }

    @Override
    public String toString() {
        return "RegisteredUserDTO{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", location='" + location + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}
