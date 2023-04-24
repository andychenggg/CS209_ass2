package SelfDefineExceptions;

/**
 * This is a class indicate that the information is illegal when you're setting something.
 */
public class IllegalFormatException extends Exception{
    private final String message;

    public IllegalFormatException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
