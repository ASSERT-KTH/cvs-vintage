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
package org.columba.core.backgroundtask;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.columba.api.backgroundtask.IBackgroundTaskManager;
import org.columba.core.command.TaskManager;

/**
 * This manager runs in background.
 * <p>
 * If the user doesn't do anything with Columba, it starts some cleanup
 * workers, like saving configuration, saving header-cache, etc.
 *
 * @author fdietz
 */
public class BackgroundTaskManager implements ActionListener, IBackgroundTaskManager {

    private static final Logger LOG = Logger.getLogger("org.columba.api.backgroundtask");

    // one second (=1000 ms)
    private static final int ONE_SECOND = 1000;

    // sleep 5 minutes
    private static final int SLEEP_TIME = ONE_SECOND * 60 * 5;

    private Timer timer;

    private List list;
    
    private static BackgroundTaskManager instance = new BackgroundTaskManager();

    public BackgroundTaskManager() {
        super();

        list = new Vector();

        timer = new Timer(SLEEP_TIME, this);
        timer.start();
    }
    
    public static BackgroundTaskManager getInstance() {
    	return instance;
    }

    /**
	 * @see org.columba.api.backgroundtask.IBackgroundTaskManager#register(java.lang.Runnable)
	 */
    public void register(Runnable runnable) {
        list.add(runnable);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        // test if a task is already running
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();

        if ((queue.peekEvent() == null) && (TaskManager.getInstance().count() == 0)) {
            // no java task running -> start background tasks
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Starting background tasks...");
            }
            runTasks();
        }
    }

    public void runTasks() {
        for (Iterator it = list.iterator(); it.hasNext();) {
            Runnable task = (Runnable) it.next();
            task.run();
        }
    }

    public void stop() {
        timer.stop();
    }
}
