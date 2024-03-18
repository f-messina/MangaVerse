package it.unipi.lsmsd.fnf.dao.exception;

public class DuplicatedException extends Exception{
    private final DuplicatedExceptionType type;
    /**
     * Constructs a new DuplicatedException with a specified message.
     *
     * @param message The detail message.
     */
    public DuplicatedException(String message){
        super(message);
        this.type = DuplicatedExceptionType.DUPLICATED_KEY;
    }

    /**
     * Constructs a new DuplicatedException with a specified type and message.
     *
     * @param type    The type of DuplicatedException.
     * @param message The detail message.
     */
    public DuplicatedException(DuplicatedExceptionType type, String message){
        super(message);
        this.type = type;
    }

    /**
     * Retrieves the type of DuplicatedException.
     *
     * @return The type of DuplicatedException.
     */
    public DuplicatedExceptionType getType() {
        return type;
    }
}
