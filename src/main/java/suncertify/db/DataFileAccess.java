package suncertify.db;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Logger;


/**
 * Support-class that deals with low-level file-access and database-structure.
 * The main reason for not embedding this code in <code>Data</code> is that they
 * work on a different level of abstraction, <code>Data</code> takes care of
 * higher-level functionality like searching, updating etc. <br/>
 * <br/>
 * This class is not thread-safe, proper synchronization is the client's
 * responsibility.
 *
 * @author Lars Hvile
 */
final class DataFileAccess {
    
    // hardcoded field-indexes required for the type-system, ideally this
    // information should be present in the database-file
    private static final int FIELD_SIZE  = 3;
    private static final int FIELD_RATE  = 4;
    private static final int FIELD_OWNER = 5;

    // magic-value used to identify a database-file
    private static final int MAGIC = 513;

    // number of bytes in the record-header
    private static final int RECORD_HEADER_SIZE = 1;

    // charset of strings in the database
    private static final Charset CHARSET = Charset.forName("US-ASCII");

    private final Logger           logger = Logger.getLogger(
            DataFileAccess.class.getName());

    private final RandomAccessFile dbFile;
    private final int              recordLen;   // number of bytes / record
    private final long             recordStart; // pos of 1st byte in 1st record
    private final DatabaseField[]  fields;
    private volatile int           numActiveRecords;


    /**
     * Factory-method that creates a <code>DataFileAccess</code> instance
     * based on a <code>File</code>.
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
    public static DataFileAccess create(File databaseFile)
            throws FileNotFoundException, IOException {

        final RandomAccessFile dbFile = openDatabaseFile(databaseFile);

        verifyDbHeader(dbFile);

        final int recordLen          = extractRecordLength(dbFile);
        final DatabaseField[] fields = extractFields(dbFile);
        final long recordStart       = dbFile.getFilePointer();

        return new DataFileAccess(dbFile, recordLen, recordStart, fields);
    }


    private static RandomAccessFile openDatabaseFile(File databaseFile)
            throws FileNotFoundException {
        if (databaseFile.exists()) {
            return new RandomAccessFile(databaseFile, "rws");
        } else {
            throw new FileNotFoundException("db-file doesn't exist, "
                    + databaseFile);
        }
    }


    private static void verifyDbHeader(RandomAccessFile dbFile)
            throws IOException {
        if (MAGIC != dbFile.readInt()) {
            throw (new DatabaseException("unknown file-type"));
        }
    }


    private static int extractRecordLength(RandomAccessFile dbFile)
            throws IOException {
        return (RECORD_HEADER_SIZE + dbFile.readInt());
    }


    private static DatabaseField[] extractFields(RandomAccessFile dbFile)
            throws IOException {
        final DatabaseField[] fields = new DatabaseField[dbFile.readShort()];
        for (int iField = 0; iField < fields.length; iField++) {
            fields[iField] = new DatabaseField(iField, readString(dbFile, dbFile
                    .readShort()), dbFile.readShort(), getFieldType(iField));
        }
        return fields;
    }
    
    
    /*
     * Returns a field's type based on it's index, ideally this information
     * should be present in the database-file.
     */
    private static DatabaseField.Type getFieldType(int index) {
        switch (index) {
            case FIELD_SIZE:
            case FIELD_OWNER:
                return DatabaseField.Type.NUMBER;
            case FIELD_RATE:
                return DatabaseField.Type.MONEY;
            default:
                return DatabaseField.Type.TEXT;
        }
    }


    private static String readString(RandomAccessFile dbFile, int length)
            throws IOException {
        final byte[] tmp = new byte[length];
        dbFile.readFully(tmp);
        return new String(tmp, CHARSET).trim();
    }


    private static void writeString(RandomAccessFile dbFile, String str,
            int length) throws IOException {
        byte[] tmp = str.getBytes(CHARSET);
        tmp = Arrays.copyOf(tmp, length); // truncate/pad to the correct length
        dbFile.write(tmp);
    }


    /*
     * Private constructor for the factory-method.
     */
    private DataFileAccess(RandomAccessFile dbFile, int recordLen,
            long recordStart, DatabaseField[] fields) throws IOException {

        this.dbFile = dbFile;
        this.recordLen = recordLen;
        this.recordStart = recordStart;
        this.fields = Arrays.copyOf(fields, fields.length);
        this.numActiveRecords = getNumActiveRecords();

        logger.info("database loaded, record-length: " + recordLen
                + ", fields: " + Arrays.asList(fields));
    }


    private int getNumActiveRecords() throws IOException {
        final int numRecords = getNumRecords();
        int numActive = 0;
        for (int i = 0; i < numRecords; i++) {
            if (!isDeleted(i)) {
                numActive++;
            }
        }
        return numActive;
    }


