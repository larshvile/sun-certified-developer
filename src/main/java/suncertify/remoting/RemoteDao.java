package suncertify.remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;

import suncertify.dao.Dao;
import suncertify.dao.Record;
import suncertify.dao.RecordAlreadyLockedException;
import suncertify.db.DatabaseField;
import suncertify.db.RecordNotFoundException;


/**
 * Remote version of the <code>Dao</code> interface.
 * 
 * @author Lars Hvile
 */
interface RemoteDao extends Remote {
    /*
     * The only reason for separating RemoteDao & Dao is that I don't wan't to
     * expose remoting details like RemoteException to the main application.
     * See choices.txt for an explanation.
     */
    
    /**
     * @see Dao#getFields()
     */
    DatabaseField[] getFields() throws RemoteException;
    
    
    /**
     * @see Dao#find(String[])
     */
    Record[] find(String[] criteria) throws RecordNotFoundException,
            RemoteException;
    
    
    /**
     * @see Dao#lock(int) 
     */
    Record lock(int recNo) throws RecordNotFoundException,
            RecordAlreadyLockedException, RemoteException;
    
    
    /**
     * @see Dao#unlock(int)
     */
    void unlock(int recNo) throws RecordNotFoundException, RemoteException;
    
    
    /**
     * @see Dao#update(Record)
     */
    void update(Record record) throws RecordNotFoundException, RemoteException;

}
