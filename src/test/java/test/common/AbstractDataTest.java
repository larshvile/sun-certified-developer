package test.common;

import org.junit.After;
import org.junit.Before;

import suncertify.db.Data;


/**
 * Abstract superclass for tests using Data as a DBMain implementation.
 * 
 * @author	Lars Hvile
 */
public abstract class AbstractDataTest extends AbstractFileDbTest {
    
    protected Data db;
    
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        db = new Data(dbFile);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (null != db) {
            ((Data)db).close();
        }
        super.tearDown();
    }    

}
