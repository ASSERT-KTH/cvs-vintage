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
import java.util.Vector;

import org.columba.core.gui.FrameController;
import org.columba.core.gui.statusbar.event.WorkerStatusChangeListener;
import org.columba.core.gui.statusbar.event.WorkerStatusChangedEvent;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.SwingWorker;

public class Worker extends SwingWorker implements WorkerStatusController {
	protected Command op;
	protected int operationMode;
	protected DefaultProcessor boss;

	protected String displayText;
	protected int progressBarMax;
	protected int progressBarValue;
	
	protected boolean cancelled;

	protected Vector workerStatusChangeListeners;

	private GuiUpdater guiUpdater;

	private int timeStamp;

	public Worker(DefaultProcessor parent) {
		super();
		this.op = op;
		this.boss = parent;

		guiUpdater = parent.getGuiUpdater();

		displayText = "";
		progressBarValue = 0;
		progressBarMax = 0;

		cancelled = false;

		workerStatusChangeListeners = new Vector();
	}

	public void process(Command op, int operationMode, int timeStamp) {
		this.op = op;
		this.operationMode = operationMode;
		this.timeStamp = timeStamp;
	}

	public int getPriority() {
		return op.getPriority();
	}

	private void returnLocks(int opMode) {
		op.releaseAllFolderLocks(operationMode);
	}

	/*
	private void setWorkerStatusController() {
		FolderController[] controller = op.getFolderLocks();
		int size = Array.getLength(controller);
		
		for( int i=0; i<size; i++ ) {
			controller[i].setWorkerStatusController(this);
		}		
	}	
	*/

	public Object construct() {
		//setWorkerStatusController();

		try {
			op.process(this, operationMode);
			if( !cancelled() && (operationMode == Command.FIRST_EXECUTION)) boss.getUndoManager().addToUndo(op);
		} 
		catch( CommandCancelledException e ) {
			ColumbaLogger.log.debug( "Command cancelled" );
		} catch (Exception e) {
			// Must create a ExceptionProcessor
			e.printStackTrace();
			
			ExceptionDialog dialog = new ExceptionDialog();
			dialog.showDialog(e);
			
		}

		returnLocks(operationMode);

		return null;
	}

	public void finished() {
		if (guiUpdater.amIGuiUpdater(this) && !cancelled()) {

			try {
				op.updateGUI();
				setDisplayText(displayText + " done");
			} catch (Exception e) {
				// Must create a ExceptionProcessor
				e.printStackTrace();
			}
		}
		unregister();
		boss.operationFinished(op, this);
	}

	public void register(TaskManager t) {
		this.taskManager = t;

		taskManager.register(this);

	}

	public void unregister() {
		taskManager.unregister(threadVar);
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent();
		e.setType(WorkerStatusChangedEvent.FINISHED);
		fireWorkerStatusChanged(e);
		workerStatusChangeListeners.clear();
		displayText = "";
		progressBarValue = 0;
		progressBarMax = 0;
	}

	public void setProgressBarMaximum(int max) {
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent();
		e.setType(WorkerStatusChangedEvent.PROGRESSBAR_MAX_CHANGED);
		e.setOldValue(new Integer(progressBarMax));

		progressBarMax = max;

		e.setNewValue(new Integer(progressBarMax));
		fireWorkerStatusChanged(e);
	}

	public void setProgressBarValue(int value) {
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent();
		e.setType(WorkerStatusChangedEvent.PROGRESSBAR_VALUE_CHANGED);
		e.setOldValue(new Integer(progressBarValue));

		progressBarValue = value;

		e.setNewValue(new Integer(progressBarValue));
		fireWorkerStatusChanged(e);
	}

	public void incProgressBarValue() {
		incProgressBarValue(1);
	}

	public void incProgressBarValue(int increment) {
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent();
		e.setType(WorkerStatusChangedEvent.PROGRESSBAR_VALUE_CHANGED);
		e.setOldValue(new Integer(progressBarValue));

		progressBarValue += increment;

		e.setNewValue(new Integer(progressBarValue));
		fireWorkerStatusChanged(e);
	}

	public int getProgessBarMaximum() {
		return progressBarMax;
	}

	public int getProgressBarValue() {
		return progressBarValue;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		WorkerStatusChangedEvent e = new WorkerStatusChangedEvent();
		e.setType(WorkerStatusChangedEvent.DISPLAY_TEXT_CHANGED);
		e.setOldValue(displayText);

		this.displayText = displayText;

		e.setNewValue(displayText);
		fireWorkerStatusChanged(e);
	}

	public void addWorkerStatusChangeListener(WorkerStatusChangeListener l) {
		workerStatusChangeListeners.add(l);
	}

	public void removeWorkerStatusChangeListener(WorkerStatusChangeListener l) {
		workerStatusChangeListeners.remove(l);
	}

	protected void fireWorkerStatusChanged(WorkerStatusChangedEvent e) {
		for (int i = 0; i < workerStatusChangeListeners.size(); i++) {
			(
				(WorkerStatusChangeListener) workerStatusChangeListeners.get(
					i)).workerStatusChanged(
				e);
		}
	}
	
	public void cancel() {
		cancelled = true;	
	}
	
	public boolean cancelled() {
		return cancelled;	
	}

	/**
	 * Returns the timeStamp.
	 * @return int
	 */
	public int getTimeStamp() {
		return timeStamp;
	}

	public FrameController getFrameController() {
		return op.getFrameController();	
	}

}