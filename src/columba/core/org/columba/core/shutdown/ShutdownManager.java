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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.columba.core.backgroundtask.TaskInterface;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ShutdownManager {

	private static int ONE_SECOND = 1000;
	private static int DELAY = ONE_SECOND * 4;
	private static int INITIAL_DELAY = ONE_SECOND * 10;

	private static int currentDelay;

	private static Vector list;

	private static Timer delayedTimer;
	private static Timer timer;

	public ShutdownManager() {
		list = new Vector();
	}

	public void register(TaskInterface plugin) {
		list.add(plugin);
	}

	protected static void restartDelayedTimer() {
		if (delayedTimer != null)
			delayedTimer.stop();

		//		increase "waiting time" (when should we open the dialog the next time)
		currentDelay = currentDelay * 2;

		if (MainInterface.DEBUG)
			ColumbaLogger.log.debug("current delay=" + currentDelay);
			
		//		start delayed timer
		delayedTimer = new Timer(currentDelay, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				delayedTimerCheck();
			}
		});
		
		delayedTimer.setInitialDelay(INITIAL_DELAY+currentDelay);
		delayedTimer.start();
	}

	public void shutdown() {

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
		timer = new Timer(ONE_SECOND, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				defaultTimerCheck();
			}
		});
		timer.start();
	}

	protected static void showSaveFoldersDialog() {
		JFrame dialog = new JFrame("Exiting Columba...");

		dialog.getContentPane().add(
			new JButton("Saving Folders..."),
			BorderLayout.CENTER);
		dialog.pack();

		java.awt.Dimension dim = new Dimension(300, 50);
		dialog.setSize(dim);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setLocation(
			screenSize.width / 2 - dim.width / 2,
			screenSize.height / 2 - dim.height / 2);

		dialog.setVisible(true);
	}

	protected static void defaultTimerCheck() {
		// timer with 1 second delay

		// exit if no task is running anymore
		if (MainInterface.processor.getTaskManager().count() == 0) {
			if (MainInterface.DEBUG)
				ColumbaLogger.log.debug("one second timer exited Columba");

			System.exit(1);
		}
	}

	protected static void delayedTimerCheck() {
		//		delayed timer with increasing delay time
		// to no annoy the user

		// exit if no task is running anymore
		if (MainInterface.processor.getTaskManager().count() == 0)
			System.exit(1);
		else {
			// ask user to kill pending running tasks or wait

			Object[] options = { "Wait", "Exit" };
			int n =
				JOptionPane.showOptionDialog(
					null,
					"Some tasks seem to be running. \nWould you like to wait for these to finish or just exit Columba?",
					"Wait or exit Columba",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			if (n == 0) {
				// do nothing

				// restart timer with increased delay time
				
				restartDelayedTimer();
				
				/*
				delayedTimer.stop();
				delayedTimer = new Timer(currentDelay, this);
				delayedTimer.start();
				*/
				/*
				delayedTimer.setDelay(currentDelay);
				delayedTimer.restart();
				*/
			} else {
				// quit Columba
				System.exit(1);
			}
		}

	}
}