    /**
     * Closes the database-file after use. Any attempts to use the database
     * after this operation will fail.
     *
     * @throws IOException
     *             if an IO-error occurs while closing
     */
    public void close() throws IOException {
        dbFile.close();
    }


    /**
     * Returns the database's field-definitions. <br/>
     * NOTE: this method can safely be called without any synchronization.
     *
     * @return a <code>DatabaseField[]</code>
     */
    public DatabaseField[] getFields() {
        return Arrays.copyOf(fields, fields.length);
    }


    /**
     * Returns the number of active records in the database. <br/>
     * NOTE: this method can safely be called without any synchronization.
     *
     * @return the number of records in the file
     */
    public int size() {
        return numActiveRecords;
    }


    /**
     * Checks if a record is deleted.
     *
     * @param record
     *            index of the record to check
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index < 0 || index >= size())
     * @throws IOException
     *             on I/O-errors
     */
    public boolean isDeleted(int record) throws IOException {
        seek(record);
        return dbFile.readBoolean();
    }


    /**
     * Deletes a record.
     *
     * @param record
     *            index of the record to check
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index < 0 || index >= size())
     * @throws IOException
     *             on I/O-errors
     */
    public void delete(int record) throws IOException {
        seek(record);
        dbFile.writeBoolean(true);
        numActiveRecords--;
    }


    /**
     * Creates space for a new record in the database, possibly reusing an
     * existing, but deleted record.
     *
     * @return the index of the new record
     * @throws IOException
     *             on I/O-errors
     */
    public int create() throws IOException {

        final int position = getAvailableRecordPosition();

        dbFile.seek(recordStart + ((long) recordLen * position));
        dbFile.writeBoolean(false); // not deleted
        dbFile.write(new byte[recordLen - RECORD_HEADER_SIZE]); // clear

        numActiveRecords++;

        return position;
    }


    /**
     * Reads a record's fields.
     *
     * @param record
     *            index of the record to read
     * @return a <code>String[]</code> with the field-values
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index < 0 || index >= size())
     * @throws IOException
     *             on I/O-errors
     */
    public String[] read(int record) throws IOException {

        seekToContent(record);

        final String[] tmp = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {
            tmp[i] = readString(dbFile, fields[i].getLength());
        }

        return tmp;
    }


    /**
     * Writes the field-values of a record.
     *
     * @param record
     *            index of the record to write
     * @param data
     *            a <code>String[]</code> with the field-values
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index < 0 || index >= size())
     * @throws IllegalArgumentException
     *             if the fields can't be stored in the database record
     * @throws IOException
     *             on I/O-errors
     */
    public void write(int record, String[] data) throws IOException {

        verifyFieldFormat(data);
        seekToContent(record);

        for (int i = 0; i < data.length; i++) {
            writeString(dbFile, data[i], fields[i].getLength());
        }
    }


    /**
     * Verifies that the field-values are valid / approved for storage.
     *
     * @param data
     *            a <code>String[]</code> with the field-values
     * @throws IllegalArgumentException
     *             if the fields can't be stored in the database
     */
    public void verifyFieldFormat(String[] data) {

        if (data.length != this.fields.length) {
            throw new IllegalArgumentException("invalid number of fields");
        }

        for (String f : data) {
            if (null == f) {
                throw new IllegalArgumentException(
                        "field-values can't be null");
            }
        }
    }


    /*
     * Finds the next available position for a record, possibly reusing the
     * storage-slot of a deleted record.
     */
    private int getAvailableRecordPosition() throws IOException {

        int position = size();

        // check if an existing record can be reused
        for (int i = 0; i < size(); i++) {
            if (isDeleted(i)) {
                logger.fine("reusing record #" + i);
                position = i;
                break;
            }
        }

        return position;
    }


    /*
     * Returns the total number of records in the database, included records
     * that are deleted.
     */
    private int getNumRecords() throws IOException {
        return ((int) ((dbFile.length() - recordStart) / recordLen));
    }


    /*
     * Sets the file-pointer at the start of a given record.
     */
    private void seek(int record) throws IOException {

        final int size = getNumRecords();

        if ((record < 0) || (record >= size)) {
            throw new IndexOutOfBoundsException("index out-of-bounds, "
                    + record + ", size=" + size);
        }

        dbFile.seek(recordStart + ((long) recordLen * record));
    }


    /*
     * Sets the file-pointer at the start of a records content (skipping the
     * header-data).
     */
    private void seekToContent(int record) throws IOException {
        seek(record);
        dbFile.seek(dbFile.getFilePointer() + RECORD_HEADER_SIZE);
    }
}
