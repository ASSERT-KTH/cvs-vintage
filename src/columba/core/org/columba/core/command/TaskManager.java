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

import java.util.Vector;

import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.util.Mutex;
import org.columba.core.util.SwingWorker.ThreadVar;

public class TaskManager {
	private Vector workerList;

	protected Mutex workerListMutex;

	protected Vector workerListChangeListeners;


	public TaskManager() {
		workerList = new Vector();

		workerListMutex = new Mutex();

		workerListChangeListeners = new Vector();
	}

	public Vector getWorkerList() {
		return workerList;
	}

	private void addWorker(Worker w) {
		Worker compareWorker;
		int workerPriority = w.getPriority();

		for (int i = 0; i < workerList.size(); i++) {
			compareWorker = (Worker) workerList.get(i);
			if (compareWorker.getPriority() < workerPriority) {
				workerList.insertElementAt(w, i);
				return;
			}
		}

		// Lowest Priority		
		workerList.add(w);
	}

	public Worker get(int index) {
		Worker w;

		workerListMutex.getMutex();
		w = (Worker) workerList.get(index);
		workerListMutex.releaseMutex();

		return w;
	}

	public void register(Worker t) {
		WorkerListChangedEvent changeEvent = new WorkerListChangedEvent();

		workerListMutex.getMutex();

		changeEvent.setOldValue(workerList.size());

		addWorker(t);

		changeEvent.setNewValue(workerList.size());

		workerListMutex.releaseMutex();

		fireWorkerListChangedEvent(changeEvent);
	}

	public void unregister(ThreadVar tvar) {
		Worker worker;
		WorkerListChangedEvent e = new WorkerListChangedEvent();
		e.setType(WorkerListChangedEvent.SIZE_CHANGED);

		workerListMutex.getMutex();
		int size = workerList.size();
		e.setOldValue(size);

		for (int i = 0; i < size; i++) {
			worker = (Worker) workerList.get(i);

			if (tvar == worker.getThreadVar()) {
				workerList.remove(i);
				e.setNewValue(workerList.size());
				break;
			}
		}

		workerListMutex.releaseMutex();

		fireWorkerListChangedEvent(e);
	}

	public void addWorkerListChangeListener(WorkerListChangeListener l) {
		workerListChangeListeners.add(l);
	}

	protected void fireWorkerListChangedEvent(WorkerListChangedEvent e) {
		for (int i = 0; i < workerListChangeListeners.size(); i++) {
			(
				(WorkerListChangeListener) workerListChangeListeners.get(
					i)).workerListChanged(
				e);
		}
	}
}