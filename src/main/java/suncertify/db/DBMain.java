package suncertify.db;


/**
 * Represents a database where records are identified by an integer-id,
 * and have fields represented as Strings. Records are also assigned
 * a key that is used to prevent duplicate entries. <br />
 * <br />
 * All methods that read/modify data from the database can throw the unchecked
 * <code>DatabaseException</code> to signal implementation- specific exceptions.
 *
 * @author Lars Hvile
 */
public interface DBMain {

    /**
     * Reads a record from the database.
     *
     * @param recNo
     *            index of the record to read
     * @return a <code>String[]</code> with the record-values
     * @throws RecordNotFoundException
     *             if the specified record doesn't exist
     * @throws DatabaseException
     *             if unable to read the record
     */
    String[] read(int recNo) throws RecordNotFoundException;


    /**
     * Modifies the fields of a record. The new value for field n appears in
     * data[n].
     *
     * @param recNo
     *            id of the record to modify
     * @param data
     *            a <code>String[]</code> with the new record-values
     * @throws RecordNotFoundException
     *             if the specified record doesn't exist
     * @throws IllegalArgumentException
     *             if <code>data</code> doesn't contain the correct number of
     *             fields, or if some of the fields are <code>null</code>
     * @throws DatabaseException
     *             if not able to update the record
     */
    void update(int recNo, String[] data) throws RecordNotFoundException;


    /**
     * Deletes a record.
     *
     * @param recNo
     *            id of the record to delete
     * @throws RecordNotFoundException
     *             if the specified record doesn't exist
     * @throws DatabaseException
     *             if not able to delete the record
     */
    void delete(int recNo) throws RecordNotFoundException;


    /**
     * Searches the database for record-numbers matching the specified criteria.
     * Field n in the database file is described by criteria[n]. A null value in
     * criteria[n] matches any field value. A non-null value in criteria[n]
     * matches any field value that begins with criteria[n]. (For example,
     * "Fred" matches "Fred" or "Freddy".)
     *
     * @param criteria
     *            a <code>String[]</code> with the criteria to search for
     * @return an <code>int[]</code> with the matching record- numbers
     * @throws RecordNotFoundException
     *             if no matching records are found
     * @throws IllegalArgumentException
     *             if <code>criteria.length</code> doesn't match the number of
     *             db-fields
     * @throws DatabaseException
     *             on errors while searching
     */
    int[] find(String[] criteria) throws RecordNotFoundException;


    /**
     * Creates a new record in the database.
     *
     * @param data
     *            a <code>String[]</code> with the record-values
     * @return the created record's id
     * @throws DuplicateKeyException
     *             if <code>data</code> represents a key that is already in use
     *             in the db
     * @throws IllegalArgumentException
     *             if the fields can't be stored in the database
     * @throws DatabaseException
     *             if unable to create the record
     */
    int create(String[] data) throws DuplicateKeyException;


    /**
     * Locks a record for updating/deleting.
     *
     * @param recNo
     *            id of the record to lock
     * @throws RecordNotFoundException
     *             if the specified record doesn't exist
     * @throws DatabaseException
     *             on errors during record lookup
     */
    void lock(int recNo) throws RecordNotFoundException;


    /**
     * Unlocks a record after modification. Deleted records are automatically
     * unlocked.
     *
     * @param recNo
     *            id of the record to unlock
     * @throws RecordNotFoundException
     *             if the record-id is unknown
     * @throws DatabaseException
     *             on errors during record lookup
     */
    void unlock(int recNo) throws RecordNotFoundException;


    /**
     * Checks if a record is locked.
     *
     * @param recNo
     *            id of a record
     * @return true if the record is locked, false otherwise
     * @throws RecordNotFoundException
     *             if the specified record doesn't exist
     * @throws DatabaseException
     *             on errors during record lookup
     */
    boolean isLocked(int recNo) throws RecordNotFoundException;

}
