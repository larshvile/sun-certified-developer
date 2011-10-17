package suncertify.db;


/**
 * A simple lock, similar to <code>ReentrantLock</code>, but without the concept
 * of a lock-owner. This means that any thread can unlock
 * <code>SimpleLock</code>s locked by other threads. Another consequence of this
 * is that the same thread can't acquire the same lock twice without
 * deadlocking.
 * 
 * @author Lars Hvile
 */
public final class SimpleLock {
    
    private final Object     monitor = new Object();
    private volatile boolean locked;
    
    
    /**
     * Returns the current state of the lock.
     * 
     * @return <code>true</code> if locked
     */
    public boolean isLocked() {
        return locked;
    }


    /**
     * Acquires the lock. Returns immediately if the lock is not already
     * acquired. If the lock is then the current thread becomes disabled
     * until the lock has been acquired.
     */
    public void lock() {
        synchronized (monitor) {
            while (isLocked()) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {}
            }
            locked = true;
        }
    }
    
    
    /**
     * Releases the lock.
     * 
     * @throws IllegalStateException if the lock isn't locked
     */
    public void unlock() {
        synchronized (monitor) {
            if (!isLocked()) {
                throw new IllegalStateException(
                        "attempting to unlock an already unlocked lock");
            }
            locked = false;
            monitor.notifyAll();
        }
    }
}
