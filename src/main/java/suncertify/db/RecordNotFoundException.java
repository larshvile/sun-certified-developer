package suncertify.db;


/**
 * Exception indicating that a record could not be found.
 *
 * @author Lars Hvile
 */
public class RecordNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;


    /**
     * Constructs the exception without a detailed message.
     */
    public RecordNotFoundException() {
        this(null);
    }


    /**
     * Constructs the exception with a detailed message.
     *
     * @param message
     *            a detailed exception-message
     */
    public RecordNotFoundException(String message) {
        this(message, null);
    }


    /**
     * Constructs the exception with a detailed message and a cause.
     *
     * @param message
     *            a detailed exception-message
     * @param cause
     *            the exception's cause
     */
    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
