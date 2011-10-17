package suncertify.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import suncertify.config.TextResolver;
import suncertify.config.TextResolver.TextKey;
import suncertify.db.DatabaseField;


/**
 * Support-class containing common functionality for dialogs that display/
 * edit fields from the database.
 * 
 * @author Lars Hvile
 */
final class DbFieldDialogSupport {
    
    private static final int COL_COUNT = 2;
    private static final int PADDING   = 6;
    
    
    /*
     * Private constructor
     */
    private DbFieldDialogSupport() {
    }
    
    
    /**
     * Creates a <code>JPanel</code> with textfields/labels for a given set of
     * <code>DatabaseField</code>s. The output-param <code>textFields</code>
     * is of type <code>LinkedHashMap</code> because of the predictable
     * ordering. The created <code>JTextField</code>s will be created with an
     * <code>InputVerifier</code> according to it's type.
     * 
     * @param fields
     *            an array with the <code>DatabaseFields</code> to display
     * @param textFieldColumns
     *            the number of characters that each textfield should display
     * @param textFields
     *            a <code>LinkedHashMap</code> that will be filled with the
     *            <code>DatabaseField</code>s and their corresponding
     *            <code>JTextField</code>s
     * @return a <code>JPanel</code> which holds the controls
     */
    public static JPanel createFieldPanel(DatabaseField[] fields,
            int textFieldColumns, LinkedHashMap<DatabaseField,
            JTextField> textFields) {
        
        final JPanel panel = new JPanel();
        
        final List<Component> components = new ArrayList<Component>();
        
        for (int i = 0; i < fields.length; i++) {
            final DatabaseField field = fields[i];

            final JLabel label = new JLabel(field.getName(), JLabel.TRAILING);
            components.add(label);

            final JTextField textField = new JTextField(textFieldColumns);
            label.setLabelFor(textField);
            textField.setName(field.getName());
            textField.setInputVerifier(createInputVerifier(field));
            components.add(textField);
            textFields.put(field, textField);
        }
        
        SwingHelper.makeCompactGrid(panel, COL_COUNT, PADDING, components);
        
        return panel;
    }
    
    
    private static InputVerifier createInputVerifier(DatabaseField field) {
        switch (field.getType()) {
            case TEXT:
                return new FieldVerifier(field.getLength());
            case MONEY:
                return new MoneyFieldVerifier(field.getLength());
            case NUMBER:
                return new NumberFieldVerifier(field.getLength());
            default:
                throw new RuntimeException("unknown field-type: "
                        + field.getType());
        }
    }
    
    
    /**
     * Validates the contents of each textfield with it's verifier, displays an
     * error-message to the user if the format is invalid.
     * 
     * @param textFields
     *            a <code>Collection</code> of <code>JTextField</code>s to
     *            validate
     * @return <code>true</code> if all fields are valid
     */
    public static boolean validateFieldContent(
            Collection<JTextField> textFields) {

        for (JTextField textField : textFields) {
            final InputVerifier verifier = textField.getInputVerifier();
            if ((verifier != null) && !verifier.verify(textField)) {
                Toolkit.getDefaultToolkit().beep();
                SwingHelper.displayError(null, (TextResolver.getInstance()
                        .get(TextKey.MESSAGE_INVALID_FIELD_FORMAT)
                        + " " + textField.getName()));
                return false;
            }
        }
        
        return true;
    }
    
    
    /*
     * InputVerifier implementation used for verification of db-fields.
     * Ideally this kind of verification should be done in the data-layer, but
     * it seems a bit overkill for a text-only flat-file database.
     */
    private static class FieldVerifier extends InputVerifier {
        
        private final int length;
        
        public FieldVerifier(int length) {
            this.length = length;
        }
        
        @Override
        public boolean verify(JComponent input) {
            final String text = ((JTextField) input).getText();
            
            if (text.isEmpty()) {
                return true;
            } else if (text.length() > length) {
                return false;
            } else {
                return verify(text);
            }
        }
        
        protected boolean verify(String value) {
            return true;
        }
    }
    
    
    /*
     * Specialized InputVerifier used for MONEY-fields.
     */
    private static final class MoneyFieldVerifier extends FieldVerifier {
        
        public MoneyFieldVerifier(int length) {
            super(length);
        }
        
        @Override
        protected boolean verify(String value) {
            if (!"$".equals(value.substring(0, 1))) {
                return false;
            }
            if (value.length() > 1) {
                try {
                    final String amount = value.substring(1); 
                    Double.parseDouble(amount);
                    for (int i = 0; i < amount.length(); i++) {
                        final char c = amount.charAt(i);
                        if (!Character.isDigit(c) && (c != '.')) {
                            return false;
                        }
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
    }
    
    
    /*
     * Specialized InputVerifier used for NUMBER-fields.
     */
    private static final class NumberFieldVerifier extends FieldVerifier {
        
        public NumberFieldVerifier(int length) {
            super(length);
        }

        @Override
        protected boolean verify(String value) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
