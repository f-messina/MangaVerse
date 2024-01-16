package it.unipi.lsmsd.fnf.dto;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Date;

public class RegisteredUserDTO {

    private ObjectId id;
    private String username;
    private String profilePicUrl;
    private String location;
    private LocalDate birthday;
    private int age;

    public RegisteredUserDTO(ObjectId id, String username, String profilePicUrl) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
    }

    public RegisteredUserDTO(ObjectId id, String location, LocalDate birthday) {
        this.id = id;
        this.location = location;
        this.birthday = birthday;
        age = calculateAge();
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

    public int getAge() {
        if (age == 0 && birthday != null) {
            age = calculateAge();
        }
        return age;
    }

    public int calculateAge() {
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthday.getYear();
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
