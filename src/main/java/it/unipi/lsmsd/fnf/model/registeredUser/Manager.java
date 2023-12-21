package it.unipi.lsmsd.fnf.model.registeredUser;

import it.unipi.lsmsd.fnf.model.registeredUser.RegisteredUser;

import java.util.Date;

public class Manager extends RegisteredUser {
    private Date hiredDate;
    private String title;

    public Date getHiredDate() {
        return hiredDate;
    }

    public String getTitle() {
        return title;
    }

    public void setHiredDate(Date hiredDate) {
        this.hiredDate = hiredDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "hiredDate=" + hiredDate +
                ", title='" + title + '\'' +
                '}';
    }
}
