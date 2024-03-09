package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration representing different genders.
 */
public enum Gender {
    MALE(1),
    FEMALE(2),
    NON_BINARY(3),
    UNKNOWN(4);
    private final int code;

    /**
     * Constructor for Gender enum.
     * @param code The code associated with the enum value.
     */
    Gender(int code){
        this.code=code;
    }

    /**
     * Returns a string representation of the enum value.
     * @return A string representation of the enum value.
     */
    public String toString() {
        String enumName = name().toLowerCase();
        if (enumName.equals("unknown")) {
            return Constants.NULL_GENDER;
        } else if (enumName.equals("non_binary")) {
            return "Non Binary";
        } else {
            return Character.toUpperCase(enumName.charAt(0)) + enumName.substring(1);
        }
    }

    /**
     * Returns the Gender enum value corresponding to the given string value.
     * @param value The string value to convert to Gender enum.
     * @return The Gender enum value corresponding to the given string value.
     * @throws IllegalArgumentException if no enum constant is found for the given string value.
     */
    public static Gender fromString(String value) {
        if (StringUtils.isBlank(value) || value.equals(Constants.NULL_GENDER))
            return UNKNOWN;
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + value);
    }


    /**
     * Returns the code associated with the enum value.
     * @return The code associated with the enum value.
     */
    public int getCode() {
        return code;
    }
}
