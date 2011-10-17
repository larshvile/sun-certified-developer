package suncertify.gui;

import static suncertify.config.TextResolver.TextKey.*; 
import static suncertify.gui.SwingHelper.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import suncertify.config.TextResolver;
import suncertify.dao.Dao;
import suncertify.dao.Record;
import suncertify.dao.RecordAlreadyLockedException;
import suncertify.db.DatabaseField;
import suncertify.db.RecordNotFoundException;
import suncertify.gui.EditDialog.EditObserver;
import suncertify.gui.SearchDialog.SearchObserver;


/**
 * Main GUI-window for the client-part of the application.
 * 
 * @author Lars Hvile
 */
final class ClientFrame extends JFrame implements SearchObserver, EditObserver {
    
    private static final int    PREFERRED_WIDTH  = 800;
    private static final int    PREFERRED_HEIGHT = 600;
    private static final String FIELD_OWNER = "owner";

    private static final long   serialVersionUID = 1L;
    
    private static final TextResolver textResolver = TextResolver.getInstance();
    private static final Logger logger = Logger.getLogger(ClientFrame.class
                                                              .getName());
    
    private final Dao               dao;
    private final ResultModel       model;
    private final SearchDialog      searchDialog;
    private final DatabaseField[]   editableFields;


    /**
     * Constructs & displays the window.
     * 
     * @param dao a <code>Dao</code>
     */
    public ClientFrame(Dao dao) {
        logger.info("creating client GUI");
        
        final DatabaseField[] fields = dao.getFields();
        
        this.dao = dao;
        this.model = new ResultModel(fields);
        this.editableFields = getEditableFields();
        
        initComponents();
        this.searchDialog = new SearchDialog(this, this, fields);
        setVisible(true);
    }
    
    
    /*
     * This method could be extended to include some kind of authorization
     * to determine which fields a user can edit.
     */
    private DatabaseField[] getEditableFields() {        
        for (DatabaseField f : dao.getFields()) {
            if (FIELD_OWNER.equals(f.getName())) {
                return new DatabaseField[] {f};
            }
        }
        throw new IllegalStateException("unable to find editable fields");
    }
    
    
    @Override
    public void notifySearch(String[] fields) {
        logger.info("starting a search for: " + Arrays.asList(fields));
        model.clear();
        try {
            final Record[] results = dao.find(fields);
            model.setResults(Arrays.asList(results));
        } catch (RecordNotFoundException e) {
            displayMessage(this, textResolver.get(MESSAGE_NO_RESULTS));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "unable to query", e);
            displayError(this, textResolver.get(MESSAGE_UNKNOWN_ERROR));
        }
    }
    
    
    @Override
    public void notifyRecordUpdated(Record record) {
        model.update(record);
    }
    
    
    /*
     * Displays an edit-GUI for a given <code>Record</code>
     */
    private void editRecord(Record record) {
        try {
            new EditDialog(this, this, dao, record.getRecNo(), editableFields);
        } catch (RecordNotFoundException e) {
            displayError(this, textResolver.get(MESSAGE_RECORD_NOT_FOUND));
        } catch (RecordAlreadyLockedException e) {
            displayError(this, textResolver.get(MESSAGE_RECORD_ALREADY_LOCKED));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "unable to edit record " + record, e);
            displayError(this, textResolver.get(MESSAGE_UNKNOWN_ERROR));
        }
    }
    
    
    private void initComponents() {

        setTitle(textResolver.get(CLIENT_FRAME_TITLE));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        initMenu();
        initResultsTable();

        pack();        
        setLocation(SwingHelper.getCenteredLocation(getSize()));        
    }
    
    
    private void initMenu() {
        final JMenuBar bar = new JMenuBar();
        bar.add(createFileMenu());
        bar.add(createEditMenu());
        setJMenuBar(bar);
    }


    private JMenu createFileMenu() {

        final JMenu menu = new JMenu(textResolver.get(MENU_FILE));
        menu.setMnemonic(KeyEvent.VK_F);

        menu.add(createMenuItem(
                textResolver.get(MENU_QUIT),
                KeyEvent.VK_Q,
                null,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                }));

        return menu;
    }


    private JMenu createEditMenu() {

        final JMenu menu = new JMenu(textResolver.get(MENU_EDIT));
        menu.setMnemonic(KeyEvent.VK_E);

        menu.add(createMenuItem(
                textResolver.get(MENU_SEARCH),
                KeyEvent.VK_S,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK),
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        searchDialog.setVisible(true);
                    }
                }));

        menu.add(createMenuItem(
                textResolver.get(MENU_CONFIGURATION),
                KeyEvent.VK_E,
                null,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        displayConfiguration();
                    }
                }));

        return menu;
    }
    
    
    private void displayConfiguration() {
        final GuiFactory gf = GuiFactory.getInstance();
        gf.displayConfigurationDialog(new ConfigurationObserver() {
            @Override
            public void configurationDialogClosed(boolean isUpdated) {
                if (isUpdated) {
                    displayMessage(ClientFrame.this, textResolver
                            .get(MESSAGE_CONFIG_UPDATED_AT_RUNTIME));
                }
            }
        });
    }


    private void initResultsTable() {

        final JTable resultsTable = new JTable(model);
        resultsTable.setFillsViewportHeight(true);
        
        // mouse, double-click listener
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedRecord(resultsTable);
                    e.consume();
                }
            }
        });
        
        // keyboard, enter-listener
        resultsTable.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    editSelectedRecord(resultsTable);
                    e.consume();
                }
            }
        });
        
        final JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(PREFERRED_WIDTH,
                PREFERRED_HEIGHT));
        
        add(scrollPane);
    }
    
    
    private void editSelectedRecord(JTable table) {
        final int selection = table.getSelectedRow();
        if (selection != -1) {
            editRecord(model.getRecord(selection));
        }
    }
}
