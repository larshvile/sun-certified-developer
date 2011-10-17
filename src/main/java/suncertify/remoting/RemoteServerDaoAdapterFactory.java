package suncertify.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import suncertify.dao.Dao;


/**
 * Static factory which provides proxied <code>RemoteDao</code> instances based
 * on <code>Dao</code>s that can be exported using RMI.
 * 
 * @author Lars Hvile
 */
final class RemoteServerDaoAdapterFactory {
    
    /*
     * Class-constructor.
     */
    private RemoteServerDaoAdapterFactory() {
    }
    
    
    /**
     * Returns a new <code>RemoteDao</code>-adapter for a <code>Dao</code>.
     * 
     * @param dao the <code>Dao</code> to a adapt
     * @return a <code>RemoteDao</code>
     */
    public static RemoteDao adapt(Dao dao) {        
        return (RemoteDao) Proxy.newProxyInstance(
                                RemoteDao.class.getClassLoader(),
                                new Class[] {RemoteDao.class},
                                new TargetInvocationHandler(dao));
    }
    
    
    /*
     * InvocationHandler that forwards all invocations to a Dao.
     */
    private static final class TargetInvocationHandler implements
            InvocationHandler {
        
        private final Dao target;
        
        public TargetInvocationHandler(Dao target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            final Method m = Dao.class.getMethod(method.getName(), method
                    .getParameterTypes());
            try {
                return m.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
