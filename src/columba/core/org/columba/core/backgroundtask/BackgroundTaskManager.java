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
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Timer;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;

/**
 * @author fdietz
 *
 * This manager runs in background.
 * 
 * If the user doesn't do anything with Columba, it starts some
 * cleanup workers, like saving configuration, saving header-cache, etc.
 *
 */
public class BackgroundTaskManager implements ActionListener {

	// one second (=1000 ms)
	private int ONE_SECOND = 1000;

	// sleep 5 minutes
	private int SLEEP_TIME = ONE_SECOND * 60 * 5;

	private Timer timer;

	private Vector list;

	public BackgroundTaskManager() {
		super();

		list = new Vector();

		timer = new Timer(SLEEP_TIME, this);
		timer.start();

	}

	public void register(TaskInterface task) {
		list.add(task);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		ColumbaLogger.log.debug("is their any task running?");

		// test if a task is already running 
		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		if ((queue.peekEvent() == null)
			&& (MainInterface.processor.getTaskManager().count() == 0)) {
			// no java task running -> start background tasks

			ColumbaLogger.log.info("starting background tasks...");
			runTasks();
		}

	}

	public void runTasks() {
		for (Enumeration e = list.elements(); e.hasMoreElements();) {
			TaskInterface task = (TaskInterface) e.nextElement();
			task.run();
		}
	}

}
