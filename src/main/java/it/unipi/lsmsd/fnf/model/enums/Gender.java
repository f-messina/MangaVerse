package it.unipi.lsmsd.fnf.model.enums;

public enum Gender {
    MALE(1),
    FEMALE(2),
    NOT_BINARY(3),
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
        if (name().equals("UNKNOWN"))
            return "Prefer not to answer";
        else if (name().equals("NOT_BINARY"))
            return "Not Binary";
        else {
            String enumName = name().toLowerCase();
            return Character.toUpperCase(enumName.charAt(0)) + enumName.substring(1);
        }
    }
    public static Gender fromString(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        // If the input string doesn't match any enum value, you may throw an exception or return a default value.
        throw new IllegalArgumentException("No enum constant for string: " + value);
    }
    public int getCode() {
        return code;
    }

}

