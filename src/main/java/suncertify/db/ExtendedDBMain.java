package suncertify.db;


/**
 * Extended data-access interface.
 *
 * @author Lars Hvile
 * @see DBMain
 */
public interface ExtendedDBMain extends DBMain {
    /*
     * This extension is required because we can't touch the original
     * DBMain interface.
     */

    /**
     * Returns the database's field-definitions.
     *
     * @return a <code>DatabaseField[]</code>
     */
    DatabaseField[] getFields();
    /*
     * This method is extremely important as it allows some parts of the GUI
     * to be loosely coupled to the data-model. 
     */

}
