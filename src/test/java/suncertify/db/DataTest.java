package suncertify.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import test.common.AbstractDataTest;


@RunWith(JUnit4.class)
public class DataTest extends AbstractDataTest {
    
    private static final List<String> dummyFields = Arrays.asList(new String[] {
            "one",
            "two",
            "three",
            "four",
            "five",
            "six"
    });
    
    
    /**
     * verify that getFields() returns the correct fields
     */
    @Test
    public void verify_field_data() {
        
        final DatabaseField[] fields = db.getFields();
        
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
     * read record #-1, which doesn't exist
     */
    @Test(expected=RecordNotFoundException.class)
    public void read_undenfined_record() throws Exception {
        db.read(-1);
    }
    
    
    /**
     * read a record that doesn't exist
     */
    @Test(expected=RecordNotFoundException.class)
    public void read_record_max_plus1() throws Exception {
        db.read(db.size());
    }
    
    
    /**
     * read a deleted record
     */
    @Test
    public void read_deleted_record() throws Exception {
        db.lock(0);
        db.delete(0);
        try {
            db.read(0);
            fail();
        } catch (RecordNotFoundException e) {}
    }
    
    
    /**
     * read a record, verify data
     */
    @Test
    public void read() throws Exception {
        
        final List<String> expected = Arrays.asList(new String[] {
                "Hamner & Tong",
                "Whoville",
                "Roofing, Carpets, Electrical",
                "9",
                "$90.00",
                ""
        });
        
        assertEquals(expected, Arrays.asList(db.read(3)));
        
    }
    
    
    /**
     * Create a record with missing columns
     */
    @Test(expected=IllegalArgumentException.class)
    public void create_with_missing_columns() throws Exception {
        db.create(new String[] {"one"});
    }
    
    
    /**
     * Create a record with null in columns.
     */
    @Test(expected=IllegalArgumentException.class)
    public void create_with_null_columns() throws Exception {
        db.create(new String[db.getFields().length]);
    }
        
    
    /**
     * Provoke DuplicateKeyException
     */
    @Test(expected=DuplicateKeyException.class)
    public void create_duplicate_key() throws Exception {
        db.create(new String[] {
                "Hamner & Tong",
                "Whoville",
                "",
                "",
                "",
                ""
        });
    }
    
    
    /**
     * Create a valid record and verify it
     */
    @Test
    public void create() throws Exception {
        
        assertEquals(29, db.size());                
        
        final int id = db.create(dummyFields.toArray(
                new String[dummyFields.size()]));
        
        assertEquals(30, db.size());
        assertEquals(   dummyFields,
                        Arrays.asList(db.read(id))  );
        
    }
    
    
    /**
     * delete a record that doesn't exist
     */
    @Test(expected=RecordNotFoundException.class)
    public void delete_undefined_record() throws Exception {
        db.delete(db.size());
    }
    
    
    @Test(expected=AssertionError.class)
    public void delete_fail_if_not_locked() throws Exception {
        db.delete(1);
    }
    
    
    @Test(expected=AssertionError.class)
    public void update_fail_if_not_locked() throws Exception {
        db.update(1, db.read(1));
    }
    
    
    /**
     * delete a record & verify that it's gone
     */
    @Test
    public void delete() throws Exception {
        assertEquals(29, db.size());
        db.lock(0);
        db.delete(0);
        assertEquals(28, db.size());
    }
    
    
    /**
     * delete a record that has already been deleted
     */
    @Test
    public void delete_deleted_record() throws Exception {
        db.lock(0);
        db.delete(0);
        try {
            db.delete(0);
            fail();
        } catch (RecordNotFoundException e) {}
    }
    
    
    /**
     * update an undefined record-index
     */
    @Test(expected=RecordNotFoundException.class)
    public void update_undefined_record() throws Exception {
        db.update(db.size(), new String[0]);
    }
    
    
    /**
     * update a deleted record
     */
    @Test
    public void update_deleted_record() throws Exception {
        db.lock(0);
        db.delete(0);
        try {
            db.update(0, new String[0]);
            fail();
        } catch (RecordNotFoundException e) {
        }
    }
    
    
    /**
     * update with invalid fields
     */
    @Test(expected=IllegalArgumentException.class)
    public void update_with_invalid_fields() throws Exception {
        db.lock(0);
        db.update(0, new String[2]);        
    }
    
    
    /**
     * update & provoke DuplicateKeyExc 
     */
    @Test
    public void update_and_provoke_duplicate_key() throws Exception {

        db.lock(0);
        db.update(0, dummyFields.toArray(new String[] {}));
        db.unlock(0);
        try {
            db.lock(1);
            db.update(1, dummyFields.toArray(new String[] {}));
            fail();
        } catch (RuntimeException e) {
            db.unlock(1);
            assertEquals(DuplicateKeyException.class, e.getCause().getClass());                    
        }
    }
    
    
    /**
     * update all fields and verify the new data
     */
    @Test
    public void update_all_and_verify() throws Exception {
        
        assertFalse(dummyFields.equals(Arrays.asList(db.read(0))));
        db.lock(0);
        db.update(0, dummyFields.toArray(new String[dummyFields.size()]));
        db.unlock(0);
        assertEquals(dummyFields, Arrays.asList(db.read(0)));
        
    }
    
    
    /**
     * update non-key fields and verify
     */
    @Test
    public void update_some_and_verify() throws Exception {
        
        List<String> fields = Arrays.asList(db.read(0));
        
        assertFalse(fields.get(fields.size() - 1).equals("NEW"));
        fields.set(fields.size() - 1, "NEW");
        
        db.lock(0);
        db.update(0, fields.toArray(new String[] {}));
        db.unlock(0);
        
        assertEquals(   fields,
                        Arrays.asList(db.read(0))   );
        
    }
    
    
    /**
     * use find() to match a single column
     */
    @Test
    public void find_match_single_column() throws Exception {
        
        int[] res = db.find(new String[] { null, null, null, null, "$", null});
        
        assertEquals(db.size(), res.length);
    }
    
    
    /**
     * verify that find() ignores the case
     */
    @Test
    public void find_match_ignore_case() throws Exception {
        
        int[] results = db.find(new String[] { "b", null, null, null, null,
                                null });
        
        assertEquals(9, results.length);
        assertEquals(0, results[0]);
    }
    
    
    /**
     * use find() to match multiple columns
     */
    @Test
    public void find_match_multiple_columns() throws Exception {
        
        int[] specific1 = db.find(new String[] { "Buonarotti", null, null, null,
                "$", null});
        int[] specific2 = db.find(new String[] { "Buonarotti", "Smallville",
                null, null, "$", null});
        
        assertEquals(4, specific1.length);
        assertEquals(1, specific2.length);        
    }
    
    
    /**
     * find(), without any matches
     */
    @Test(expected=RecordNotFoundException.class)
    public void find_without_matches() throws Exception {
        db.find(new String[] {"Mor.di", null, null, null, null, null});
    }
    
    
    /**
     * find(), with invalid criteria-list
     */
    @Test(expected=IllegalArgumentException.class)
    public void find_with_invalid_critera() throws Exception {
        db.find(new String[1]);
    }
    
    
    /**
     * isLocked(), invalid record-id
     */
    @Test(expected=RecordNotFoundException.class)
    public void is_locked_on_invalid_record() throws Exception {
        db.isLocked(-1);
    }
    
    
    @Test
    public void is_locked_when_not_locked() throws Exception {
        assertFalse(db.isLocked(0));
    }
    
    
    @Test
    public void is_locked_when_locked() throws Exception {
        db.lock(0);
        assertTrue(db.isLocked(0));
    }
    
    
    @Test
    public void is_locked_when_unlocked() throws Exception {
        db.lock(0);
        db.unlock(0);
        assertFalse(db.isLocked(0));
    }
    
    
    @Test(expected=RecordNotFoundException.class)
    public void lock_invalid_record() throws Exception {
        db.lock(-1);
    }
    
    
    @Test(expected=RecordNotFoundException.class)
    public void unlock_invalid_record() throws Exception {
        db.unlock(-1);
    }
    
    
    @Test
    public void unlock_deleted_record() throws Exception {
        db.lock(1);
        db.delete(1);
        
        try {
            db.isLocked(1);
            fail();
        } catch (RecordNotFoundException e) {
        }
        
        try {
            db.unlock(1);
            fail();
        } catch (RecordNotFoundException e) {
        }
    }
    
    
    @Test
    public void test_locking() throws Exception {
        
        final CountDownLatch latch_unlock1 = new CountDownLatch(1);
        final CountDownLatch latch_unlock2 = new CountDownLatch(1);
        
        final List<String> actions = new ArrayList<String>();
        
        db.lock(0);
        actions.add("master.lock");
        
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    latch_unlock1.countDown();
                    
                    db.lock(0);
                    actions.add("locker.lock");
                    
                    latch_unlock2.countDown();
                    
                    Thread.sleep(100);              // make sure master reaches lock()
                    actions.add("locker.delete");   // implicit unlock
                    db.delete(0);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        }).start();
        
        latch_unlock1.await();
        
        actions.add("master.unlock");
        db.unlock(0);
        
        latch_unlock2.await();
        
        db.lock(0);
        actions.add("master.lock");
        
        assertEquals(Arrays.asList(new String[] { "master.lock",
                "master.unlock", "locker.lock", "locker.delete", "master.lock"}),
                actions);
        
    }
}
