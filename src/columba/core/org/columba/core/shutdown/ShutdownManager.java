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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Timer;

import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.main.MainInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ShutdownManager implements ActionListener {

	private static int ONE_SECOND = 1000;
	private static int DELAY = ONE_SECOND * 5;

	Vector list;

	Timer timer;

	public ShutdownManager() {
		list = new Vector();
	}

	public void register(TaskInterface plugin) {
		list.add(plugin);
	}

	public void shutdown() {

		// we start from the end, to be sure that
		// the core-plugins are saved as last
		for (int i = list.size() - 1; i >= 0; i--) {
			TaskInterface plugin = (TaskInterface) list.get(i);

			plugin.run();
		}

		timer = new Timer(DELAY, this);
		timer.start();
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		// exit if no task is running anymore
		if (MainInterface.processor.getTaskManager().count() == 0)
			System.exit(1);
		else {
			// ask user to kill pending running tasks or wait
		}

	}

}
