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
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.columba.core.backgroundtask.TaskInterface;
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
 * 
 * @author fdietz
 */
public class ShutdownManager {
    private static final int ONE_SECOND = 1000;
    private static final int DELAY = ONE_SECOND * 4;
    private static final int INITIAL_DELAY = ONE_SECOND * 10;
    public static final int MAINTAINANCE = 0;
    public static final int SHUTDOWN = 1;
    private static int currentDelay;
    private static List list;
    private static Timer delayedTimer;
    private static Timer timer;
    private static int mode = MAINTAINANCE;

    public ShutdownManager() {
        list = new Vector();
    }

    public void register(TaskInterface plugin) {
        list.add(plugin);
    }

    protected static void restartDelayedTimer() {
        if (delayedTimer != null) {
            delayedTimer.stop();
        }

        //		increase "waiting time" (when should we open the dialog the next time)
        currentDelay = currentDelay * 2;

        ColumbaLogger.log.info("current delay=" + currentDelay);

        //		start delayed timer
        delayedTimer = new Timer(currentDelay,
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        delayedTimerCheck();
                    }
                });

        delayedTimer.setInitialDelay(INITIAL_DELAY + currentDelay);
        delayedTimer.start();
    }

    public void shutdown() {
        mode = SHUTDOWN;
        
        // stop columba server
        ColumbaClient.getColumbaServer().stop();

        // stop background-manager so it doesn't interfere with
        // shutdown manager
        MainInterface.backgroundTaskManager.stop();

        // show shutdown dialog
        showSaveFoldersDialog();

        // we start from the end, to be sure that
        // the core-plugins are saved as last
        for (int i = list.size() - 1; i >= 0; i--) {
            TaskInterface plugin = (TaskInterface) list.get(i);

            plugin.run();
        }

        // initialize delay time
        currentDelay = DELAY;
        restartDelayedTimer();

        // start timer
        timer = new Timer(ONE_SECOND,
                new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        defaultTimerCheck();
                    }
                });
        timer.start();
    }

    protected static void showSaveFoldersDialog() {
        JFrame dialog = new JFrame("Exiting Columba...");

        dialog.getContentPane().add(new JButton("Saving Folders..."),
            BorderLayout.CENTER);
        dialog.pack();

        java.awt.Dimension dim = new Dimension(300, 50);
        dialog.setSize(dim);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((screenSize.width / 2) - (dim.width / 2),
            (screenSize.height / 2) - (dim.height / 2));

        dialog.setVisible(true);
    }

    protected static void defaultTimerCheck() {
        // timer with 1 second delay
        // exit if no task is running anymore
        if (MainInterface.processor.getTaskManager().count() == 0) {
            ColumbaLogger.log.info("one second timer exited Columba");

            // save xml configuration 
            new SaveConfigPlugin().run();

            // quit Columba	 
            System.exit(1);
        }
    }

    protected static void delayedTimerCheck() {
        //		delayed timer with increasing delay time
        // to no annoy the user
        // exit if no task is running anymore
        if (MainInterface.processor.getTaskManager().count() == 0) {
            System.exit(1);
        } else {
            // ask user to kill pending running tasks or wait
            Object[] options = { "Wait", "Exit" };
            int n = JOptionPane.showOptionDialog(null,
                    "Some tasks seem to be running. \nWould you like to wait for these to finish or just exit Columba?",
                    "Wait or exit Columba", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (n == 0) {
                // do nothing
                // restart timer with increased delay time
                restartDelayedTimer();
            } else {
                // save xml configuration 
                new SaveConfigPlugin().run();

                // quit Columba
                System.exit(1);
            }
        }
    }

    /**
     * @return
     */
    public static int getMode() {
        return mode;
    }
}