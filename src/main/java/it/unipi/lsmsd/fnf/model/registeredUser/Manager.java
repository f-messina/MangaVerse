package it.unipi.lsmsd.fnf.model.registeredUser;

import it.unipi.lsmsd.fnf.dto.LoggedUserDTO;
import it.unipi.lsmsd.fnf.model.enums.UserType;

import java.time.LocalDate;

public class Manager extends RegisteredUser {
    private LocalDate hiredDate;

    public LocalDate getHiredDate() {
        return hiredDate;
    }

    public void setHiredDate(LocalDate hiredDate) {
        this.hiredDate = hiredDate;
    }

    /**
     * Overrides the default toString method to provide a custom string representation of the Manager object.
     * @return A string representation of the Manager object.
     */
    @Override
    public String toString() {
        return "Manager{" +
                super.toString() +
                ", hiredDate=" + hiredDate +
                '}';
    }

    public LoggedUserDTO toLoggedUserDTO() {
        return new LoggedUserDTO(this.getId(), this.getFullname(), this.getProfilePicUrl(), UserType.MANAGER);
    }
}
