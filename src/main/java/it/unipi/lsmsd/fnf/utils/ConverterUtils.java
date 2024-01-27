package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static java.time.ZoneId.systemDefault;

public class ConverterUtils {

    // Convert Date to LocalDate
    public static LocalDate convertDateToLocalDate(Date date) {
        if (date == null) return null;
        Instant instant = date.toInstant();
        return instant.atZone(systemDefault()).toLocalDate();
    }

    // Convert LocalDate to Date
    public static Date convertLocalDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(systemDefault()).toInstant());
    }

    public static UserRegistrationDTO fromRequestToUserRegDTO(HttpServletRequest request){
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername(request.getParameter("username"));
        userRegistrationDTO.setPassword(request.getParameter("password"));
        userRegistrationDTO.setEmail(request.getParameter("email"));
        userRegistrationDTO.setFullname(request.getParameter("fullname"));
        userRegistrationDTO.setLocation(request.getParameter("location"));
        userRegistrationDTO.setBirthday(LocalDate.parse(request.getParameter("birthday")));
        return userRegistrationDTO;
    }
}
