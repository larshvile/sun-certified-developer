package suncertify.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import suncertify.dao.Record;
import suncertify.db.DatabaseField;


/**
 * Table-model used for displaying the search results.
 * 
 * @author Lars Hvile
 */
final class ResultModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;
    
    private final DatabaseField[] fields;
    private final List<Record>    results;
    
    
    /**
     * Class-constructor.
     * 
     * @param fields an array of <code>DatabaseField</code>
     */
    public ResultModel(DatabaseField[] fields) {
        this.fields = Arrays.copyOf(fields, fields.length);
        this.results = new ArrayList<Record>();
    }
    
    
    /**
     * Sets the results.
     * 
     * @param results a <code>List&lt;Record&gt;</code>
     */
    public void setResults(List<Record> results) {
        this.results.clear();
        this.results.addAll(results);
        fireTableDataChanged();
    }
    
    
    /**
     * Clears the current results.
     */
    public void clear() {
        this.results.clear();
        fireTableDataChanged();
    }
    
    
    /**
     * Returns the record associated with a given row.
     * 
     * @param rowIndex
     * @return a <code>Record</code>
     */
    public Record getRecord(int rowIndex) {
        return results.get(rowIndex);
    }
    
    
    /**
     * Update a single record.
     * 
     * @param a <code>Record</code>
     */
    public void update(Record record) {
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getRecNo() == record.getRecNo()) {
                results.set(i, record);
                fireTableRowsUpdated(i, i);
                break;
            }
        }
    }
    
    
    @Override
    public int getColumnCount() {
        return fields.length;
    }
    
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (fields[columnIndex].getType()) {
            case MONEY:  // not really an Integer, but it suffices
            case NUMBER:
                return Integer.class;
            case TEXT:
                return String.class;
            default:
                throw new RuntimeException("unknown type: "
                        + fields[columnIndex].getType());
        }
    }
    
    
    @Override
    public String getColumnName(int column) {
        return fields[column].getName();
    }


    @Override
    public int getRowCount() {
        return results.size();
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return results.get(rowIndex).getField(columnIndex);
    }
}
