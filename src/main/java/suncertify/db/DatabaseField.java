package suncertify.db;

import java.io.Serializable;


/**
 * Represents a field-/column-definition in the database.
 * 
 * @author Lars Hvile
 */
public final class DatabaseField implements Serializable {
    
    private static final long serialVersionUID = -4242726277612950514L;
    
    
    /**
     * Defines the various data-types for the fields.
     */
    public enum Type {
        
        /**
         * A field that contains text.
         */
        TEXT,
        
        /**
         * A field containing numbers.
         */
        NUMBER,
        
        /**
         * A field containing an amount w/currency.
         */
        MONEY
    }
    
    
    private final int    index;
    private final String name;
    private final short  length;
    private final Type   type;
    
    
    /**
     * Returns the field's index.
     * 
     * @return the index as an <code>int</code>
     */
    public int getIndex() {
        return index;
    }
    
    
    /**
     * Returns the field's name.
     * 
     * @return the name as a <code>String</code>
     */
    public String getName() {
        return name;
    }
    
    
    /**
     * Returns the field's maximum length.
     * 
     * @return the max-length as a <code>short</code>
     */
    public short getLength() {
        return length;
    }
    
    
    /**
     * Returns the field's type.
     * 
     * @return a <code>Type</code>
     */
    public Type getType() {
        return type;
    }
    
    
    /**
     * Class-constructor.
     * 
     * @param index the field's index
     * @param name the field's name
     * @param length the field's max-length
     * @param type the field's type
     */
    public DatabaseField(int index, String name, short length, Type type) {
        this.index = index;
        this.name = name;
        this.length = length;
        this.type = type;
    }

    
    @Override
    public String toString() {
        return (index + ":" + name + " (" + length + ", " + type + ")");
    }
}
