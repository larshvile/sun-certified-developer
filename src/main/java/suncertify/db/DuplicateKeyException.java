package suncertify.db;


/**
 * Exception indicating an attempt to insert/update a duplicate key to the
 * database.
 *
 * @author Lars Hvile
 */
public class DuplicateKeyException extends Exception {

    private static final long serialVersionUID = 1L;


    /**
     * Constructs the exception without a detailed message.
     */
    public DuplicateKeyException() {
        this(null);
    }


    /**
     * Constructs the exception with a detailed message.
     *
     * @param message
     *            a detailed exception-message
     */
    public DuplicateKeyException(String message) {
        super(message);
    }

}
