package suncertify.dao;

import suncertify.db.DatabaseField;
import suncertify.db.ExtendedDBMain;
import suncertify.db.RecordNotFoundException;


/**
 * Default implementation of <code>Dao</code>.
 * 
 * @author Lars Hvile
 */
public final class DefaultDao implements Dao {
    
    private final ExtendedDBMain database;
    
    
    /**
     * Class-constructor.
     * 
     * @param database a <code>ExtendedDBMain</code>
     */
    public DefaultDao(ExtendedDBMain database) {
        this.database = database;
    }
    
    
    @Override
    public DatabaseField[] getFields() {
        return database.getFields();
    }
    
    
    @Override
    public Record[] find(String[] criteria) throws RecordNotFoundException {
        
        final int[]    results = database.find(criteria);
        final Record[] records = new Record[results.length];
        
        for (int i = 0; i < results.length; i++) {
            records[i] = new Record(results[i], database.read(results[i]));
        }
        
        return records;
    }
    
    
    @Override
    public Record lock(int recNo) throws RecordNotFoundException,
            RecordAlreadyLockedException {
        
        if (database.isLocked(recNo)) {
            throw new RecordAlreadyLockedException();
        }
        
        database.lock(recNo);
        
        return new Record(recNo, database.read(recNo));
    }
    
    
    @Override
    public void unlock(int recNo) throws RecordNotFoundException {
        database.unlock(recNo);
    }
    
    
    @Override
    public void update(Record record) throws RecordNotFoundException {        
        database.update(record.getRecNo(), record.getFields());
    }
}
