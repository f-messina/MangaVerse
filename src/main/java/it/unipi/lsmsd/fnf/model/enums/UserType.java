package it.unipi.lsmsd.fnf.model.enums;

public enum UserType {
    MANAGER(1),
    USER(2);
    int code;
    UserType(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
