package suncertify.gui;

import static suncertify.config.TextResolver.TextKey.*;
import static suncertify.gui.SwingHelper.displayError;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import suncertify.config.TextResolver;
import suncertify.dao.Dao;
import suncertify.dao.Record;
import suncertify.dao.RecordAlreadyLockedException;
import suncertify.db.DatabaseField;
import suncertify.db.DatabaseException; 
import suncertify.db.RecordNotFoundException;


/**
 * GUI-window used to edit a record.
 * 
 * @author Lars Hvile
 */
final class EditDialog extends JDialog {
    
    /**
     * Observer-interface with notification-functionality for record-editing.
     */
    public interface EditObserver {
        
        /**
         * Notifies the observer that a record has been updated.
         * 
         * @param record a <code>Record</code>
         */
        void notifyRecordUpdated(Record record);
    }
    
    private static final long         serialVersionUID = 1L;
    private static final int          PANELS_COL_COUNT = 1;
    private static final TextResolver textResolver = TextResolver.getInstance();
    private static final Logger       logger = Logger.getLogger(EditDialog.class
                                                                .getName());
    
    private final EditObserver observer;
    private final Dao dao;
    private final LinkedHashMap<DatabaseField, JTextField> textFields;
    private Record record;


    /**
     * Creates a new edit-window.
     * 
     * @param parent
     *            the parent <code>Frame</code>
     * @param observer
     *            an <code>EditObserver</code>
     * @param dao
     *            a <code>Dao</code> used for locking/unlocking records
     * @param recNo
     *            index of the record to edit
     * @param editableFields
     *            a <code>DatabaseField[]</code> holding the fields that can be
     *            edited
     * @throws RecordNotFoundException
     * @throws RecordAlreadyLockedException
     * @throws DatabaseException
     */
    public EditDialog(Frame parent, EditObserver observer, Dao dao, int recNo,
            DatabaseField[] editableFields)
            throws RecordNotFoundException, RecordAlreadyLockedException {
        
        super(parent, true);
        
        this.observer = observer;
        this.dao = dao;
        this.textFields = new LinkedHashMap<DatabaseField, JTextField>();
        this.record = dao.lock(recNo);
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent we) {
                unlockRecord();
            }
        });
        
        initComponents(dao.getFields());
        initTextFields(Arrays.asList(editableFields));
        setLocation(SwingHelper.getCenteredLocation(getSize()));
        setVisible(true);
    }
    
    
    private void unlockRecord() {
        try {
            if (record != null) {
                dao.unlock(record.getRecNo());
                record = null;
            }
        } catch (Exception e) {
            /*
             * no special exception-handling here since all the known exceptions
             * should be 'impossible' to get..
             */
            logger.log(Level.SEVERE, "unable to unlock record " + record, e);
            displayError(this, textResolver.get(MESSAGE_UNKNOWN_ERROR));
        }
    }
    
    
    private void initComponents(DatabaseField[] fields) {
        
        setTitle(textResolver.get(EDIT_DIALOG_TITLE));
        
        final List<Component> panels = new ArrayList<Component>();
        
        panels.add(DbFieldDialogSupport
                .createFieldPanel(fields, 25, textFields));
        panels.add(createControls());
        
        SwingHelper.makeCompactGrid(this, PANELS_COL_COUNT,
                SwingHelper.ROW_PADDING, panels);
        
        pack();
    }
    
    
    private void initTextFields(List<DatabaseField> editableFields) {
        
        boolean focusSet = false;
        
        // initialize the textboxes
        for (Entry<DatabaseField, JTextField> field : textFields.entrySet()) {
            final DatabaseField dbField  = field.getKey();
            final JTextField    textField = field.getValue();
            
            textField.setText(record.getField(dbField.getIndex()));
            textField.setEditable(isFieldEditable(editableFields, dbField));
            
            if (!focusSet && textField.isEditable()) {
                textField.requestFocusInWindow();
                focusSet = true;
            }
        }
    }
    
    
    private boolean isFieldEditable(List<DatabaseField> editableFields,
            DatabaseField field) {
        for (DatabaseField editableField : editableFields) {
            if (editableField.getIndex() == field.getIndex()) {
                return true;
            }
        }
        return false;
    }
    
    
    private Component createControls() {
        final JPanel panel = new JPanel();
        
        final JButton save = new JButton(textResolver.get(BUTTON_SAVE));
        save.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (updateRecord()) {
                    dispose();
                }
            }
        });
        getRootPane().setDefaultButton(save);
        
        final JButton cancel = new JButton(textResolver.get(BUTTON_CANCEL));
        cancel.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panel.add(save);
        panel.add(cancel);
        
        return panel;
    }
    
    
    private boolean updateRecord() {
        
        if (!DbFieldDialogSupport.validateFieldContent(textFields.values())) {
            return false;
        }
        
        // extract updated fields
        for (Entry<DatabaseField, JTextField> field : textFields.entrySet()) {
            final DatabaseField dbField   = field.getKey();
            final JTextField    textField = field.getValue();
            
            if (textField.isEditable()) {
                record.setField(dbField.getIndex(), textField.getText());
            }
        }
        
        // update database
        try {
            dao.update(record);
        } catch (Exception e) {
            /*
             * no special exception-handling here.. the record is locked, so it
             * must still exists, and the field-format has already been
             * validated.
             */
            logger.log(Level.SEVERE, "unable to update record " + record, e);
            displayError(this, textResolver.get(MESSAGE_UNKNOWN_ERROR));
        }
        
        // notify listener
        observer.notifyRecordUpdated(record);
        
        return true;
    }
}
