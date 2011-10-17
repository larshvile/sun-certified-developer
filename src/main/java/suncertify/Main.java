package suncertify;

import static suncertify.config.TextResolver.TextKey.*;

import static suncertify.gui.SwingHelper.displayError;
import static suncertify.gui.SwingHelper.displayMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.rmi.ConnectException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import suncertify.config.Configuration;
import suncertify.config.TextResolver;
import suncertify.config.Configuration.Mode;
import suncertify.config.Configuration.Option;
import suncertify.dao.Dao;
import suncertify.dao.DefaultDao;
import suncertify.db.Data;
import suncertify.gui.GuiFactory;
import suncertify.gui.ConfigurationObserver;
import suncertify.gui.SwingHelper;
import suncertify.remoting.RegistryHelper;


/**
 * Main/executable class. Responsible for starting the correct application based
 * on command-line input.
 * 
 * @author Lars Hvile
 */
public final class Main {

    private static final String ARG_SERVER = "server";
    private static final String ARG_ALONE  = "alone";
    
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final TextResolver textResolver = TextResolver.getInstance();
    
    
    // private constructor, to prevent direct instantiation
    private Main() {
    }
    
    
    /**
     * Application entry-point.
     * 
     * @param args <code>String[]</code> with program-arguments
     */
    public static void main(String[] args) {
        
        initialize();

        if (args.length == 0) {
            startNetworkedClient();
        } else if (ARG_ALONE.equals(args[0])) {
            startStandaloneClient(); 
        } else if (ARG_SERVER.equals(args[0])) {
            startServer();
        } else {
            System.out.println("usage: [OPTION]\n"
                + "\t"
                + ARG_ALONE
                + ",\tstart the client in "
                + "standalone-mode\n"
                + "\t"
                + ARG_SERVER
                + ",\tstart the server\n\n"
                + "if no option is given, the network-client will start");
            System.exit(1);
        }
    }
    
    
    private static void initialize() {

        // set up logging
        try {
            LogManager.getLogManager().readConfiguration(
                    Main.class.getClassLoader().getResourceAsStream(
                            "logging.properties"));
        } catch (Exception e) {
            System.err.println("unable to set up logging");
            e.printStackTrace();
            System.exit(1);
        }
        
        // make sure uncaught exceptions are logged
        Thread.setDefaultUncaughtExceptionHandler(
                new UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        logger.log(Level.SEVERE, "thread " + t
                                + " threw an exception", e);
                        SwingHelper.displayError(null,
                                textResolver.get(MESSAGE_UNKNOWN_ERROR));
                    }
                }
        );
    }
    
    
    private static void startNetworkedClient() {
        Configuration.getInstance().setMode(Mode.CLIENT_NETWORK);
        
        final Dao dao = selectAndConnectToServer();        
        GuiFactory.getInstance().createClientFrame(dao);        
    }
    
    
    private static Dao selectAndConnectToServer() {
        Dao dao = null;
        
        // loop until valid server is found
        while (dao == null) {
            final String host = Configuration.getInstance().getValue(
                    Option.SERVER_HOST);
            
            dao = connectToServer(host);
            
            if (dao == null) {
                updateConfiguration();
            }
        }
        
        return dao;
    }
    
    
    private static Dao connectToServer(String host) {
        
        if (host == null) {
            displayMessage(null, textResolver.get(MESSAGE_SELECT_SERVER_HOST));
            return null;
        }
        
        try {
            return RegistryHelper.get(host);
        } catch (ConnectException e) {
            displayError(null, textResolver.get(MESSAGE_UNABLE_TO_CONNECT));
            return null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "unable to connect", e);
            displayError(null, textResolver.get(MESSAGE_UNKNOWN_ERROR));
            return null;
        }
    }
    
    
    private static void startStandaloneClient() {
        Configuration.getInstance().setMode(Mode.CLIENT_STANDALONE);
        
        final Data database = selectAndOpenDatabaseFile();
        final Dao  dao      = new DefaultDao(database);
        GuiFactory.getInstance().createClientFrame(dao);
    }
    
    
    private static void startServer() {
        Configuration.getInstance().setMode(Mode.SERVER);
        
        final Data        database = selectAndOpenDatabaseFile();
        final DefaultDao  dao      = new DefaultDao(database);
        
        exportRemoteDao(dao);
        
        GuiFactory.getInstance().createServerFrame();
    }
    
    
    private static Data selectAndOpenDatabaseFile() {
        Data database = null;
        
        // loop until valid db is found
        while (database == null) {
            final String dbLocation = Configuration.getInstance().getValue(
                    Option.DATABASE_LOCATION);
            
            database = openDatabaseFile(dbLocation);
            
            if (database == null) {
                updateConfiguration();
            }
        }
        
        // close db properly on exit
        new CloseDatabaseFileShutdownHook(database);
        
        return database;
    }
    
    
    private static Data openDatabaseFile(String file) {
        
        if (file == null) {
            displayMessage(null, textResolver.get(MESSAGE_SELECT_DB_FILE));
            return null;
        }
        
        try {
            return new Data(new File(file));
        } catch (FileNotFoundException e) {
            displayError(null, textResolver.get(MESSAGE_DB_FILE_NOT_FOUND));
            return null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "unable to open database-file", e);
            displayError(null, textResolver.get(MESSAGE_UNKNOWN_ERROR));
            return null;
        }
    }
    
    
    private static void exportRemoteDao(Dao dao) {
        while (true) {
            final String port = Configuration.getInstance().getValue(
                    Option.SERVER_PORT);
            
            if (port == null) {
                displayMessage(null, textResolver
                        .get(MESSAGE_SELECT_SERVER_PORT));
            } else {
                try {
                    RegistryHelper.put(dao, Integer.valueOf(port));
                    return;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "unable to export remote dao-object", e);
                    displayError(null, textResolver
                            .get(MESSAGE_ERROR_WHILE_EXPORTING_DAO)
                            + " " + e.getMessage());
                }
            }
            
            updateConfiguration();
        }
    }
    
    
    /*
     * Displays the config-dialog and synchronizes this thread with the
     * GUI-thread to wait for the user's selection. This requires some
     * acrobatics with a boolean-array to return the config-update result from
     * another thread.
     */
    private static void updateConfiguration() {
        final boolean[]      result = new boolean[1];
        final CountDownLatch latch  = new CountDownLatch(1);
        
        GuiFactory.getInstance().displayConfigurationDialog(
            new ConfigurationObserver() {
                @Override
                public void configurationDialogClosed(boolean isUpdated) {
                    result[0] = isUpdated;
                    latch.countDown();
                }
            });
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        if (!result[0]) {
            logger.info("missing configuration, cancelled by user");
            System.exit(0);
        }
    }
    
    
    /**
     * Shutdown-hook that closes a database-file.
     */
    private static final class CloseDatabaseFileShutdownHook extends Thread {
        
        private final Data database;
        
        public CloseDatabaseFileShutdownHook(Data database) {
            this.database = database;
            Runtime.getRuntime().addShutdownHook(this);
        }
        
        @Override
        public void run() {
            logger.info("closing database");
            try {
                database.close();
            } catch (IOException e) {
                throw new RuntimeException("unable to close database", e);
            }
        }
    }
}
