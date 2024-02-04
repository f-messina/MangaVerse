package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;
import org.apache.commons.lang3.StringUtils;

public enum Gender {
    MALE(1),
    FEMALE(2),
    NON_BINARY(3),
    UNKNOWN(4);
    private final int code;
    Gender(int code){
        this.code=code;
    }

    public static boolean isValidGender(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }


    public String toString() {
        String enumName = name().toLowerCase();
        if (enumName.equals("unknown")) {
            return Constants.NULL_STRING;
        } else if (enumName.equals("non_binary")) {
            return "Non_Binary";
        } else {
            return Character.toUpperCase(enumName.charAt(0)) + enumName.substring(1);
        }
    }

    public static Gender fromString(String value) {
        if (StringUtils.isEmpty(value))
            return UNKNOWN;
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + value);
    }


    public int getCode() {
        return code;
    }
}