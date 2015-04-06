package reaper.exceptions;

/**
 * Should be throwed when creating link is not valid
 * @author nikita.vanku
 */
public class InvalidLinkException extends Exception{
    public InvalidLinkException(String message){
        super(message);
    }
}
