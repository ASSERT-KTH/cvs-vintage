//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.shutdown;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.Timer;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.ColumbaClient;
import org.columba.core.main.MainInterface;

/**
 * Manages all tasks which are responsible for doing clean-up work
 * when shutting down Columba.
 * <p>
 * This includes saving the xml configuration, saving folder data, etc.
 * <p>
 * Tasks use <code>register</code> to the managers shutdown queue.
 * <p>
 * When shutting down Columba, the tasks will be running in the opposite
 * order the have registered at.<br>
 * Currently this is the following:<br>
 * <ul>
 *  <li>addressbook folders header cache</li>
 *  <li>POP3 header cache</li>
 *  <li>email folders header cache</li>
 *  <li>core tasks (no core tasks used currently)!</li>
 * <ul>
 * <p>
 * Note, that I used the opposite ordering to make sure that core tasks are
 * executed first. But, currently there are no core tasks available which
 * would demand this behaviour.
 * <p>
 * Saving email folder header cache is running as a {@link Command}. Its therefore
 * a background thread, where we don't know when its finished. This is the reason
 * why we use <code>MainInterface.processor.getTaskManager().count()</code> to check
 * if no more commands are running. This happens in <code>defaultTimerCheck()</code>
 * and <code>delayedTimerCheck()</code>.  
 * <p>
 * <code>delayedTimerCheck()</code> is more special, because its a fail-safe timer,
 * which asks the user to kill all pending running tasks, instead of waiting
 * forever for tasks which are broken for whatever reason.
 * <p>
 * Finally, note that the {@link ColumbaServer} is stopped first, then the
 * background manager, afterwards all registered shutdown tasks and finally
 * the xml configuration is saved. Note, that the xml configuration has to
 * be saved <b>after</b> the email folders where saved.
 *  
 * @author fdietz
 */
public class ShutdownManager {
    
    /**
     * The singleton instance of this class.
     */
    private static ShutdownManager instance;
    
    /**
     * The list of runnable plugins that should be executed on shutdown.
     */
    protected List list = new LinkedList();

    /**
     * This constructor is only to be accessed by getShutdownManager() and
     * by subclasses. It registers the instance as a system shutdown hook.
     */
    protected ShutdownManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                // stop columba server
                ColumbaClient.getColumbaServer().stop();

                // stop background-manager so it doesn't interfere with
                // shutdown manager
                MainInterface.backgroundTaskManager.stop();
                
                // wait as long as there are background tasks executing
                // but not longer than 5 seconds
                for (int i = 0;
                     MainInterface.processor.getTaskManager().count() > 0 && i < 5;
                     i++) {
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch(InterruptedException ie) {
                        break;
                    }
                }

                Iterator iterator = list.iterator();
                Runnable plugin;
                while (iterator.hasNext()) {
                    plugin = (Runnable) iterator.next();
                    try {
                        plugin.run();
                    } catch(Exception e) {
                        ColumbaLogger.log.severe(e.getMessage());
                    }
                }
            }
        }, "ShutdownManager"));
    }

    /**
     * Registers a runnable plugin that should be executed on shutdown.
     */
    public void register(Runnable plugin) {
        ColumbaLogger.log.fine("Registering new shutdown plugin " + 
                plugin.getClass().getName());
        list.add(0, plugin);
    }

    /**
     * Returns the singleton instance of this class.
     */
    public synchronized static ShutdownManager getShutdownManager() {
        if (instance == null) {
            instance = new ShutdownManager();
        }
        return instance;
    }
}
