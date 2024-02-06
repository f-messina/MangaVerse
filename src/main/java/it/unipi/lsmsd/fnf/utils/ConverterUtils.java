package it.unipi.lsmsd.fnf.utils;

import it.unipi.lsmsd.fnf.dto.UserRegistrationDTO;
import it.unipi.lsmsd.fnf.model.enums.Gender;
import it.unipi.lsmsd.fnf.model.registeredUser.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ConverterUtils {

    // Convert Date to LocalDate
    public static LocalDate dateToLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Convert LocalDate to Date
    public static Date localDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static UserRegistrationDTO fromRequestToUserRegDTO(HttpServletRequest request){
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername(request.getParameter("username"));
        userRegistrationDTO.setPassword(request.getParameter("password"));
        userRegistrationDTO.setEmail(request.getParameter("email"));
        if (StringUtils.isNotBlank(request.getParameter("fullname")))
            userRegistrationDTO.setFullname(request.getParameter("fullname"));
        if (StringUtils.isNotBlank(request.getParameter("country")))
            userRegistrationDTO.setLocation(request.getParameter("country"));
        if (StringUtils.isNotBlank(request.getParameter("birthday")))
            userRegistrationDTO.setBirthday(LocalDate.parse(request.getParameter("birthday")));
        userRegistrationDTO.setGender(Gender.fromString(request.getParameter("gender")));
        return userRegistrationDTO;
    }

}
