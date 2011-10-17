package suncertify.remoting;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import suncertify.dao.Dao;


/**
 * Provides static functions for binding & looking up objects in an RMI-
 * registry.
 * 
 * @author Lars Hvile
 */
public final class RegistryHelper {
    
    private static final String REMOTE_DAO_NAME = "Dao";
    
    
    /*
     * Private constructor.
     */
    private RegistryHelper() {
    }


    /**
     * Creates a new RMI-registry at the default port and exports a
     * <code>Dao</code> instance. The <code>Dao</code> will be wrapped in an
     * adapter used for RMI as well as extended with extra logging-facilities.
     * 
     * @param dao a <code>Dao</code>
     * @param port the port to expose the object on
     * @throws RemoteException
     * @throws AccessException
     */
    public static void put(Dao dao, int port) throws RemoteException,
            AccessException {
        
        final Dao instrumentedDao = InstrumentedServerDaoFactory
                .instrument(dao);

        final RemoteDao remoteDao = RemoteServerDaoAdapterFactory
                .adapt(instrumentedDao);

        final Registry registry = LocateRegistry
                .createRegistry(Registry.REGISTRY_PORT);
        
        final RemoteDao stub = (RemoteDao) UnicastRemoteObject.exportObject(
                remoteDao, port);

        registry.rebind(REMOTE_DAO_NAME, stub);
    }
    
    
    /**
     * Connects to the RMI-registry at a given host and returns the reference
     * to a <code>Dao</code>.
     * 
     * @param host host for the remote registry
     * @return a <code>Dao</code>
     * @throws RemoteException
     * @throws NotBoundException
     * @throws AccessException
     */
    public static Dao get(String host) throws RemoteException,
            NotBoundException, AccessException {
        
        final RemoteDao dao = (RemoteDao) LocateRegistry.getRegistry(host)
                .lookup(REMOTE_DAO_NAME);
        
        return RemoteClientDaoAdapterFactory.adapt(dao);
    }
}
