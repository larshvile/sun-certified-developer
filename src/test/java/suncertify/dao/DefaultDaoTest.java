package suncertify.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import suncertify.db.RecordNotFoundException;
import test.common.AbstractDataTest;


@RunWith(JUnit4.class)
public class DefaultDaoTest extends AbstractDataTest {
    
    private Dao dao;
    
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = new DefaultDao(db);
    }
    
    
    @Test
    public void find_record() throws Exception {
        assertEquals(3,
                dao.find(new String[] {"Hamner & Tong",
                "Whoville",
                "Roofing, Carpets, Electrical",
                "9",
                "$90.00",
                ""})[0].getRecNo());
    }
    
    
    @Test
    public void test_locking() throws Exception {
        assertNotNull(dao.lock(0));
        try {
            dao.lock(0);
            fail();
        } catch (RecordAlreadyLockedException e) {
        }
        dao.unlock(0);
        dao.lock(0);
        try {
            dao.lock(-1);
            fail();
        } catch (RecordNotFoundException e) {
        }
    }
    
    
    @Test
    public void test_update() throws Exception {        
        Record r = new Record(0, new String[] { "a", "b", "c", "d", "e", "f"});
        dao.lock(0);
        dao.update(r);
        dao.unlock(0);
        assertEquals(0, dao.find(new String[] { "a", "b", "c", "d", "e", "f"})[0].getRecNo());
   }
}
