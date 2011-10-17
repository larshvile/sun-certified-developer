package suncertify.db;


/**
 * Runtime-exception used to signal database-exceptions that are
 * implementation-specific and therefore can't be part of the DBMain interface.
 * IOException's for example are converted to DatabaseException's before
 * reaching the client-code.
 *
 * @author Lars Hvile
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    /**
     * Constructs the exception without a detailed message.
     */
    public DatabaseException() {
        this(null, null);
    }


    /**
     * Constructs the exception with a detailed message.
     *
     * @param message
     *            a detailed exception-message
     */
    public DatabaseException(String message) {
        this(message, null);
    }


    /**
     * Constructs the exception with a cause.
     *
     * @param cause
     *            a detailed exception-message
     */
    public DatabaseException(Throwable cause) {
        this(null, cause);
    }


    /**
     * Constructs the exception with a detailed message, and a cause for the
     * exception.
     *
     * @param message
     *            a detailed exception-message
     * @param cause
     *            the exception's cause
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
