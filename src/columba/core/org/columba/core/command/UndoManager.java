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
package org.columba.core.command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.KeyStroke;

import org.columba.core.action.BasicAction;
import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

public class UndoManager implements ActionListener, WorkerListChangeListener {

	List undoQueue;
	List redoQueue;
	public BasicAction undoAction;
	public BasicAction redoAction;

	DefaultProcessor processor;

	int runningTasks;

	public UndoManager(DefaultProcessor processor) {
		undoQueue = new Vector();
		redoQueue = new Vector();

		this.processor = processor;
		runningTasks = 0;

		
		try
		{
		
		initActions();
		}
		catch ( Exception ex)
		{
			ex.printStackTrace();
		}

		processor.getTaskManager().addWorkerListChangeListener(this);
	}

	public void initActions() {
		// Initialize undo
		undoAction =new BasicAction(
				MailResourceLoader.getString("action", "menu_edit_undo"));
		undoAction.setActionCommand("UNDO");
		undoAction.setSmallIcon(
				ImageLoader.getSmallImageIcon("stock_undo-16.png"));
		undoAction.setLargeIcon(
				ImageLoader.getImageIcon("stock_undo.png"));
		undoAction.setAcceleratorKey(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		undoAction.setMnemonic('T');

		undoAction.setEnabled(false);

		undoAction.addActionListener(this);

		// Initialize redo
		redoAction = new BasicAction(
		 		MailResourceLoader.getString("action", "menu_edit_redo"));
		redoAction.setActionCommand("REDO");
		redoAction.setSmallIcon(
		 		ImageLoader.getSmallImageIcon("stock_redo-16.png"));
		redoAction.setLargeIcon(
				ImageLoader.getImageIcon("stock_redo.png"));
		redoAction.setAcceleratorKey(
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		redoAction.setMnemonic('T');

		redoAction.setEnabled(false);

		redoAction.addActionListener(this);
	}

	public void addToUndo(Command op) {

		switch (op.getCommandType()) {

			case Command.NO_UNDO_OPERATION :
				undoQueue.clear();
				redoQueue.clear();
				redoAction.setEnabled(false);
				updateActions();				
				break;

			case Command.UNDOABLE_OPERATION :
				addCommand(undoQueue, op);
				redoQueue.clear();
				redoAction.setEnabled(false);
				updateActions();				
				
				break;

			case Command.NORMAL_OPERATION :
				break;
		}
	}

	/**
	 * Method addCommand.
	 * 
	 * 
	 * @param vec
	 * @param command
	 */
	protected void addCommand(List vec, Command command) {
		Command nextCommand;

		int p = 0;
		int size = vec.size();

		// Sort in with respect to priority

		while (p != size) {
			nextCommand = (Command) vec.get(p);
			if (nextCommand.getTimeStamp() < command.getTimeStamp())
				p++;
			else
				break;
		}

		vec.add(p, command);
	}

	/**
	 * Method updateActions.
	 * Enables/Disables the Undo/Redo-Actions in respect to the queues and
	 * running tasks
	 */
	protected void updateActions() {
		undoAction.setEnabled((runningTasks == 0) && (undoQueue.size() > 0));
		redoAction.setEnabled((runningTasks == 0) && (redoQueue.size() > 0));
	}

	/**
	 * Method undoLast.
	 * Undos the last executed Command
	 */
	public void undoLast() {
		Command lastUndoOp = (Command) undoQueue.remove(undoQueue.size()-1);
		lastUndoOp.setPriority(Command.DAEMON_PRIORITY);
		lastUndoOp.setSynchronize(true);

		processor.addOp(lastUndoOp, Command.UNDO);		
		addCommand(redoQueue, lastUndoOp);
		updateActions();
	}

	/**
	 * Method redoLast.
	 * Redos the last undone Command
	 */
	public void redoLast() {
		Command lastRedoOp = (Command) redoQueue.remove(0);
		lastRedoOp.setPriority(Command.DAEMON_PRIORITY);
		lastRedoOp.setSynchronize(true);
		processor.addOp(lastRedoOp, Command.REDO);
		addCommand( undoQueue, lastRedoOp);

		updateActions();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("UNDO")) {
			undoLast();
		} else if (command.equals("REDO")) {
			redoLast();
		}
	}

	/**
	 * @see org.columba.core.gui.statusbar.event.WorkerListChangeListener#workerListChanged(WorkerListChangedEvent)
	 */
	public void workerListChanged(WorkerListChangedEvent e) {
		if (e.getType() == WorkerListChangedEvent.SIZE_CHANGED) {
			runningTasks = e.getNewValue();
		}
		updateActions();
	}
	
	
		
	/**
	 * Method getUndoQueue. This method is for testing only!
	 * @return Vector
	 */
	public List getUndoQueue() {
		return undoQueue;
	}

	/**
	 * Method getRedoQueue. This method is for testing only!
	 * @return Vector
	 */
	public List getRedoQueue() {
		return redoQueue;
	}

}
