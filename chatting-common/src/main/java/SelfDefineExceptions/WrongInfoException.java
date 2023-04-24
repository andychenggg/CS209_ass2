package SelfDefineExceptions;

/**
 * This is a class indicate that the information is wrong.
 * The field implies which information is wrong.
 */
public class WrongInfoException extends Exception {

    private final String message;

    public WrongInfoException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
