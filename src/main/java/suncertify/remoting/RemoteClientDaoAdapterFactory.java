package suncertify.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import suncertify.dao.Dao;
import suncertify.db.DatabaseException;


/**
 * Static factory which provides proxied <code>Dao</code> instances based
 * on <code>RemoteDao</code>s.
 * 
 * @author Lars Hvile
 */
final class RemoteClientDaoAdapterFactory {
    
    /*
     * Private constructor.
     */
    private RemoteClientDaoAdapterFactory() {
    }
    
    
    /**
     * Returns a new <code>Dao</code>-adapter for a <code>RemoteDao</code>.
     * 
     * @param dao the <code>RemoteDao</code> to a adapt
     * @return a <code>Dao</code>
     */
    public static Dao adapt(RemoteDao dao) {
        return (Dao) Proxy.newProxyInstance(
                                Dao.class.getClassLoader(),
                                new Class[] {Dao.class},
                                new TargetInvocationHandler(dao));
    }
    
    
    /*
     * InvocationHandler that forwards all invocations to a RemoteDao and
     * converts any RemoteException to a DatabaseException.
     */
    private static final class TargetInvocationHandler implements
            InvocationHandler {
        
        private final RemoteDao target;
        
        public TargetInvocationHandler(RemoteDao target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                final Method m = RemoteDao.class.getMethod(method.getName(),
                        method.getParameterTypes());
                return m.invoke(target, args);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RemoteException) {
                    throw new DatabaseException(e);
                } else {
                    throw e.getCause();
                }
            }
        }
    }
}
