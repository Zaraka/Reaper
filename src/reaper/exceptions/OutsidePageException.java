package reaper.exceptions;

/**
 * Should be trowed when scanning document is outside scanning area
 * @author zaraka
 */
public class OutsidePageException extends Exception{
    public OutsidePageException(String message){
        super(message);
    }
}