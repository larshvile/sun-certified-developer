package test.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;

import junit.framework.TestCase;

/**
 * Abstract superclass for tests that use a database-file, provides a clean
 * db-file for each test.
 * 
 * @author	Lars Hvile
 */
public abstract class AbstractFileDbTest extends TestCase {
    
    protected File dbFile;
    
    

    @Override
    @Before
    public void setUp() throws Exception {
        
        dbFile = File.createTempFile("temp-db-file", null);
        
        // copy it
        {
            InputStream in = null;
            OutputStream out = null;
            
            try {
                in = this.getClass().getClassLoader().getResourceAsStream("database.db");
                out = new FileOutputStream(dbFile);
                
                while (true) {
                    int b = in.read();
                    if (-1 == b) {
                        break;
                    }
                    out.write(b);
                }
            }
            finally {
                try { if (null != in) { in.close(); }   } catch (IOException e) {}
                try { if (null != out) { out.close(); } } catch (IOException e) {}
            }
        }
    }   // setUp()

    @Override
    @After
    public void tearDown() throws Exception {
        if (!dbFile.delete()) {
            dbFile.deleteOnExit();
        }
    }

}   // AbstractFileDbTest
