package suncertify.config;

import java.util.ResourceBundle;


/**
 * Singleton utility-class that provides access to application texts based on
 * the current locale.
 * 
 * @author Lars Hvile
 */
public final class TextResolver {
    /*
     * Currently implemented as a wrapper around ResourceBundle.
     */
    
    /**
     * Defines the available text-keys.
     */
    public enum TextKey {
        
        /** Title of client GUI-frame. */
        CLIENT_FRAME_TITLE,
        
        /** Title of server GUI-frame. */
        SERVER_FRAME_TITLE,
        
        /** Title of the search-dialog. */
        SEARCH_DIALOG_TITLE,
        
        /** Title of the edit-dialog. */
        EDIT_DIALOG_TITLE,
        
        /** Title of the configuration-dialog. */
        CONFIGURATION_DIALOG_TITLE,
        
        /** Title of file-menu. */
        MENU_FILE,
        
        /** Title of the quit-menu. */
        MENU_QUIT,
        
        /** Title of the edit-menu. */
        MENU_EDIT,
        
        /** Title of the search-menu. */
        MENU_SEARCH,
        
        /** Title of the configuration-menu. */
        MENU_CONFIGURATION,
        
        /** Text for search-buttons. */
        BUTTON_SEARCH,
        
        /** Text for clear-buttons. */
        BUTTON_CLEAR,
        
        /** Text for save-buttons. */
        BUTTON_SAVE,
        
        /** Text for cancel-buttons. */
        BUTTON_CANCEL,
        
        /** Message representing "no results". */
        MESSAGE_NO_RESULTS,
        
        /** Message for unknown errors. */
        MESSAGE_UNKNOWN_ERROR,
        
        /** Message representing "record not found". */
        MESSAGE_RECORD_NOT_FOUND,
        
        /** Message representing "record locked". */
        MESSAGE_RECORD_ALREADY_LOCKED,
        
        /** Message representing "unable to save config". */
        MESSAGE_UNABLE_TO_SAVE_CONFIG,
        
        /** Message representing "invalid field-format". */
        MESSAGE_INVALID_FIELD_FORMAT,
        
        /** Message warning the user that the application needs a restart. */
        MESSAGE_CONFIG_UPDATED_AT_RUNTIME,
        
        /** Message notifying the user to select a db-file. */
        MESSAGE_SELECT_DB_FILE,
        
        /** Message warning the user that a file wasn't found. */
        MESSAGE_DB_FILE_NOT_FOUND,
        
        /** Error-message for RMI-export. */
        MESSAGE_ERROR_WHILE_EXPORTING_DAO,
        
        /** Message representing "select server-host". */
        MESSAGE_SELECT_SERVER_HOST,
        
        /** Message representing "select server-port". */
        MESSAGE_SELECT_SERVER_PORT,
        
        /** Message representing "unable to connect". */
        MESSAGE_UNABLE_TO_CONNECT,
        
        /** Prompt for application-exit. */
        MESSAGE_CONFIRM_SERVER_EXIT,
        
        /** Error-message for invalid file-names. */
        MESSAGE_INVALID_FILE,
        
        /** Error-message for invalid numbers. */
        MESSAGE_INVALID_NUMBER,
        
        /** Text-representation of the DATABASE_LOCATION option. */
        CONFIG_KEY_DATABASE_LOCATION,
        
        /** Text-representation of the SERVER_HOST option. */
        CONFIG_KEY_SERVER_HOST,
        
        /** Text-representation of the SERVER_PORT option. */
        CONFIG_KEY_SERVER_PORT
    }

    private static final TextResolver instance = new TextResolver();
    
    private final ResourceBundle bundle = ResourceBundle.getBundle("text");


    /*
     * Private constructor, prevents direct instantiation.
     */
    private TextResolver() {
    }


    /**
     * Returns an instance of the <code>TextResolver</code>.
     * 
     * @return a <code>TextResolver</code>
     */
    public static TextResolver getInstance() {        
        return instance;
    }
    
    
    /**
     * Returns the locale-specific value for a given text-key.
     * 
     * @param key the <code>TextKey</code> to resolve
     * @return the value of the given key
     */
    public String get(TextKey key) {
        return bundle.getString(key.name());
    }
}
