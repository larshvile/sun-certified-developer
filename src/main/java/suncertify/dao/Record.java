package suncertify.dao;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Represents a record in the database.
 * 
 * @author Lars Hvile
 */
public final class Record implements Serializable {

    private static final long   serialVersionUID = -4465363248943650456L;

    private final int      recNo;
    private final String[] fields;
    
    
    /**
     * Class-constructor.
     * 
     * @param recNo the record's id
     * @param fields the record's field-values
     */
    public Record(int recNo, String[] fields) {
        this.recNo = recNo;
        this.fields = Arrays.copyOf(fields, fields.length);
    }
    
    
    @Override
    public String toString() {
        return "#" + recNo + " " + Arrays.asList(fields);
    }
    
    
    /**
     * Returns the record's id.
     * 
     * @return the id
     */
    public int getRecNo() {
        return recNo;
    }
    
    
    /**
     * Returns the value of a field.
     * 
     * @param index the field's index
     * @return the field's value
     */
    public String getField(int index) {
        return fields[index];
    }
    
    
    /**
     * Sets a new value for a field.
     * 
     * @param index the field's index
     * @param value the new value
     */
    public void setField(int index, String value) {
        fields[index] = value;
    }
    
    
    /**
     * Returns the field-values.
     * 
     * @return the field-values as a <code>String[]</code>
     */
    public String[] getFields() {
        return Arrays.copyOf(fields, fields.length);
    }
}
