package it.unipi.lsmsd.fnf.model.enums;

import it.unipi.lsmsd.fnf.utils.Constants;

/**
 * Enumeration representing different genders.
 */
public enum Gender {
    MALE(1),
    FEMALE(2),
    NON_BINARY(3),
    UNKNOWN(4);
    private final int code;

    Gender(int code){
        this.code = code;
    }
    public int getCode() {
        return code;
    }

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
     * @param value                         The string value to convert to Gender enum.
     * @return                              The Gender enum value corresponding to the given string value.
     * @throws IllegalArgumentException     If no enum constant is found for the given string value.
     */
    public static Gender fromString(String value) {
        return switch (value) {
            case "Non Binary" -> NON_BINARY;
            case "Male" -> MALE;
            case "Female" -> FEMALE;
            case Constants.NULL_GENDER, "", "unknown" -> UNKNOWN;
            case null -> UNKNOWN;
            default -> throw new IllegalArgumentException("No enum constant for string: " + value);
        };
    }
}
