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

package org.columba.core.gui.statusbar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.columba.core.command.TaskManager;
import org.columba.core.command.TaskManagerEvent;
import org.columba.core.command.TaskManagerListener;
import org.columba.core.command.Worker;
import org.columba.core.gui.toolbar.ToolbarButton;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.ConnectionStateImpl;

/**
 * A status bar intended to be displayed at the bottom of each window.
 * <p>
 * Implementation notes:
 * <p>
 * An update timer is used to only update the statusbar, every xx seconds. As a
 * nice side-effect, all swing method calls happen in the awt-event dispatcher
 * thread automatically. If we don't do this, we have to wrap every swing method
 * in a Runnable interface and execute it using SwingUtilities.invokeLater().
 * <p>
 * Note, that without an update timer, the statusbar text and most importantly
 * the progressbar are updated very frequently, using very small updates. But,
 * because these are called using invokeLater(), all have to be placed in the
 * awt-event dispatcher queue. This makes things very slow. We discovered, when
 * moving around 1000 messages and updating the progressbar for every message,
 * it will take more time to update the statusbar than actually moving the
 * messages.
 * <p>
 * There's another Timer, addWorkerTimer which makes sure that only Workers who
 * are alive for at most 2000 ms will appear in the statusbar. This prevents the
 * statusbar from flicker, caused by many smaller tasks which usually tend to
 * hide the parent task. For example when downloaded POP3 messages and using
 * filters which move message to different folders. Without the addWorkerTimer
 * you would see all those little move tasks. Instead now, you only see the POP3
 * tasks which is much more comfortable for the user.
 * <p>
 * There's yet another Timer ;-), clearTextTimer which automatically clears the 
 * statusbar after a delay of 2000 ms. 
 */
