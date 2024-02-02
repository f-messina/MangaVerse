package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.time.ZoneId.systemDefault;

public class ConverterUtils {

    // Convert Date to LocalDate
    public static LocalDate convertDateToLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Convert LocalDate to Date
    public static Date convertLocalDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static UserRegistrationDTO fromRequestToUserRegDTO(HttpServletRequest request){
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername(request.getParameter("username"));
        userRegistrationDTO.setPassword(request.getParameter("password"));
        userRegistrationDTO.setEmail(request.getParameter("email"));
        if (request.getParameter("fullname") != null && !request.getParameter("fullname").isEmpty())
            userRegistrationDTO.setFullname(request.getParameter("fullname"));
        if (request.getParameter("country") != null && !request.getParameter("country").isEmpty())
            userRegistrationDTO.setLocation(request.getParameter("country"));
        if (request.getParameter("birthday") != null && !request.getParameter("birthday").isEmpty())
            userRegistrationDTO.setBirthday(LocalDate.parse(request.getParameter("birthday")));
        userRegistrationDTO.setGender(Gender.fromString(request.getParameter("gender")));
        return userRegistrationDTO;
    }
}
