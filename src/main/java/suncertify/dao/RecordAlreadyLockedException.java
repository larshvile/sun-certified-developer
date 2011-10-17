package suncertify.dao;

/**
 * Exception used to signal that a record has been locked by another user.
 * 
 * @author Lars Hvile
 */
public class RecordAlreadyLockedException extends Exception {
    private static final long serialVersionUID = 1L;
}
