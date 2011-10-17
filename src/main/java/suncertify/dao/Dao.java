package suncertify.dao;

import suncertify.db.DBMain;
import suncertify.db.DatabaseException;
import suncertify.db.DatabaseField;
import suncertify.db.RecordNotFoundException;


/**
 * Defines a high-level data-access-object interface for the database.
 * 
 * @author Lars Hvile
 */
public interface Dao {
    /*
     * This high-level interface is better suited for over the wire transport
     * and hides some of the low-level concepts of the DBMain interface, which
     * shouldn't be exposed to the rest of the application.
     */
    
    /**
     * Returns the database's field-definitions.
     *
     * @return a <code>DatabaseField[]</code>
     */
    DatabaseField[] getFields();
    
    
    /**
     * Searches the database for records matching the specified criteria.
     * 
     * @param criteria
     *            a <code>String[]</code> with the criteria to search for
     * @return a <code>Record[]</code> with the results
     * @throws RecordNotFoundException
     *             if no matching records are found
     * @throws IllegalArgumentException
     *             if <code>criteria.length</code> doesn't match the number of
     *             db-fields
     * @throws DatabaseException
     *             on errors while searching for / reading the records
     * @see DBMain#find(String[])
     */
    Record[] find(String[] criteria) throws RecordNotFoundException;
    
    
    /**
     * Locks a record. This method may or may not block depending on timing-
     * issues. If the record is known to be locked, a
     * RecordAlreadyLockedException will be thrown.
     * 
     * @param recNo
     *            id of the record to lock
     * @return a <code>Record</code> containing the current contents of the
     *         record
     * @throws RecordNotFoundException
     *             if the record doesn't exist
     * @throws RecordAlreadyLockedException
     *             if the record has already been locked
     * @throws DatabaseException
     *             on errors during locking/reading the record
     * @see DBMain#isLocked(int)
     * @see DBMain#lock(int)
     */
    Record lock(int recNo) throws RecordNotFoundException,
            RecordAlreadyLockedException;
    
    
    /**
     * Unlocks a record.
     * 
     * @param recNo id of the record to unlock
     * @throws RecordNotFoundException if the record doesn't exist
     * @throws DatabaseException on errors during unlocking
     * @see DBMain#unlock(int)
     */
    void unlock(int recNo) throws RecordNotFoundException;

    
    /**
     * Updates a record. Calling this method on a record that hasn't been locked
     * yields undefined results.
     * 
     * @param record
     *            the <code>Record</code> to update
     * @throws RecordNotFoundException
     *             if the record doesn't exist
     * @throws IllegalArgumentException
     *             if <code>record</code> doesn't contain the correct number of
     *             fields, or if some of the fields are <code>null</code>
     * @throws DatabaseException
     *             if unable to update the record
     * @see DBMain#update(int, String[])
     */
    void update(Record record) throws RecordNotFoundException;

}
