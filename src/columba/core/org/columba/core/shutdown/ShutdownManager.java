// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.shutdown;

import org.columba.core.main.MainInterface;
import org.columba.core.util.GlobalResourceLoader;

import java.awt.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Manages all tasks which are responsible for doing clean-up work when shutting
 * down Columba.
 * <p>
 * This includes saving the xml configuration, saving folder data, etc.
 * <p>
 * Tasks use <code>register</code> to the managers shutdown queue.
 * <p>
 * When shutting down Columba, the tasks will be running in the opposite order
 * the have registered at. <br>
 * Currently this is the following: <br>
 * <ul>
 * <li>addressbook folders header cache</li>
 * <li>POP3 header cache</li>
 * <li>email folders header cache</li>
 * <li>core tasks (no core tasks used currently)!</li>
 * <ul>
 * <p>
 * Note, that I used the opposite ordering to make sure that core tasks are
 * executed first. But, currently there are no core tasks available which would
 * demand this behaviour.
 * <p>
 * Saving email folder header cache is running as a {@link Command}. Its
 * therefore a background thread, where we don't know when its finished. This is
 * the reason why we use
 * <code>MainInterface.processor.getTaskManager().count()</code> to check if
 * no more commands are running.
 * <p>
 * Finally, note that the {@link ColumbaServer}is stopped first, then the
 * background manager, afterwards all registered shutdown tasks and finally the
 * xml configuration is saved. Note, that the xml configuration has to be saved
 * <b>after </b> the email folders where saved.
 * 
 * @author fdietz
 */
public class ShutdownManager {

    private static final Logger LOG = Logger
            .getLogger("org.columba.core.shutdown");

    protected static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";

    /**
     * The singleton instance of this class.
     */
    private static ShutdownManager instance;

    /**
     * Indicates whether this ShutdownManager instance is registered as a system
     * shutdown hook.
     */
    private boolean shutdownHook = false;

    /**
     * The thread performing the actual shutdown procedure.
     */
    protected final Thread shutdownThread;

    /**
     * The list of runnable plugins that should be executed on shutdown.
     */
    protected List list = new LinkedList();

    /**
     * This constructor is only to be accessed by getShutdownManager() and by
     * subclasses.
     */
    protected ShutdownManager() {
        shutdownThread = new Thread(new Runnable() {

            public void run() {
                // stop background-manager so it doesn't interfere with
                // shutdown manager
                MainInterface.backgroundTaskManager.stop();

                while (!isShutdownHook()
                        && (MainInterface.processor.getTaskManager().count() > 0)) {
                    // ask user to kill pending running commands or wait
                    Object[] options = {
                            GlobalResourceLoader.getString(RESOURCE_PATH,
                                    "session", "tasks_wait"),
                            GlobalResourceLoader.getString(RESOURCE_PATH,
                                    "session", "tasks_exit")};
                    int n = JOptionPane.showOptionDialog(null,
                            GlobalResourceLoader.getString(RESOURCE_PATH,
                                    "session", "tasks_msg"),
                            GlobalResourceLoader.getString(RESOURCE_PATH,
                                    "session", "tasks_title"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);

                    if (n == 0) {
                        //wait 10 seconds and check for pending commands again
                        //this is useful if a command causes a deadlock
                        for (int i = 0; i < 10; i++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                            }
                        }
                    } else {
                        //don't wait, just continue shutdown procedure,
                        // commands will
                        //be killed
                        break;
                    }
                }

                ShutdownDialog dialog = (ShutdownDialog) openShutdownDialog();

                Iterator iterator = list.iterator();
                Runnable plugin;

                while (iterator.hasNext()) {
                    plugin = (Runnable) iterator.next();

                    try {
                        plugin.run();
                    } catch (Exception e) {
                        LOG.severe(e.getMessage());

                        //TODO: better exception handling
                    }
                }

                //we don't need to check for running commands here because
                // there aren't
                //any, shutdown plugins only use this thread
                dialog.close();
            }
        }, "ShutdownManager");
        setShutdownHook(true);
    }

    /**
     * Registers a runnable plugin that should be executed on shutdown.
     */
    public void register(Runnable plugin) {
        list.add(0, plugin);
    }

    /**
     * Returns whether this ShutdownManager instance runs inside a system
     * shutdown hook.
     */
    public synchronized boolean isShutdownHook() {
        return shutdownHook;
    }

    /**
     * Registers or unregisters this ShutdownManager instance as a system
     * shutdown hook.
     */
    protected synchronized void setShutdownHook(boolean b) {
        if (shutdownHook == b) { return; }

        if (b) {
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        } else {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        }

        shutdownHook = b;
    }

    /**
     * Starts the shutdown procedure.
     */
    public synchronized void shutdown(final int status) {
        setShutdownHook(false);
        new Thread(new Runnable() {

            public void run() {
                shutdownThread.run();
                System.exit(status);
            }
        }, "ShutdownManager").start();
    }

    /**
     * Returns a component notifying the user of the shutdown procedure.
     */
    protected Component openShutdownDialog() {
        JFrame dialog = null;
        try {
            dialog = new ShutdownDialog();
        } catch (Exception e) {
            if (MainInterface.DEBUG) e.printStackTrace();
        }
        return dialog;
    }

    /**
     * Returns the singleton instance of this class.
     */
    public static synchronized ShutdownManager getShutdownManager() {
        if (instance == null) {
            instance = new ShutdownManager();
        }

        return instance;
    }
}