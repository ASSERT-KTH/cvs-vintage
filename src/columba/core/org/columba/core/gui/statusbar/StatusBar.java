// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.statusbar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.columba.core.command.TaskManager;
import org.columba.core.command.Worker;
import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.gui.statusbar.event.WorkerStatusChangeListener;
import org.columba.core.gui.statusbar.event.WorkerStatusChangedEvent;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.ToolbarButton;
import org.columba.mail.gui.action.BasicAction;
import org.columba.mail.util.MailResourceLoader;

public class StatusBar
	extends JComponent
	implements WorkerListChangeListener, ActionListener, WorkerStatusChangeListener {

	private JLabel label;
	private JProgressBar progressBar;

	private Border border;

	private JPanel mainRightPanel;
	private JButton taskButton;
	private JPanel leftMainPanel;

	private int displayedWorkerIndex;
	private int workerListSize;
	private Worker displayedWorker;

	private TaskManager taskManager;

	private BasicAction cancelAction;
	private ImageSequenceTimer imageSequenceTimer;

	private JButton onlineButton;

	private boolean online = true;

	public StatusBar(TaskManager tm) {
		taskManager = tm;
		tm.addWorkerListChangeListener(this);

		imageSequenceTimer = new ImageSequenceTimer();

		setBorder( BorderFactory.createEmptyBorder(0,2,0,2) );
		
		displayedWorkerIndex = 0;
		workerListSize = 0;
		label = new JLabel("");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);

		onlineButton = new ToolbarButton(ImageLoader.getImageIcon("online.png"));
		onlineButton.setToolTipText("You are in ONLINE state");
		onlineButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		onlineButton.setRolloverEnabled(true);
		onlineButton.setActionCommand("ONLINE");
		onlineButton.addActionListener(this);

		progressBar = new JProgressBar(0, 100);
		
		//progressBar.setAlignmentX(Component.RIGHT_ALIGNMENT);
		//progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
		progressBar.setStringPainted(false);
		progressBar.setBorderPainted(false);
		progressBar.setValue(0);

		taskButton = new ToolbarButton();
		taskButton.setIcon(ImageLoader.getImageIcon("group_small.png"));
		taskButton.setToolTipText("Show list of running tasks");
		taskButton.setRolloverEnabled(true);
		//taskButton.setMargin(new Insets(0, 0, 0, 0));
		taskButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		//taskButton.setBorder(null);
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

		initActions();

	}

	public Border getDefaultBorder() {
		
		if (UIManager.getBorder("TableHeader.cellBorder") != null) {
			return UIManager.getBorder("StatusBar.border");
		} else
			return UIManager.getBorder("TableHeader.cellBorder");

	}

	public void displayTooltipMessage(String message) {

	}

	protected void setTaskCount(int i) {
		final int n = i;

		Runnable run = new Runnable() {
			public void run() {
				//taskButton.setText("Tasks: " + n);

			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setText(String s) {
		final String str = s;

		Runnable run = new Runnable() {
			public void run() {
				label.setText(str);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setMaximum(int i) {
		final int n = i;

		Runnable run = new Runnable() {
			public void run() {
				progressBar.setValue(0);
				progressBar.setMaximum(n);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setMaximumAndValue(int v, int m) {
		final int max = m;
		final int val = v;

		Runnable run = new Runnable() {
			public void run() {
				progressBar.setValue(val);
				progressBar.setMaximum(max);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setValue(int i) {
		final int n = i;

		Runnable run = new Runnable() {
			public void run() {
				progressBar.setValue(n);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void displayWorker(int index) {
		// set to default state
		setMaximumAndValue(100, 100);
		setText("");

		// now switch to worker
		Worker w = taskManager.get(index);

		setText(w.getDisplayText());
		setMaximumAndValue(w.getProgressBarValue(), w.getProgessBarMaximum());

		if (displayedWorker != null) {
			displayedWorker.removeWorkerStatusChangeListener(this);
		}

		w.addWorkerStatusChangeListener(this);
		displayedWorker = w;
	}

	public void workerListChanged(WorkerListChangedEvent e) {

		if (e.getType() == WorkerListChangedEvent.SIZE_CHANGED) {
			workerListSize = e.getNewValue();

			setTaskCount(workerListSize);
			if (displayedWorkerIndex > workerListSize - 1)
				displayedWorkerIndex = workerListSize - 1;
			if (displayedWorkerIndex < 0)
				displayedWorkerIndex = 0;

			if ((workerListSize > 0) && (e.getOldValue() == 0)) {
				displayWorker(0);
			}

			/*
			if( (workerListSize > 1) && (e.getOldValue() <= 1) ) {
				right.setEnabled( true );
			}
			*/

			if (workerListSize > 0) {
				cancelAction.setEnabled(true);
				imageSequenceTimer.start();
			} else {
				cancelAction.setEnabled(false);
				setMaximumAndValue(100,100);
				imageSequenceTimer.stop();
			}
		}

	}

	public void workerStatusChanged(WorkerStatusChangedEvent e) {
		switch (e.getType()) {

			case WorkerStatusChangedEvent.DISPLAY_TEXT_CHANGED :
				setText((String) e.getNewValue());
				break;

			case WorkerStatusChangedEvent.PROGRESSBAR_MAX_CHANGED :
				setMaximum(((Integer) e.getNewValue()).intValue());
				break;

			case WorkerStatusChangedEvent.PROGRESSBAR_VALUE_CHANGED :
				setValue(((Integer) e.getNewValue()).intValue());
				break;

			case WorkerStatusChangedEvent.FINISHED :
				if (workerListSize > 0)
					displayWorker(0);

		}
	}

	protected void initActions() {

		cancelAction =
			new BasicAction(
				MailResourceLoader.getString(
					"menu",
					"mainframe",
					"menu_file_cancel"),
				MailResourceLoader.getString(
					"menu",
					"mainframe",
					"menu_file_cancel"),
				"CANCEL_ACTION",
				ImageLoader.getSmallImageIcon("stock_stop-16.png"),
				ImageLoader.getImageIcon("stock_stop.png"),
				'0',
				null,
				false);

		cancelAction.setEnabled(false);
		cancelAction.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("ONLINE")) {
			if (online == false) {
				onlineButton.setIcon(ImageLoader.getImageIcon("online.png"));
				onlineButton.setToolTipText("You are in ONLINE state");
				online = true;
			} else {
				onlineButton.setIcon(ImageLoader.getImageIcon("offline.png"));
				onlineButton.setToolTipText("You are in OFFLINE state");
				online = false;
			}
		}
		/*
		if (command.equals(left.getActionCommand())) {
			displayedWorkerIndex--;
			if (displayedWorkerIndex == 0) {
				left.setEnabled(false);
			}
			if( displayedWorkerIndex < workerListSize-1 ) {
				right.setEnabled( true );
			}
		
			displayWorker(displayedWorkerIndex);
		
		} else if (command.equals(right.getActionCommand())) {
			displayedWorkerIndex++;
			if (displayedWorkerIndex == workerListSize - 1) {
				right.setEnabled(false);
			}
			if( displayedWorkerIndex > 0 ) {
				left.setEnabled( true );
			}
			displayWorker(displayedWorkerIndex);
		} else 
		*/
		if (command.equals("CANCEL_ACTION")) {
			displayedWorker.cancel();
		}

	}
	/**
	 * Returns the cancelAction.
	 * @return BasicAction
	 */
	public BasicAction getCancelAction() {
		return cancelAction;
	}

	/**
	 * Returns the imageSequenceTimer.
	 * @return ImageSequenceTimer
	 */
	public ImageSequenceTimer getImageSequenceTimer() {
		return imageSequenceTimer;
	}

}