package it.unipi.lsmsd.fnf.model.registeredUser;

import it.unipi.lsmsd.fnf.model.PersonalList;
import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;

import java.util.List;

public class User extends RegisteredUser {
    private String birthdate;
    private String description;
    private String gender;
    private String location;
    private List<PersonalList> lists;

    public String getBirthdate() {
        return birthdate;
    }

    public String getDescription() {
        return description;
    }

    public String getGender() {
        return gender;
    }

    public String getLocation() {
        return location;
    }

    public List<PersonalList> getLists() {
        return lists;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLists(List<PersonalList> lists) {
        this.lists = lists;
    }

    public void addList(PersonalList list) {
        this.lists.add(list);
    }

    public void removeList(PersonalList list) {
        this.lists.remove(list);
    }

    @Override
    public String toString() {
        return "User{" +
                ", birthdate='" + birthdate + '\'' +
                ", description='" + description + '\'' +
                ", gender='" + gender + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
