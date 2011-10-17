package suncertify.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.dao.Dao;


/**
 * Static factory which provides instrumented Dao's using the decorator pattern.
 * The instrumented targets adds logging capabilities to the <code>Dao</code>
 * interface which is only useful in an RMI-environment.
 * 
 * @author Lars Hvile
 */
final class InstrumentedServerDaoFactory {
    
    private static final Logger logger = Logger.getLogger(
            "InstrumentedServerDao");
    
    
    /*
     * Private constructor.
     */
    private InstrumentedServerDaoFactory() {
    }
    
    
    /**
     * Returns a decorated <code>Dao</code> instance with server-related
     * logging.
     * 
     * @param dao a <code>Dao</code> to instrument
     * @return a <code>Dao</code>
     */
    public static Dao instrument(Dao dao) {
        return (Dao) Proxy.newProxyInstance(
                            Dao.class.getClassLoader(),
                            new Class[] {Dao.class},
                            new InstrumentedDaoInvocationHandler(dao));
    }
    
    
    /*
     * InvocationHandler that adds the extra logging etc.
     */
    private static final class InstrumentedDaoInvocationHandler implements
            InvocationHandler {
        
        private final Dao target;
        
        public InstrumentedDaoInvocationHandler(Dao target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            final long   timer  = System.currentTimeMillis();
            final String client = UnicastRemoteObject.getClientHost();
            final String methodName = ("'" + method.getName() + "'");
            
            logger.info("invoking " + methodName + " from " + client);
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                logger.log(Level.INFO, "exception while invoking " + methodName
                        + " from " + client, e.getCause());
                throw e.getCause();
            } finally {
                logger.info(methodName + " from " + client + " completed in "
                        + (System.currentTimeMillis() - timer) + "ms");
            }
        }
    }
}
