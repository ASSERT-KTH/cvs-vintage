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

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.command.*;
import org.columba.core.gui.statusbar.event.WorkerStatusChangeListener;
import org.columba.core.gui.statusbar.event.WorkerStatusChangedEvent;
import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.help.HelpManager;
import org.columba.core.main.MainInterface;
import org.columba.core.util.GlobalResourceLoader;

import org.columba.mail.gui.config.filter.FilterTransferHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

/**
 * Dialog showing all running tasks.
 * <p>
 * Lets the user cancel or kill tasks.
 * 
 * @author fdietz
 */
public class TaskManagerDialog extends JDialog
		implements
			TaskManagerListener,
			ActionListener,
			WorkerStatusChangeListener {

	private static TaskManagerDialog instance;

	private TaskManager taskManager;
	private JButton cancelButton;
	private JButton killButton;
	private JList list;

	public TaskManagerDialog(TaskManager tm) {
		super((JFrame) null, "Task Manager", false);
		this.taskManager = tm;

		initComponents();
		pack();
		setLocationRelativeTo(null);

		tm.addTaskManagerListener(this);
	}

	public static TaskManagerDialog createInstance() {
		if (instance == null) {
			instance = new TaskManagerDialog(MainInterface.processor
					.getTaskManager());
		}

		if (!instance.isVisible()) {
			instance.setVisible(true);
		}
		instance.toFront();

		return instance;
	}

	public void initComponents() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		getContentPane().add(mainPanel);

		cancelButton = new ButtonWithMnemonic("Cancel");
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);

		killButton = new ButtonWithMnemonic("Kill");
		killButton.setActionCommand("KILL");
		killButton.setEnabled(false);
		killButton.addActionListener(this);

		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel eastPanel = new JPanel(gridBagLayout);
		eastPanel.setLayout(gridBagLayout);
		mainPanel.add(eastPanel, BorderLayout.EAST);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints(cancelButton, c);
		eastPanel.add(cancelButton);

		Component strut1 = Box.createRigidArea(new Dimension(30, 6));
		gridBagLayout.setConstraints(strut1, c);
		eastPanel.add(strut1);

		gridBagLayout.setConstraints(killButton, c);
		eastPanel.add(killButton);

		Component glue = Box.createVerticalGlue();
		glue = Box.createVerticalGlue();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		gridBagLayout.setConstraints(glue, c);
		eastPanel.add(glue);

		// centerpanel
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
		DefaultListModel model = new DefaultListModel();
		Worker[] workers = taskManager.getWorkers();
		for (int i = 0; i < workers.length; i++) {
			model.addElement(workers[i]);
		}
		list = new JList(model);
		list.setCellRenderer(new TaskRenderer());

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(300, 250));
		scrollPane.getViewport().setBackground(Color.white);
		scrollPane.setTransferHandler(new FilterTransferHandler(scrollPane));
		centerPanel.add(scrollPane);

		mainPanel.add(centerPanel);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		ButtonWithMnemonic closeButton = new ButtonWithMnemonic(
				GlobalResourceLoader.getString("", "global", "close"));
		closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);

		ButtonWithMnemonic helpButton = new ButtonWithMnemonic(
				GlobalResourceLoader.getString("", "global", "help"));
		buttonPanel.add(helpButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(closeButton);
		getRootPane().registerKeyboardAction(this, "CLOSE",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		// associate with JavaHelp
		HelpManager.getHelpManager().enableHelpOnButton(helpButton,
				"organising_and_managing_your_email_3");
		HelpManager.getHelpManager().enableHelpKey(getRootPane(),
				"organising_and_managing_your_email_3");
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CLOSE")) {
			setVisible(false);
		} else if (action.equals("CANCEL")) {
			Worker worker = (Worker) list.getSelectedValue();
			worker.setCancel(true);

		} else if (action.equals("KILL")) {

		}
	}

	public void workerAdded(final TaskManagerEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Worker worker = e.getWorker();

				worker.addWorkerStatusChangeListener(TaskManagerDialog.this);

				((DefaultListModel) list.getModel()).addElement(e.getWorker());
			}
		});
	}

	public void workerRemoved(final TaskManagerEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Worker worker = e.getWorker();

				((DefaultListModel) list.getModel()).removeElement(worker);
			}
		});
	}

	public void workerStatusChanged(WorkerStatusChangedEvent e) {
		final WorkerStatusChangedEvent event = e;

		switch (e.getType()) {

			case WorkerStatusChangedEvent.DISPLAY_TEXT_CHANGED :
				break;

			case WorkerStatusChangedEvent.DISPLAY_TEXT_CLEARED :
				break;

			case WorkerStatusChangedEvent.PROGRESSBAR_MAX_CHANGED :
				break;

			case WorkerStatusChangedEvent.PROGRESSBAR_VALUE_CHANGED :
				break;

			case WorkerStatusChangedEvent.FINISHED :
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// force repaint
				DefaultListModel m = ((DefaultListModel) list.getModel());
				int index = m.indexOf(event.getSource());
				if (index != -1)
					m.setElementAt(event.getSource(), index);

			}
		});

	}
}