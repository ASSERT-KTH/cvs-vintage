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

package org.columba.core.command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.KeyStroke;

import org.columba.core.action.BasicAction;
import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

public class UndoManager implements ActionListener, WorkerListChangeListener {

	Vector undoQueue;
	Vector redoQueue;
	public BasicAction undoAction;
	public BasicAction redoAction;

	DefaultProcessor processor;

	int runningTasks;

	public UndoManager(DefaultProcessor processor) {
		undoQueue = new Vector();
		redoQueue = new Vector();

		this.processor = processor;
		runningTasks = 0;

		initActions();

		processor.getTaskManager().addWorkerListChangeListener(this);
	}

	public void initActions() {
		undoAction =
			new BasicAction(
				MailResourceLoader.getString("action", "menu_edit_undo"),
				MailResourceLoader.getString("action", "menu_edit_undo"),
				"UNDO",
				ImageLoader.getSmallImageIcon("stock_undo-16.png"),
				ImageLoader.getImageIcon("stock_undo.png"),
				'T',
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		undoAction.setEnabled(false);

		undoAction.addActionListener(this);

		redoAction =
			new BasicAction(
				MailResourceLoader.getString("action", "menu_edit_redo"),
				MailResourceLoader.getString("action", "menu_edit_redo"),
				"REDO",
				ImageLoader.getSmallImageIcon("stock_redo-16.png"),
				ImageLoader.getImageIcon("stock_redo.png"),
				'T',
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
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
	protected void addCommand(Vector vec, Command command) {
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

		vec.insertElementAt(command, p);
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
	public Vector getUndoQueue() {
		return undoQueue;
	}

	/**
	 * Method getRedoQueue. This method is for testing only!
	 * @return Vector
	 */
	public Vector getRedoQueue() {
		return redoQueue;
	}

}
