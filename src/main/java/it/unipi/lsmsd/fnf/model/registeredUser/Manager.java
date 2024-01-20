package it.unipi.lsmsd.fnf.model.registeredUser;

import java.time.LocalDate;

public class Manager extends RegisteredUser {
    private LocalDate hiredDate;
    private String title;

    public LocalDate getHiredDate() {
        return hiredDate;
    }

    public String getTitle() {
        return title;
    }

    public void setHiredDate(LocalDate hiredDate) {
        this.hiredDate = hiredDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Manager{" +
                super.toString() +
                ", hiredDate=" + hiredDate +
                ", title='" + title + '\'' +
                '}';
    }
}
