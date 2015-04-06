package reaper.exceptions;

/**
 * Should be throwed when database connection has been lost
 * @author nikita.vanku
 */
public class DatabaseNotConnectedException extends Exception {

    public DatabaseNotConnectedException(String message) {
        super(message);
    }
}
