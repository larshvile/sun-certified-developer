package suncertify.gui;

import static suncertify.config.TextResolver.TextKey.*; 
import static suncertify.gui.SwingHelper.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import suncertify.config.TextResolver;


/**
 * Main GUI-window for the application's server.
 * 
 * @author Lars Hvile
 */
final class ServerFrame extends JFrame {
    
    private static final int  PREFERRED_WIDTH = 480;
    private static final int  PREFERRED_HEIGHT = 250;
    
    private static final long serialVersionUID = 1L;
    
    private static final TextResolver textResolver = TextResolver.getInstance();
    private static final Logger logger = Logger.getLogger(ServerFrame.class
                                                              .getName());


    /**
     * Class-constructor.
     */
    public ServerFrame() {
        logger.info("creating server GUI");
        initComponents();
        setVisible(true);
    }
    
    
    private void initComponents() {
        setTitle(textResolver.get(SERVER_FRAME_TITLE));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        initMenu();
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
                        if (displayYesNoPrompt(ServerFrame.this, textResolver
                                .get(MESSAGE_CONFIRM_SERVER_EXIT))) {
                            System.exit(0);
                        }
                    }
                }));

        return menu;
    }


    private JMenu createEditMenu() {

        final JMenu menu = new JMenu(textResolver.get(MENU_EDIT));
        menu.setMnemonic(KeyEvent.VK_E);

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
                    displayMessage(ServerFrame.this, textResolver
                            .get(MESSAGE_CONFIG_UPDATED_AT_RUNTIME));
                }
            }
        });
    }
}
