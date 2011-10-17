package suncertify.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;


/**
 * Support-class for creating widgets & dealing with Swing.
 * 
 * @author Lars Hvile
 */
public final class SwingHelper {
    
    /**
     * Default padding in pixels between controls.
     */
    public static final int PADDING = 4;
    
    
    /**
     * Default padding in pixels between groups of controls.
     */
    public static final int ROW_PADDING = 10;
    
    
    /*
     * Private constructor, prevents direct instantiation
     */
    private SwingHelper() {
    }
    
    
    /**
     * Displays a yes/no question to the user.
     * 
     * @param parent the parent <code>Component</code>
     * @param message the message to display
     * @return <code>true</code> if the user pressed 'yes'
     */
    public static boolean displayYesNoPrompt(Component parent, String message) {
        return (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(parent,
                message, null, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null));
    }
    
    
    /**
     * Displays a message.
     * 
     * @param parent the parent <code>Component</code>
     * @param message the message to display
     */
    public static void displayMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message);
    }
    
    
    /**
     * Displays an error-message.
     * 
     * @param parent the parent <code>Component</code>
     * @param message the message to display
     */
    public static void displayError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, null,
                JOptionPane.ERROR_MESSAGE);
    }
    
    
    /**
     * Creates a <code>JMenuItem</code>.
     * 
     * @param label
     *            the item's label
     * @param mnemonic
     *            the item's mnemonic, e.g. <code>KeyEvent.VK_X</code>, or
     *            <code>null</code> for no mnemonic
     * @param accelerator
     *            the keyboard-accelerator as a <code>KeyStroke</code>, or
     *            <code>null</code> for no accelerator
     * @param listener
     *            an <code>ActionListener</code> for the item
     * @return the created <code>JMenuItem</code>
     */
    public static JMenuItem createMenuItem(String label, Integer mnemonic,
            KeyStroke accelerator, ActionListener listener) {

        final JMenuItem item = new JMenuItem(label);

        if (null != mnemonic) {
            item.setMnemonic(mnemonic);
        }
        if (null != accelerator) {
            item.setAccelerator(accelerator);
        }

        item.addActionListener(listener);

        return item;
    }
    
    
    /**
     * Returns a <code>Point</code> used as location for a window that
     * should be centered on the screen.
     * 
     * @param size a <code>Dimension</code> describing the window's size
     * @return a <code>Point</code> describing the top-left position
     */
    public static Point getCenteredLocation(Dimension size) {
        
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension screenSize = tk.getScreenSize();
        
        return new Point(
                ((screenSize.width - size.width) / 2),
                ((screenSize.height - size.height) / 2));
    }
    
    
    /**
     * Lays out a list of controls in a compact grid-layout, meaning that each
     * cell in the grid only takes up it's preferred size.
     * 
     * @param parent the parent <code>Container</code>
     * @param colCount the number of columns
     * @param padding the number of pixels to pad between cells
     * @param components a <code>List</code> of <code>Component</code>s that
     *        should be added to <code>parent</code>
     */
    public static void makeCompactGrid(Container parent, int colCount,
            int padding, List<? extends Component> components) {
        
        parent.setLayout(new GridBagLayout());
        
        final int componentCount = components.size();
        final int rowCount       = getRowCount(componentCount, colCount);
        
        final GridBagConstraints c           = new GridBagConstraints();
        final int                realPadding = (padding / 2);
        
        c.gridheight = 1;
        c.gridwidth  = 1;
        c.fill       = GridBagConstraints.BOTH;
        c.weightx    = 1;
        c.weighty    = 1;
        c.insets = new Insets(realPadding, realPadding, realPadding,
                realPadding);
        
        for (int i = 0; i < (rowCount * colCount); i++) {
            final int x = (i % colCount);
            final int y = (i / colCount);
        
            c.gridx = x;
            c.gridy = y;
            
            parent.add(components.get(i), c);
        }
    }
    
    
    private static int getRowCount(int componentCount, int colCount) {
        int rowCount = (componentCount / colCount);
        if ((componentCount % colCount) != 0) {
            rowCount++;
        }
        return rowCount;
    }
}
