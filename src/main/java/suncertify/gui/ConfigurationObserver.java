package suncertify.gui;


/**
 * Interface used to observe user-actions regarding the configuration.
 */
public interface ConfigurationObserver {
    
    /**
     * Notifies the observer that the config-dialog has been closed.
     * 
     * @param isUpdated
     *            <code>true</code> if the configuration was updated
     */
    void configurationDialogClosed(boolean isUpdated);
}
