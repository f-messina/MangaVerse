package it.unipi.lsmsd.fnf.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static java.time.ZoneId.systemDefault;

public class ConverterUtils {
    public static LocalDate convertDateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        return instant.atZone(systemDefault()).toLocalDate();
    }
}
