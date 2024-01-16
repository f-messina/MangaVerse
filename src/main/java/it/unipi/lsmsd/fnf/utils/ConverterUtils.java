package it.unipi.lsmsd.fnf.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static java.time.ZoneId.systemDefault;

public class ConverterUtils {

    // Convert Date to LocalDate
    public static LocalDate convertDateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        return instant.atZone(systemDefault()).toLocalDate();
    }

    // Convert LocalDate to Date
    public static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(systemDefault()).toInstant());
    }
}
