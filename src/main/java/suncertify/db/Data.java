package suncertify.db;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Implementation of <code>DBMain</code> used for a local flat-file database.
 * Each record will be assigned a key based on the combined value of the fields
 * name and location.
 *
 * @author Lars Hvile
 */
public final class Data implements ExtendedDBMain {

    private static final String FIELD_NAME     = "name";
    private static final String FIELD_LOCATION = "location";

    // lock used to secure all internal state, e.g. access to the db-file or
    // modification of the record-lock map
    private final Object stateLock = new Object();

    // map containing locks for individual database-records
    private final Map<Integer, SimpleLock> recordLocks
            = new HashMap<Integer, SimpleLock>();

    private final Logger logger = Logger.getLogger(Data.class.getName());

    private final DataFileAccess dbAccess;
    private final File           dbFile;
    private final int            nameIndex;
    private final int            locationIndex;


    /**
     * Opens an existing database.
     *
     * @param databaseFile
     *            a <code>File</code> used as the database -source.
     * @throws FileNotFoundException
     *             if the file doesn't exist, or isn't writable
     * @throws DatabaseException
     *             if the file isn't recognized as a valid database-file
     * @throws IOException
     *             on I/O-errors
     */
    public Data(File databaseFile) throws FileNotFoundException, IOException {

        logger.config("using database-file: " + databaseFile);

        this.dbAccess   = DataFileAccess.create(databaseFile);
        this.dbFile     = databaseFile;
        
        // extract the field-index of the name-/location-fields which
        // are used as a composite-key
        {
            nameIndex = getFieldIndex(FIELD_NAME);
            locationIndex = getFieldIndex(FIELD_LOCATION);

            if ((-1 == nameIndex) || (-1 == locationIndex)) {
                throw new DatabaseException("invalid db-file");
            }
        }
    }
    
    
    private int getFieldIndex(String name) {
        final DatabaseField[] fields = dbAccess.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Closes the database-file after use. The only way to re-open the database
     * after this operation is to create a new instance of <code>Data</code>
     *
     * @throws IOException
     *             if an IO-error occurs while closing
     */
    public void close() throws IOException {
        logger.info("closing database-file");
        synchronized (stateLock) {
            dbAccess.close();
        }
    }


    @Override
    public String toString() {
        return ("Data, " + dbFile + ", numRecords: " + dbAccess.size());
    }


    @Override
    public DatabaseField[] getFields() {
        return dbAccess.getFields();
    }


    /**
     * Returns the number of active (not deleted) records in the database.
     *
     * @return the number of active records
     */
    public int size() {
        return dbAccess.size();
    }


    @Override
    public int create(String[] data) throws DuplicateKeyException {
        synchronized (stateLock) {
            dbAccess.verifyFieldFormat(data);
            verifyUniqueKey(data);
            return createAndWriteRecord(data);
        }
    }


    @Override
    public String[] read(int recNo) throws RecordNotFoundException {
        try {
            synchronized (stateLock) {
                verifyActiveRecord(recNo);
                return dbAccess.read(recNo);
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }


    @Override
    public void update(int recNo, String[] data)
            throws RecordNotFoundException {
        try {
            synchronized (stateLock) {
                verifyActiveRecord(recNo);
                verifyUniqueKeyExcept(data, recNo);
                
                assert isLocked(recNo);
                
                dbAccess.write(recNo, data);
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException( // NOTE, documented in choices.txt
                    "modification of record caused a key-integrity error", e);
        }
    }


    @Override
    public void delete(int recNo) throws RecordNotFoundException {
        try {
            synchronized (stateLock) {
                verifyActiveRecord(recNo);
                
                assert isLocked(recNo);
                
                dbAccess.delete(recNo);
                removeLockForDeletedRecord(recNo);
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }


    /*
     * Removes & unlocks a record-lock when a record is being deleted. See
     * choices.txt for an explanation..
     */
    private void removeLockForDeletedRecord(int recNo) {
        final SimpleLock lock = recordLocks.get(recNo);
        lock.unlock();
        recordLocks.remove(recNo);
    }


    @Override
    public int[] find(String[] criteria) throws RecordNotFoundException {

        if (criteria.length != dbAccess.getFields().length) {
            throw new IllegalArgumentException("invalid number of fields");
        }

        synchronized (stateLock) {
            final int[] result = doFind(criteria);

            if (0 == result.length) {
                throw new RecordNotFoundException();
            } else {
                return result;
            }
        }
    }


    private int[] doFind(String[] criteria) {
        try {
            final int   numRecords = dbAccess.size();
            final int[] result     = new int[numRecords];
            int         numMatches = 0;

            for (int iRecord = 0; iRecord < numRecords; iRecord++) {
                if (dbAccess.isDeleted(iRecord)) {
                    continue;
                } else if (isMatch(criteria, dbAccess.read(iRecord))) {
                    result[numMatches++] = iRecord;
                }
            }

            return Arrays.copyOfRange(result, 0, numMatches);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }


    private boolean isMatch(String[] criteria, String[] record) {
        for (int iCol = 0; iCol < record.length; iCol++) {
            if (null == criteria[iCol]) {
                continue;
            } else if (!record[iCol].toLowerCase().startsWith(
                    criteria[iCol].toLowerCase())) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean isLocked(int recNo) throws RecordNotFoundException {
        synchronized (stateLock) {
            verifyActiveRecord(recNo);
            final SimpleLock recordLock = recordLocks.get(recNo);            
            return ((null != recordLock) && recordLock.isLocked());
        }
    }
    
    
    @Override
    public void lock(int recNo) throws RecordNotFoundException {

        SimpleLock recordLock;

        synchronized (stateLock) {
            verifyActiveRecord(recNo);
            if (null == recordLocks.get(recNo)) {
                recordLocks.put(recNo, new SimpleLock());
            }
            recordLock = recordLocks.get(recNo);
        }

        recordLock.lock(); // it's extremely important to acquire this lock
                           // outside of the synchronized block above, if not
                           // there will be deadlocks..
    }


    @Override
    public void unlock(int recNo) throws RecordNotFoundException {
        synchronized (stateLock) {
            verifyActiveRecord(recNo);
            
            assert isLocked(recNo);
            
            final SimpleLock recordLock = recordLocks.get(recNo);
            recordLock.unlock();
            // NOTE, don't remove the lock here, potential race-condition
            // in the recordLock.lock() line in the method above..
        }
    }
    
    
    /*
     * Verifies that the records's key is unique within the database.
     */
    private void verifyUniqueKey(String[] data) throws DuplicateKeyException {
        verifyUniqueKeyExcept(data, -1);
    }


    /*
     * Verifies that the records's key is unique within the database, with the
     * exception of a given record-index.
     */
    private void verifyUniqueKeyExcept(String[] data, int except)
            throws DuplicateKeyException {
        try {
            final String key = generateKey(data);
            final int numRecords = dbAccess.size();

            for (int i = 0; i < numRecords; i++) {
                if (dbAccess.isDeleted(i) || (i == except)) {
                    continue;
                }

                if (key.equals(generateKey(dbAccess.read(i)))) {
                    throw new DuplicateKeyException("record #" + i
                            + " already has key: " + key);
                }
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }


    private String generateKey(String[] data) {
        return (data[nameIndex] + "_" + data[locationIndex]);
    }


    /*
     * Checks that a record-index is valid, and that the record isn't deleted.
     */
    private void verifyActiveRecord(int recNo) throws RecordNotFoundException {
        try {
            if (dbAccess.isDeleted(recNo)) {
                throw new IndexOutOfBoundsException();
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        } catch (IndexOutOfBoundsException e) {
            throw new RecordNotFoundException("record #" + recNo
                    + " doesn't exist", e);
        }
    }


    private int createAndWriteRecord(String[] data) {
        try {
            final int recordIndex = dbAccess.create();
            dbAccess.write(recordIndex, data);
            return recordIndex;
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }
}