public class StatusBar extends JComponent implements TaskManagerListener,
		ActionListener, ChangeListener {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.statusbar");

	/**
	 * update status every 10 ms
	 */
	private static final int UPDATE_TIMER_INTERVAL = 10;

	/**
	 * Constant definining the delay used when using
	 * clearDisplayTextWithDelay(). Defined to be 2000 millisec.
	 */
	private static final int CLEAR_TIMER_DELAY = 2000;

	/**
	 * time to wait until statusbar will show a tasks progress
	 */
	private static final int ADDWORKER_TIMER_INTERVAL = 2000;

	protected static Icon onlineIcon = ImageLoader.getImageIcon("online.png");

	protected static Icon offlineIcon = ImageLoader.getImageIcon("offline.png");

	/**
	 * showing status messages
	 */
	private JLabel label;

	/**
	 * showing progress info
	 */
	private JProgressBar progressBar;

	private Border border;

	private JPanel mainRightPanel;

	/**
	 * button opening task manager dialog
	 */
	private JButton taskButton;

	private JPanel leftMainPanel;

	/**
	 * Currently displayed worker
	 */
	private Worker displayedWorker;

	/**
	 * manager of all running tasks
	 */
	private TaskManager taskManager;

	/**
	 * timer used by the spinner component showing tasks activity (right-most
	 * component on the toolbar)
	 */
	private ImageSequenceTimer imageSequenceTimer;

	/**
	 * connection state button
	 */
	private JButton onlineButton;

	/** Timer to use when clearing status bar text after a certain timeout */
	private Timer clearTextTimer;

	/**
	 * Timer makes sure that statusbar is only updated every xx ms, to make that
	 * its not getting flooded with too many update notifications
	 */
	private Timer updateTimer;

	private Timer addWorkerTimer;

	/**
	 * last displayed message
	 */
	private String lastMessage;

	private TaskManagerEvent currentEvent;

	public StatusBar(TaskManager tm) {
		taskManager = tm;
		tm.addTaskManagerListener(this);
		ConnectionStateImpl.getInstance().addChangeListener(this);

		setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

		initComponents();

		layoutComponents();

		// update connection state
		stateChanged(null);

		clearTextTimer = new Timer(CLEAR_TIMER_DELAY, this);

		// init update timer
		updateTimer = new Timer(UPDATE_TIMER_INTERVAL, this);
		//updateTimer.start();

		addWorkerTimer = new Timer(ADDWORKER_TIMER_INTERVAL, this);

	}

	/**
	 * init components
	 */
	private void initComponents() {
		imageSequenceTimer = new ImageSequenceTimer(taskManager);

		label = new JLabel("");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);

		onlineButton = new ToolbarButton();
		onlineButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		onlineButton.setRolloverEnabled(true);
		onlineButton.setActionCommand("ONLINE");
		onlineButton.addActionListener(this);

		progressBar = new JProgressBar(0, 100);

		progressBar.setStringPainted(false);
		progressBar.setBorderPainted(false);
		progressBar.setValue(0);

		taskButton = new ToolbarButton();
		taskButton.setIcon(ImageLoader.getImageIcon("group_small.png"));
		taskButton.setToolTipText("Show list of running tasks");
		taskButton.setRolloverEnabled(true);
		taskButton.setActionCommand("TASKMANAGER");
		taskButton.addActionListener(this);

		taskButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	}

	/**
	 * layout components
	 */
	protected void layoutComponents() {
		setLayout(new BorderLayout());

		leftMainPanel = new JPanel();
		leftMainPanel.setLayout(new BorderLayout());

		JPanel taskPanel = new JPanel();
		taskPanel.setLayout(new BorderLayout());

		Border border = getDefaultBorder();
		Border margin = new EmptyBorder(0, 0, 0, 2);

		taskPanel.setBorder(new CompoundBorder(border, margin));

		taskPanel.add(taskButton, BorderLayout.CENTER);

		leftMainPanel.add(taskPanel, BorderLayout.WEST);
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BorderLayout());
		margin = new EmptyBorder(0, 10, 0, 10);
		labelPanel.setBorder(new CompoundBorder(border, margin));

		margin = new EmptyBorder(0, 0, 0, 2);
		labelPanel.add(label, BorderLayout.CENTER);

		leftMainPanel.add(labelPanel, BorderLayout.CENTER);

		add(leftMainPanel, BorderLayout.CENTER);

		mainRightPanel = new JPanel();
		mainRightPanel.setLayout(new BorderLayout());

		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());
		progressPanel.setBorder(new CompoundBorder(border, margin));

		progressPanel.add(progressBar, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());

		rightPanel.add(progressPanel, BorderLayout.CENTER);

		JPanel onlinePanel = new JPanel();
		onlinePanel.setLayout(new BorderLayout());
		onlinePanel.setBorder(new CompoundBorder(border, margin));

		onlinePanel.add(onlineButton, BorderLayout.CENTER);

		rightPanel.add(onlinePanel, BorderLayout.EAST);
		add(rightPanel, BorderLayout.EAST);
	}

	public Border getDefaultBorder() {
		return UIManager.getBorder("TableHeader.cellBorder");
	}

	/**
	 * Display status message. Used when moving the mouse in the menuitem. Its
	 * used for displaying menuitem tooltip messages.
	 * 
	 * @param message
	 */
	public void displayTooltipMessage(String message) {
		label.setText(message);
	}

	public void workerAdded(TaskManagerEvent e) {

		if (getDisplayedWorker() == null) {

			currentEvent = e;

			if (taskManager.getWorkers().length == 1) {
				setDisplayedWorker(currentEvent.getWorker());

				// update text and progress bar
				updateTimer.restart();

				addWorkerTimer.stop();
			} else {

				addWorkerTimer.restart();
			}

		}
	}

	public void workerRemoved(TaskManagerEvent e) {

		if (e.getWorker() == displayedWorker) {

			// remember last message
			lastMessage = e.getWorker().getDisplayText();

			// immediately update text and progress bar
			//updateGui();

			Worker[] workers = taskManager.getWorkers();
			setDisplayedWorker(workers.length > 0 ? workers[0] : null);
		}

		// if only one task left
		if (taskManager.getWorkers().length == 0) {

			// stop update timer
			updateTimer.stop();

			// set text
			label.setText(lastMessage);

			// clear text with delay
			clearTextTimer.restart();
		}

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == updateTimer) {
			// update timer event
			updateGui();
			return;
		}

		if (e.getSource() == clearTextTimer) {

			// clear label
			label.setText("");

			// stop clear timer
			clearTextTimer.stop();

			return;
		}

		if (e.getSource() == addWorkerTimer) {

			if (taskManager.exists(currentEvent.getWorker())) {

				setDisplayedWorker(currentEvent.getWorker());

				// update text and progress bar
				updateTimer.restart();

				addWorkerTimer.stop();

				return;
			} else {

				addWorkerTimer.stop();
				return;
			}
		}

		String command = e.getActionCommand();

		if (command.equals("ONLINE")) {
			ConnectionStateImpl.getInstance().setOnline(
					!ConnectionStateImpl.getInstance().isOnline());
		} else if (command.equals("TASKMANAGER")) {
			TaskManagerDialog.createInstance();
		} else if (command.equals("CANCEL_ACTION")) {
			displayedWorker.cancel();
		}
	}

	/**
	 * Update statusbar with currently selected worker status.
	 * <p>
	 * Runs in awt-event dispatcher thread
	 */
	private void updateGui() {
		//System.out.println("update-gui");

		if (displayedWorker != null) {
			label.setText(displayedWorker.getDisplayText());
			progressBar.setValue(displayedWorker.getProgressBarValue());
			progressBar.setMaximum(displayedWorker.getProgessBarMaximum());
		}

	}

	/**
	 * Sets the worker to be displayed.
	 */
	protected void setDisplayedWorker(Worker w) {
		displayedWorker = w;

	}

	/**
	 * Returns the worker currently displayed.
	 */
	public Worker getDisplayedWorker() {
		return displayedWorker;
	}

	/**
	 * Returns the imageSequenceTimer.
	 * 
	 * @return ImageSequenceTimer
	 */
	public ImageSequenceTimer getImageSequenceTimer() {
		return imageSequenceTimer;
	}

	/**
	 * Returns the task manager this status bar is attached to.
	 */
	public TaskManager getTaskManager() {
		return taskManager;
	}

	public void stateChanged(ChangeEvent e) {
		if (ConnectionStateImpl.getInstance().isOnline()) {
			onlineButton.setIcon(onlineIcon);
			//TODO: i18n
			onlineButton.setToolTipText("You are in ONLINE state");
		} else {
			onlineButton.setIcon(offlineIcon);
			//TODO: i18n
			onlineButton.setToolTipText("You are in OFFLINE state");
		}
	}
}