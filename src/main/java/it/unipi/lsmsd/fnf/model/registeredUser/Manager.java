package it.unipi.lsmsd.fnf.model.registeredUser;

import it.unipi.lsmsd.fnf.dto.registeredUser.LoggedUserDTO;
import it.unipi.lsmsd.fnf.model.enums.UserType;

import java.time.LocalDate;

/**
 * Model Class that represents a Manager of the platform.
 * It extends the RegisteredUser class and adds the hiredDate attribute.
 */
public class Manager extends RegisteredUser {
    private LocalDate hiredDate;

    public LocalDate getHiredDate() {
        return hiredDate;
    }

    public void setHiredDate(LocalDate hiredDate) {
        this.hiredDate = hiredDate;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", joinedDate='" + joinedDate + '\'' +
                ", hiredDate=" + hiredDate +
                '}';
    }

    /**
     * Converts the Manager object to a LoggedUserDTO object.
     *
     * @return      A LoggedUserDTO object representing the Manager object.
     */
    public LoggedUserDTO toLoggedUserDTO() {
        return new LoggedUserDTO(this.getId(), this.getFullname(), this.getProfilePicUrl(), null, null, UserType.MANAGER);
    }
}
