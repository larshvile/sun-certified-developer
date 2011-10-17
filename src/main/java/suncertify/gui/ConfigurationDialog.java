package suncertify.gui;

import static suncertify.config.TextResolver.TextKey.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import suncertify.config.Configuration;
import suncertify.config.TextResolver;
import suncertify.config.Configuration.Option;
import suncertify.config.Configuration.UnableToSaveException;
import suncertify.config.TextResolver.TextKey;


/**
 * Dialog that displays/edits the application-configuration.
 * 
 * @author Lars Hvile
 */
final class ConfigurationDialog extends JDialog {
    
    private static final int          PANELS_COL_COUNT   = 1;
    private static final int          FIELDS_COL_COUNT   = 3;
    private static final int          FILE_BUTTON_WIDTH = 30;
    private static final String       CONFIG_KEY_PREFIX = "CONFIG_KEY_";
    
    private static final long         serialVersionUID = 1L;
    
    private static final TextResolver textResolver = TextResolver.getInstance();
    private static final Logger       logger = Logger.getLogger(
                                           ConfigurationDialog.class.getName());
    
    private ConfigurationObserver         observer;
    private final Map<Option, JTextField> textFields;
    private boolean                       isSaved;
    
    
    /**
     * Creates the dialog with an observer.
     * 
     * @param observer a <code>ConfigurationObserver</code>
     */
    public ConfigurationDialog(ConfigurationObserver observer) {
        this.observer   = observer;
        this.textFields = new LinkedHashMap<Option, JTextField>();
        
        initComponents();
        setLocation(SwingHelper.getCenteredLocation(getSize()));
        setVisible(true);
    }
    
    
    private void initComponents() {
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent we) {
                if (observer != null) {
                    observer.configurationDialogClosed(isSaved);
                    observer = null;
                }
            }
        });
        
        setTitle(textResolver.get(CONFIGURATION_DIALOG_TITLE));
        
        final List<Component> panels = new ArrayList<Component>();
        
        panels.add(createFields());
        panels.add(createControls());
        
        SwingHelper.makeCompactGrid(this, PANELS_COL_COUNT,
                SwingHelper.ROW_PADDING, panels);
        
        pack();
    }
    
    
    private Component createFields() {
        final JPanel panel = new JPanel();
        final List<Component> components = new ArrayList<Component>();
        
        for (Option option
                : Configuration.getInstance().getApplicableOptions()) {
            createField(option, components);
        }
        
        SwingHelper.makeCompactGrid(panel, FIELDS_COL_COUNT,
                SwingHelper.PADDING, components);
        
        return panel;
    }
    
    
    private void createField(Option option, List<Component> components) {
        
        final String fieldName = textResolver.get(TextKey
                .valueOf(CONFIG_KEY_PREFIX + option.name()));
                
        final JLabel label = new JLabel(fieldName, JLabel.TRAILING);
        components.add(label);

        final JTextField textField = new JTextField(20);
        label.setLabelFor(textField);
        textField.setName(fieldName);
        textField.setText(Configuration.getInstance().getValue(option));
        components.add(textField);
        
        textFields.put(option, textField);
        
        if (option.isFile()) {            
            final JButton fileButton = new JButton("...");
            components.add(fileButton);
            fileButton.setPreferredSize(new Dimension(FILE_BUTTON_WIDTH, 1));
            
            fileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chooseFile(textField);                    
                }
            });
        } else {
            components.add(Box.createRigidArea(new Dimension(0, 0)));
        }
    }
    
    
    private void chooseFile(JTextField destination) {
        final JFileChooser fc = new JFileChooser();
        
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            destination.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }
    
    
    private Component createControls() {
        final JPanel panel = new JPanel();
        
        final JButton save = new JButton(textResolver.get(BUTTON_SAVE));
        save.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (save()) {
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
    
    
    private boolean save() {
        
        if (!validateConfig()) {
            return false;
        }
        
        final Configuration config = Configuration.getInstance();
        try {
            for (Entry<Option, JTextField> entry : textFields.entrySet()) {
                config.setValue(entry.getKey(), entry.getValue()
                        .getText());
            }
            config.save();
            isSaved = true;
            return true;
        } catch (UnableToSaveException e) {
            logger.log(Level.WARNING, "unable to save configuration", e);
            SwingHelper.displayError(this, textResolver
                    .get(MESSAGE_UNABLE_TO_SAVE_CONFIG));
            return false;
        }
    } 
    
    
    private boolean validateConfig() {
        for (Entry<Option, JTextField> entry : textFields.entrySet()) {
            final Option     option = entry.getKey();
            final JTextField field  = entry.getValue();
            final String     value  = field.getText();
            
            if (value.trim().isEmpty()) {
                continue;
            } else if (option.isFile() && !validateFile(value)) {
                SwingHelper.displayError(this, textResolver
                        .get(MESSAGE_INVALID_FILE) + " " + field.getName());
                return false;
            } else if (option.isNumeric() && !validateNumber(value)) {
                SwingHelper.displayError(this, textResolver
                        .get(MESSAGE_INVALID_NUMBER) + " " + field.getName());
                return false;
            }
        }

        return true;
    }
    
    
    private boolean validateFile(String value) {
        final File f = new File(value);
        return (f.exists() && f.isFile());
    }
    
    
    private boolean validateNumber(String value) {
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
