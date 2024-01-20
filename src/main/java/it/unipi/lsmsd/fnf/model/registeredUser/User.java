package it.unipi.lsmsd.fnf.model.registeredUser;

import it.unipi.lsmsd.fnf.model.PersonalList;

import java.time.LocalDate;
import java.util.List;

public class User extends RegisteredUser {
    private LocalDate birthday;
    private String description;
    private String gender;
    private String location;
    private List<PersonalList> lists;

    public LocalDate getBirthday() {
        return birthday;
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

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
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
                super.toString() +
                ", birthday='" + birthday + '\'' +
                ", description='" + description + '\'' +
                ", gender='" + gender + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
