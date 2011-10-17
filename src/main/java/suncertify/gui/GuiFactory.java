package suncertify.gui;

import javax.swing.SwingUtilities;

import suncertify.dao.Dao;


/**
 * Singleton factory-class which creates the main GUI-components.
 * 
 * @author Lars Hvile
 */
public final class GuiFactory {
    
    private static final GuiFactory instance = new GuiFactory();
    
    
    /*
     * Private constructor, prevents direct instantiation
     */
    private GuiFactory() {
    }


    /**
     * Returns the factory-singleton.
     * 
     * @return a <code>GuiFactory</code>
     */
    public static GuiFactory getInstance() {
        return instance;
    }
    
    
    /**
     * Creates the main GUI-window for the client-application.
     * 
     * @param dao a <code>Dao</code> instance
     */
    public void createClientFrame(final Dao dao) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new ClientFrame(dao);
            }
        });
    }
    
    
    /**
     * Creates the main GUI-window for the server-application.
     */
    public void createServerFrame() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new ServerFrame();
            }
        });
    }
    
    
    /**
     * Displays a configuration-dialog and attaches an observer.
     * 
     * @param observer a <code>ConfigurationObserver</code>
     */
    public void displayConfigurationDialog(
            final ConfigurationObserver observer) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new ConfigurationDialog(observer);
            }
        });
    }
}
