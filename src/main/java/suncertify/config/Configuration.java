package suncertify.config;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Singleton utility-class that reads/writes configuration options.
 * 
 * @author Lars Hvile
 */
public final class Configuration {
    
    /**
     * Exception thrown when configuration-updates can't be saved.
     */
    public static class UnableToSaveException extends Exception {
        /*
         * Basically a wrapper exception so client-code isn't coupled directly
         * to I/O-exceptions just because the configuration happens to be saved
         * to a file.
         */
        
        private static final long serialVersionUID = 1L;
       
        /**
         * Class-constructor.
         * 
         * @param cause the exception's root-cause
         */
        public UnableToSaveException(Throwable cause) {
            super(cause);
        }
    }
    
    
    /**
     * Defines the various operation-modes of the application.
     */
    public enum Mode {
        
        /**
         * Standalone client mode.
         */
        CLIENT_STANDALONE,
        
        /**
         * Networked client mode.
         */
        CLIENT_NETWORK,
        
        /**
         * Server mode.
         */
        SERVER
    }
    
    
    /**
     * Defines the configurable options.
     */
    public enum Option {
        
        /**
         * Location/path of the application's database-file.
         */
        DATABASE_LOCATION(true, false) {
            @Override public boolean isApplicableFor(Mode mode) {
                return ((Mode.SERVER == mode)
                        || (Mode.CLIENT_STANDALONE == mode));
            }
        },
        
        /**
         * IP-address/hostname of the application's server.
         */
        SERVER_HOST(false, false) {
            @Override public boolean isApplicableFor(Mode mode) {
                return (Mode.CLIENT_NETWORK == mode);
            }
        },
        
        /**
         * Port of the server-application.
         */
        SERVER_PORT(false, true) {
            @Override public boolean isApplicableFor(Mode mode) {
                return (Mode.SERVER == mode);
            }
        };
        
        
        private final boolean file;
        private final boolean numeric;
        
        Option(boolean file, boolean numeric) {
            this.file = file;
            this.numeric = numeric;
        }


        /**
         * Tests whether this option is applicable for a given <code>Mode</code>
         * .
         * 
         * @param mode a <code>Mode</code>
         * @return <code>true</code> if this option is applicable
         */
        public abstract boolean isApplicableFor(Mode mode);


        /**
         * Tests whether this option represents the path to a file.
         * 
         * @return <code>true</code> if this option represents the path to a
         *         file.
         */
        public boolean isFile() {
            return file;
        }
        
        
        /**
         * Tests whether this option represents a number.
         * 
         * @return <code>true</code> if this option represents a number.
         */
        public boolean isNumeric() {
            return numeric;
        }
    }
    
    private static final String CONFIGURATION_FILE = "suncertify.properties";
    
    private static final Logger logger = Logger.getLogger(Configuration.class
                                                                    .getName());
    private static final Configuration instance = new Configuration();
    
    private final Map<Option, String>  options = new HashMap<Option, String>();
    private Mode                       mode;
    
    
    /*
     * Private constructor.
     */
    private Configuration() {
        load();
    }
    
    
    /**
     * Returns the instance.
     * 
     * @return a <code>Configuration</code>
     */
    public static Configuration getInstance() {
        return instance;
    }
    
    
    /*
     * Loads the configuration from file.
     */
    private void load() {
        InputStream in = null;
        try {
            in = new FileInputStream(CONFIGURATION_FILE); 
            final Properties props = new Properties();
            props.load(in);
            for (Object key : props.keySet()) {
                options.put(Option.valueOf(key.toString()),
                        props.getProperty(key.toString()));
            }
            logger.config("loaded " + options + " from "
                    + CONFIGURATION_FILE);
        } catch (FileNotFoundException e) {
            // ignored
            logger.config("no configuration file found");
        } catch (IOException e) {
            logger.log(Level.WARNING, "unable to load configuration-file", e);
        } finally {
            closeStream(in);
        }
    }
    
    
    /**
     * Sets the current operation-mode.
     * 
     * @param mode a <code>Mode</code>
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }


    /**
     * Returns the applicable <code>Option</code>s for the current
     * operation-mode.
     * 
     * @return a <code>Option[]</code>
     */
    public Option[] getApplicableOptions() {
        final List<Option> options = new ArrayList<Option>();
        
        assert mode != null;
        
        for (Option o : Option.values()) {
            if (o.isApplicableFor(mode)) {
                options.add(o);
            }
        }
        
        return options.toArray(new Option[options.size()]);
    }
    
    
    /**
     * Saves the current configuration values.
     * 
     * @throws UnableToSaveException if not able to save the configuration
     */
    public void save() throws UnableToSaveException {
        OutputStream out = null;
        try {            
            final Properties props = new Properties();
            for (Entry<Option, String> option : options.entrySet()) {
                if (option.getValue() != null) {
                    props.put(option.getKey().name(), option.getValue());
                }
            }
            out = new FileOutputStream(CONFIGURATION_FILE);
            props.store(out, null);
            logger.config("stored " + options + " to " + CONFIGURATION_FILE);
        } catch (Exception e) {
            throw new UnableToSaveException(e);
        } finally {
            closeStream(out);
        }
    }
    
    
    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "unable to close stream", e);
            }
        }
    }
    
    
    /**
     * Retrieves the value of a configuration-option.
     * 
     * @param option
     *            a <code>Option</code>
     * @return the associated value as a <code>String</code>, or
     *         <code>null</code> if not set
     */
    public String getValue(Option option) {
        return options.get(option);
    }
    
    
    /**
     * Updates the value of a configuration-option. The value-string is
     * trimmed before storage, empty strings are converted to <code>null</code>.
     * 
     * @param option
     *            a <code>Option</code>
     * @param value
     *            the new value, as a <code>String</code>
     */
    public void setValue(Option option, String value) {
        String tmp = (value == null) ? null : value.trim();
        if ((tmp != null) && tmp.isEmpty()) {
            tmp = null;
        }
        options.put(option, tmp);
    }    
}
