package suncertify.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import test.common.AbstractFileDbTest;


@RunWith(JUnit4.class)
public class DataFileAccessTest extends AbstractFileDbTest {
    
    private DataFileAccess access;
        
    
    /**
     * test-setup
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        access = DataFileAccess.create(dbFile);
    }

    
    /**
     * test-cleanup
     */
    @Override
    @After
    public void tearDown() throws Exception {
        if (null != access) {
            access.close();
        }
        super.tearDown();
    }
    
    
    /**
     * verify FileNotFoundException if the database-file doesn't exist
     */
    @Test(expected=FileNotFoundException.class)
    public void db_file_not_found() throws Exception {
        DataFileAccess.create(new File("I-do-not-exist"));
    }
    
    
    /**
     * verify FileNotFoundException if the file isn't writable
     */
    @Test
    public void db_file_not_writable() throws Exception {
        
        final File tmpFile = File.createTempFile("read-only-db", null);        
        assertTrue(tmpFile.exists());
        tmpFile.setWritable(false);
        
        try {
            DataFileAccess.create(tmpFile);
            fail();
        } catch (FileNotFoundException e) {}
        finally {
            if (!tmpFile.delete()) {
                tmpFile.deleteOnExit();
            }
        }
    }
    
    
    /**
     * verify getFields()
     */
    @Test
    public void field_data() throws Exception {
        
        final DatabaseField[] fields = access.getFields();
        
        assertEquals(6, fields.length);
        
        assertEquals(fields[0].getName(), "name");
        assertEquals(fields[1].getName(), "location");
        assertEquals(fields[2].getName(), "specialties");
        assertEquals(fields[3].getName(), "size");
        assertEquals(fields[4].getName(), "rate");
        assertEquals(fields[5].getName(), "owner");
        
        assertEquals(fields[0].getLength(), 32);
        assertEquals(fields[1].getLength(), 64);
        assertEquals(fields[2].getLength(), 64);
        assertEquals(fields[3].getLength(), 6);
        assertEquals(fields[4].getLength(), 8);
        assertEquals(fields[5].getLength(), 8);
        
        assertEquals(fields[0].getType(), DatabaseField.Type.TEXT);
        assertEquals(fields[1].getType(), DatabaseField.Type.TEXT);
        assertEquals(fields[2].getType(), DatabaseField.Type.TEXT);
        assertEquals(fields[3].getType(), DatabaseField.Type.NUMBER);
        assertEquals(fields[4].getType(), DatabaseField.Type.MONEY);
        assertEquals(fields[5].getType(), DatabaseField.Type.NUMBER);
    }
    
    
    /**
     * verify that size() works
     */
    @Test
    public void size() throws Exception {
        
        assertEquals(29, access.size());
        access.create();
        assertEquals(30, access.size());
        
    }
    
    
    /**
     * verify isDeleted() & deleted()
     */
    @Test
    public void deletion() throws Exception {
        
        assertEquals(29, access.size());
        assertFalse (   access.isDeleted(0) );
        
        access.delete(0);
        
        assertTrue  (   access.isDeleted(0) );
        assertEquals(28, access.size());
        
    }
    
    
    /**
     * Read a record that doesn't exist
     */
    @Test(expected=IndexOutOfBoundsException.class)
    public void read_out_of_bounds() throws Exception {
        access.read(access.size());
    }
    
        
    /**
     * verify that read works as excepted
     */
    @Test
    public void read_existing_record() throws Exception {
        
        final List<String> expected = Arrays.asList(new String[] {
                "Hamner & Tong",
                "Whoville",
                "Roofing, Carpets, Electrical",
                "9",
                "$90.00",
                ""
        });
        
        assertEquals(expected, Arrays.asList(access.read(3)));
        
    }
    
    
    /**
     * Check the default field-values after a create()
     */
    @Test
    public void create_default_field_values() throws Exception {
        
        String[] defaults = access.read(access.create());
        
        for (String d : defaults) {
            assertEquals("", d);
        }
    }
    
    
    /**
     * create a new record, verify that it ends up at eof
     */
    @Test
    public void new_record_at_eof() throws Exception {
        
        final String[]  lastRecord  = access.read(access.size() - 1);
        final int       newRecord   = access.create();
        
        // make sure it's the last record
        assertEquals(access.size() - 1, newRecord);
        
        // make sure the record before it hasn't been touched
        assertEquals(   Arrays.asList(lastRecord),
                        Arrays.asList(access.read(access.size() - 2))   );
        
    }
    
    
    /**
     * create a new record at the location of a previously deleted record
     */
    @Test
    public void new_record_overwrite_old() throws Exception {
        
        final List<String>  record0 = Arrays.asList(access.read(0)),
                            record1 = Arrays.asList(access.read(1)),
                            record2 = Arrays.asList(access.read(2));
        
        // overwrite #1 with a new record
        access.delete(1);
        assertEquals(1, access.create());
        
        // make sure the neighbors aren't touched
        assertEquals(record0, Arrays.asList(access.read(0)));
        assertEquals(record2, Arrays.asList(access.read(2)));
        
        // make sure #1 is overwritten
        assertFalse(record1.equals(Arrays.asList(access.read(1))));
        
    }
    
    
    /**
     * write to a record that doesn't exist
     */
    @Test(expected=IndexOutOfBoundsException.class)
    public void write_out_of_bounds() throws Exception {
        access.write(access.size(), new String[] {
            "",
            "",
            "",
            "",
            "",
            ""
        });
    }
    
    
    /**
     * write with invalid number of fields
     */
    @Test
    public void write_with_invalid_fields() throws Exception {
        try {
            access.write(0, new String[1]);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue( e.getMessage(),
                        e.getMessage().contains("invalid number of fields") );
        }
    }
    
    
    /**
     * write field with null-values
     */
    @Test
    public void write_with_null_values() throws Exception {
        try {
            access.write(0, new String[access.getFields().length]);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue( e.getMessage(),
                        e.getMessage().contains("can't be null")    );
        }
    }
    
    
    /**
     * write, and verify that too long fields are truncated 
     */
    @Test
    public void write_with_field_truncation() throws Exception {
        
        access.write(0, new String[] { "", "", "", "1234567", "", ""});
        
        assertEquals(   "123456",
                        access.read(0)[3]   );
        
    }
    
    
    /**
     * write, and verify that the correct data was written
     */
    public void write_and_verify_results() throws Exception {
        
        final String[] expected = new String[] {
                "one",
                "two",
                "three",
                "four",
                "five",
                "six"
        };
        
        assertFalse(Arrays.asList(expected).equals(Arrays.asList(access.read(0))));
        
        access.write(0, expected);
        
        assertEquals(   expected,
                        Arrays.asList(access.read(0))   );
        
    }

}   // DataFileAccessTest
