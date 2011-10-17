package suncertify.gui;

import static suncertify.config.TextResolver.TextKey.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import suncertify.config.TextResolver;
import suncertify.db.DatabaseField;


/**
 * Dialog used to search the database.
 * 
 * @author Lars Hvile
 */
final class SearchDialog extends JDialog {

    /**
     * Observer-interface with notification-functionality for searches.
     */
    public interface SearchObserver {
        
        /**
         * Notifies the observer that the user has started a search.
         * 
         * @param fields a <code>String[]</code> holding the search-details.
         */
        void notifySearch(String[] fields);
    }

    private static final int          DISPLACEMENT          = 100;
    private static final int          PANEL_COL_COUNT        = 1;
    private static final int          TEXTFIELD_COLUMNS     = 10;
    
    private static final long         serialVersionUID = 1L;
    private static final TextResolver textResolver = TextResolver.getInstance();

    private final SearchObserver   observer;
    private Collection<JTextField> textFields;


    /**
     * Creates a new search-window.
     * 
     * @param parent
     *            the parent <code>Frame</code>
     * @param observer
     *            a <code>SearchObserver</code> used for callbacks.
     * @param fields
     *            a <code>DatabaseField[]</code> holding the databases's fields.
     */
    public SearchDialog(Frame parent, SearchObserver observer,
            DatabaseField[] fields) {
        super(parent);
        this.observer = observer;
        initComponents(fields);
        setLocation();
    }
    
    
    private void setLocation() {
        final Point parentLocation = getParent().getLocation();
        setLocation(parentLocation.x + DISPLACEMENT,
                parentLocation.y + DISPLACEMENT);
    }


    private void initComponents(DatabaseField[] fields) {
        
        final LinkedHashMap<DatabaseField, JTextField> textFields
                = new LinkedHashMap<DatabaseField, JTextField>();
        
        setTitle(textResolver.get(SEARCH_DIALOG_TITLE));
        hideOnEscape();
        
        final List<Component> panels = new ArrayList<Component>();
        
        panels.add(DbFieldDialogSupport.createFieldPanel(fields,
                TEXTFIELD_COLUMNS, textFields));
        panels.add(createControls());
        
        SwingHelper.makeCompactGrid(this, PANEL_COL_COUNT,
                SwingHelper.ROW_PADDING, panels);
        pack();
        
        this.textFields = textFields.values();
    }
    
    
    private void hideOnEscape() {
        
        final ActionListener hideListener = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                setVisible(false);                
            }
        };
        
        getRootPane().registerKeyboardAction(hideListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    
    private Component createControls() {
        
        final JPanel panel = new JPanel();
        
        final JButton search = new JButton(textResolver.get(BUTTON_SEARCH));
        search.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                doSearch();               
            }
        });        
        getRootPane().setDefaultButton(search);
        
        final JButton clear = new JButton(textResolver.get(BUTTON_CLEAR));
        clear.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                doClear();                
            }
        });
        
        panel.add(search);
        panel.add(clear);
        
        return panel;
    }
    
    
    private void doSearch() {
        
        if (!DbFieldDialogSupport.validateFieldContent(textFields)) {
            return;
        }
        
        final List<String> values = new ArrayList<String>();
        
        for (JTextField textField : textFields) {
            String value = textField.getText();
            if (value != null) {
                value = value.trim();
                if (value.isEmpty()) {
                    value = null;
                }
            }
            values.add(value);
        }
        
        observer.notifySearch(values.toArray(new String[values.size()]));
    }
    
    
    private void doClear() {
        for (JTextField field : textFields) {
            field.setText("");
        }
    }
}
