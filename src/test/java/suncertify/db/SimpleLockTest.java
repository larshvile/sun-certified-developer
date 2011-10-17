package suncertify.db;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class SimpleLockTest {
    
    private Map<Long, Integer> lockedBy = new LinkedHashMap<Long, Integer>();
    private SimpleLock         lock     = new SimpleLock();
    
    
    private final class LockThread extends Thread {
        
        private final int id;
        private final CountDownLatch startLatch;
        private final CountDownLatch completeLatch;
        

        public LockThread(int id, CountDownLatch startLatch,
                CountDownLatch completeLatch) {
            super("LockThread#" + id);
            this.id = id;
            this.startLatch = startLatch;
            this.completeLatch = completeLatch;
        }
        
        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            lock.lock();
            System.out.println(getName() + " > locked");
            try {
                assertTrue(lock.isLocked());
                lockedBy.put(System.currentTimeMillis(), id);
                Thread.sleep(10);
            } catch (Exception e) {
                fail(e.getMessage());
            } finally {
                System.out.println(getName() + " < locked");
                lock.unlock();
                completeLatch.countDown();
            }
        }
    }
    
    
    @Test
    public void stress_test() throws Exception {
        
        assertFalse(lock.isLocked());
        
        final int numThreads          = 500;
        final CountDownLatch start    = new CountDownLatch(1);
        final CountDownLatch complete = new CountDownLatch(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            new LockThread(i, start, complete).start();
        }
        
        start.countDown();
        complete.await();
        
        // verify lock-state
        assertFalse(lock.isLocked());
                
        // verify lock-timing
        Long lastLock = null;
        for (Long time : lockedBy.keySet()) {
            if (lastLock != null) {
                assertTrue(Math.abs(time - lastLock) > 9);  // make sure it's ~10ms between each lock
            }
            lastLock = time;            
        }
        
        // make sure all threads lock, and uniquely
        assertEquals(numThreads, lockedBy.size());
        Set<Integer> locks = new HashSet<Integer>(lockedBy.values());
        assertEquals(numThreads, locks.size());
    }
    
    
    @Test(expected=IllegalStateException.class)
    public void unlock_clean_lock() {
        lock.unlock();
    }
}
