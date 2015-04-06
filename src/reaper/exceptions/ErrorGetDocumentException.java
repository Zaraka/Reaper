package reaper.exceptions;

/**
 * Should be throwed when retrieving resource is not in database
 * @author nikita.vanku
 */
public class ErrorGetDocumentException extends Exception{
    public ErrorGetDocumentException(String message){
        super(message);
    }
}
